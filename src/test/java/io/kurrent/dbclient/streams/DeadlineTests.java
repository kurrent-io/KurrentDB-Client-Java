package io.kurrent.dbclient.streams;

import io.kurrent.dbclient.*;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.Assertions;
import org.junitpioneer.jupiter.RetryingTest;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

public interface DeadlineTests extends ConnectionAware {
    @RetryingTest(10)
    default void testDefaultDeadline() {
        KurrentDBClient client = getDatabase().connectWith(opts ->
                opts.defaultDeadline(1)
                        .maxDiscoverAttempts(3));
        UUID id = UUID.randomUUID();

        EventData data = EventDataBuilder.binary(id, "type", new byte[]{}).build();
        ExecutionException e = Assertions.assertThrows(ExecutionException.class, () -> client.appendToStream("toto", data).get());
        StatusRuntimeException status = (StatusRuntimeException) e.getCause();

        Assertions.assertEquals(Status.Code.DEADLINE_EXCEEDED, status.getStatus().getCode());
    }

    @RetryingTest(3)
    default void testOptionLevelDeadline() {
        KurrentDBClient client = getDatabase().defaultClient();
        UUID id = UUID.randomUUID();

        EventData data = EventDataBuilder.binary(id, "type", new byte[]{}).build();
        AppendToStreamOptions options = AppendToStreamOptions.get().deadline(1);
        ExecutionException e = Assertions.assertThrows(ExecutionException.class, () -> client.appendToStream("toto", options, data).get());
        StatusRuntimeException status = (StatusRuntimeException) e.getCause();

        Assertions.assertEquals(Status.Code.DEADLINE_EXCEEDED, status.getStatus().getCode());
    }

    @RetryingTest(3)
    default void testReadStreamWithDefaultDeadline() {
        KurrentDBClient client = getDatabase().connectWith(opts ->
                opts.defaultDeadline(1)
                        .maxDiscoverAttempts(3));

        ReadStreamOptions options = ReadStreamOptions.get();

        ExecutionException e = Assertions.assertThrows(ExecutionException.class, () -> client.readStream("$users", options).get());
        StatusRuntimeException status = (StatusRuntimeException) e.getCause();

        Assertions.assertEquals(Status.Code.DEADLINE_EXCEEDED, status.getStatus().getCode());
    }

    @RetryingTest(3)
    default void testReadStreamWithLevelDeadline() {
        KurrentDBClient client = getDefaultClient();

        ExecutionException e = Assertions.assertThrows(
                ExecutionException.class,
                () -> client.readStream("$users", ReadStreamOptions.get().deadline(1)).get()
        );
        StatusRuntimeException status = (StatusRuntimeException) e.getCause();

        Assertions.assertEquals(Status.Code.DEADLINE_EXCEEDED, status.getStatus().getCode());
    }

    @RetryingTest(3)
    default void testReadAllWithDefaultDeadline() {
        KurrentDBClient client = getDatabase().connectWith(opts ->
                opts.defaultDeadline(1)
                        .maxDiscoverAttempts(3));

        ReadAllOptions options = ReadAllOptions.get();

        ExecutionException e = Assertions.assertThrows(ExecutionException.class, () -> client.readAll(options).get());
        StatusRuntimeException status = (StatusRuntimeException) e.getCause();

        Assertions.assertEquals(Status.Code.DEADLINE_EXCEEDED, status.getStatus().getCode());
    }

    @RetryingTest(3)
    default void testReadAllWithLevelDeadline() {
        KurrentDBClient client = getDefaultClient();

        ReadAllOptions options = ReadAllOptions.get().deadline(1);

        ExecutionException e = Assertions.assertThrows(ExecutionException.class, () -> client.readAll(options).get());
        StatusRuntimeException status = (StatusRuntimeException) e.getCause();

        Assertions.assertEquals(Status.Code.DEADLINE_EXCEEDED, status.getStatus().getCode());
    }
}
