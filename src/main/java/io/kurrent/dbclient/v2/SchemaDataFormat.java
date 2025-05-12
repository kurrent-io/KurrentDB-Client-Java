package io.kurrent.dbclient.v2;

/**
 * Specifies the format of schema data.
 */
public enum SchemaDataFormat {
    /**
     * Unspecified format.
     */
    UNSPECIFIED(0),
    
    /**
     * JSON format.
     */
    JSON(1),
    
    /**
     * Protocol Buffers format.
     */
    PROTOBUF(2),
    
    /**
     * Apache Avro format.
     */
    AVRO(3),
    
    /**
     * Raw bytes format.
     */
    BYTES(4);
    
    private final int value;
    
    SchemaDataFormat(int value) {
        this.value = value;
    }
    
    /**
     * Gets the integer value of the enum.
     *
     * @return The integer value.
     */
    public int getValue() {
        return value;
    }
    
    /**
     * Gets a SchemaDataFormat from its integer value.
     *
     * @param value The integer value.
     * @return The corresponding SchemaDataFormat, or UNSPECIFIED if not found.
     */
    public static SchemaDataFormat fromValue(int value) {
        for (SchemaDataFormat format : SchemaDataFormat.values()) {
            if (format.value == value) {
                return format;
            }
        }
        return UNSPECIFIED;
    }
}