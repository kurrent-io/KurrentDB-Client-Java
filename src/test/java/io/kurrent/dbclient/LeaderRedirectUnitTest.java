package io.kurrent.dbclient;

import com.google.protobuf.Any;
import com.google.rpc.ErrorInfo;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.protobuf.StatusProto;
import io.kurrentdb.protocol.v2.common.errors.NotLeaderNodeErrorDetails;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A NotLeader redirect on a subscription must not throw and must still deliver onCancelled.
 * The bug: onError called reportNewLeader(...) on a null args, throwing before
 * consumer.onCancelled(...) and swallowing the failure.
 */
public class LeaderRedirectUnitTest {
    @Test
    public void onErrorLeaderRedirectStillInvokesOnCancelled() {
        AtomicBoolean cancelledFired = new AtomicBoolean(false);
        AtomicReference<Throwable> cancelErr = new AtomicReference<>();

        SubscriptionStreamConsumer consumer = new SubscriptionStreamConsumer(
            new SubscriptionListener() {
                @Override
                public void onCancelled(Subscription subscription, Throwable exception) {
                    cancelledFired.set(true);
                    cancelErr.set(exception);
                }
            },
            null,
            new CompletableFuture<>(),
            (id, ev, action) -> action.run()
        );

        // no onConnected(): args stays null
        ReadResponseObserver observer = new ReadResponseObserver(SubscribeToStreamOptions.get(), consumer);

        Metadata trailers = new Metadata();
        trailers.put(Metadata.Key.of("leader-endpoint-host", Metadata.ASCII_STRING_MARSHALLER), "127.0.0.1");
        trailers.put(Metadata.Key.of("leader-endpoint-port", Metadata.ASCII_STRING_MARSHALLER), "2113");
        StatusRuntimeException leaderRedirect = new StatusRuntimeException(Status.UNAVAILABLE, trailers);

        Assertions.assertDoesNotThrow(() -> observer.onError(leaderRedirect));
        Assertions.assertTrue(cancelledFired.get(), "onCancelled must fire on a leader redirect");
        Assertions.assertTrue(cancelErr.get() instanceof NotLeaderException,
            "listener should receive a NotLeaderException, got: " + cancelErr.get());
    }

    @Test
    public void onErrorLeaderRedirectReportsNewLeaderWhenConnected() {
        AtomicReference<Throwable> cancelErr = new AtomicReference<>();

        SubscriptionStreamConsumer consumer = new SubscriptionStreamConsumer(
            new SubscriptionListener() {
                @Override
                public void onCancelled(Subscription subscription, Throwable exception) {
                    cancelErr.set(exception);
                }
            },
            null,
            new CompletableFuture<>(),
            (id, ev, action) -> action.run()
        );

        LinkedBlockingQueue<Msg> queue = new LinkedBlockingQueue<>();
        WorkItemArgs args = new WorkItemArgs(UUID.randomUUID(), null, null, null, queue);

        ReadResponseObserver observer = new ReadResponseObserver(SubscribeToStreamOptions.get(), consumer);
        observer.onConnected(args);

        Metadata trailers = new Metadata();
        trailers.put(Metadata.Key.of("leader-endpoint-host", Metadata.ASCII_STRING_MARSHALLER), "127.0.0.1");
        trailers.put(Metadata.Key.of("leader-endpoint-port", Metadata.ASCII_STRING_MARSHALLER), "2113");

        Assertions.assertDoesNotThrow(() ->
            observer.onError(new StatusRuntimeException(Status.UNAVAILABLE, trailers)));

        Msg enqueued = queue.poll();
        Assertions.assertTrue(enqueued instanceof CreateChannel,
            "leader redirect should enqueue a CreateChannel for the new leader, got: " + enqueued);
        Assertions.assertTrue(cancelErr.get() instanceof NotLeaderException,
            "listener should receive a NotLeaderException, got: " + cancelErr.get());
    }

    @Test
    public void handleNotLeaderErrorReadsNotLeaderNodeErrorDetails() {
        com.google.rpc.Status status = com.google.rpc.Status.newBuilder()
            .setCode(Status.Code.FAILED_PRECONDITION.value())
            .setMessage("The server is not the leader node and cannot handle the request.")
            .addDetails(Any.pack(ErrorInfo.newBuilder().setReason("SERVER_ERROR_NOT_LEADER_NODE").build()))
            .addDetails(Any.pack(NotLeaderNodeErrorDetails.newBuilder()
                .setCurrentLeader(NotLeaderNodeErrorDetails.NodeInfo.newBuilder().setHost("127.0.0.1").setPort(2113))
                .build()))
            .build();
        CompletableFuture<Object> dest = new CompletableFuture<>();

        Assertions.assertTrue(GrpcUtils.handleNotLeaderError(StatusProto.toStatusRuntimeException(status), dest));

        ExecutionException error = Assertions.assertThrows(ExecutionException.class, dest::get);
        Assertions.assertTrue(error.getCause() instanceof NotLeaderException,
            "should complete with a NotLeaderException, got: " + error.getCause());
        Assertions.assertEquals(new InetSocketAddress("127.0.0.1", 2113),
            ((NotLeaderException) error.getCause()).getLeaderEndpoint());
    }

    @Test
    public void handleNotLeaderErrorFailsWithOriginalErrorOnInvalidTrailerPort() {
        Metadata trailers = new Metadata();
        trailers.put(Metadata.Key.of("leader-endpoint-host", Metadata.ASCII_STRING_MARSHALLER), "127.0.0.1");
        trailers.put(Metadata.Key.of("leader-endpoint-port", Metadata.ASCII_STRING_MARSHALLER), "abc");

        assertFailsWithOriginalError(new StatusRuntimeException(Status.NOT_FOUND, trailers));
    }

    @Test
    public void handleNotLeaderErrorFailsWithOriginalErrorOnOutOfRangeDetailsPort() {
        com.google.rpc.Status status = com.google.rpc.Status.newBuilder()
            .setCode(Status.Code.FAILED_PRECONDITION.value())
            .addDetails(Any.pack(NotLeaderNodeErrorDetails.newBuilder()
                .setCurrentLeader(NotLeaderNodeErrorDetails.NodeInfo.newBuilder().setHost("127.0.0.1").setPort(99999))
                .build()))
            .build();

        assertFailsWithOriginalError(StatusProto.toStatusRuntimeException(status));
    }

    @Test
    public void handleNotLeaderErrorFailsWithOriginalErrorOnStatusCodeMismatch() {
        Metadata trailers = new Metadata();
        trailers.put(Metadata.Key.of("grpc-status-details-bin", ProtoUtils.metadataMarshaller(com.google.rpc.Status.getDefaultInstance())),
            com.google.rpc.Status.newBuilder().setCode(Status.Code.FAILED_PRECONDITION.value()).build());

        assertFailsWithOriginalError(new StatusRuntimeException(Status.INTERNAL, trailers));
    }

    private static void assertFailsWithOriginalError(StatusRuntimeException original) {
        CompletableFuture<Object> dest = new CompletableFuture<>();

        Assertions.assertTrue(GrpcUtils.handleNotLeaderError(original, dest));

        ExecutionException error = Assertions.assertThrows(ExecutionException.class, dest::get);
        Assertions.assertSame(original, error.getCause());
    }
}
