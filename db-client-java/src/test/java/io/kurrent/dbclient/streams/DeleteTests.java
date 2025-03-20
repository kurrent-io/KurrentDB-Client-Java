package io.kurrent.dbclient.streams;

import io.kurrent.dbclient.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

public interface DeleteTests extends ConnectionAware {
    @Test
    default void testCanDeleteStream() throws Throwable {
        KurrentDBClient client = getDatabase().defaultClient();
        String streamName = generateName();

        client.appendToStream(streamName, generateEvents(1, "foobar").iterator()).get();

        client.deleteStream(streamName).get();
    }

    @Test
    default void testDeleteStreamWhenAlreadyDeleted() throws Throwable {
        KurrentDBClient client = getDatabase().defaultClient();
        String streamName = generateName();

        client.appendToStream(streamName, generateEvents(1, "foobar").iterator()).get();
        client.tombstoneStream(streamName, DeleteStreamOptions.get()).get();
        Assertions.assertThrows(StreamDeletedException.class, () -> {
            try {
                client.tombstoneStream(streamName, DeleteStreamOptions.get()).get();
            } catch (ExecutionException e) {
                throw e.getCause();
            }
        });
    }

    @Test
    default void testDeleteStreamWhenDoesntExist() throws Throwable {
        KurrentDBClient client = getDatabase().defaultClient();

        String streamName = generateName();
        DeleteStreamOptions options = DeleteStreamOptions.get()
            .streamState(StreamState.streamExists());

        Assertions.assertThrows(WrongExpectedVersionException.class, () -> {
            try {
                client.tombstoneStream(streamName, options).get();
            } catch (ExecutionException e) {
                throw e.getCause();
            }
        });
    }
}
