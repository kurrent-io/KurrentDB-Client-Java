package io.kurrent.dbclient;

import io.kurrent.dbclient.proto.persistentsubscriptions.Persistent;
import io.kurrent.dbclient.proto.shared.Shared;
import io.kurrent.dbclient.proto.streams.StreamsOuterClass;
import com.fasterxml.jackson.databind.json.JsonMapper;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a previously written event.
 */
public class RecordedEvent {
    @NotNull
    private final String streamId;
    @NotNull
    private final long revision;
    @NotNull
    private final UUID eventId;
    @NotNull
    private final String eventType;
    @NotNull
    private final byte[] eventData;
    @NotNull
    private final byte[] userMetadata;
    @NotNull
    private final Instant created;
    @NotNull
    private final Position position;
    @NotNull
    private final String contentType;

    RecordedEvent(
            @NotNull String eventStreamId,
            @NotNull long streamRevision,
            @NotNull UUID eventId,
            @NotNull Position position,
            @NotNull Map<String, String> systemMetadata,
            @NotNull byte[] eventData,
            @NotNull byte[] userMetadata
    ) {
        this.streamId = eventStreamId;
        this.revision = streamRevision;
        this.eventId = eventId;
        this.position = position;
        this.eventData = eventData;
        this.userMetadata = userMetadata;
        this.eventType = systemMetadata.get(SystemMetadataKeys.TYPE);
        this.contentType = systemMetadata.get(SystemMetadataKeys.CONTENT_TYPE);
        this.created = systemMetadataDateToInstant(systemMetadata.get(SystemMetadataKeys.CREATED));
    }

    /**
     * Converts a timestamp in the form of .NET "Ticks" (100ns increments) since the UNIX Epoch
     * into a java.time.Instant;
     *
     * @param timestamp String representation of the source timestamp
     * @return A java.time.Instant representing the timestamp
     */
    private static Instant systemMetadataDateToInstant(String timestamp) {
        long nanos = Long.parseLong(timestamp) * 100;
        return Instant.EPOCH.plusNanos(nanos);
    }

    /**
     * The stream that event belongs to.
     */
    @NotNull
    public String getStreamId() {
        return streamId;
    }

    /**
     * The event's stream revision number.
     */
    @NotNull
    public long getRevision() {
        return revision;
    }

    /**
     * The event's unique identifier.
     */
    @NotNull
    public UUID getEventId() {
        return eventId;
    }

    /**
     * The event's type.
     */
    @NotNull
    public String getEventType() {
        return eventType;
    }

    /**
     * The event's payload data.
     */
    public byte[] getEventData() {
        return eventData;
    }

    /**
     * The event's metadata.
     */
    public byte[] getUserMetadata() {
        return userMetadata;
    }

    /**
     * When the event was created.
     */
    @NotNull
    public Instant getCreated() {
        return created;
    }

    /**
     * The event's transaction log position.
     */
    @NotNull
    public Position getPosition() {
        return position;
    }

    /**
     * The event's content type. Could be <i>application/json</i> or <i>application/octet-stream</i>.
     */
    @NotNull
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecordedEvent that = (RecordedEvent) o;
        return streamId.equals(that.streamId) && revision == that.revision && eventId.equals(that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(streamId, revision, eventId);
    }

    static RecordedEvent fromWire(StreamsOuterClass.ReadResp.ReadEvent.RecordedEvent wireEvent) {
        UUID eventId;
        if (wireEvent.getId().hasStructured()) {
            Shared.UUID.Structured structured = wireEvent.getId().getStructured();
            eventId = new UUID(structured.getMostSignificantBits(), structured.getLeastSignificantBits());
        } else {
            eventId = UUID.fromString(wireEvent.getId().getString());
        }

        return new RecordedEvent(
                wireEvent.getStreamIdentifier().getStreamName().toStringUtf8(),
                wireEvent.getStreamRevision(),
                eventId,
                new Position(wireEvent.getCommitPosition(), wireEvent.getPreparePosition()),
                wireEvent.getMetadataMap(),
                wireEvent.getData().toByteArray(),
                wireEvent.getCustomMetadata().toByteArray());
    }

    static RecordedEvent fromWire(Persistent.ReadResp.ReadEvent.RecordedEvent wireEvent) {
        UUID eventId;
        if (wireEvent.getId().hasStructured()) {
            Shared.UUID.Structured structured = wireEvent.getId().getStructured();
            eventId = new UUID(structured.getMostSignificantBits(), structured.getLeastSignificantBits());
        } else {
            eventId = UUID.fromString(wireEvent.getId().getString());
        }

        return new RecordedEvent(
                wireEvent.getStreamIdentifier().getStreamName().toStringUtf8(),
                wireEvent.getStreamRevision(),
                eventId,
                new Position(wireEvent.getCommitPosition(), wireEvent.getPreparePosition()),
                wireEvent.getMetadataMap(),
                wireEvent.getData().toByteArray(),
                wireEvent.getCustomMetadata().toByteArray());
    }

    @Override
    public String toString() {
        return "RecordedEvent{" +
                "streamId='" + streamId + '\'' +
                ", revision=" + revision +
                ", eventId=" + eventId +
                ", eventType='" + eventType + '\'' +
                ", eventData=" + Arrays.toString(eventData) +
                ", userMetadata=" + Arrays.toString(userMetadata) +
                ", created=" + created +
                ", position=" + position +
                ", contentType='" + contentType + '\'' +
                '}';
    }
}
