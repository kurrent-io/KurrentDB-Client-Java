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
     default void testDefaultDeadline() throws Throwable {
        KurrentDBClient client = getDatabase().connectWith(opts ->
                opts.defaultDeadline(1)
                        .maxDiscoverAttempts(3));
        UUID id = UUID.randomUUID();

        EventData data = EventDataBuilder.binary(id, "type", new byte[]{}).build();
        ExecutionException e = Assertions.assertThrows(ExecutionException.class, () -> client.appendToStream("toto", data).get());
        StatusRuntimeException status = (StatusRuntimeException) e.getCause();

        Assertions.assertEquals(status.getStatus().getCode(), Status.Code.DEADLINE_EXCEEDED);
    }

    @RetryingTest(3)
    default void testOptionLevelDeadline() throws Throwable {
        KurrentDBClient client = getDatabase().defaultClient();
        UUID id = UUID.randomUUID();

        EventData data = EventDataBuilder.binary(id, "type", new byte[]{}).build();
        AppendToStreamOptions options = AppendToStreamOptions.get().deadline(1);
        ExecutionException e = Assertions.assertThrows(ExecutionException.class, () -> client.appendToStream("toto", options, data).get());
        StatusRuntimeException status = (StatusRuntimeException) e.getCause();

        Assertions.assertEquals(status.getStatus().getCode(), Status.Code.DEADLINE_EXCEEDED);
    }
}
