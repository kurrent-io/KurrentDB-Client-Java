package io.kurrent.dbclient;

@FunctionalInterface
interface ThrowingFunction<TInput, TResult, TException extends Throwable> {

    TResult apply(TInput first) throws TException;
}
