package io.kurrent.dbclient;

/**
 * Named consumer strategies for use with persistent subscriptions.
 */
public class NamedConsumerStrategy {
    private final String value;

    /**
     * Distributes events to a single client until the buffer size is reached. After which the next client is selected
     * in a round-robin style, and the process is repeated.
     */
    public static final NamedConsumerStrategy DISPATCH_TO_SINGLE = new NamedConsumerStrategy("DispatchToSingle");

    /**
     * Distributes events to all client evenly. If the client buffer-size is reached, the client is ignored until
     * events are (not) acknowledged.
     */
    public static final NamedConsumerStrategy ROUND_ROBIN = new NamedConsumerStrategy("RoundRobin");

    /**
     * For use with an indexing projection such as the system $by_category projection. KurrentDB inspects event for
     * its source stream id, hashing the id to one of 1024 buckets assigned to individual clients. When a client
     * disconnects, its buckets are assigned to other clients. When a client connects, it is assigned some existing
     * buckets. This naively attempts to maintain a balanced workload. The main goal of this strategy is to decrease the
     * likelihood of concurrency and ordering issues while maintaining load balancing. This not a guarantee, and you
     * should handle the usual ordering and concurrency issues.
     */
    public static final NamedConsumerStrategy PINNED = new NamedConsumerStrategy("Pinned");

    /**
     * This is similar to the Pinned strategy, but instead of using the source stream id to bucket the messages, it distributes the events based on the event's correlationId.
     */
    public static final NamedConsumerStrategy PINNED_BY_CORRELATION = new NamedConsumerStrategy("PinnedByCorrelation");

    NamedConsumerStrategy(String value) {
        this.value = value;
    }

    /**
     * Checks if it's a <i>DispatchToSingle</i> strategy.
     */
    public boolean isDispatchToSingle() {
        return isNamed("DispatchToSingle");
    }

    /**
     * Checks if it's a <i>RoundRobin</i> strategy.
     */
    public boolean isRoundRobin() {
        return isNamed("RoundRobin");
    }

    /**
     * Checks if it's a <i>Pinned</i> strategy.
     */
    public boolean isPinned() {
        return isNamed("Pinned");
    }


    /**
     * Checks if it's a <i>PinnedByCorrelation</i> strategy.
     */
    public boolean isPinnedByCorrelation() {
        return isNamed("PinnedByCorrelation");
    }

    /**
     * Checks if the strategy's name matches the string passed as a parameter.
     */
    public boolean isNamed(String named) {
        return value.equals(named);
    }

    public String toString() {
        return value;
    }
}
