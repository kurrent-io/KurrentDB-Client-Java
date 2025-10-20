package io.kurrent.dbclient;

/**
 * Thrown when an append transaction exceeds the maximum allowed size.
 */
public class TransactionMaxSizeExceededException extends RuntimeException {
    /**
     * The size of the transaction in bytes.
     */
    private final int size;

    /**
     * The maximum allowed size of the append transaction in bytes.
     */
    private final int maxSize;

    public TransactionMaxSizeExceededException(int size, int maxSize) {
        super(String.format("The total size of the append transaction (%d bytes) exceeds the maximum allowed size of %d bytes by %d bytes", size, maxSize, size - maxSize));
        this.size = size;
        this.maxSize = maxSize;
    }

    public int getSize() {
        return size;
    }

    public int getMaxSize() {
        return maxSize;
    }
}
