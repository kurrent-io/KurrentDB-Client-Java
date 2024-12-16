package io.kurrent.dbclient;

/**
 * A request not supported by the targeted KurrentDB node was sent.
 */
public class UnsupportedFeatureException extends RuntimeException {
    UnsupportedFeatureException(){
        super("Unsupported feature exception");
    }
}
