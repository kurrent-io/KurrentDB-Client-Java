package io.kurrent.dbclient;

import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class AppendRecordsWriteOnlyTests implements ConnectionAware {
    static private Database database;
    static private Logger logger;

    @BeforeAll
    public static void setup() {
        database = DatabaseFactory.spawn();
        logger = LoggerFactory.getLogger(AppendRecordsWriteOnlyTests.class);
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

    private void assumeSupported(KurrentDBClient client) throws ExecutionException, InterruptedException {
        Optional<ServerVersion> version = client.getServerVersion().get();
        Assumptions.assumeTrue(
                version.isPresent() && version.get().isGreaterOrEqualThan(26, 1),
                "AppendRecords requires server version 26.1 or later"
        );
    }

    private void seedStream(KurrentDBClient client, String stream, int eventCount) throws ExecutionException, InterruptedException {
        List<AppendRecord> records = new ArrayList<>();
        for (int i = 0; i < eventCount; i++) {
            records.add(new AppendRecord(stream, EventData.builderAsJson("seed-event", "{}".getBytes()).build()));
        }
        client.appendRecords(records).get();
    }

    // ==================== WhenExpectingAny ====================

    @Test
    public void testWriteAny_succeeds_when_stream_has_revision() throws ExecutionException, InterruptedException {
        KurrentDBClient client = getDefaultClient();
        assumeSupported(client);

        String stream = generateName();
        seedStream(client, stream, 3);

        List<AppendRecord> records = Collections.singletonList(
                new AppendRecord(stream, EventData.builderAsJson("event-a", "{}".getBytes()).build())
        );
        List<ConsistencyCheck> checks = Collections.singletonList(
                new ConsistencyCheck.StreamStateCheck(stream, StreamState.any())
        );

        AppendRecordsResponse result = client.appendRecords(records, checks).get();
        Assertions.assertTrue(result.getPosition() > 0);
        Assertions.assertEquals(1, result.getResults().size());
    }

    @Test
    public void testWriteAny_succeeds_when_stream_not_found() throws ExecutionException, InterruptedException {
        KurrentDBClient client = getDefaultClient();
        assumeSupported(client);

        String stream = generateName();

        List<AppendRecord> records = Collections.singletonList(
                new AppendRecord(stream, EventData.builderAsJson("event-a", "{}".getBytes()).build())
        );
        List<ConsistencyCheck> checks = Collections.singletonList(
                new ConsistencyCheck.StreamStateCheck(stream, StreamState.any())
        );

        AppendRecordsResponse result = client.appendRecords(records, checks).get();
        Assertions.assertTrue(result.getPosition() > 0);
    }

    @Test
    public void testWriteAny_succeeds_when_stream_is_deleted() throws ExecutionException, InterruptedException {
        KurrentDBClient client = getDefaultClient();
        assumeSupported(client);

        String stream = generateName();
        seedStream(client, stream, 3);
        client.deleteStream(stream).get();

        List<AppendRecord> records = Collections.singletonList(
                new AppendRecord(stream, EventData.builderAsJson("event-a", "{}".getBytes()).build())
        );
        List<ConsistencyCheck> checks = Collections.singletonList(
                new ConsistencyCheck.StreamStateCheck(stream, StreamState.any())
        );

        AppendRecordsResponse result = client.appendRecords(records, checks).get();
        Assertions.assertTrue(result.getPosition() > 0);
    }

    @Test
    public void testWriteAny_fails_when_stream_is_tombstoned() throws ExecutionException, InterruptedException {
        KurrentDBClient client = getDefaultClient();
        assumeSupported(client);

        String stream = generateName();
        seedStream(client, stream, 3);
        client.tombstoneStream(stream).get();

        List<AppendRecord> records = Collections.singletonList(
                new AppendRecord(stream, EventData.builderAsJson("event-a", "{}".getBytes()).build())
        );
        List<ConsistencyCheck> checks = Collections.singletonList(
                new ConsistencyCheck.StreamStateCheck(stream, StreamState.any())
        );

        Assertions.assertThrows(AppendConsistencyViolationException.class, () -> {
            try {
                client.appendRecords(records, checks).get();
            } catch (ExecutionException e) {
                throw e.getCause();
            }
        });
    }

    // ==================== WhenExpectingExists ====================

    @Test
    public void testWriteExists_succeeds_when_stream_has_revision() throws ExecutionException, InterruptedException {
        KurrentDBClient client = getDefaultClient();
        assumeSupported(client);

        String stream = generateName();
        seedStream(client, stream, 3);

        List<AppendRecord> records = Collections.singletonList(
                new AppendRecord(stream, EventData.builderAsJson("event-a", "{}".getBytes()).build())
        );
        List<ConsistencyCheck> checks = Collections.singletonList(
                new ConsistencyCheck.StreamStateCheck(stream, StreamState.streamExists())
        );

        AppendRecordsResponse result = client.appendRecords(records, checks).get();
        Assertions.assertTrue(result.getPosition() > 0);
    }

    @Test
    public void testWriteExists_fails_when_stream_not_found() throws ExecutionException, InterruptedException {
        KurrentDBClient client = getDefaultClient();
        assumeSupported(client);

        String stream = generateName();

        List<AppendRecord> records = Collections.singletonList(
                new AppendRecord(stream, EventData.builderAsJson("event-a", "{}".getBytes()).build())
        );
        List<ConsistencyCheck> checks = Collections.singletonList(
                new ConsistencyCheck.StreamStateCheck(stream, StreamState.streamExists())
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
        Assertions.assertEquals(stream, ex.getViolations().get(0).getStream());
    }

    @Test
    public void testWriteExists_fails_when_stream_is_deleted() throws ExecutionException, InterruptedException {
        KurrentDBClient client = getDefaultClient();
        assumeSupported(client);

        String stream = generateName();
        seedStream(client, stream, 3);
        client.deleteStream(stream).get();

        List<AppendRecord> records = Collections.singletonList(
                new AppendRecord(stream, EventData.builderAsJson("event-a", "{}".getBytes()).build())
        );
        List<ConsistencyCheck> checks = Collections.singletonList(
                new ConsistencyCheck.StreamStateCheck(stream, StreamState.streamExists())
        );

        Assertions.assertThrows(AppendConsistencyViolationException.class, () -> {
            try {
                client.appendRecords(records, checks).get();
            } catch (ExecutionException e) {
                throw e.getCause();
            }
        });
    }

    @Test
    public void testWriteExists_fails_when_stream_is_tombstoned() throws ExecutionException, InterruptedException {
        KurrentDBClient client = getDefaultClient();
        assumeSupported(client);

        String stream = generateName();
        seedStream(client, stream, 3);
        client.tombstoneStream(stream).get();

        List<AppendRecord> records = Collections.singletonList(
                new AppendRecord(stream, EventData.builderAsJson("event-a", "{}".getBytes()).build())
        );
        List<ConsistencyCheck> checks = Collections.singletonList(
                new ConsistencyCheck.StreamStateCheck(stream, StreamState.streamExists())
        );

        Assertions.assertThrows(AppendConsistencyViolationException.class, () -> {
            try {
                client.appendRecords(records, checks).get();
            } catch (ExecutionException e) {
                throw e.getCause();
            }
        });
    }

    // ==================== WhenExpectingNoStream ====================

    @Test
    public void testWriteNoStream_succeeds_when_stream_not_found() throws ExecutionException, InterruptedException {
        KurrentDBClient client = getDefaultClient();
        assumeSupported(client);

        String stream = generateName();

        List<AppendRecord> records = Collections.singletonList(
                new AppendRecord(stream, EventData.builderAsJson("event-a", "{}".getBytes()).build())
        );
        List<ConsistencyCheck> checks = Collections.singletonList(
                new ConsistencyCheck.StreamStateCheck(stream, StreamState.noStream())
        );

        AppendRecordsResponse result = client.appendRecords(records, checks).get();
        Assertions.assertTrue(result.getPosition() > 0);
    }

    @Test
    public void testWriteNoStream_succeeds_when_stream_is_deleted() throws ExecutionException, InterruptedException {
        KurrentDBClient client = getDefaultClient();
        assumeSupported(client);

        String stream = generateName();
        seedStream(client, stream, 3);
        client.deleteStream(stream).get();

        List<AppendRecord> records = Collections.singletonList(
                new AppendRecord(stream, EventData.builderAsJson("event-a", "{}".getBytes()).build())
        );
        List<ConsistencyCheck> checks = Collections.singletonList(
                new ConsistencyCheck.StreamStateCheck(stream, StreamState.noStream())
        );

        AppendRecordsResponse result = client.appendRecords(records, checks).get();
        Assertions.assertTrue(result.getPosition() > 0);
    }

    @Test
    public void testWriteNoStream_fails_when_stream_has_revision() throws ExecutionException, InterruptedException {
        KurrentDBClient client = getDefaultClient();
        assumeSupported(client);

        String stream = generateName();
        seedStream(client, stream, 3);

        List<AppendRecord> records = Collections.singletonList(
                new AppendRecord(stream, EventData.builderAsJson("event-a", "{}".getBytes()).build())
        );
        List<ConsistencyCheck> checks = Collections.singletonList(
                new ConsistencyCheck.StreamStateCheck(stream, StreamState.noStream())
        );

        Assertions.assertThrows(AppendConsistencyViolationException.class, () -> {
            try {
                client.appendRecords(records, checks).get();
            } catch (ExecutionException e) {
                throw e.getCause();
            }
        });
    }

    @Test
    public void testWriteNoStream_fails_when_stream_is_tombstoned() throws ExecutionException, InterruptedException {
        KurrentDBClient client = getDefaultClient();
        assumeSupported(client);

        String stream = generateName();
        seedStream(client, stream, 3);
        client.tombstoneStream(stream).get();

        List<AppendRecord> records = Collections.singletonList(
                new AppendRecord(stream, EventData.builderAsJson("event-a", "{}".getBytes()).build())
        );
        List<ConsistencyCheck> checks = Collections.singletonList(
                new ConsistencyCheck.StreamStateCheck(stream, StreamState.noStream())
        );

        Assertions.assertThrows(AppendConsistencyViolationException.class, () -> {
            try {
                client.appendRecords(records, checks).get();
            } catch (ExecutionException e) {
                throw e.getCause();
            }
        });
    }

    // ==================== WhenExpectingRevision ====================

    private static final int EXPECTED_REVISION = 10;

    @Test
    public void testWriteRevision_succeeds_when_stream_has_revision() throws ExecutionException, InterruptedException {
        KurrentDBClient client = getDefaultClient();
        assumeSupported(client);

        String stream = generateName();
        seedStream(client, stream, EXPECTED_REVISION + 1);

        List<AppendRecord> records = Collections.singletonList(
                new AppendRecord(stream, EventData.builderAsJson("event-a", "{}".getBytes()).build())
        );
        List<ConsistencyCheck> checks = Collections.singletonList(
                new ConsistencyCheck.StreamStateCheck(stream, StreamState.streamRevision(EXPECTED_REVISION))
        );

        AppendRecordsResponse result = client.appendRecords(records, checks).get();
        Assertions.assertTrue(result.getPosition() > 0);
    }

    @Test
    public void testWriteRevision_fails_when_stream_not_found() throws ExecutionException, InterruptedException {
        KurrentDBClient client = getDefaultClient();
        assumeSupported(client);

        String stream = generateName();

        List<AppendRecord> records = Collections.singletonList(
                new AppendRecord(stream, EventData.builderAsJson("event-a", "{}".getBytes()).build())
        );
        List<ConsistencyCheck> checks = Collections.singletonList(
                new ConsistencyCheck.StreamStateCheck(stream, StreamState.streamRevision(EXPECTED_REVISION))
        );

        Assertions.assertThrows(AppendConsistencyViolationException.class, () -> {
            try {
                client.appendRecords(records, checks).get();
            } catch (ExecutionException e) {
                throw e.getCause();
            }
        });
    }

    @Test
    public void testWriteRevision_fails_when_stream_has_wrong_revision() throws ExecutionException, InterruptedException {
        KurrentDBClient client = getDefaultClient();
        assumeSupported(client);

        String stream = generateName();
        seedStream(client, stream, 5);

        List<AppendRecord> records = Collections.singletonList(
                new AppendRecord(stream, EventData.builderAsJson("event-a", "{}".getBytes()).build())
        );
        List<ConsistencyCheck> checks = Collections.singletonList(
                new ConsistencyCheck.StreamStateCheck(stream, StreamState.streamRevision(EXPECTED_REVISION))
        );

        Assertions.assertThrows(AppendConsistencyViolationException.class, () -> {
            try {
                client.appendRecords(records, checks).get();
            } catch (ExecutionException e) {
                throw e.getCause();
            }
        });
    }

    @Test
    public void testWriteRevision_fails_when_stream_is_deleted() throws ExecutionException, InterruptedException {
        KurrentDBClient client = getDefaultClient();
        assumeSupported(client);

        String stream = generateName();
        seedStream(client, stream, 3);
        client.deleteStream(stream).get();

        List<AppendRecord> records = Collections.singletonList(
                new AppendRecord(stream, EventData.builderAsJson("event-a", "{}".getBytes()).build())
        );
        List<ConsistencyCheck> checks = Collections.singletonList(
                new ConsistencyCheck.StreamStateCheck(stream, StreamState.streamRevision(EXPECTED_REVISION))
        );

        Assertions.assertThrows(AppendConsistencyViolationException.class, () -> {
            try {
                client.appendRecords(records, checks).get();
            } catch (ExecutionException e) {
                throw e.getCause();
            }
        });
    }

    @Test
    public void testWriteRevision_fails_when_stream_is_tombstoned() throws ExecutionException, InterruptedException {
        KurrentDBClient client = getDefaultClient();
        assumeSupported(client);

        String stream = generateName();
        seedStream(client, stream, 3);
        client.tombstoneStream(stream).get();

        List<AppendRecord> records = Collections.singletonList(
                new AppendRecord(stream, EventData.builderAsJson("event-a", "{}".getBytes()).build())
        );
        List<ConsistencyCheck> checks = Collections.singletonList(
                new ConsistencyCheck.StreamStateCheck(stream, StreamState.streamRevision(EXPECTED_REVISION))
        );

        Assertions.assertThrows(AppendConsistencyViolationException.class, () -> {
            try {
                client.appendRecords(records, checks).get();
            } catch (ExecutionException e) {
                throw e.getCause();
            }
        });
    }
}
