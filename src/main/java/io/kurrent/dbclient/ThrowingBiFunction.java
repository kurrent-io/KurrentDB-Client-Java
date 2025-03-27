package io.kurrent.dbclient;

@FunctionalInterface
interface ThrowingBiFunction<TFirst, TSecond, TResult, TException extends Throwable> {

    TResult apply(TFirst first, TSecond second) throws TException;
}
