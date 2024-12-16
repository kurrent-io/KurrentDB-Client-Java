package io.kurrent.dbclient;

import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

class GrpcClient {
    private static final Logger logger = LoggerFactory.getLogger(GrpcClient.class);
    private final AtomicBoolean closed;
    private final LinkedBlockingQueue<Msg> queue;
    private final KurrentDBClientSettings settings;

    GrpcClient(KurrentDBClientSettings settings, AtomicBoolean closed, LinkedBlockingQueue<Msg> queue) {
        this.settings = settings;
        this.closed = closed;
        this.queue = queue;
    }

    public boolean isShutdown() {
        return this.closed.get();
    }

    private CompletableFuture<Void> push(Msg msg) {
        return CompletableFuture.runAsync(() -> {
            try {
                if (this.closed.get()) {
                    if (msg instanceof RunWorkItem) {
                        RunWorkItem args = (RunWorkItem) msg;
                        args.reportError(new ConnectionShutdownException());
                    }

                    if (msg instanceof Shutdown) {
                        ((Shutdown) msg).complete();
                    }

                    return;
                }

                this.queue.put(msg);
                logger.debug("Scheduled msg: {}", msg);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Optional<ServerVersion>> getServerVersion() {
        return runWithArgs(args -> CompletableFuture.completedFuture(args.getServerVersion()));
    }

    public <A> CompletableFuture<A> run(Function<ManagedChannel, CompletableFuture<A>> action) {
        return runWithArgs(args -> action.apply(args.getChannel()));
    }

    public <A> CompletableFuture<A> runWithArgs(Function<WorkItemArgs, CompletableFuture<A>> action) {
        final CompletableFuture<A> result = new CompletableFuture<>();
        final String msgId = UUID.randomUUID().toString();
        final LinkedBlockingQueue<Msg> queue = this.queue;

        return this.push(new RunWorkItem(msgId, (args, fatalError) -> {
            if (fatalError != null) {
                result.completeExceptionally(fatalError);
                return;
            }

            action.apply(args).whenComplete((outcome, error) -> {
                if (outcome != null) {
                    result.complete(outcome);
                    return;
                }

                try {
                    if (error instanceof NotLeaderException) {
                        NotLeaderException ex = (NotLeaderException) error;
                        // TODO - Currently we don't retry on not leader exception but we might consider
                        // allowing this on a case-by-case basis.
                        result.completeExceptionally(ex);
                        queue.put(new CreateChannel(args.getId(), ex.getLeaderEndpoint()));

                        return;
                    }

                    if (error instanceof StatusRuntimeException) {
                        StatusRuntimeException ex = (StatusRuntimeException) error;

                        if (ex.getStatus().getCode().equals(Status.Code.UNAVAILABLE)) {
                            queue.put(new CreateChannel(args.getId()));
                        }
                    }
                    logger.debug("RunWorkItem[{}] completed exceptionally: {}", msgId, error.toString());

                    result.completeExceptionally(error);
                } catch (InterruptedException e) {
                    result.completeExceptionally(e);
                }
            });
        })).thenComposeAsync(x -> result);
    }

    public CompletableFuture<Void> shutdown() {
        final CompletableFuture<Void> completion = new CompletableFuture<>();

        return this.push(new Shutdown(completion::complete)).thenComposeAsync(x -> completion);
    }

    public KurrentDBClientSettings getSettings() {
        return this.settings;
    }
}