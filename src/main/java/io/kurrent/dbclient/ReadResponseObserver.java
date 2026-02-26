package io.kurrent.dbclient;

import io.kurrent.dbclient.proto.shared.Shared;
import io.kurrent.dbclient.proto.streams.StreamsOuterClass;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

class ReadResponseObserver implements ClientResponseObserver<StreamsOuterClass.ReadReq, StreamsOuterClass.ReadResp> {
    private final static Logger logger = LoggerFactory.getLogger(ReadResponseObserver.class);
    private final OptionsWithBackPressure<?> options;
    private final AtomicInteger requested = new AtomicInteger(0);
    private final AtomicBoolean completed = new AtomicBoolean(false);
    private final StreamConsumer consumer;
    private ClientCallStreamObserver<StreamsOuterClass.ReadReq> requestStream;
    private int outstandingRequests;
    private WorkItemArgs args;


    public ReadResponseObserver(OptionsWithBackPressure<?> options, StreamConsumer consumer) {
        this.options = options;
        this.consumer = consumer;
    }

    public Subscription getSubscription() {
        return new InternalSubscription(this);
    }

    public void onConnected(WorkItemArgs args) {
        this.args = args;
    }

    // this method can be called from a different thread.
    void cancel(String reason, Throwable cause) {
        if (!this.completed.compareAndSet(false, true))
            return;

        // means the streaming was cancelled before the operation even started.
        if (this.requestStream == null)
            return;

        this.requestStream.cancel(reason, cause);
        if (cause instanceof StreamNotFoundException)
            this.consumer.onStreamNotFound(((StreamNotFoundException) cause).getStreamName());
        else
            this.consumer.onCancelled(cause);
    }

    void manageFlowControl() {
        int requestedCount = this.requested.getAndSet(0);
        int bufferRequestSize = this.options.computeRequestThreshold();
        this.outstandingRequests = Math.max(this.outstandingRequests, 0);
        int received = this.options.getBatchSize() - this.outstandingRequests;

        if (requestedCount > 0) {
            int needed = Math.min(requestedCount, received);
            this.requestStream.request(needed);
            this.outstandingRequests += needed;
            return;
        }

        // check if we received enough to request more from the server.
       if (received < bufferRequestSize)
           return;

         this.requestStream.request(received);
         this.outstandingRequests += received;
    }

    @Override
    public void beforeStart(ClientCallStreamObserver<StreamsOuterClass.ReadReq> requestStream) {
        this.requestStream = requestStream;

        if (this.completed.get()) {
            this.requestStream.cancel("the streaming operation was cancelled manually", null);
            return;
        }

        this.requestStream.disableAutoRequestWithInitial(this.options.getBatchSize());
        this.outstandingRequests = this.options.getBatchSize();
        this.consumer.onSubscribe(getSubscription());
    }

    @Override
    public void onNext(StreamsOuterClass.ReadResp value) {
        if (this.completed.get())
            return;

        this.outstandingRequests -= 1;
        if (value.hasStreamNotFound()) {
            String streamName = value.getStreamNotFound()
                    .getStreamIdentifier()
                    .getStreamName()
                    .toString(Charset.defaultCharset());

            cancel(String.format("stream '%s' is not found", streamName), new StreamNotFoundException(streamName));
            return;
        }

        try {
            handleMessage(value);
        } catch (Exception ex) {
            logger.error("Exception thrown by subscription handler", ex);
            cancel("subscription handler threw an exception", ex);
            return;
        }

        manageFlowControl();
    }

    private void handleMessage(StreamsOuterClass.ReadResp value) {
        if (value.hasEvent())
            consumer.onEvent(ResolvedEvent.fromWire(value.getEvent()));
        else if (value.hasConfirmation())
            consumer.onSubscriptionConfirmation(value.getConfirmation().getSubscriptionId());
        else if (value.hasCheckpoint()) {
            StreamsOuterClass.ReadResp.Checkpoint checkpoint = value.getCheckpoint();
            consumer.onCheckpoint(checkpoint.getCommitPosition(), checkpoint.getPreparePosition());
        } else if (value.hasFirstStreamPosition())
            consumer.onFirstStreamPosition(value.getFirstStreamPosition());
        else if (value.hasLastStreamPosition())
            consumer.onLastStreamPosition(value.getLastStreamPosition());
        else if (value.hasLastAllStreamPosition()) {
            Shared.AllStreamPosition position = value.getLastAllStreamPosition();
            consumer.onLastAllStreamPosition(position.getCommitPosition(), position.getPreparePosition());
        } else if (value.hasCaughtUp()) {
            StreamsOuterClass.ReadResp.CaughtUp caughtUp = value.getCaughtUp();
            Instant timestamp = Instant.ofEpochSecond(caughtUp.getTimestamp().getSeconds(), caughtUp.getTimestamp().getNanos());
            Long streamRevision = caughtUp.hasStreamRevision() ? caughtUp.getStreamRevision() : null;
            Position position = caughtUp.hasPosition() ? new Position(caughtUp.getPosition().getCommitPosition(), caughtUp.getPosition().getPreparePosition()) : null;

            consumer.onCaughtUp(timestamp, streamRevision, position);
        } else if (value.hasFellBehind()) {
            StreamsOuterClass.ReadResp.FellBehind fellBehind = value.getFellBehind();
            Instant timestamp = Instant.ofEpochSecond(fellBehind.getTimestamp().getSeconds(), fellBehind.getTimestamp().getNanos());
            Long streamRevision = fellBehind.hasStreamRevision() ? fellBehind.getStreamRevision() : null;
            Position position = fellBehind.hasPosition() ? new Position(fellBehind.getPosition().getCommitPosition(), fellBehind.getPosition().getPreparePosition()) : null;

            consumer.onFellBehind(timestamp, streamRevision, position);
        } else {
            logger.warn("received unknown message variant");
        }
    }

    @Override
    public void onError(Throwable t) {
        if (!this.completed.compareAndSet(false, true))
            return;

        if (t instanceof StatusRuntimeException) {
            StatusRuntimeException e = (StatusRuntimeException) t;

            if (e.getStatus().getCode() == Status.Code.CANCELLED) {
                this.consumer.onCancelled(null);
                return;
            }

            Metadata trailers = e.getTrailers();

            if (trailers != null) {
                String leaderHost = trailers.get(Metadata.Key.of("leader-endpoint-host", Metadata.ASCII_STRING_MARSHALLER));
                String leaderPort = trailers.get(Metadata.Key.of("leader-endpoint-port", Metadata.ASCII_STRING_MARSHALLER));

                if (leaderHost != null && leaderPort != null) {
                    int port = Integer.parseInt(leaderPort);
                    this.args.reportNewLeader(leaderHost, port);
                    t = new NotLeaderException(leaderHost, port);
                }
            }
        }

        this.consumer.onCancelled(t);
    }

    @Override
    public void onCompleted() {
        if (!this.completed.compareAndSet(false, true))
            return;

        this.consumer.onComplete();
    }

    static class InternalSubscription implements Subscription {
        private final ReadResponseObserver observer;

        InternalSubscription(ReadResponseObserver observer) {
            this.observer = observer;
        }

        @Override
        public void request(long n) {
            if (n <= 0)
                throw new IllegalArgumentException("non-positive subscription request");

            this.observer.requested.set((int)n);
        }

        @Override
        public void cancel() {
            this.observer.cancel("subscription was manually cancelled", null);
        }
    }
}
