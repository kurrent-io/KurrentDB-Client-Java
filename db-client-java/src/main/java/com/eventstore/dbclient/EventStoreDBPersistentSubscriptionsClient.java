package com.eventstore.dbclient;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

/**
 * Represents EventStoreDB client for persistent subscriptions management. A client instance maintains a two-way communication to EventStoreDB.
 * Many threads can use the EventStoreDB client simultaneously, or a single thread can make many asynchronous requests.
 */
public class EventStoreDBPersistentSubscriptionsClient {
    private final KurrentDBClientBase inner;

    private EventStoreDBPersistentSubscriptionsClient(KurrentDBClientSettings settings) {
        inner = new KurrentDBClientBase(settings);
    }

    private EventStoreDBPersistentSubscriptionsClient(KurrentDBClientBase inner) {
        this.inner = inner;
    }

    /**
     * Creates a persistent subscription client instance.
     */
    public static EventStoreDBPersistentSubscriptionsClient create(KurrentDBClientSettings settings) {
        return new EventStoreDBPersistentSubscriptionsClient(settings);
    }

    /**
     * Returns a Persistent Subscription Management client based on existing client.
     * @param existingClient Existing client.
     */
    public static EventStoreDBPersistentSubscriptionsClient from(KurrentDBClientBase existingClient) {
        return new EventStoreDBPersistentSubscriptionsClient(existingClient);
    }
    /**
     * Creates a persistent subscription group on a stream.
     *
     * <p>
     * Persistent subscriptions are special kind of subscription where the server remembers the state of the
     * subscription. This allows for many different modes of operations compared to a regular or catchup subscription
     * where the client holds the subscription state. Persistent subscriptions don't guarantee ordering and unlike
     * catchup-subscriptions, they start from the end of stream by default.
     * </p>
     * @param stream stream's name.
     * @param group group's name
     */
    public CompletableFuture createToStream(String stream, String group) {
        return this.createToStream(stream, group, CreatePersistentSubscriptionToStreamOptions.get());
    }

    /**
     * Creates a persistent subscription group on the $all stream.
     *
     * <p>
     * Persistent subscriptions are special kind of subscription where the server remembers the state of the
     * subscription. This allows for many different modes of operations compared to a regular or catchup subscription
     * where the client holds the subscription state. Persistent subscriptions don't guarantee ordering and unlike
     * catchup-subscriptions, they start from the end of stream by default.
     * </p>
     * @param group group's name
     */
    public CompletableFuture createToAll(String group) {
        return this.createToAll(group, CreatePersistentSubscriptionToAllOptions.get());
    }

    /**
     * Creates a persistent subscription group on a stream.
     *
     * <p>
     * Persistent subscriptions are special kind of subscription where the server remembers the state of the
     * subscription. This allows for many different modes of operations compared to a regular or catchup subscription
     * where the client holds the subscription state. Persistent subscriptions don't guarantee ordering and unlike
     * catchup-subscriptions, they start from the end of stream by default.
     * </p>
     * @param stream stream's name.
     * @param group group's name
     * @param options create persistent subscription request's options.
     */
    public CompletableFuture createToStream(String stream, String group, CreatePersistentSubscriptionToStreamOptions options) {
        if (options == null) {
            options = CreatePersistentSubscriptionToStreamOptions.get();
        }

        return new CreatePersistentSubscriptionToStream(inner.getGrpcClient(), stream, group, options).execute();
    }

    /**
     * Creates a persistent subscription group on the $all stream.
     *
     * <p>
     * Persistent subscriptions are special kind of subscription where the server remembers the state of the
     * subscription. This allows for many different modes of operations compared to a regular or catchup subscription
     * where the client holds the subscription state. Persistent subscriptions don't guarantee ordering and unlike
     * catchup-subscriptions, they start from the end of stream by default.
     * </p>
     * @param group group's name
     * @param options create persistent subscription request's options.
     */
    public CompletableFuture createToAll(String group, CreatePersistentSubscriptionToAllOptions options) {
        if (options == null) {
            options = CreatePersistentSubscriptionToAllOptions.get();
        }

        return new CreatePersistentSubscriptionToAll(inner.getGrpcClient(), group, options).execute();
    }

    /**
     * Updates a persistent subscription group on a stream.
     * @param stream stream's name.
     * @param group group's name.
     */
    public CompletableFuture updateToStream(String stream, String group) {
        return this.updateToStream(stream, group, UpdatePersistentSubscriptionToStreamOptions.get());
    }

    /**
     * Updates a persistent subscription group on the $all stream.
     * @param group group's name.
     */
    public CompletableFuture updateToAll(String group) {
        return this.updateToAll(group, UpdatePersistentSubscriptionToAllOptions.get());
    }

    /**
     * Updates a persistent subscription group on a stream.
     * @param stream stream's name.
     * @param group group's name.
     * @param options update persistent subscription request's options.
     */
    public CompletableFuture updateToStream(String stream, String group, UpdatePersistentSubscriptionToStreamOptions options) {
        if (options == null) {
            options = UpdatePersistentSubscriptionToStreamOptions.get();
        }

        return new UpdatePersistentSubscriptionToStream(inner.getGrpcClient(), stream, group, options).execute();
    }

    /**
     * Updates a persistent subscription group on the $all stream.
     * @param group group's name.
     * @param options update persistent subscription request's options.
     */
    public CompletableFuture updateToAll(String group, UpdatePersistentSubscriptionToAllOptions options) {
        if (options == null) {
            options = UpdatePersistentSubscriptionToAllOptions.get();
        }

        return new UpdatePersistentSubscriptionToAll(inner.getGrpcClient(), group, options).execute();
    }

    /**
     * Deletes a persistent subscription group on a stream.
     * @param stream stream's name.
     * @param group group's name.
     */
    public CompletableFuture deleteToStream(String stream, String group) {
        return this.deleteToStream(stream, group, DeletePersistentSubscriptionOptions.get());
    }

    /**
     * Deletes a persistent subscription group on the $all stream.
     * @param group group's name.
     */
    public CompletableFuture deleteToAll(String group) {
        return this.deleteToAll(group, DeletePersistentSubscriptionOptions.get());
    }

    /**
     * Deletes a persistent subscription group on a stream.
     * @param stream stream's name.
     * @param group group's name.
     * @param options the delete persistent subscription request's options.
     */
    public CompletableFuture deleteToStream(String stream, String group, DeletePersistentSubscriptionOptions options) {
        if (options == null) {
            options = DeletePersistentSubscriptionOptions.get();
        }

        return new DeletePersistentSubscriptionToStream(inner.getGrpcClient(), stream, group, options).execute();
    }

    /**
     * Deletes a persistent subscription group on the $all stream.
     * @param group group's name.
     * @param options the delete persistent subscription request's options.
     */
    public CompletableFuture deleteToAll(String group, DeletePersistentSubscriptionOptions options) {
        if (options == null) {
            options = DeletePersistentSubscriptionOptions.get();
        }

        return new DeletePersistentSubscriptionToAll(inner.getGrpcClient(), group, options).execute();
    }

    /**
     * Connects to a persistent subscription group on a stream.
     * @param stream stream's name.
     * @param group group's name.
     * @param listener persistent subscription event listener.
     * @return a persistent subscription handle.
     */
    public CompletableFuture<PersistentSubscription> subscribeToStream(String stream, String group, PersistentSubscriptionListener listener) {
        return this.subscribeToStream(stream, group, SubscribePersistentSubscriptionOptions.get(), listener);
    }

    /**
     * Connects to a persistent subscription group on the $all stream.
     * @param group group's name.
     * @param listener persistent subscription event listener.
     * @return a persistent subscription handle.
     */
    public CompletableFuture<PersistentSubscription> subscribeToAll(String group, PersistentSubscriptionListener listener) {
        return this.subscribeToAll(group, SubscribePersistentSubscriptionOptions.get(), listener);
    }

    /**
     * Connects to a persistent subscription group on a stream.
     * @param stream stream's name.
     * @param group group's name.
     * @param options a persistent subscription subscribe's request.
     * @param listener persistent subscription event listener.
     * @return a persistent subscription handle.
     */
    public CompletableFuture<PersistentSubscription> subscribeToStream(String stream, String group, SubscribePersistentSubscriptionOptions options, PersistentSubscriptionListener listener) {
        if (options == null) {
            options = SubscribePersistentSubscriptionOptions.get();
        }

        return new SubscribePersistentSubscriptionToStream(inner.getGrpcClient(), stream, group, options, listener).execute();
    }

    /**
     * Connects to a persistent subscription group on the $all stream.
     * @param group group's name.
     * @param options a persistent subscription subscribe's request.
     * @param listener persistent subscription event listener.
     * @return a persistent subscription handle.
     */
    public CompletableFuture<PersistentSubscription> subscribeToAll(String group, SubscribePersistentSubscriptionOptions options, PersistentSubscriptionListener listener) {
        if (options == null) {
            options = SubscribePersistentSubscriptionOptions.get();
        }

        return new SubscribePersistentSubscriptionToAll(inner.getGrpcClient(), group, options, listener).execute();
    }

    /**
     * Lists all existing persistent subscriptions.

     * @param options list persistent subscriptions request's options.
     * @see PersistentSubscriptionInfo
     */
    public CompletableFuture<List<PersistentSubscriptionInfo>> listAll(ListPersistentSubscriptionsOptions options) {
        return ListPersistentSubscriptions.execute(inner.getGrpcClient(), options, "", Function.identity());
    }

    /**
     * Lists all existing persistent subscriptions.

     * @see PersistentSubscriptionInfo
     */
    public CompletableFuture<List<PersistentSubscriptionInfo>> listAll() {
        return listAll(ListPersistentSubscriptionsOptions.get());
    }

    /**
     * Lists all persistent subscriptions of a specific stream.
     * @param stream stream's name.
     * @param options list persistent subscriptions request's options.
     */
    public CompletableFuture<List<PersistentSubscriptionToStreamInfo>> listToStream(String stream, ListPersistentSubscriptionsOptions options) {
        return ListPersistentSubscriptions.execute(inner.getGrpcClient(), options, stream, info -> (PersistentSubscriptionToStreamInfo) info);
    }

    /**
     * Lists all persistent subscriptions of a specific stream.
     * @param stream stream's name.
     * @see PersistentSubscriptionToStreamInfo
     */
    public CompletableFuture<List<PersistentSubscriptionToStreamInfo>> listToStream(String stream) {
        return listToStream(stream, ListPersistentSubscriptionsOptions.get());
    }

    /**
     * Lists all persistent subscriptions of a specific to the $all stream.
     * @see PersistentSubscriptionToAllInfo
     */
    public CompletableFuture<List<PersistentSubscriptionToAllInfo>> listToAll() {
        return listToAll(ListPersistentSubscriptionsOptions.get());
    }

    /**
     * Lists all persistent subscriptions of a specific to the $all stream.
     * @param options list persistent subscriptions request's options.
     * @see PersistentSubscriptionToAllInfo
     */
    public CompletableFuture<List<PersistentSubscriptionToAllInfo>> listToAll(ListPersistentSubscriptionsOptions options) {
        return ListPersistentSubscriptions.execute(inner.getGrpcClient(), options, "$all", info -> (PersistentSubscriptionToAllInfo) info);
    }

    /**
     * Gets a specific persistent subscription info.
     * @param stream stream's name.
     * @param groupName group's name.
     * @param options get persistent subscription info request's options.
     * @see PersistentSubscriptionInfo
     */
    public CompletableFuture<Optional<PersistentSubscriptionToStreamInfo>> getInfoToStream(String stream, String groupName, GetPersistentSubscriptionInfoOptions options) {
        return GetPersistentSubscriptionInfo.execute(inner.getGrpcClient(), options, stream, groupName).thenApply(res ->
            res.map(PersistentSubscriptionToStreamInfo.class::cast)
        );
    }

    /**
     * Gets a specific persistent subscription info to a stream.
     * @param stream stream's name.
     * @param groupName group's name.
     * @see PersistentSubscriptionInfo
     */
    public CompletableFuture<Optional<PersistentSubscriptionToStreamInfo>> getInfoToStream(String stream, String groupName) {
        return getInfoToStream(stream, groupName, GetPersistentSubscriptionInfoOptions.get());
    }

    /**
     * Gets a specific persistent subscription info to the $all stream.
     * @param groupName group's name.
     * @param options get persistent subscription info request's options.
     * @see PersistentSubscriptionToAllInfo
     */
    public CompletableFuture<Optional<PersistentSubscriptionToAllInfo>> getInfoToAll(String groupName, GetPersistentSubscriptionInfoOptions options) {
        return GetPersistentSubscriptionInfo.execute(inner.getGrpcClient(), options, "$all", groupName).thenApply(res ->
                res.map(PersistentSubscriptionToAllInfo.class::cast)
        );
    }

    /**
     * Gets a specific persistent subscription info to the $all stream.
     * @param groupName group's name.
     * @see PersistentSubscriptionToAllInfo
     */
    public CompletableFuture<Optional<PersistentSubscriptionToAllInfo>> getInfoToAll(String groupName) {
        return getInfoToAll(groupName, GetPersistentSubscriptionInfoOptions.get());
    }

    /**
     * Replays a persistent subscription to a stream parked events.
     * @param stream stream's name.
     * @param groupName group's name.
     * @param options replay parked messages to stream request's options.
     */
    public CompletableFuture replayParkedMessagesToStream(String stream, String groupName, ReplayParkedMessagesOptions options) {
        return ReplayParkedMessages.execute(inner.getGrpcClient(), options, stream, groupName);
    }

    /**
     * Replays a persistent subscription to a stream parked events.
     * @param stream stream's name.
     * @param groupName group's name.
     */
    public CompletableFuture replayParkedMessagesToStream(String stream, String groupName) {
        return replayParkedMessagesToStream(stream, groupName, ReplayParkedMessagesOptions.get());
    }

    /**
     * Replays a persistent subscription to the $all stream parked events.
     * @param groupName group's name.
     * @param options replay parked messages to stream request's options.
     */
    public CompletableFuture replayParkedMessagesToAll(String groupName, ReplayParkedMessagesOptions options) {
        return replayParkedMessagesToStream("$all", groupName, options);
    }

    /**
     * Replays a persistent subscription to the $all stream parked events.
     * @param groupName group's name.
     */
    public CompletableFuture replayParkedMessagesToAll(String groupName) throws ExecutionException, InterruptedException {
        return replayParkedMessagesToAll(groupName, ReplayParkedMessagesOptions.get());
    }

    /**
     * Restarts the server persistent subscription subsystem.
     */
    public CompletableFuture restartSubsystem() {
        return restartSubsystem(RestartPersistentSubscriptionSubsystemOptions.get());
    }

    /**
     * Restarts the server persistent subscription subsystem.
     * @param options restart persistent subscription subsystem request's options.

     */
    public CompletableFuture restartSubsystem(RestartPersistentSubscriptionSubsystemOptions options) {
        return RestartPersistentSubscriptionSubsystem.execute(inner.getGrpcClient(), options);
    }

    /**
     * Closes a connection and cleans all its allocated resources.
     */
    public CompletableFuture<Void> shutdown() {
        return inner.shutdown();
    }

    /**
     * Checks if this client instance has been shutdown.
     * After shutdown a client instance can no longer process new operations and
     * a new client instance has to be created.
     * @return {@code true} if client instance has been shutdown.
     */
    public boolean isShutdown() {
        return inner.isShutdown();
    }
}
