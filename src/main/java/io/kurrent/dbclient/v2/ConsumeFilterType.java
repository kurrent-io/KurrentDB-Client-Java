package io.kurrent.dbclient.v2;

/**
 * Defines the type of a consume filter.
 */
public enum ConsumeFilterType {
    /**
     * Unspecified filter type.
     */
    UNSPECIFIED(0),
    
    /**
     * Literal string filter.
     */
    LITERAL(1),
    
    /**
     * Regular expression filter.
     */
    REGEX(2);
    
    private final int value;
    
    ConsumeFilterType(int value) {
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