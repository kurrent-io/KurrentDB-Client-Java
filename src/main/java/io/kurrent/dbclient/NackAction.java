package io.kurrent.dbclient;

/**
 * Gathers every possible Nak actions.
 */
public enum NackAction {
    /**
     * Client does not know what action to take, let the server decide.
     */
    Unknown,

    /**
     * Park message do not resend. Put on poison queue.
     */
    Park,

    /**
     * Explicit retry the message.
     */
    Retry,

    /**
     * Skip this message do not resend do not put in poison queue.
     */
    Skip,

    /**
     * Stop the subscription.
     */
    Stop,
}
