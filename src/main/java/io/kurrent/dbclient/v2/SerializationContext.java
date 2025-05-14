package io.kurrent.dbclient.v2;

/**
 * Allows the serialization operations to be autonomous.
 */
public final class SerializationContext {
    private final Metadata metadata;
    private final String stream;
    private final SchemaInfo schemaInfo;

    /**
     * Initializes a new instance of the SerializationContext class with the specified metadata, stream, and cancellation flag.
     *
     * @param metadata The metadata providing additional information for the serialization process.
     * @param stream The stream that the record belongs to.
     */
    public SerializationContext(Metadata metadata, String stream) {
        this.metadata = metadata;
        this.stream = stream;
        this.schemaInfo = SchemaInfo.fromMetadata(this.metadata);
    }

    /**
     * Gets the metadata providing additional information for the serialization process.
     *
     * @return The metadata.
     */
    public Metadata getMetadata() {
        return metadata;
    }

    /**
     * Gets the stream that the record belongs to.
     *
     * @return The stream.
     */
    public String getStream() {
        return stream;
    }

    /**
     * Gets the schema information extracted from the headers.
     * If the headers do not contain schema information, it will return an undefined schema information.
     *
     * @return The schema information.
     */
    public SchemaInfo getSchemaInfo() {
        return schemaInfo;
    }
}
