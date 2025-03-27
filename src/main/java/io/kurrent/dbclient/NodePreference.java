
package io.kurrent.dbclient;

/**
 * Indicates which order of preferred nodes for connecting to.
 */
public enum NodePreference {
    /**
     * When attempting connection, prefers leader nodes.
     */
    LEADER,

    /**
     * When attempting connection, prefers follower nodes.
     */
    FOLLOWER,

    /**
     * When attempting connection, prefers read-replica nodes.
     */
    READ_ONLY_REPLICA,

    /**
     * When attempting connection, has no node preference.
     */
    RANDOM
}