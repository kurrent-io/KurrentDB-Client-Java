package io.kurrent.dbclient.v2;

import java.util.UUID;
import java.util.function.Function;

/**
 * Represents a message with payload, metadata, and schema information.
 */
public final class Message {
    /**
     * An empty message instance.
     */
    public static final Message EMPTY = new Message();

    private final Object value;
    private final Metadata metadata;
    private final UUID recordId;
    private final SchemaDataFormat dataFormat;

    /**
     * Initializes a new instance of the Message class with default values.
     */
    public Message() {
        this(null, new Metadata(), UUID.randomUUID(), SchemaDataFormat.JSON);
    }

    /**
     * Initializes a new instance of the Message class.
     *
     * @param value The message payload.
     * @param metadata The message metadata.
     * @param recordId The assigned record id.
     * @param dataFormat The format of the schema associated with the message.
     */
    public Message(Object value, Metadata metadata, UUID recordId, SchemaDataFormat dataFormat) {
        this.value = value;
        this.metadata = metadata != null ? metadata : new Metadata();
        this.recordId = recordId != null ? recordId : UUID.randomUUID();
        this.dataFormat = dataFormat != null ? dataFormat : SchemaDataFormat.JSON;
    }

    /**
     * Gets the message payload.
     *
     * @return The message payload.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Gets the message metadata.
     *
     * @return The message metadata.
     */
    public Metadata getMetadata() {
        return metadata;
    }

    /**
     * Gets the assigned record id.
     *
     * @return The assigned record id.
     */
    public UUID getRecordId() {
        return recordId;
    }

    /**
     * Gets the format of the schema associated with the message.
     *
     * @return The schema data format.
     */
    public SchemaDataFormat getDataFormat() {
        return dataFormat;
    }

    /**
     * Creates a new Message builder.
     *
     * @return A new Message builder.
     */
    public static Builder builder() {
        return new Builder(null, new Metadata(), null, SchemaDataFormat.JSON);
    }

    /**
     * Builder for creating Message instances.
     */
    public static class Builder {
        private final Object value;
        private final Metadata metadata;
        private final UUID recordId;
        private final SchemaDataFormat dataFormat;

        private Builder(Object value, Metadata metadata, UUID recordId, SchemaDataFormat dataFormat) {
            this.value = value;
            this.metadata = metadata;
            this.recordId = recordId;
            this.dataFormat = dataFormat;
        }

        /**
         * Sets the message payload.
         *
         * @param value The message payload.
         * @return This builder instance.
         */
        public Builder value(Object value) {
            return new Builder(value, this.metadata, this.recordId, this.dataFormat);
        }

        /**
         * Sets the message metadata.
         *
         * @param metadata The message metadata.
         * @return This builder instance.
         */
        public Builder metadata(Metadata metadata) {
            assert metadata != null : "Metadata cannot be null";
            return new Builder(this.value, metadata, this.recordId, this.dataFormat);
        }

        /**
         * Sets the assigned record id.
         *
         * @param recordId The assigned record id.
         * @return This builder instance.
         */
        public Builder recordId(UUID recordId) {
            assert recordId != null : "Record ID cannot be null";
            return new Builder(this.value, this.metadata, recordId, this.dataFormat);
        }

        /**
         * Sets the format of the schema associated with the message.
         *
         * @param dataFormat The schema data format.
         * @return This builder instance.
         */
        public Builder dataFormat(SchemaDataFormat dataFormat) {
            return new Builder(this.value, this.metadata, this.recordId, dataFormat);
        }

        public Builder when(boolean condition, Function<Builder, Builder> func) {
            return condition ? func.apply(this) : this;
        }

        /**
         * Builds a new Message instance.
         *
         * @return A new Message instance.
         */
        public Message build() {
            return new Message(this.value, this.metadata, this.recordId != null ? this.recordId : UUID.randomUUID(), this.dataFormat);
        }
    }
}