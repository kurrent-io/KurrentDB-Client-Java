package io.kurrent.dbclient;

/**
 * Represents a record to be appended to a specific stream in an
 * {@link KurrentDBClient#appendRecords} operation.
 * Each record specifies its own target stream, allowing interleaved writes across multiple streams.
 */
public class AppendRecord {
    private final String stream;
    private final EventData record;

    /**
     * Creates a new AppendRecord targeting the specified stream.
     *
     * @param stream the name of the target stream for this record.
     * @param record the event data to append.
     */
    public AppendRecord(String stream, EventData record) {
        this.stream = stream;
        this.record = record;
    }

    /**
     * Returns the name of the target stream.
     */
    public String getStream() {
        return stream;
    }

    /**
     * Returns the event data to append.
     */
    public EventData getRecord() {
        return record;
    }
}
