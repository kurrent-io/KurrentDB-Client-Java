package io.kurrent.dbclient;

/**
 * When no node was found based on the connection string provided.
 */
public class NoClusterNodeFoundException extends RuntimeException {
    NoClusterNodeFoundException(){}
}
