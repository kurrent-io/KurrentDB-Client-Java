package io.kurrent.dbclient;

import io.kurrent.dbclient.proto.persistentsubscriptions.Persistent;
import io.kurrent.dbclient.proto.streams.StreamsOuterClass;
import io.kurrent.dbclient.serialization.MessageSerializer;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents an event with a potential link.
 */
public class ResolvedEvent {
    private final RecordedEvent event;
    private final RecordedEvent link;
    private final Position position;
    private final Message message;

    public ResolvedEvent(RecordedEvent event, RecordedEvent link, Position position) {
        this(event, link, null, position);
    }

    public ResolvedEvent(RecordedEvent event, RecordedEvent link, Message message, Position position) {
        this.event = event;
        this.link = link;
        this.message = message;
        this.position = position;
    }

    /**
     * The event, or the resolved linked event if the original event is a link.
     */
    public RecordedEvent getEvent() {
        return event;
    }

    /**
     * The link event if the original event is a link.
     */
    public RecordedEvent getLink() {
        return link;
    }

    /**
     * Returns the event that was read or which triggered the subscription. If the resolved event represents a link
     * event, the link will be the original event, otherwise it will be the event.
     */
    public RecordedEvent getOriginalEvent() {
        return this.link != null ? this.link : this.event;
    }


    /**
     * Returns the deserialized message
     * It will be provided or equal to null, depending on the automatic deserialization settings you choose.
     * If it's null, you can use OriginalEvent to deserialize it manually.
     */
    public Optional<Message> getMessage() {
        return Optional.ofNullable(message);
    }

    /**
     * Returns the deserialized message data.
     */
    public Optional<Object> getDeserializedData() {
        return getMessage().map(Message::data);
    }

    /**
     * Returns the transaction log position of the resolved event.
     *
     * @see Position
     */
    public Optional<Position> getPosition() {
        return Optional.ofNullable(position);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResolvedEvent that = (ResolvedEvent) o;
        return event.equals(that.event) && Objects.equals(link, that.link);
    }

    @Override
    public int hashCode() {
        return Objects.hash(event, link);
    }

    static ResolvedEvent fromWire(
            StreamsOuterClass.ReadResp.ReadEvent wireEvent,
            MessageSerializer messageSerializer
    ) {
        RecordedEvent event = wireEvent.hasEvent() ? RecordedEvent.fromWire(wireEvent.getEvent()) : null;
        RecordedEvent link = wireEvent.hasLink() ? RecordedEvent.fromWire(wireEvent.getLink()) : null;
        Position position = wireEvent.hasNoPosition() ? null : new Position(wireEvent.getCommitPosition(), wireEvent.getCommitPosition());

        return ResolvedEvent.from(event, link, position, messageSerializer);
    }

    static ResolvedEvent fromWire(
            Persistent.ReadResp.ReadEvent wireEvent,
            MessageSerializer messageSerializer
    ) {
        RecordedEvent event = wireEvent.hasEvent() ? RecordedEvent.fromWire(wireEvent.getEvent()) : null;
        RecordedEvent link = wireEvent.hasLink() ? RecordedEvent.fromWire(wireEvent.getLink()) : null;
        Position position = wireEvent.hasNoPosition() ? null : new Position(wireEvent.getCommitPosition(), wireEvent.getCommitPosition());


        return ResolvedEvent.from(event, link, position, messageSerializer);
    }

    static ResolvedEvent from(
            RecordedEvent event,
            RecordedEvent link,
            Position position,
            MessageSerializer messageSerializer
    ) {
        RecordedEvent originalEvent = link != null ? link : event;
        Optional<Message> message = messageSerializer.tryDeserialize(originalEvent);

        return message
                .map(value -> new ResolvedEvent(event, link, value, position))
                .orElseGet(() -> new ResolvedEvent(event, link, position));
    }

    @Override
    public String toString() {
        return "ResolvedEvent{" +
                "event=" + event +
                ", link=" + link +
                ", position=" + position +
                '}';
    }
}
