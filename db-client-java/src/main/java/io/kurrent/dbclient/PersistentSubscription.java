package io.kurrent.dbclient;

import com.eventstore.dbclient.proto.persistentsubscriptions.Persistent;
import com.eventstore.dbclient.proto.shared.Shared;
import com.google.protobuf.ByteString;
import io.grpc.stub.ClientCallStreamObserver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Persistent subscription handle.
 */
public class PersistentSubscription {
    private final ClientCallStreamObserver<Persistent.ReadReq> requestStream;
    private final String subscriptionId;

    PersistentSubscription(ClientCallStreamObserver<Persistent.ReadReq> requestStream, String subscriptionId) {
        this.requestStream = requestStream;
        this.subscriptionId = subscriptionId;
    }

    /**
     * Returns the persistent subscription's id.
     */
    public String getSubscriptionId() {
        return subscriptionId;
    }

    /**
     * Stops the persistent subscription.
     */
    public void stop() {
        this.requestStream.cancel("user-initiated", null);
    }

    /**
     * Acknowledges events have been successfully processed.
     */
    public void ack(ResolvedEvent ...events) {
        this.ack(Arrays.stream(events).iterator());
    }

    /**
     * Acknowledges events have been successfully processed.
     */
    public void ack(Iterator<ResolvedEvent> events) {
        Persistent.ReadReq.Ack.Builder ackBuilder = Persistent.ReadReq.Ack.newBuilder()
                .setId(ByteString.copyFromUtf8(subscriptionId));

        while (events.hasNext()) {
            ResolvedEvent event = events.next();
            RecordedEvent record = event.getLink() != null ? event.getLink() : event.getEvent();
            Shared.UUID.Structured structured = Shared.UUID.Structured.newBuilder()
                    .setLeastSignificantBits(record.getEventId().getLeastSignificantBits())
                    .setMostSignificantBits(record.getEventId().getMostSignificantBits())
                    .build();

            Shared.UUID uuid = Shared.UUID.newBuilder().setStructured(structured).build();
            ackBuilder.addIds(uuid);
        }

        Persistent.ReadReq.Ack ack = ackBuilder.build();
        Persistent.ReadReq req = Persistent.ReadReq.newBuilder()
                .setAck(ack)
                .build();

        requestStream.onNext(req);
    }

    /**
     * Acknowledges events failed processing.
     */
    public void nack(NackAction action, String reason, ResolvedEvent...events) {
        this.nack(action, reason, Arrays.stream(events).iterator());
    }

    /**
     * Acknowledges events failed processing.
     */
    public void nack(NackAction action, String reason, Iterator<ResolvedEvent> events) {
        Persistent.ReadReq.Nack.Builder nackBuilder = Persistent.ReadReq.Nack.newBuilder()
                .setId(ByteString.copyFromUtf8(subscriptionId));

        while (events.hasNext()) {
            ResolvedEvent event = events.next();
            RecordedEvent record = event.getLink() != null ? event.getLink() : event.getEvent();
            Shared.UUID.Structured structured = Shared.UUID.Structured.newBuilder()
                    .setLeastSignificantBits(record.getEventId().getLeastSignificantBits())
                    .setMostSignificantBits(record.getEventId().getMostSignificantBits())
                    .build();

            Shared.UUID uuid = Shared.UUID.newBuilder().setStructured(structured).build();
            nackBuilder.addIds(uuid);
        }

        nackBuilder.setReason(reason);
        switch (action) {
            case Unknown:
                nackBuilder.setAction(Persistent.ReadReq.Nack.Action.Unknown);
                break;
            case Park:
                nackBuilder.setAction(Persistent.ReadReq.Nack.Action.Park);
                break;
            case Retry:
                nackBuilder.setAction(Persistent.ReadReq.Nack.Action.Retry);
                break;
            case Skip:
                nackBuilder.setAction(Persistent.ReadReq.Nack.Action.Skip);
                break;
            case Stop:
                nackBuilder.setAction(Persistent.ReadReq.Nack.Action.Stop);
                break;
        }

        Persistent.ReadReq req = Persistent.ReadReq.newBuilder()
                .setNack(nackBuilder)
                .build();

        requestStream.onNext(req);
    }
}
