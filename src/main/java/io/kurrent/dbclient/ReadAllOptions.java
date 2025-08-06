package io.kurrent.dbclient;

/**
 * Options of the read $all stream request.
 */
public class ReadAllOptions extends OptionsWithPositionAndResolveLinkTosBase<ReadAllOptions> {
    private Direction direction;
    private long maxCount;

    private ReadAllOptions() {
        this.direction = Direction.Forwards;
        this.maxCount = Long.MAX_VALUE;
    }

    /**
     * Returns options with default values.
     */
    public static ReadAllOptions get() {
        return new ReadAllOptions();
    }

    Direction getDirection() {
        return this.direction;
    }

    long getMaxCount() {
        return this.maxCount;
    }

    /**
     * Reads stream in the given direction.
     */
    public ReadAllOptions direction(Direction direction) {
        this.direction = direction;
        return this;
    }

    /**
     * Reads stream in revision-ascending order.
     */
    public ReadAllOptions forwards() {
        return direction(Direction.Forwards);
    }

    /**
     * Reads stream in revision-descending order.
     */
    public ReadAllOptions backwards() {
        return direction(Direction.Backwards);
    }

    /**
     * The maximum event count KurrentDB will return.
     */
    public ReadAllOptions maxCount(long maxCount) {
        this.maxCount = maxCount;
        return this;
    }
}
