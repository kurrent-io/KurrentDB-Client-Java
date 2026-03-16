package io.kurrent.dbclient;

import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class AppendRecordsCheckOnlyTests implements ConnectionAware {
    static private Database database;
    static private Logger logger;

    @BeforeAll
    public static void setup() {
        database = DatabaseFactory.spawn();
        logger = LoggerFactory.getLogger(AppendRecordsCheckOnlyTests.class);
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

    // ==================== WhenExpectingExists (CheckOnly) ====================

    @Test
    public void testCheckExists_succeeds_when_stream_has_revision() throws ExecutionException, InterruptedException {
        KurrentDBClient client = getDefaultClient();
        assumeSupported(client);

        String checkStream = generateName();
        String writeStream = generateName();
        seedStream(client, checkStream, 3);

        List<AppendRecord> records = Collections.singletonList(
                new AppendRecord(writeStream, EventData.builderAsJson("event-a", "{}".getBytes()).build())
        );
        List<ConsistencyCheck> checks = Collections.singletonList(
                new ConsistencyCheck.StreamStateCheck(checkStream, StreamState.streamExists())
        );

        AppendRecordsResponse result = client.appendRecords(records, checks).get();
        Assertions.assertEquals(1, result.getResults().size());
        Assertions.assertEquals(writeStream, result.getResults().get(0).getStream());
    }

    @Test
    public void testCheckExists_fails_when_stream_not_found() throws ExecutionException, InterruptedException {
        KurrentDBClient client = getDefaultClient();
        assumeSupported(client);

        String checkStream = generateName();
        String writeStream = generateName();

        List<AppendRecord> records = Collections.singletonList(
                new AppendRecord(writeStream, EventData.builderAsJson("event-a", "{}".getBytes()).build())
        );
        List<ConsistencyCheck> checks = Collections.singletonList(
                new ConsistencyCheck.StreamStateCheck(checkStream, StreamState.streamExists())
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
        Assertions.assertEquals(checkStream, ex.getViolations().get(0).getStream());
    }

    @Test
    public void testCheckExists_fails_when_stream_is_deleted() throws ExecutionException, InterruptedException {
        KurrentDBClient client = getDefaultClient();
        assumeSupported(client);

        String checkStream = generateName();
        String writeStream = generateName();
        seedStream(client, checkStream, 3);
        client.deleteStream(checkStream).get();

        List<AppendRecord> records = Collections.singletonList(
                new AppendRecord(writeStream, EventData.builderAsJson("event-a", "{}".getBytes()).build())
        );
        List<ConsistencyCheck> checks = Collections.singletonList(
                new ConsistencyCheck.StreamStateCheck(checkStream, StreamState.streamExists())
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
    public void testCheckExists_fails_when_stream_is_tombstoned() throws ExecutionException, InterruptedException {
        KurrentDBClient client = getDefaultClient();
        assumeSupported(client);

        String checkStream = generateName();
        String writeStream = generateName();
        seedStream(client, checkStream, 3);
        client.tombstoneStream(checkStream).get();

        List<AppendRecord> records = Collections.singletonList(
                new AppendRecord(writeStream, EventData.builderAsJson("event-a", "{}".getBytes()).build())
        );
        List<ConsistencyCheck> checks = Collections.singletonList(
                new ConsistencyCheck.StreamStateCheck(checkStream, StreamState.streamExists())
        );

        Assertions.assertThrows(AppendConsistencyViolationException.class, () -> {
            try {
                client.appendRecords(records, checks).get();
            } catch (ExecutionException e) {
                throw e.getCause();
            }
        });
    }

    // ==================== WhenExpectingNoStream (CheckOnly) ====================

    @Test
    public void testCheckNoStream_succeeds_when_stream_not_found() throws ExecutionException, InterruptedException {
        KurrentDBClient client = getDefaultClient();
        assumeSupported(client);

        String checkStream = generateName();
        String writeStream = generateName();

        List<AppendRecord> records = Collections.singletonList(
                new AppendRecord(writeStream, EventData.builderAsJson("event-a", "{}".getBytes()).build())
        );
        List<ConsistencyCheck> checks = Collections.singletonList(
                new ConsistencyCheck.StreamStateCheck(checkStream, StreamState.noStream())
        );

        AppendRecordsResponse result = client.appendRecords(records, checks).get();
        Assertions.assertEquals(1, result.getResults().size());
        Assertions.assertEquals(writeStream, result.getResults().get(0).getStream());
    }

    @Test
    public void testCheckNoStream_succeeds_when_stream_is_deleted() throws ExecutionException, InterruptedException {
        KurrentDBClient client = getDefaultClient();
        assumeSupported(client);

        String checkStream = generateName();
        String writeStream = generateName();
        seedStream(client, checkStream, 3);
        client.deleteStream(checkStream).get();

        List<AppendRecord> records = Collections.singletonList(
                new AppendRecord(writeStream, EventData.builderAsJson("event-a", "{}".getBytes()).build())
        );
        List<ConsistencyCheck> checks = Collections.singletonList(
                new ConsistencyCheck.StreamStateCheck(checkStream, StreamState.noStream())
        );

        AppendRecordsResponse result = client.appendRecords(records, checks).get();
        Assertions.assertEquals(1, result.getResults().size());
    }

    @Test
    public void testCheckNoStream_succeeds_when_stream_is_tombstoned() throws ExecutionException, InterruptedException {
        KurrentDBClient client = getDefaultClient();
        assumeSupported(client);

        String checkStream = generateName();
        String writeStream = generateName();
        seedStream(client, checkStream, 3);
        client.tombstoneStream(checkStream).get();

        List<AppendRecord> records = Collections.singletonList(
                new AppendRecord(writeStream, EventData.builderAsJson("event-a", "{}".getBytes()).build())
        );
        List<ConsistencyCheck> checks = Collections.singletonList(
                new ConsistencyCheck.StreamStateCheck(checkStream, StreamState.noStream())
        );

        AppendRecordsResponse result = client.appendRecords(records, checks).get();
        Assertions.assertEquals(1, result.getResults().size());
    }

    @Test
    public void testCheckNoStream_fails_when_stream_has_revision() throws ExecutionException, InterruptedException {
        KurrentDBClient client = getDefaultClient();
        assumeSupported(client);

        String checkStream = generateName();
        String writeStream = generateName();
        seedStream(client, checkStream, 3);

        List<AppendRecord> records = Collections.singletonList(
                new AppendRecord(writeStream, EventData.builderAsJson("event-a", "{}".getBytes()).build())
        );
        List<ConsistencyCheck> checks = Collections.singletonList(
                new ConsistencyCheck.StreamStateCheck(checkStream, StreamState.noStream())
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
        Assertions.assertEquals(checkStream, ex.getViolations().get(0).getStream());
    }

    // ==================== WhenExpectingRevision (CheckOnly) ====================

    private static final int EXPECTED_REVISION = 10;

    @Test
    public void testCheckRevision_succeeds_when_stream_has_revision() throws ExecutionException, InterruptedException {
        KurrentDBClient client = getDefaultClient();
        assumeSupported(client);

        String checkStream = generateName();
        String writeStream = generateName();
        seedStream(client, checkStream, EXPECTED_REVISION + 1);

        List<AppendRecord> records = Collections.singletonList(
                new AppendRecord(writeStream, EventData.builderAsJson("event-a", "{}".getBytes()).build())
        );
        List<ConsistencyCheck> checks = Collections.singletonList(
                new ConsistencyCheck.StreamStateCheck(checkStream, StreamState.streamRevision(EXPECTED_REVISION))
        );

        AppendRecordsResponse result = client.appendRecords(records, checks).get();
        Assertions.assertEquals(1, result.getResults().size());
        Assertions.assertEquals(writeStream, result.getResults().get(0).getStream());
    }

    @Test
    public void testCheckRevision_fails_when_stream_not_found() throws ExecutionException, InterruptedException {
        KurrentDBClient client = getDefaultClient();
        assumeSupported(client);

        String checkStream = generateName();
        String writeStream = generateName();

        List<AppendRecord> records = Collections.singletonList(
                new AppendRecord(writeStream, EventData.builderAsJson("event-a", "{}".getBytes()).build())
        );
        List<ConsistencyCheck> checks = Collections.singletonList(
                new ConsistencyCheck.StreamStateCheck(checkStream, StreamState.streamRevision(EXPECTED_REVISION))
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
    public void testCheckRevision_fails_when_stream_has_wrong_revision() throws ExecutionException, InterruptedException {
        KurrentDBClient client = getDefaultClient();
        assumeSupported(client);

        String checkStream = generateName();
        String writeStream = generateName();
        seedStream(client, checkStream, 5);

        List<AppendRecord> records = Collections.singletonList(
                new AppendRecord(writeStream, EventData.builderAsJson("event-a", "{}".getBytes()).build())
        );
        List<ConsistencyCheck> checks = Collections.singletonList(
                new ConsistencyCheck.StreamStateCheck(checkStream, StreamState.streamRevision(EXPECTED_REVISION))
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
    public void testCheckRevision_fails_when_stream_is_deleted() throws ExecutionException, InterruptedException {
        KurrentDBClient client = getDefaultClient();
        assumeSupported(client);

        String checkStream = generateName();
        String writeStream = generateName();
        seedStream(client, checkStream, 3);
        client.deleteStream(checkStream).get();

        List<AppendRecord> records = Collections.singletonList(
                new AppendRecord(writeStream, EventData.builderAsJson("event-a", "{}".getBytes()).build())
        );
        List<ConsistencyCheck> checks = Collections.singletonList(
                new ConsistencyCheck.StreamStateCheck(checkStream, StreamState.streamRevision(EXPECTED_REVISION))
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
    public void testCheckRevision_fails_when_stream_is_tombstoned() throws ExecutionException, InterruptedException {
        KurrentDBClient client = getDefaultClient();
        assumeSupported(client);

        String checkStream = generateName();
        String writeStream = generateName();
        seedStream(client, checkStream, 3);
        client.tombstoneStream(checkStream).get();

        List<AppendRecord> records = Collections.singletonList(
                new AppendRecord(writeStream, EventData.builderAsJson("event-a", "{}".getBytes()).build())
        );
        List<ConsistencyCheck> checks = Collections.singletonList(
                new ConsistencyCheck.StreamStateCheck(checkStream, StreamState.streamRevision(EXPECTED_REVISION))
        );

        Assertions.assertThrows(AppendConsistencyViolationException.class, () -> {
            try {
                client.appendRecords(records, checks).get();
            } catch (ExecutionException e) {
                throw e.getCause();
            }
        });
    }

    // ==================== WhenMultipleChecks ====================

    @Test
    public void testMultipleChecks_succeeds_when_all_checks_pass() throws ExecutionException, InterruptedException {
        KurrentDBClient client = getDefaultClient();
        assumeSupported(client);

        String checkStreamA = generateName();
        String checkStreamB = generateName();
        String writeStream = generateName();
        seedStream(client, checkStreamA, 3);
        seedStream(client, checkStreamB, 5);

        List<AppendRecord> records = Collections.singletonList(
                new AppendRecord(writeStream, EventData.builderAsJson("event-a", "{}".getBytes()).build())
        );
        List<ConsistencyCheck> checks = Arrays.asList(
                new ConsistencyCheck.StreamStateCheck(checkStreamA, StreamState.streamRevision(2)),
                new ConsistencyCheck.StreamStateCheck(checkStreamB, StreamState.streamRevision(4))
        );

        AppendRecordsResponse result = client.appendRecords(records, checks).get();
        Assertions.assertEquals(1, result.getResults().size());
    }

    @Test
    public void testMultipleChecks_succeeds_when_all_mixed_check_types_pass() throws ExecutionException, InterruptedException {
        KurrentDBClient client = getDefaultClient();
        assumeSupported(client);

        String checkStreamA = generateName();
        String checkStreamB = generateName();
        String checkStreamC = generateName();
        String writeStream = generateName();
        seedStream(client, checkStreamA, 3);
        seedStream(client, checkStreamB, 5);

        List<AppendRecord> records = Collections.singletonList(
                new AppendRecord(writeStream, EventData.builderAsJson("event-a", "{}".getBytes()).build())
        );
        List<ConsistencyCheck> checks = Arrays.asList(
                new ConsistencyCheck.StreamStateCheck(checkStreamA, StreamState.streamRevision(2)),
                new ConsistencyCheck.StreamStateCheck(checkStreamB, StreamState.streamExists()),
                new ConsistencyCheck.StreamStateCheck(checkStreamC, StreamState.noStream())
        );

        AppendRecordsResponse result = client.appendRecords(records, checks).get();
        Assertions.assertEquals(1, result.getResults().size());
    }

    @Test
    public void testMultipleChecks_fails_when_all_checks_fail() throws ExecutionException, InterruptedException {
        KurrentDBClient client = getDefaultClient();
        assumeSupported(client);

        String checkStreamA = generateName();
        String checkStreamB = generateName();
        String writeStream = generateName();

        List<AppendRecord> records = Collections.singletonList(
                new AppendRecord(writeStream, EventData.builderAsJson("event-a", "{}".getBytes()).build())
        );
        List<ConsistencyCheck> checks = Arrays.asList(
                new ConsistencyCheck.StreamStateCheck(checkStreamA, StreamState.streamRevision(5)),
                new ConsistencyCheck.StreamStateCheck(checkStreamB, StreamState.streamExists())
        );

        AppendConsistencyViolationException ex = Assertions.assertThrows(
                AppendConsistencyViolationException.class, () -> {
                    try {
                        client.appendRecords(records, checks).get();
                    } catch (ExecutionException e) {
                        throw e.getCause();
                    }
                });
        Assertions.assertEquals(2, ex.getViolations().size());
    }

    @Test
    public void testMultipleChecks_fails_when_first_check_fails_and_second_passes() throws ExecutionException, InterruptedException {
        KurrentDBClient client = getDefaultClient();
        assumeSupported(client);

        String checkStreamA = generateName();
        String checkStreamB = generateName();
        String writeStream = generateName();
        seedStream(client, checkStreamB, 6);

        List<AppendRecord> records = Collections.singletonList(
                new AppendRecord(writeStream, EventData.builderAsJson("event-a", "{}".getBytes()).build())
        );
        List<ConsistencyCheck> checks = Arrays.asList(
                new ConsistencyCheck.StreamStateCheck(checkStreamA, StreamState.streamRevision(5)),
                new ConsistencyCheck.StreamStateCheck(checkStreamB, StreamState.streamRevision(5))
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
        Assertions.assertEquals(0, ex.getViolations().get(0).getCheckIndex());
        Assertions.assertEquals(checkStreamA, ex.getViolations().get(0).getStream());
    }

    @Test
    public void testMultipleChecks_fails_when_two_of_three_checks_fail() throws ExecutionException, InterruptedException {
        KurrentDBClient client = getDefaultClient();
        assumeSupported(client);

        String checkStreamA = generateName();
        String checkStreamB = generateName();
        String checkStreamC = generateName();
        String writeStream = generateName();
        seedStream(client, checkStreamA, 3);

        List<AppendRecord> records = Collections.singletonList(
                new AppendRecord(writeStream, EventData.builderAsJson("event-a", "{}".getBytes()).build())
        );
        List<ConsistencyCheck> checks = Arrays.asList(
                new ConsistencyCheck.StreamStateCheck(checkStreamA, StreamState.streamRevision(2)),
                new ConsistencyCheck.StreamStateCheck(checkStreamB, StreamState.streamExists()),
                new ConsistencyCheck.StreamStateCheck(checkStreamC, StreamState.streamRevision(5))
        );

        AppendConsistencyViolationException ex = Assertions.assertThrows(
                AppendConsistencyViolationException.class, () -> {
                    try {
                        client.appendRecords(records, checks).get();
                    } catch (ExecutionException e) {
                        throw e.getCause();
                    }
                });
        Assertions.assertEquals(2, ex.getViolations().size());
    }

    @Test
    public void testMultipleChecks_fails_with_mixed_violation_states() throws ExecutionException, InterruptedException {
        KurrentDBClient client = getDefaultClient();
        assumeSupported(client);

        String deletedStream = generateName();
        String tombstonedStream = generateName();
        String missingStream = generateName();
        String writeStream = generateName();

        seedStream(client, deletedStream, 3);
        client.deleteStream(deletedStream).get();

        seedStream(client, tombstonedStream, 3);
        client.tombstoneStream(tombstonedStream).get();

        List<AppendRecord> records = Collections.singletonList(
                new AppendRecord(writeStream, EventData.builderAsJson("event-a", "{}".getBytes()).build())
        );
        List<ConsistencyCheck> checks = Arrays.asList(
                new ConsistencyCheck.StreamStateCheck(deletedStream, StreamState.streamExists()),
                new ConsistencyCheck.StreamStateCheck(tombstonedStream, StreamState.streamExists()),
                new ConsistencyCheck.StreamStateCheck(missingStream, StreamState.streamExists())
        );

        AppendConsistencyViolationException ex = Assertions.assertThrows(
                AppendConsistencyViolationException.class, () -> {
                    try {
                        client.appendRecords(records, checks).get();
                    } catch (ExecutionException e) {
                        throw e.getCause();
                    }
                });
        Assertions.assertEquals(3, ex.getViolations().size());
    }

    @Test
    public void testMultipleChecks_fails_when_check_on_write_target_and_separate_check_fails() throws ExecutionException, InterruptedException {
        KurrentDBClient client = getDefaultClient();
        assumeSupported(client);

        String writeStream = generateName();
        String checkStream = generateName();
        seedStream(client, writeStream, 4);

        List<AppendRecord> records = Collections.singletonList(
                new AppendRecord(writeStream, EventData.builderAsJson("event-a", "{}".getBytes()).build())
        );
        List<ConsistencyCheck> checks = Arrays.asList(
                new ConsistencyCheck.StreamStateCheck(checkStream, StreamState.streamRevision(5)),
                new ConsistencyCheck.StreamStateCheck(writeStream, StreamState.streamRevision(3))
        );

        AppendConsistencyViolationException ex = Assertions.assertThrows(
                AppendConsistencyViolationException.class, () -> {
                    try {
                        client.appendRecords(records, checks).get();
                    } catch (ExecutionException e) {
                        throw e.getCause();
                    }
                });
        Assertions.assertFalse(ex.getViolations().isEmpty());
        Assertions.assertEquals(0, ex.getViolations().get(0).getCheckIndex());
        Assertions.assertEquals(checkStream, ex.getViolations().get(0).getStream());
    }
}
