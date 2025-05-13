package io.kurrent.dbclient.v2;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Represents a registered schema with its metadata.
 */
public class RegisteredSchema {
    /**
     * Represents an empty or unspecified registered schema.
     */
    public static final RegisteredSchema NONE = new RegisteredSchema();

    private final String schemaName;
    private final SchemaDataFormat dataFormat;
    private final String schemaVersionId;
    private final String definition;
    private final int versionNumber;
    private final OffsetDateTime createdAt;

    /**
     * Creates a default instance with null or default values.
     */
    private RegisteredSchema() {
        this.schemaName = null;
        this.dataFormat = SchemaDataFormat.UNSPECIFIED;
        this.schemaVersionId = null;
        this.definition = null;
        this.versionNumber = 0;
        this.createdAt = OffsetDateTime.now();
    }

    /**
     * Creates a new instance of RegisteredSchema with the specified values.
     *
     * @param schemaName      The name of the schema.
     * @param dataFormat      The data format of the schema.
     * @param schemaVersionId The version ID of the schema.
     * @param definition      The definition of the schema.
     * @param versionNumber   The version number of the schema.
     * @param createdAt       The creation timestamp of the schema.
     */
    public RegisteredSchema(
            String schemaName,
            SchemaDataFormat dataFormat,
            String schemaVersionId,
            String definition,
            int versionNumber,
            OffsetDateTime createdAt) {
        this.schemaName = schemaName;
        this.dataFormat = dataFormat;
        this.schemaVersionId = schemaVersionId;
        this.definition = definition;
        this.versionNumber = versionNumber;
        this.createdAt = createdAt;
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
     * Gets the schema version ID.
     *
     * @return The schema version ID.
     */
    public String getSchemaVersionId() {
        return schemaVersionId;
    }

    /**
     * Gets the schema definition.
     *
     * @return The schema definition.
     */
    public String getDefinition() {
        return definition;
    }

    /**
     * Gets the version number.
     *
     * @return The version number.
     */
    public int getVersionNumber() {
        return versionNumber;
    }

    /**
     * Gets the creation timestamp.
     *
     * @return The creation timestamp.
     */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Converts this registered schema to a SchemaInfo object.
     *
     * @return A new SchemaInfo instance.
     */
    public SchemaInfo toSchemaInfo() {
        return new SchemaInfo(schemaName, dataFormat);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegisteredSchema that = (RegisteredSchema) o;
        return versionNumber == that.versionNumber &&
                Objects.equals(schemaName, that.schemaName) &&
                dataFormat == that.dataFormat &&
                Objects.equals(schemaVersionId, that.schemaVersionId) &&
                Objects.equals(definition, that.definition) &&
                Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(schemaName, dataFormat, schemaVersionId, definition, versionNumber, createdAt);
    }

    @Override
    public String toString() {
        return "RegisteredSchema{" +
                "schemaName='" + schemaName + '\'' +
                ", dataFormat=" + dataFormat +
                ", schemaVersionId='" + schemaVersionId + '\'' +
                ", definition='" + definition + '\'' +
                ", versionNumber=" + versionNumber +
                ", createdAt=" + createdAt +
                '}';
    }
}