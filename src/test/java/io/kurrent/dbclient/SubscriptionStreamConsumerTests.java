package io.kurrent.dbclient;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class SubscriptionStreamConsumerTests {
    private static final SubscriptionTracingCallback NO_OP_TRACING =
        (subscriptionId, event, action) -> action.run();

    @Test
    public void testOnCancelledCompletesFutureExceptionallyBeforeConfirmation() {
        CompletableFuture<Subscription> future = new CompletableFuture<>();
        AtomicBoolean listenerCalled = new AtomicBoolean(false);
        AtomicReference<Throwable> listenerException = new AtomicReference<>();

        SubscriptionStreamConsumer consumer = new SubscriptionStreamConsumer(
            new SubscriptionListener() {
                @Override
                public void onCancelled(Subscription subscription, Throwable exception) {
                    listenerCalled.set(true);
                    listenerException.set(exception);
                }
            },
            null,
            future,
            NO_OP_TRACING
        );

        RuntimeException error = new RuntimeException("server went away");
        consumer.onCancelled(error);

        Assertions.assertTrue(future.isCompletedExceptionally());
        ExecutionException ex = Assertions.assertThrows(ExecutionException.class, future::get);
        Assertions.assertSame(error, ex.getCause());
        Assertions.assertTrue(listenerCalled.get());
        Assertions.assertSame(error, listenerException.get());
    }

    @Test
    public void testOnCancelledAfterConfirmationDoesNotAffectFuture() {
        CompletableFuture<Subscription> future = new CompletableFuture<>();
        AtomicBoolean listenerCalled = new AtomicBoolean(false);
        AtomicReference<Subscription> listenerSubscription = new AtomicReference<>();

        SubscriptionStreamConsumer consumer = new SubscriptionStreamConsumer(
            new SubscriptionListener() {
                @Override
                public void onCancelled(Subscription subscription, Throwable exception) {
                    listenerCalled.set(true);
                    listenerSubscription.set(subscription);
                }
            },
            null,
            future,
            NO_OP_TRACING
        );

        consumer.onSubscribe(new org.reactivestreams.Subscription() {
            @Override public void request(long n) {}
            @Override public void cancel() {}
        });

        consumer.onSubscriptionConfirmation("test-sub-id");
        Assertions.assertTrue(future.isDone());
        Assertions.assertFalse(future.isCompletedExceptionally());

        consumer.onCancelled(null);

        Assertions.assertFalse(future.isCompletedExceptionally(), "future should remain successfully completed");
        Assertions.assertTrue(listenerCalled.get());
        Assertions.assertNotNull(listenerSubscription.get());
        Assertions.assertEquals("test-sub-id", listenerSubscription.get().getSubscriptionId());
    }
}
