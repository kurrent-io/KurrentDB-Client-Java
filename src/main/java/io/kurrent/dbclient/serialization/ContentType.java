package io.kurrent.dbclient.serialization;

public enum ContentType {
    JSON(1),
    // PROTBUF(2),
    // AVRO(3),
    BYTES(4);

    private final int value;

    ContentType(final int newValue) {
        value = newValue;
    }

    public int getValue() { return value; }
}