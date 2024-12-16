package io.kurrent.dbclient;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import java.util.ArrayList;
import java.util.function.Predicate;

public class Exceptions {
    final private ArrayList<Predicate<Throwable>> exceptions;

    public Exceptions() {
        exceptions = new ArrayList<>();
    }

    public <A extends Throwable> Exceptions register(Predicate<A> predicate) {
        this.exceptions.add(e -> {
            @SuppressWarnings("unchecked")
            A target = (A) e;
            return target != null && predicate.test(target);
        });

        return this;
    }

    public <A extends Throwable> Exceptions register(Class<A> clazz) {
        this.exceptions.add(clazz::isInstance);
        return this;
    }

    public Exceptions registerGoAwayError() {
        return this.<StatusRuntimeException>register(e ->
            e.getStatus().getCode() == Status.Code.INTERNAL
        );
    }

    public Exceptions registerUnknownError() {
        return this.<StatusRuntimeException>register(e ->
                e.getStatus().getCode() == Status.Code.UNKNOWN
        );
    }

    public boolean contains(Throwable target) {
        for (Predicate<Throwable> predicate : this.exceptions) {
            if (predicate.test(target)) {
                return true;
            }
        }

        return false;
    }
}
