package io.kurrent.dbclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.kurrent.dbclient.serialization.MessageSerializationContext;
import io.kurrent.dbclient.serialization.MessageSerializer;
import org.reactivestreams.Publisher;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static io.kurrent.dbclient.serialization.MessageTypeNamingResolutionContext.fromStreamName;

/**
 * Represents EventStoreDB client for stream operations. A client instance maintains a two-way communication to EventStoreDB.
 * Many threads can use the EventStoreDB client simultaneously, or a single thread can make many asynchronous requests.
 */
public class KurrentDBClient extends KurrentDBClientBase {
    private KurrentDBClient(KurrentDBClientSettings settings) {
        super(settings);
    }

    /**
     * Creates a gRPC client to KurrentDB database.
     */
    public static KurrentDBClient create(KurrentDBClientSettings settings) {
        return new KurrentDBClient(settings);
    }

    /**
     * Appends events to a given stream.
     *
     * @deprecated This method may be removed in future releases. Prefer using appendToStream method with explicit stream state parameter.
     * @param streamName stream's name.
     * @param events     events to store.
     * @return a write result if successful.
     * @see WriteResult
     */
    public CompletableFuture<WriteResult> appendToStream(String streamName, EventData... events) {
        return this.appendToStream(streamName, Arrays.stream(events).iterator());
    }

    /**
     * Appends events to a given stream.
     *
     * @deprecated This method may be removed in future releases. Prefer using appendToStream method with explicit stream state parameter.
     * @param streamName stream's name.
     * @param events     events to store.
     * @return a write result if successful.
     * @see WriteResult
     */
    public CompletableFuture<WriteResult> appendToStream(String streamName, Iterator<EventData> events) {
        return this.appendToStream(streamName, AppendToStreamOptions.get(), events);
    }

    /**
     * Appends events to a given stream.
     *
     * @deprecated This method may be removed in future releases. Prefer using appendToStream method with explicit stream state parameter.
     * @param streamName stream's name.
     * @param options    append stream request's options.
     * @param events     events to store.
     * @return a write result if successful.
     * @see WriteResult
     */
    public CompletableFuture<WriteResult> appendToStream(String streamName, AppendToStreamOptions options, EventData... events) {
        return this.appendToStream(streamName, options, Arrays.stream(events).iterator());
    }
    
    /**
     * Appends events to a given stream.
     *
     * @deprecated This method may be removed in future releases. Prefer using appendToStream method with explicit stream state parameter.
     * @param streamName stream's name.
     * @param options    append stream request's options.
     * @param events     events to store.
     * @return a write result if successful.
     * @see WriteResult
     */
    public CompletableFuture<WriteResult> appendToStream(String streamName, AppendToStreamOptions options, Iterator<EventData> events) {
        if (options == null)
            options = AppendToStreamOptions.get();

        Iterator<MessageData> messageData =
                StreamSupport.stream(Spliterators.spliteratorUnknownSize(events, Spliterator.ORDERED), false)
                        .map(EventData::toMessageData)
                        .iterator();

        return new AppendToStream(this.getGrpcClient(), streamName, options.getStreamState(), messageData, options).execute();
    }

    /**
     * Appends messages to a given stream.
     *
     * @param streamName  stream's name.
     * @param streamState expected stream's state in the database
     * @param messages    messages to store.
     * @return a write result if successful.
     * @see WriteResult
     */
    public CompletableFuture<WriteResult> appendToStream(
            String streamName, 
            StreamState streamState,
            Object... messages
    ) {
        return this.appendToStream(
                streamName, 
                streamState, 
                Arrays.stream(messages).collect(Collectors.toList()), 
                AppendToStreamOptions.get()
        );
    }


    /**
     * Appends messages to a given stream.
     *
     * @param streamName  stream's name.
     * @param streamState expected stream's state in the database
     * @param messages    messages to store.
     * @return a write result if successful.
     * @see WriteResult
     */
    public CompletableFuture<WriteResult> appendToStream(
            String streamName,
            StreamState streamState,
            Message... messages
    ) {
        List<Message> toAppend = Arrays.stream(messages).collect(Collectors.toList());
        return this.appendToStream(
                streamName,
                streamState,
                toAppend,
                AppendToStreamOptions.get()
        );
    }

    /**
     * Appends messages to a given stream.
     *
     * @param streamName  stream's name.
     * @param streamState expected stream's state in the database
     * @param messages    messages to store.
     * @return a write result if successful.
     * @see WriteResult
     */
    public CompletableFuture<WriteResult> appendToStream(
            String streamName,
            StreamState streamState,
            Iterable<Object> messages
    ) {
        return this.appendToStream(
                streamName,
                streamState,
                messages,
                AppendToStreamOptions.get()
        );
    }


    /**
     * Appends messages to a given stream.
     *
     * @param streamName  stream's name.
     * @param streamState expected stream's state in the database
     * @param messages    messages to store.
     * @param options     append stream request's options.
     * @return a write result if successful.
     * @see WriteResult
     */
    public CompletableFuture<WriteResult> appendToStream(
            String streamName,
            StreamState streamState,
            Object[] messages,
            AppendToStreamOptions options
    ) {
        List<Message> toAppend =
                Arrays.stream(messages)
                        .map(Message::from)
                        .collect(Collectors.toList());

        return this.appendToStream(
                streamName,
                streamState,
                toAppend,
                options
        );
    }

    /**
     * Appends messages to a given stream.
     *
     * @param streamName  stream's name.
     * @param streamState expected stream's state in the database
     * @param messages    messages to store.
     * @param options     append stream request's options.
     * @return a write result if successful.
     * @see WriteResult
     */
    public CompletableFuture<WriteResult> appendToStream(
            String streamName,
            StreamState streamState,
            Iterable<Object> messages,
            AppendToStreamOptions options
    ) {
        List<Message> toAppend =
                StreamSupport.stream(messages.spliterator(), false)
                        .map(Message::from)
                        .collect(Collectors.toList());

        return this.appendToStream(
                streamName,
                streamState,
                toAppend,
                options
        );
    }

    /**
     * Appends messages to a given stream.
     *
     * @param streamName  stream's name.
     * @param streamState expected stream's state in the database
     * @param messages    messages to store.
     * @return a write result if successful.
     * @see WriteResult
     */
    public CompletableFuture<WriteResult> appendToStream(
            String streamName,
            StreamState streamState,
            List<Message> messages
    ) {
        return this.appendToStream(streamName, streamState, messages, AppendToStreamOptions.get());
    }

    /**
     * Appends messages to a given stream.
     *
     * @param streamName  stream's name.
     * @param streamState expected stream's state in the database.
     * @param messages    messages to store.
     * @param options     append stream request's options.
     * @return a write result if successful.
     * @see WriteResult
     */
    public CompletableFuture<WriteResult> appendToStream(
            String streamName,
            StreamState streamState,
            Message[] messages,
            AppendToStreamOptions options
    ) {
        List<Message> toAppend = Arrays.stream(messages).collect(Collectors.toList());

        return this.appendToStream(
                streamName,
                streamState,
                toAppend,
                options
        );
    }

    /**
     * Appends messages to a given stream.
     *
     * @param streamName  stream's name.
     * @param streamState expected stream's state in the database.
     * @param messages    messages to store.
     * @param options     append stream request's options.
     * @return a write result if successful.
     * @see WriteResult
     */
    public CompletableFuture<WriteResult> appendToStream(
            String streamName,
            StreamState streamState,
            List<Message> messages,
            AppendToStreamOptions options
    ) {
        if (options == null)
            options = AppendToStreamOptions.get();

        MessageSerializationContext serializationContext =
                new MessageSerializationContext(fromStreamName(streamName));

        MessageSerializer serializer = getGrpcClient()
                .getSerializer(options.serializationSettings().orElse(null));

        Iterator<MessageData> messageData = serializer
                .serialize(messages, serializationContext)
                .iterator();

        return new AppendToStream(this.getGrpcClient(), streamName, streamState, messageData, options).execute();
    }

    /**
     * Sets a stream's metadata.
     *
     * @param streamName stream's name.
     * @param metadata   stream's metadata
     * @return a write result if successful.
     * @see WriteResult
     */
    public CompletableFuture<WriteResult> setStreamMetadata(String streamName, StreamMetadata metadata) {
        return setStreamMetadata(streamName, null, metadata);
    }

    /**
     * Sets a stream's metadata.
     *
     * @param streamName stream's name.
     * @param options    append stream request's options.
     * @param metadata   stream's metadata
     * @return a write result if successful.
     * @see WriteResult
     */
    public CompletableFuture<WriteResult> setStreamMetadata(String streamName, AppendToStreamOptions options, StreamMetadata metadata) {
        JsonMapper mapper = new JsonMapper();

        try {
            EventData event = EventDataBuilder.json("$metadata", mapper.writeValueAsBytes(metadata)).build();
            return appendToStream("$$" + streamName, options, event);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads events from a given stream. The reading can be done forwards and backwards.
     *
     * @param streamName stream's name.
     * @param options    read request's operations.
     */
    public CompletableFuture<ReadResult> readStream(String streamName, ReadStreamOptions options) {
        return readEventsFromPublisher(readStreamReactive(streamName, options));
    }

    /**
     * Reads events from a given stream. The reading can be done forwards and backwards.
     *
     * @param streamName stream's name.
     */
    public Publisher<ReadMessage> readStreamReactive(String streamName) {
        return this.readStreamReactive(streamName, ReadStreamOptions.get());
    }


    /**
     * Reads events from a given stream. The reading can be done forwards and backwards.
     *
     * @param streamName stream's name.
     * @param options    read request's operations.
     */
    public Publisher<ReadMessage> readStreamReactive(String streamName, ReadStreamOptions options) {
        if (options == null)
            options = ReadStreamOptions.get();

        return new ReadStream(this.getGrpcClient(), streamName, options);
    }

    /**
     * Reads stream's metadata.
     *
     * @param streamName stream's name.
     * @see StreamMetadata
     */
    public CompletableFuture<StreamMetadata> getStreamMetadata(String streamName) {
        return getStreamMetadata(streamName, null);
    }

    /**
     * Reads stream's metadata.
     *
     * @param streamName stream's name.
     * @param options    read request's operations.
     * @see StreamMetadata
     */
    public CompletableFuture<StreamMetadata> getStreamMetadata(String streamName, ReadStreamOptions options) {

        return readStream("$$" + streamName, options).thenCompose(result -> {
            RecordedEvent event = result.getEvents().get(0).getOriginalEvent();
            CompletableFuture<StreamMetadata> out = new CompletableFuture<>();

            try {
                JsonMapper mapper = new JsonMapper();
                StreamMetadata metadata = mapper.readValue(event.getEventData(), StreamMetadata.class);
                out.complete(metadata);
            } catch (Throwable e) {
                out.completeExceptionally(e);
            }

            return out;
        }).exceptionally(e -> {
            if (e.getCause() instanceof StreamNotFoundException) {
                return new StreamMetadata();
            }

            throw new RuntimeException(e);
        });
    }

    /**
     * Reads events from the $all stream. The reading can be done forwards and backwards.
     */
    public CompletableFuture<ReadResult> readAll() {
        return this.readAll(ReadAllOptions.get());
    }

    /**
     * Reads events from the $all stream. The reading can be done forwards and backwards.
     *
     * @param options options of the read $all request.
     */
    public CompletableFuture<ReadResult> readAll(ReadAllOptions options) {
        return readEventsFromPublisher(this.readAllReactive(options));
    }

    public Publisher<ReadMessage> readAllReactive() {
        return this.readAllReactive(ReadAllOptions.get());
    }

    /**
     * Reads events from the $all stream. The reading can be done forwards and backwards.
     *
     * @param options options of the read $all request.
     */
    public Publisher<ReadMessage> readAllReactive(ReadAllOptions options) {
        if (options == null)
            options = ReadAllOptions.get();

        return new ReadAll(this.getGrpcClient(), options);
    }

    /**
     * Subscriptions allow you to subscribe to a stream and receive notifications about new events added to the stream.
     * You provide an even handler and an optional starting point to the subscription. The handler is called for each
     * event from the starting point onward. If events already exist, the handler will be called for each event one by
     * one until it reaches the end of the stream. From there, the server will notify the handler whenever a new event
     * appears.
     *
     * @param streamName stream's name.
     * @param listener   consumes a subscription's events.
     * @return a subscription handle.
     */
    public CompletableFuture<Subscription> subscribeToStream(String streamName, SubscriptionListener listener) {
        return this.subscribeToStream(streamName, listener, SubscribeToStreamOptions.get());
    }

    /**
     * Subscriptions allow you to subscribe to a stream and receive notifications about new events added to the stream.
     * You provide an even handler and an optional starting point to the subscription. The handler is called for each
     * event from the starting point onward. If events already exist, the handler will be called for each event one by
     * one until it reaches the end of the stream. From there, the server will notify the handler whenever a new event
     * appears.
     *
     * @param streamName stream's name.
     * @param listener   consumes a subscription's events.
     * @param options    a subscription request's options.
     * @return a subscription handle.
     */
    public CompletableFuture<Subscription> subscribeToStream(String streamName, SubscriptionListener listener, SubscribeToStreamOptions options) {
        if (options == null)
            options = SubscribeToStreamOptions.get();

        return new SubscribeToStream(this.getGrpcClient(), streamName, listener, options).execute();
    }

    /**
     * Subscriptions allow you to subscribe to $all stream and receive notifications about new events added to the stream.
     * You provide an even handler and an optional starting point to the subscription. The handler is called for each
     * event from the starting point onward. If events already exist, the handler will be called for each event one by
     * one until it reaches the end of the stream. From there, the server will notify the handler whenever a new event
     * appears.
     *
     * @param listener consumes a subscription's events.
     * @return a subscription handle.
     */
    public CompletableFuture<Subscription> subscribeToAll(SubscriptionListener listener) {
        return this.subscribeToAll(listener, SubscribeToAllOptions.get());
    }

    /**
     * Subscriptions allow you to subscribe to $all stream and receive notifications about new events added to the stream.
     * You provide an even handler and an optional starting point to the subscription. The handler is called for each
     * event from the starting point onward. If events already exist, the handler will be called for each event one by
     * one until it reaches the end of the stream. From there, the server will notify the handler whenever a new event
     * appears.
     *
     * @param listener consumes a subscription's events.
     * @param options  subscription to $all request's options.
     * @return a subscription handle.
     */
    public CompletableFuture<Subscription> subscribeToAll(SubscriptionListener listener, SubscribeToAllOptions options) {
        if (options == null)
            options = SubscribeToAllOptions.get();

        return new SubscribeToAll(this.getGrpcClient(), listener, options).execute();
    }

    /**
     * Deletes a given stream.
     * <p>
     * Makes use of Truncate before. When a stream is deleted, its Truncate before is set to the stream's current last
     * event number. When a deleted stream is read, the read will return a <i>StreamNotFound</i> error. After
     * deleting the stream, you are able to write to it again, continuing from where it left off.
     * </p>
     * <i>Note: Deletion is reversible until the scavenging process runs.</i>
     *
     * @param streamName stream's name
     * @return if successful, delete result.
     * @see DeleteResult
     */
    public CompletableFuture<DeleteResult> deleteStream(String streamName) {
        return this.deleteStream(streamName, DeleteStreamOptions.get());
    }

    /**
     * Deletes a given stream.
     * <p>
     * Makes use of Truncate before, When a stream is deleted, its Truncate before is set to the stream's current last
     * event number. When a soft deleted stream is read, the read will return a <i>StreamNotFound</i> error. After
     * deleting the stream, you are able to write to it again, continuing from where it left off.
     * </p>
     * <i>Note: soft deletion is reversible until the scavenging process runs.</i>
     *
     * @param streamName stream's name
     * @param options    delete stream request's options.
     * @return if successful, delete result.
     * @see DeleteResult
     */
    public CompletableFuture<DeleteResult> deleteStream(String streamName, DeleteStreamOptions options) {
        if (options == null)
            options = DeleteStreamOptions.get();

        return new DeleteStream(this.getGrpcClient(), streamName, true, options).execute();
    }

    /**
     * Tombstones a given stream.
     * <p>
     * Writes a tombstone event to the stream, permanently deleting it. The stream cannot be recreated or
     * written to again. Tombstone events are written with the event's type '$streamDeleted'. When a tombstoned stream
     * is read, the read will return a <i>StreamDeleted</i> error.
     * </p>
     *
     * @param streamName a stream's name.
     * @return if successful, delete result.
     * @see DeleteResult
     */
    public CompletableFuture<DeleteResult> tombstoneStream(String streamName) {
        return this.tombstoneStream(streamName, DeleteStreamOptions.get());
    }

    /**
     * Tombstones a given stream.
     * <p>
     * Writes a tombstone event to the stream, permanently deleting it. The stream cannot be recreated or
     * written to again. Tombstone events are written with the event's type '$streamDeleted'. When a tombstoned stream
     * is read, the read will return a <i>StreamDeleted</i> error.
     * </p>
     *
     * @param streamName a stream's name.
     * @param options    delete stream request's options.
     * @return if successful, delete result.
     * @see DeleteResult
     */
    public CompletableFuture<DeleteResult> tombstoneStream(String streamName, DeleteStreamOptions options) {
        if (options == null)
            options = DeleteStreamOptions.get();

        return new DeleteStream(this.getGrpcClient(), streamName, false, options).execute();
    }

    private static CompletableFuture<ReadResult> readEventsFromPublisher(Publisher<ReadMessage> eventPublisher) {
        CompletableFuture<ReadResult> future = new CompletableFuture<>();
        List<ResolvedEvent> events = new LinkedList<>();

        eventPublisher.subscribe(new ReadSubscriber() {
            long firstStreamPosition = 0;
            long lastStreamPosition = 0;
            Position lastAllStreamPosition = null;

            @Override
            public void onEvent(ReadMessage e) {
                if (e.hasFirstStreamPosition()) {
                    firstStreamPosition = e.getFirstStreamPosition();
                    return;
                }

                if (e.hasLastStreamPosition()) {
                    lastStreamPosition = e.getLastStreamPosition();
                    return;
                }

                if (e.hasLastAllPosition()) {
                    lastAllStreamPosition = e.getLastAllPosition();
                    return;
                }

                if (e.hasEvent())
                    events.add(e.getEvent());
            }

            @Override
            public void onError(Throwable t) {
                future.completeExceptionally(t);
            }

            @Override
            public void onComplete() {
                future.complete(new ReadResult(events, firstStreamPosition, lastStreamPosition, lastAllStreamPosition));
            }
        });
        return future;
    }
}
