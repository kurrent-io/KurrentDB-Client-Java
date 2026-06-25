package io.kurrent.dbclient;

import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Regression guard for the leader-change NPE. When a subscription's onError receives
 * leader-endpoint trailers (the NotLeader redirect), it must NOT throw and must still
 * deliver onCancelled to the listener so the application can react/reconnect.
 *
 * Before the fix, ReadResponseObserver.onError called this.args.reportNewLeader(...) with
 * args == null on the subscription path, throwing a NullPointerException before reaching
 * consumer.onCancelled(...) and swallowing the failure. This test constructs the observer
 * without onConnected (args == null, the worst case) to lock that behavior down.
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

        // Subscription path with args unset (worst case): must not NPE, must notify listener.
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
}
