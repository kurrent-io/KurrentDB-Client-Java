package io.kurrent.dbclient;

/**
 * When a connection is already closed.
 */
public class ConnectionShutdownException extends RuntimeException {
    ConnectionShutdownException() {
        super("The connection is closed");
    }
}
