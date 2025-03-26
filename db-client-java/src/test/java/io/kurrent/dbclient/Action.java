package io.kurrent.dbclient;

@FunctionalInterface
public interface Action<A> {
    A run() throws Exception;
}
