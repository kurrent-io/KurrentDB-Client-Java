package io.kurrent.dbclient.v2;

/**
 * Represents schema information including schema name and data format.
 */
public class SchemaInfo {
    // Content type headers
    private static final String JSON_CONTENT_TYPE = "application/json";
    private static final String PROTOBUF_CONTENT_TYPE = "application/vnd.google.protobuf";
    private static final String AVRO_CONTENT_TYPE = "application/vnd.apache.avro+json";
    private static final String BYTES_CONTENT_TYPE = "application/octet-stream";

    // Metadata keys
    private static final String SCHEMA_NAME_KEY = "schema-name";
    private static final String SCHEMA_DATA_FORMAT_KEY = "schema-data-format";

    // Static instance for no schema
    public static final SchemaInfo NONE = new SchemaInfo("", SchemaDataFormat.UNSPECIFIED);

    private final String schemaName;
    private final SchemaDataFormat dataFormat;
    private final String contentTypeHeader;

    /**
     * Creates a new instance of SchemaInfo.
     *
     * @param schemaName The name of the schema.
     * @param dataFormat The data format of the schema.
     */
    public SchemaInfo(String schemaName, SchemaDataFormat dataFormat) {
        this.schemaName = schemaName;
        this.dataFormat = dataFormat;
        this.contentTypeHeader = determineContentTypeHeader(dataFormat);
    }

    /**
     * Gets the schema name.
     *
     * @return The schema name.
     */
    public String getSchemaName() {
        return schemaName;
    }

    /**
     * Gets the data format.
     *
     * @return The data format.
     */
    public SchemaDataFormat getDataFormat() {
        return dataFormat;
    }

    /**
     * Gets the content type header.
     *
     * @return The content type header.
     */
    public String getContentTypeHeader() {
        return contentTypeHeader;
    }

    /**
     * Gets the content type.
     *
     * @return The content type.
     */
    public String getContentType() {
        return contentTypeHeader;
    }

    /**
     * Checks if the schema name is missing.
     *
     * @return True if the schema name is null, empty, or whitespace; otherwise, false.
     */
    public boolean isSchemaNameMissing() {
        return schemaName == null || schemaName.trim().isEmpty();
    }

    /**
     * Injects schema information into metadata.
     *
     * @param metadata The metadata to inject into.
     */
    public void injectIntoMetadata(Metadata metadata) {
        metadata.set(SCHEMA_NAME_KEY, schemaName);
        metadata.set(SCHEMA_DATA_FORMAT_KEY, dataFormat.toString().toLowerCase());
    }

    /**
     * Injects schema name into metadata.
     *
     * @param metadata The metadata to inject into.
     */
    public void injectSchemaNameIntoMetadata(Metadata metadata) {
        metadata.set(SCHEMA_NAME_KEY, schemaName);
    }

    /**
     * Creates a SchemaInfo instance from metadata.
     *
     * @param metadata The metadata to extract from.
     * @return A new SchemaInfo instance.
     */
    public static SchemaInfo fromMetadata(Metadata metadata) {
        String schemaName = extractSchemaName(metadata);
        SchemaDataFormat dataFormat = extractSchemaDataFormat(metadata);
        return new SchemaInfo(schemaName, dataFormat);
    }

    /**
     * Creates a SchemaInfo instance from content type.
     *
     * @param schemaName  The schema name.
     * @param contentType The content type.
     * @return A new SchemaInfo instance.
     * @throws IllegalArgumentException If schemaName or contentType is null or empty.
     */
    public static SchemaInfo fromContentType(String schemaName, String contentType) {
        if (schemaName == null || schemaName.isEmpty())
            throw new IllegalArgumentException("schemaName cannot be null or empty");

        if (contentType == null || contentType.isEmpty())
            throw new IllegalArgumentException("contentType cannot be null or empty");

        SchemaDataFormat schemaDataFormat;
        switch (contentType) {
            case JSON_CONTENT_TYPE:
                schemaDataFormat = SchemaDataFormat.JSON;
                break;
            case PROTOBUF_CONTENT_TYPE:
                schemaDataFormat = SchemaDataFormat.PROTOBUF;
                break;
            case BYTES_CONTENT_TYPE:
                schemaDataFormat = SchemaDataFormat.BYTES;
                break;
            default:
                schemaDataFormat = SchemaDataFormat.UNSPECIFIED;
                break;
        }

        return new SchemaInfo(schemaName, schemaDataFormat);
    }

    private static String extractSchemaName(Metadata metadata) {
        String schemaName = metadata.get(SCHEMA_NAME_KEY, String.class);
        return schemaName != null ? schemaName : "";
    }

    private static SchemaDataFormat extractSchemaDataFormat(Metadata metadata) {
        String formatStr = metadata.get(SCHEMA_DATA_FORMAT_KEY, String.class);
        if (formatStr == null) {
            return SchemaDataFormat.UNSPECIFIED;
        }

        try {
            return SchemaDataFormat.valueOf(formatStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return SchemaDataFormat.UNSPECIFIED;
        }
    }

    private String determineContentTypeHeader(SchemaDataFormat dataFormat) {
        switch (dataFormat) {
            case JSON:
                return JSON_CONTENT_TYPE;
            case PROTOBUF:
                return PROTOBUF_CONTENT_TYPE;
            case AVRO:
                return AVRO_CONTENT_TYPE;
            case BYTES:
            case UNSPECIFIED:
            default:
                return BYTES_CONTENT_TYPE;
        }
    }
}
