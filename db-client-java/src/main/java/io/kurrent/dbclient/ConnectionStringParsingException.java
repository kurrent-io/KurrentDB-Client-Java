package io.kurrent.dbclient;

/**
 * When the provided connection string is malformed.
 */
public class ConnectionStringParsingException extends Exception {
    ConnectionStringParsingException(String connectionString, int from, int to, String expected) {
        super("Unexpected " + connectionString.substring(from, from == to ? from + 1 : to) + " at position " + from + ", expected " + expected);
    }
}
