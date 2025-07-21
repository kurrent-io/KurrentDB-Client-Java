package io.kurrent.dbclient;

import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class MultiStreamAppendTests implements ConnectionAware {
    static private Database database;
    static private Logger logger;

    @BeforeAll
    public static void setup() {
        database = DatabaseFactory.spawn();
        logger = LoggerFactory.getLogger(MultiStreamAppendTests.class);
    }

    @Override
    public Database getDatabase() {
        return database;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @AfterAll
    public static void cleanup() {
        database.dispose();
    }

    @Test
    public void testMultiStreamAppend() throws ExecutionException, InterruptedException {
        KurrentDBClient client = getDefaultClient();

        Optional<ServerVersion> version = client.getServerVersion().get();

        Assumptions.assumeTrue(
                version.isPresent() && version.get().isGreaterOrEqualThan(25, 0),
                "Multi-stream append is not supported server versions below 25.0.0"
        );

        List<AppendStreamRequest> requests = new ArrayList<>();

        List<EventData> events = new ArrayList<>();
        for (int i = 0; i < 10; i++)
            events.add(EventData.builderAsBinary("created", new byte[0]).build());

        requests.add(new AppendStreamRequest("foobar", events.iterator(), StreamState.any()));
        requests.add(new AppendStreamRequest("baz", events.iterator(), StreamState.any()));

        MultiAppendWriteResult result = client.multiAppend(AppendToStreamOptions.get(), requests.iterator()).get();

        Assertions.assertTrue(result.getSuccesses().isPresent());
    }

    @Test
    public void testMultiStreamAppendWhenUnsupported() throws ExecutionException, InterruptedException {
        KurrentDBClient client = getDefaultClient();

        Optional<ServerVersion> version = client.getServerVersion().get();
        Assumptions.assumeFalse(
                version.isPresent() && version.get().isGreaterOrEqualThan(25, 0),
                "Multi-stream is supported server versions greater or equal to 25.0.0"
        );

        List<AppendStreamRequest> requests = new ArrayList<>();

        List<EventData> events = new ArrayList<>();
        for (int i = 0; i < 10; i++)
            events.add(EventData.builderAsBinary("created", new byte[0]).build());

        requests.add(new AppendStreamRequest("foobar", events.iterator(), StreamState.any()));
        requests.add(new AppendStreamRequest("baz", events.iterator(), StreamState.any()));

        ExecutionException e = Assertions.assertThrows(
                ExecutionException.class,
                () -> client.multiAppend(AppendToStreamOptions.get(), requests.iterator()).get());

        Assertions.assertInstanceOf(UnsupportedOperationException.class, e.getCause());
    }
}

