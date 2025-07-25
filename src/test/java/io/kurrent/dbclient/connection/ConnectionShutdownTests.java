package io.kurrent.dbclient.connection;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.kurrent.dbclient.*;
import io.kurrent.dbclient.databases.DockerContainerDatabase;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ConnectionShutdownTests {
    static private DockerContainerDatabase database;
    static private Logger logger;

    @BeforeEach
    public void setup() {
        database = (DockerContainerDatabase) DatabaseFactory.spawn();
        logger = LoggerFactory.getLogger(PersistentSubscriptionsTests.class);
    }

    @AfterEach
    public void cleanup() {
        unpauseDatabase();
        database.dispose();
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    public void testCallTerminationWhenServerUnreachable() throws Throwable {
        KurrentDBClient client = database.defaultClient();

        ReadResult initialResult = client.readAll(ReadAllOptions.get()).get(5, TimeUnit.SECONDS);

        Assertions.assertFalse(initialResult.getEvents().isEmpty());

        pauseDatabase();

        ExecutionException e = Assertions.assertThrows(ExecutionException.class, () ->
                client.readAll().get(30, TimeUnit.SECONDS)
        );

        StatusRuntimeException status = (StatusRuntimeException) e.getCause();
        Assertions.assertEquals(Status.Code.UNAVAILABLE, status.getStatus().getCode());
    }

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

    static void pauseDatabase() {
        logger.debug("Pausing database container: {}", database.getContainerId());
        database.getDockerClient().pauseContainerCmd(database.getContainerId()).exec();
    }

    static void unpauseDatabase() {
        logger.debug("Unpausing database container: {}", database.getContainerId());
        database.getDockerClient().unpauseContainerCmd(database.getContainerId()).exec();
    }
}
