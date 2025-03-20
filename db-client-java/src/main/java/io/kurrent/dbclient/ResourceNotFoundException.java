package io.kurrent.dbclient;

/**
 * A remote resource was not found or because its access was denied. Could only happen when a request was performed
 * through HTTP.
 */
public class ResourceNotFoundException extends RuntimeException {
    ResourceNotFoundException(){
        super("Resource not found exception");
    }
}
