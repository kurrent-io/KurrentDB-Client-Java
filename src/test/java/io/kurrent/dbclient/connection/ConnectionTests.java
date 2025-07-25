package io.kurrent.dbclient.connection;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.kurrent.dbclient.*;
import io.kurrent.dbclient.databases.DockerContainerDatabase;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class ConnectionTests {
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

    static void pauseDatabase() {
        logger.debug("Pausing database container: {}", database.getContainerId());
        database.getDockerClient().pauseContainerCmd(database.getContainerId()).exec();
    }

    static void unpauseDatabase() {
        logger.debug("Unpausing database container: {}", database.getContainerId());
        database.getDockerClient().unpauseContainerCmd(database.getContainerId()).exec();
    }
}
