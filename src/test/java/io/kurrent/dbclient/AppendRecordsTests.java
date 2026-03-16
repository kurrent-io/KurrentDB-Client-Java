package io.kurrent.dbclient;

import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class AppendRecordsTests implements ConnectionAware {
    static private Database database;
    static private Logger logger;

    @BeforeAll
    public static void setup() {
        database = DatabaseFactory.spawn();
        logger = LoggerFactory.getLogger(AppendRecordsTests.class);
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
    public void testAppendRecordsToSingleStream() throws ExecutionException, InterruptedException {
        KurrentDBClient client = getDefaultClient();

        Optional<ServerVersion> version = client.getServerVersion().get();
        Assumptions.assumeTrue(
                version.isPresent() && version.get().isGreaterOrEqualThan(26, 1),
                "AppendRecords requires server version 26.1 or later"
        );

        String streamName = generateName();

        List<AppendRecord> records = Arrays.asList(
                new AppendRecord(streamName, EventData.builderAsJson("event-type-a", "{\"data\":\"1\"}".getBytes()).build()),
                new AppendRecord(streamName, EventData.builderAsJson("event-type-b", "{\"data\":\"2\"}".getBytes()).build()),
                new AppendRecord(streamName, EventData.builderAsJson("event-type-c", "{\"data\":\"3\"}".getBytes()).build())
        );

        AppendRecordsResponse result = client.appendRecords(records).get();

        Assertions.assertTrue(result.getPosition() > 0);
        Assertions.assertFalse(result.getResults().isEmpty());
        Assertions.assertEquals(1, result.getResults().size());
        Assertions.assertEquals(streamName, result.getResults().get(0).getStream());

        List<ResolvedEvent> readEvents = client.readStream(streamName, ReadStreamOptions.get()).get().getEvents();
        Assertions.assertEquals(3, readEvents.size());
    }

    @Test
    public void testAppendRecordsToMultipleStreams() throws ExecutionException, InterruptedException {
        KurrentDBClient client = getDefaultClient();

        Optional<ServerVersion> version = client.getServerVersion().get();
        Assumptions.assumeTrue(
                version.isPresent() && version.get().isGreaterOrEqualThan(26, 1),
                "AppendRecords requires server version 26.1 or later"
        );

        String stream1 = generateName();
        String stream2 = generateName();

        List<AppendRecord> records = Arrays.asList(
                new AppendRecord(stream1, EventData.builderAsJson("event-a", "{\"data\":\"1\"}".getBytes()).build()),
                new AppendRecord(stream1, EventData.builderAsJson("event-b", "{\"data\":\"2\"}".getBytes()).build()),
                new AppendRecord(stream2, EventData.builderAsJson("event-c", "{\"data\":\"3\"}".getBytes()).build())
        );

        AppendRecordsResponse result = client.appendRecords(records).get();

        Assertions.assertTrue(result.getPosition() > 0);
        Assertions.assertEquals(2, result.getResults().size());

        List<ResolvedEvent> readEvents1 = client.readStream(stream1, ReadStreamOptions.get()).get().getEvents();
        List<ResolvedEvent> readEvents2 = client.readStream(stream2, ReadStreamOptions.get()).get().getEvents();
        Assertions.assertEquals(2, readEvents1.size());
        Assertions.assertEquals(1, readEvents2.size());
    }

    @Test
    public void testInterleavedTracksRevisions() throws ExecutionException, InterruptedException {
        KurrentDBClient client = getDefaultClient();

        Optional<ServerVersion> version = client.getServerVersion().get();
        Assumptions.assumeTrue(
                version.isPresent() && version.get().isGreaterOrEqualThan(26, 1),
                "AppendRecords requires server version 26.1 or later"
        );

        String stream1 = generateName();
        String stream2 = generateName();

        List<AppendRecord> records = Arrays.asList(
                new AppendRecord(stream1, EventData.builderAsJson("event-1", "{}".getBytes()).build()),
                new AppendRecord(stream2, EventData.builderAsJson("event-2", "{}".getBytes()).build()),
                new AppendRecord(stream1, EventData.builderAsJson("event-3", "{}".getBytes()).build()),
                new AppendRecord(stream2, EventData.builderAsJson("event-4", "{}".getBytes()).build()),
                new AppendRecord(stream1, EventData.builderAsJson("event-5", "{}".getBytes()).build())
        );

        AppendRecordsResponse result = client.appendRecords(records).get();

        Assertions.assertTrue(result.getPosition() > 0);
        Assertions.assertEquals(2, result.getResults().size());

        AppendResponse rev1 = result.getResults().stream()
                .filter(r -> r.getStream().equals(stream1)).findFirst().get();
        AppendResponse rev2 = result.getResults().stream()
                .filter(r -> r.getStream().equals(stream2)).findFirst().get();

        Assertions.assertEquals(2, rev1.getStreamRevision());
        Assertions.assertEquals(1, rev2.getStreamRevision());

        List<ResolvedEvent> readEvents1 = client.readStream(stream1, ReadStreamOptions.get()).get().getEvents();
        List<ResolvedEvent> readEvents2 = client.readStream(stream2, ReadStreamOptions.get()).get().getEvents();
        Assertions.assertEquals(3, readEvents1.size());
        Assertions.assertEquals(2, readEvents2.size());
    }

    @Test
    public void testAppendRecordsWithConsistencyCheckSuccess() throws ExecutionException, InterruptedException {
        KurrentDBClient client = getDefaultClient();

        Optional<ServerVersion> version = client.getServerVersion().get();
        Assumptions.assumeTrue(
                version.isPresent() && version.get().isGreaterOrEqualThan(26, 1),
                "AppendRecords requires server version 26.1 or later"
        );

        String streamName = generateName();

        List<AppendRecord> records = Collections.singletonList(
                new AppendRecord(streamName, EventData.builderAsJson("event-a", "{}".getBytes()).build())
        );
        List<ConsistencyCheck> checks = Collections.singletonList(
                new ConsistencyCheck.StreamStateCheck(streamName, StreamState.noStream())
        );

        AppendRecordsResponse result = client.appendRecords(records, checks).get();

        Assertions.assertTrue(result.getPosition() > 0);
        Assertions.assertEquals(1, result.getResults().size());
        Assertions.assertEquals(streamName, result.getResults().get(0).getStream());
    }

    @Test
    public void testAppendRecordsWithConsistencyCheckFailure() throws ExecutionException, InterruptedException {
        KurrentDBClient client = getDefaultClient();

        Optional<ServerVersion> version = client.getServerVersion().get();
        Assumptions.assumeTrue(
                version.isPresent() && version.get().isGreaterOrEqualThan(26, 1),
                "AppendRecords requires server version 26.1 or later"
        );

        String streamName = generateName();

        // First, write some records so the stream exists
        client.appendRecords(Collections.singletonList(
                new AppendRecord(streamName, EventData.builderAsJson("setup-event", "{}".getBytes()).build())
        )).get();

        // Now try with NoStream check — should fail
        List<AppendRecord> records = Collections.singletonList(
                new AppendRecord(streamName, EventData.builderAsJson("event-a", "{}".getBytes()).build())
        );
        List<ConsistencyCheck> checks = Collections.singletonList(
                new ConsistencyCheck.StreamStateCheck(streamName, StreamState.noStream())
        );

        AppendConsistencyViolationException ex = Assertions.assertThrows(
                AppendConsistencyViolationException.class, () -> {
                    try {
                        client.appendRecords(records, checks).get();
                    } catch (ExecutionException e) {
                        throw e.getCause();
                    }
                });

        Assertions.assertEquals(1, ex.getViolations().size());
        ConsistencyViolation violation = ex.getViolations().get(0);
        Assertions.assertEquals(0, violation.getCheckIndex());
        Assertions.assertEquals(streamName, violation.getStream());
        Assertions.assertEquals(StreamState.noStream(), violation.getExpectedState());
        Assertions.assertEquals(StreamState.streamRevision(0), violation.getActualState());
    }

    @Test
    public void testAppendRecordsWithDecoupledConsistencyCheck() throws ExecutionException, InterruptedException {
        KurrentDBClient client = getDefaultClient();

        Optional<ServerVersion> version = client.getServerVersion().get();
        Assumptions.assumeTrue(
                version.isPresent() && version.get().isGreaterOrEqualThan(26, 1),
                "AppendRecords requires server version 26.1 or later"
        );

        String checkStream = generateName();
        String writeStream = generateName();

        // Check that checkStream does not exist, write to writeStream
        List<AppendRecord> records = Collections.singletonList(
                new AppendRecord(writeStream, EventData.builderAsJson("event-a", "{}".getBytes()).build())
        );
        List<ConsistencyCheck> checks = Collections.singletonList(
                new ConsistencyCheck.StreamStateCheck(checkStream, StreamState.noStream())
        );

        AppendRecordsResponse result = client.appendRecords(records, checks).get();

        Assertions.assertTrue(result.getPosition() > 0);
        Assertions.assertEquals(1, result.getResults().size());
        Assertions.assertEquals(writeStream, result.getResults().get(0).getStream());
    }

    @Test
    public void testAppendRecordsWithMultipleChecks() throws ExecutionException, InterruptedException {
        KurrentDBClient client = getDefaultClient();

        Optional<ServerVersion> version = client.getServerVersion().get();
        Assumptions.assumeTrue(
                version.isPresent() && version.get().isGreaterOrEqualThan(26, 1),
                "AppendRecords requires server version 26.1 or later"
        );

        String stream1 = generateName();
        String stream2 = generateName();
        String writeStream = generateName();

        // Write to stream1 so it exists at revision 0
        client.appendRecords(Collections.singletonList(
                new AppendRecord(stream1, EventData.builderAsJson("setup", "{}".getBytes()).build())
        )).get();

        // Check stream1 at NoStream (will fail) and stream2 at NoStream (will pass)
        List<AppendRecord> records = Collections.singletonList(
                new AppendRecord(writeStream, EventData.builderAsJson("event-a", "{}".getBytes()).build())
        );
        List<ConsistencyCheck> checks = Arrays.asList(
                new ConsistencyCheck.StreamStateCheck(stream1, StreamState.noStream()),
                new ConsistencyCheck.StreamStateCheck(stream2, StreamState.noStream())
        );

        AppendConsistencyViolationException ex = Assertions.assertThrows(
                AppendConsistencyViolationException.class, () -> {
                    try {
                        client.appendRecords(records, checks).get();
                    } catch (ExecutionException e) {
                        throw e.getCause();
                    }
                });

        Assertions.assertEquals(1, ex.getViolations().size());
        ConsistencyViolation violation = ex.getViolations().get(0);
        Assertions.assertEquals(0, violation.getCheckIndex());
        Assertions.assertEquals(stream1, violation.getStream());
        Assertions.assertEquals(StreamState.noStream(), violation.getExpectedState());
    }

    @Test
    public void testAppendRecordsWhenUnsupported() throws ExecutionException, InterruptedException {
        KurrentDBClient client = getDefaultClient();

        Optional<ServerVersion> version = client.getServerVersion().get();
        Assumptions.assumeFalse(
                version.isPresent() && version.get().isGreaterOrEqualThan(26, 1),
                "AppendRecords is supported on server versions 26.1 and later"
        );

        List<AppendRecord> records = Collections.singletonList(
                new AppendRecord("test-stream", EventData.builderAsJson("event-a", "{}".getBytes()).build())
        );

        ExecutionException e = Assertions.assertThrows(
                ExecutionException.class,
                () -> client.appendRecords(records).get());

        Assertions.assertInstanceOf(UnsupportedOperationException.class, e.getCause());
    }
}
