package io.kurrent.dbclient;

import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;

@SuppressWarnings("rawtypes")
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
    public void testMultiStreamAppend() throws ExecutionException, InterruptedException, IOException {
        KurrentDBClient client = getDefaultClient();

        Optional<ServerVersion> version = client.getServerVersion().get();

        Assumptions.assumeTrue(
                version.isPresent() && version.get().isGreaterOrEqualThan(25, 0),
                "Multi-stream append is not supported server versions below 25.0.0"
        );

        // Arrange
        String streamName1 = generateName();
        String streamName2 = generateName();

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("stringProperty", "hello world");
        metadata.put("intProperty", 42);
        metadata.put("longProperty", 9876543210L);
        metadata.put("booleanProperty", true);
        metadata.put("doubleProperty", 3.14159);
        metadata.put("nullProperty", null);
        metadata.put("timestampProperty", Instant.now().toString());

        byte[] metadataBytes = mapper.writeValueAsBytes(metadata);

        EventData event1 = EventData.builderAsJson("event-a", "{\"data\":\"test1\"}".getBytes())
                .metadataAsBytes(metadataBytes)
                .build();

        EventData event2 = EventData.builderAsBinary("event-b", new byte[0]).build();

        List<EventData> events1 = Collections.singletonList(event1);
        List<EventData> events2 = Collections.singletonList(event2);

        List<AppendStreamRequest> requests = Arrays.asList(
                new AppendStreamRequest(streamName1, events1.iterator(), StreamState.noStream()),
                new AppendStreamRequest(streamName2, events2.iterator(), StreamState.noStream())
        );

        // Act
        MultiStreamAppendResponse result = client.multiStreamAppend(requests.iterator()).get();

        // Assert
        Assertions.assertFalse(result.getResults().isEmpty());
        Assertions.assertTrue(result.getPosition() > 0);

        List<ResolvedEvent> readEvents1 = client.readStream(streamName1, ReadStreamOptions.get()).get().getEvents();
        Assertions.assertEquals(1, readEvents1.size());

        ResolvedEvent readEvent1 = readEvents1.get(0);
        Assertions.assertEquals(event1.getEventType(), readEvent1.getEvent().getEventType());

        byte[] readMetadata = readEvent1.getEvent().getUserMetadata();
        Assertions.assertNotNull(readMetadata);
        Assertions.assertTrue(readMetadata.length > 0);

        Map deserializedMetadata = mapper.readValue(readMetadata, Map.class);
        Assertions.assertEquals(metadata.get("stringProperty"), deserializedMetadata.get("stringProperty"));
        Assertions.assertEquals(metadata.get("intProperty"), deserializedMetadata.get("intProperty"));
        Assertions.assertEquals(metadata.get("longProperty"), ((Number) deserializedMetadata.get("longProperty")).longValue());
        Assertions.assertEquals(metadata.get("booleanProperty"), deserializedMetadata.get("booleanProperty"));
        Assertions.assertEquals((Double) metadata.get("doubleProperty"), ((Number) deserializedMetadata.get("doubleProperty")).doubleValue(), 0.00001);
        Assertions.assertEquals(metadata.get("timestampProperty"), deserializedMetadata.get("timestampProperty"));
        Assertions.assertNull(deserializedMetadata.get("nullProperty"));

        List<ResolvedEvent> readEvents2 = client.readStream(streamName2, ReadStreamOptions.get()).get().getEvents();
        Assertions.assertEquals(1, readEvents2.size());
        Assertions.assertEquals(event2.getEventType(), readEvents2.get(0).getEvent().getEventType());
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
                () -> client.multiStreamAppend(requests.iterator()).get());

        Assertions.assertInstanceOf(UnsupportedOperationException.class, e.getCause());
    }

    //
    @Test
    public void testMultiStreamAppendStreamRevisionConflict() throws ExecutionException, InterruptedException {
        KurrentDBClient client = getDefaultClient();

        Optional<ServerVersion> version = client.getServerVersion().get();

        Assumptions.assumeTrue(
                version.isPresent() && version.get().isGreaterOrEqualThan(25, 0),
                "Multi-stream append is not supported server versions below 25.0.0"
        );

        // Arrange
        String streamName = generateName();

        EventData event = EventData.builderAsJson("event-1", "{}".getBytes()).build();

        client.appendToStream(
                streamName,
                AppendToStreamOptions.get().streamState(StreamState.noStream()),
                event
        ).get();

        List<AppendStreamRequest> requests = Collections.singletonList(
                new AppendStreamRequest(
                        streamName,
                        Collections.singletonList(EventData.builderAsBinary("event-2", "{}".getBytes()).build()).iterator(),
                        StreamState.noStream()
                )
        );

        // Act & Assert
        Assertions.assertThrows(WrongExpectedVersionException.class, () -> {
            try {
                client.multiStreamAppend(requests.iterator()).get();
            } catch (ExecutionException e) {
                throw e.getCause();
            }
        });
    }

    @Test
    public void testMultiStreamAppendStreamDeleted() throws ExecutionException, InterruptedException {
        KurrentDBClient client = getDefaultClient();

        Optional<ServerVersion> version = client.getServerVersion().get();

        Assumptions.assumeTrue(
                version.isPresent() && version.get().isGreaterOrEqualThan(25, 0),
                "Multi-stream append is not supported server versions below 25.0.0"
        );

        // Arrange
        String streamName = generateName();

        EventData event1 = EventData.builderAsJson("event-1", "{}".getBytes()).build();

        client.appendToStream(
                streamName,
                AppendToStreamOptions.get().streamState(StreamState.noStream()),
                event1
        ).get();

        client.tombstoneStream(streamName).get();

        List<AppendStreamRequest> requests = Collections.singletonList(
                new AppendStreamRequest(
                        streamName,
                        Collections.singletonList(EventData.builderAsBinary("event-2", "{}".getBytes()).build()).iterator(),
                        StreamState.noStream()
                )
        );

        // Act
        Assertions.assertThrows(StreamTombstonedException.class, () -> {
            try {
                client.multiStreamAppend(requests.iterator()).get();
            } catch (ExecutionException e) {
                throw e.getCause();
            }
        });
    }
}
