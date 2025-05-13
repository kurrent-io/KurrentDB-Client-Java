package io.kurrent.dbclient.v2;

/**
 * Defines the scope of a consume filter.
 */
public enum ConsumeFilterScope {
    /**
     * Unspecified filter scope.
     */
    UNSPECIFIED(0),
    
    /**
     * Filter applies to stream names.
     */
    STREAM(1),
    
    /**
     * Filter applies to record types.
     */
    RECORD(2);
    
    private final int value;
    
    ConsumeFilterScope(int value) {
        this.value = value;
    }
    
    /**
     * Gets the integer value of the enum.
     * @return The integer value.
     */
    public int getValue() {
        return value;
    }
}