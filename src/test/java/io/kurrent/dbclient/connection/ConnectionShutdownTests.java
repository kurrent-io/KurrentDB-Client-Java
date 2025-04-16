package io.kurrent.dbclient.connection;

import io.kurrent.dbclient.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ConnectionShutdownTests {
    @Test
    @Timeout(value = 1, unit = TimeUnit.MINUTES)
    public void testDatabaseCleanupWithActiveSubscription() throws Throwable {
        Database testDatabase = DatabaseFactory.spawn();
        KurrentDBClient client = testDatabase.defaultClient();

        final AtomicInteger count = new AtomicInteger(0);
        final AtomicInteger retryCount = new AtomicInteger(-1);
        final AtomicBoolean cancellationReceived = new AtomicBoolean(false);
        final CountDownLatch cancellationLatch = new CountDownLatch(1);
        final AtomicReference<Throwable> reconnectError = new AtomicReference<>();

        SubscriptionListener listener = new SubscriptionListener() {
            @Override
            public void onEvent(Subscription subscription, ResolvedEvent event) {
                count.incrementAndGet();
            }

            @Override
            public void onCancelled(Subscription subscription, Throwable throwable) {
                cancellationReceived.set(true);

                retryCount.incrementAndGet();

                try {
                    client.subscribeToAll(this).get(10, TimeUnit.SECONDS);
                } catch (Throwable ex) {
                    reconnectError.set(ex);
                } finally {
                    cancellationLatch.countDown();
                }
            }
        };

        client.subscribeToAll(listener).get();

        testDatabase.dispose();

        boolean callbackReceived = cancellationLatch.await(30, TimeUnit.SECONDS);
        Assertions.assertTrue(callbackReceived);
        Assertions.assertTrue(cancellationReceived.get());
        Assertions.assertTrue(count.get() > 0);
        Assertions.assertEquals(2, retryCount.get());

        Throwable ex = reconnectError.get();
        Assertions.assertInstanceOf(ConnectionShutdownException.class, ex.getCause());
    }
}
