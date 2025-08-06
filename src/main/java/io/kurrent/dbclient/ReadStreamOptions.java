package io.kurrent.dbclient;

/**
 * Options of the read stream request.
 */
public class ReadStreamOptions extends OptionsWithStartRevisionAndResolveLinkTosBase<ReadStreamOptions> {
    private Direction direction;
    private long maxCount;

    private ReadStreamOptions() {
        this.direction = Direction.Forwards;
        this.maxCount = Long.MAX_VALUE;
    }

    /**
     * Returns options with default values.
     */
    public static ReadStreamOptions get() {
        return new ReadStreamOptions();
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
    public ReadStreamOptions direction(Direction direction) {
        this.direction = direction;
        return this;
    }

    /**
     * Reads stream in revision-ascending order.

     */
    public ReadStreamOptions forwards() {
        return direction(Direction.Forwards);
    }

    /**
     * Reads stream in revision-descending order.

     */
    public ReadStreamOptions backwards() {
        return direction(Direction.Backwards);
    }

    /**
     * The maximum event count EventStoreDB will return.
     */
    public ReadStreamOptions maxCount(long maxCount) {
        this.maxCount = maxCount;
        return this;
    }
}
