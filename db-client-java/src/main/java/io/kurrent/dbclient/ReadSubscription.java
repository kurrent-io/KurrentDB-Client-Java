package io.kurrent.dbclient;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ClientCallStreamObserver;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class ReadSubscription implements Subscription {
    private final Subscriber<? super ReadMessage> subscriber;
    private ClientCallStreamObserver<?> streamObserver;
    private final AtomicLong requested = new AtomicLong(0);
    private final AtomicBoolean terminated = new AtomicBoolean(false);
    private final Lock lock = new ReentrantLock();
    private final Condition hasRequested = lock.newCondition();

    ReadSubscription(Subscriber<? super ReadMessage> subscriber) {
        this.subscriber = subscriber;
    }

    public void setStreamObserver(ClientCallStreamObserver<?> streamObserver) {
        this.streamObserver = streamObserver;
    }

    public void onError(Throwable error) {
        if (error instanceof StatusRuntimeException) {
            StatusRuntimeException statusRuntimeException = (StatusRuntimeException) error;
            if (statusRuntimeException.getStatus().getCode() == Status.Code.CANCELLED) {
                return;
            }
        }
        if (!terminated.get()) {
            subscriber.onError(error);
        }
        cancel();
    }

    public void onNext(ReadMessage message) {
        lock.lock();
        while (requested.get() == 0 && !terminated.get()) {
            hasRequested.awaitUninterruptibly();
        }
        if (!terminated.get()) {
            subscriber.onNext(message);
            requested.decrementAndGet();
        }
        lock.unlock();
    }

    public void onCompleted() {
        if (terminated.compareAndSet(false, true)) {
            subscriber.onComplete();
        }
    }

    @Override
    public void request(long n) {
        if (n <= 0) {
            subscriber.onError(new IllegalArgumentException("non-positive subscription request: " + n));
        }
        lock.lock();
        requested.updateAndGet(current -> current + n);
        hasRequested.signal();
        lock.unlock();
    }

    @Override
    public void cancel() {
        if (terminated.compareAndSet(false, true)) {
            if (streamObserver != null) {
                streamObserver.cancel("Stream has been cancelled manually.", null);
            }
        }
    }

}
