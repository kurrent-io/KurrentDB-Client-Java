package io.kurrent.dbclient;

/**
 * Thrown when an append record exceeds the maximum allowed size.
 */
public class RecordSizeExceededException extends RuntimeException {
    /**
     * The name of the stream where the append was attempted.
     */
    private final String stream;

    /**
     * The identifier of the offending and oversized record.
     */
    private final String recordId;

    /**
     * The size of the huge record in bytes.
     */
    private final int size;

    /**
     * The maximum allowed size of a single record that can be appended in bytes.
     */
    private final int maxSize;

    public RecordSizeExceededException(String stream, String recordId, int size, int maxSize) {
        super(String.format("The size of record %s (%d bytes) exceeds the maximum allowed size of %d bytes by %d bytes", recordId, size, maxSize, size - maxSize));
        this.stream = stream;
        this.recordId = recordId;
        this.size = size;
        this.maxSize = maxSize;
    }

    public String getStream() {
        return stream;
    }

    public String getRecordId() {
        return recordId;
    }

    public int getSize() {
        return size;
    }

    public int getMaxSize() {
        return maxSize;
    }
}
