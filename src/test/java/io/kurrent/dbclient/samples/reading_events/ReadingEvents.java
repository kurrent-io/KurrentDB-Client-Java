package io.kurrent.dbclient.samples.reading_events;

import io.kurrent.dbclient.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.reactivestreams.*;
import org.reactivestreams.Subscription;

import java.util.concurrent.ExecutionException;

@SuppressWarnings("ALL")
public class ReadingEvents {
    private static void readFromStream(KurrentDBClient client) throws ExecutionException, InterruptedException, JsonProcessingException {
        // region read-from-stream
        ReadStreamOptions options = ReadStreamOptions.get()
                .forwards()
                .fromStart();

        ReadResult result = client.readStream("some-stream", options)
                .get();


        // or using read reactive
        Publisher<ReadMessage> publisher = client.readStreamReactive("some-stream", options);
        // endregion read-from-stream

        // region iterate-stream
        for (ResolvedEvent resolvedEvent : result.getEvents()) {
            RecordedEvent recordedEvent = resolvedEvent.getOriginalEvent();
            System.out.println(new ObjectMapper().writeValueAsString(recordedEvent.getEventData()));
        }

        // or using read reactive
        publisher.subscribe(new Subscriber<ReadMessage>() {
            @Override
            public void onSubscribe(Subscription subscription) {
            }

            @Override
            public void onNext(ReadMessage readMessage) {
                RecordedEvent recordedEvent = readMessage.getEvent().getOriginalEvent();
                try {
                    System.out.println(new ObjectMapper().writeValueAsString(recordedEvent.getEventData()));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onError(Throwable throwable) {
            }

            @Override
            public void onComplete() {
            }
        });
        // endregion iterate-stream
    }

    private static void readFromStreamPosition(KurrentDBClient client) throws ExecutionException, InterruptedException, JsonProcessingException {
        // region read-from-stream-position
        ReadStreamOptions options = ReadStreamOptions.get()
                .forwards()
                .fromRevision(10)
                .maxCount(20);

        ReadResult result = client.readStream("some-stream", options)
                .get();

        // or using read reactive
        Publisher<ReadMessage> publisher = client.readStreamReactive("some-stream", options);
        // endregion read-from-stream-position

        // region iterate-stream
        for (ResolvedEvent resolvedEvent : result.getEvents()) {
            RecordedEvent recordedEvent = resolvedEvent.getOriginalEvent();
            System.out.println(new ObjectMapper().writeValueAsString(recordedEvent.getEventData()));
        }

        // or using read reactive
        publisher.subscribe(new Subscriber<ReadMessage>() {
            @Override
            public void onSubscribe(Subscription subscription) {
            }

            @Override
            public void onNext(ReadMessage readMessage) {
                RecordedEvent recordedEvent = readMessage.getEvent().getOriginalEvent();
                try {
                    System.out.println(new ObjectMapper().writeValueAsString(recordedEvent.getEventData()));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onError(Throwable throwable) {
            }

            @Override
            public void onComplete() {
            }
        });
        // endregion iterate-stream
    }

    private static void readStreamOverridingUserCredentials(KurrentDBClient client) throws ExecutionException, InterruptedException {
        // region overriding-user-credentials
        ReadStreamOptions options = ReadStreamOptions.get()
                .forwards()
                .fromStart()
                .authenticated("admin", "changeit");

        ReadResult result = client.readStream("some-stream", options)
                .get();

        // Or using reactive stream
        Publisher<ReadMessage> publisher = client.readStreamReactive("some-stream", options);
        // endregion overriding-user-credentials
    }

    private static void readFromStreamPositionCheck(KurrentDBClient client) throws JsonProcessingException, InterruptedException {
        // region checking-for-stream-presence
        ReadStreamOptions options = ReadStreamOptions.get()
                .forwards()
                .fromRevision(10)
                .maxCount(20);

        ReadResult result = null;
        try {
            result = client.readStream("some-stream", options)
                    .get();
        } catch (ExecutionException e) {
            Throwable innerException = e.getCause();

            if (innerException instanceof StreamNotFoundException) {
                return;
            }
        }

        for (ResolvedEvent resolvedEvent : result.getEvents()) {
            RecordedEvent recordedEvent = resolvedEvent.getOriginalEvent();
            System.out.println(new ObjectMapper().writeValueAsString(recordedEvent.getEventData()));
        }

        // or using read reactive
        Publisher<ReadMessage> publisher = client.readStreamReactive("some-stream", options);

        publisher.subscribe(new Subscriber<ReadMessage>() {
            @Override
            public void onSubscribe(Subscription subscription) {
            }

            @Override
            public void onNext(ReadMessage readMessage) {
                RecordedEvent recordedEvent = readMessage.getEvent().getOriginalEvent();
                try {
                    System.out.println(new ObjectMapper().writeValueAsString(recordedEvent.getEventData()));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                Throwable innerException = throwable.getCause();

                if (innerException instanceof StreamNotFoundException) {
                    return;
                }
                // Handle other errors
            }

            @Override
            public void onComplete() {
            }
        });
        // endregion checking-for-stream-presence
    }

    private static void readFromStreamBackwards(KurrentDBClient client) throws JsonProcessingException, ExecutionException, InterruptedException {
        // region reading-backwards
        ReadStreamOptions options = ReadStreamOptions.get()
                .backwards()
                .fromEnd();

        ReadResult result = client.readStream("some-stream", options)
                .get();

        for (ResolvedEvent resolvedEvent : result.getEvents()) {
            RecordedEvent recordedEvent = resolvedEvent.getOriginalEvent();
            System.out.println(new ObjectMapper().writeValueAsString(recordedEvent.getEventData()));
        }

        // or using read reactive
        Publisher<ReadMessage> publisher = client.readStreamReactive("some-stream", options);

        publisher.subscribe(new Subscriber<ReadMessage>() {
            @Override
            public void onSubscribe(Subscription subscription) {
            }

            @Override
            public void onNext(ReadMessage readMessage) {
                RecordedEvent recordedEvent = readMessage.getEvent().getOriginalEvent();
                try {
                    System.out.println(new ObjectMapper().writeValueAsString(recordedEvent.getEventData()));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onError(Throwable throwable) {
            }

            @Override
            public void onComplete() {
            }
        });
        // endregion reading-backwards
    }

    private static void readFromAllStream(KurrentDBClient client) throws JsonProcessingException, ExecutionException, InterruptedException {
        // region read-from-all-stream
        ReadAllOptions options = ReadAllOptions.get()
                .forwards()
                .fromStart();

        ReadResult result = client.readAll(options)
                .get();

        // or using read reactive
        Publisher<ReadMessage> publisher = client.readAllReactive(options);
        // endregion read-from-all-stream

        // region read-from-all-stream-iterate
        for (ResolvedEvent resolvedEvent : result.getEvents()) {
            RecordedEvent recordedEvent = resolvedEvent.getOriginalEvent();
            System.out.println(new ObjectMapper().writeValueAsString(recordedEvent.getEventData()));
        }

        // or using read reactive
        publisher.subscribe(new Subscriber<ReadMessage>() {
            @Override
            public void onSubscribe(Subscription subscription) {
            }

            @Override
            public void onNext(ReadMessage readMessage) {
                RecordedEvent recordedEvent = readMessage.getEvent().getOriginalEvent();
                try {
                    System.out.println(new ObjectMapper().writeValueAsString(recordedEvent.getEventData()));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onError(Throwable throwable) {
            }

            @Override
            public void onComplete() {
            }
        });
        // endregion read-from-all-stream-iterate
    }

    private static void readAllOverridingUserCredentials(KurrentDBClient client) throws ExecutionException, InterruptedException {
        // region read-all-overriding-user-credentials
        ReadAllOptions options = ReadAllOptions.get()
                .forwards()
                .fromStart()
                .authenticated("admin", "changeit");

        ReadResult result = client.readAll(options)
                .get();

        // or using read reactive
        Publisher<ReadMessage> publisher = client.readAllReactive(options);
        // endregion read-all-overriding-user-credentials
    }

    private static void ignoreSystemEvents(KurrentDBClient client) throws JsonProcessingException, ExecutionException, InterruptedException {
        // region ignore-system-events
        ReadAllOptions options = ReadAllOptions.get()
                .forwards()
                .fromStart();

        ReadResult result = client.readAll(options)
                .get();

        for (ResolvedEvent resolvedEvent : result.getEvents()) {
            RecordedEvent recordedEvent = resolvedEvent.getOriginalEvent();
            if (recordedEvent.getEventType().startsWith("$")) {
                continue;
            }
            System.out.println(new ObjectMapper().writeValueAsString(recordedEvent.getEventData()));
        }

        // or using read reactive
        Publisher<ReadMessage> publisher = client.readAllReactive(options);

        publisher.subscribe(new Subscriber<ReadMessage>() {
            @Override
            public void onSubscribe(Subscription subscription) {
            }

            @Override
            public void onNext(ReadMessage readMessage) {
                RecordedEvent recordedEvent = readMessage.getEvent().getOriginalEvent();

                if (recordedEvent.getEventType().startsWith("$")) {
                    return;
                }

                try {
                    System.out.println(new ObjectMapper().writeValueAsString(recordedEvent.getEventData()));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onError(Throwable throwable) {
            }

            @Override
            public void onComplete() {
            }
        });
        // endregion ignore-system-events
    }

    private static void readFromAllStreamBackwards(KurrentDBClient client) throws JsonProcessingException, ExecutionException, InterruptedException {
        // region read-from-all-stream-backwards
        ReadAllOptions options = ReadAllOptions.get()
                .backwards()
                .fromEnd();

        ReadResult result = client.readAll(options)
                .get();

        // or using read reactive
        Publisher<ReadMessage> publisher = client.readAllReactive(options);
        // endregion read-from-all-stream-backwards

        // region read-from-all-stream-iterate
        for (ResolvedEvent resolvedEvent : result.getEvents()) {
            RecordedEvent recordedEvent = resolvedEvent.getOriginalEvent();
            System.out.println(new ObjectMapper().writeValueAsString(recordedEvent.getEventData()));
        }

        // or using read reactive
        publisher.subscribe(new Subscriber<ReadMessage>() {
            @Override
            public void onSubscribe(Subscription subscription) {
            }

            @Override
            public void onNext(ReadMessage readMessage) {
                RecordedEvent recordedEvent = readMessage.getEvent().getOriginalEvent();
                try {
                    System.out.println(new ObjectMapper().writeValueAsString(recordedEvent.getEventData()));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onError(Throwable throwable) {
            }

            @Override
            public void onComplete() {
            }
        });
        // endregion read-from-all-stream-iterate
    }

    private static void filteringOutSystemEvents(KurrentDBClient client) throws JsonProcessingException, ExecutionException, InterruptedException {
        ReadAllOptions options = ReadAllOptions.get()
                .forwards()
                .fromStart();

        ReadResult result = client.readAll(options)
                .get();

        for (ResolvedEvent resolvedEvent : result.getEvents()) {
            RecordedEvent recordedEvent = resolvedEvent.getOriginalEvent();
            if (!recordedEvent.getEventType().startsWith("$")) {
                continue;
            }
            System.out.println(new ObjectMapper().writeValueAsString(recordedEvent.getEventData()));
        }

        // or using read reactive
        Publisher<ReadMessage> publisher = client.readAllReactive(options);

        publisher.subscribe(new Subscriber<ReadMessage>() {
            @Override
            public void onSubscribe(Subscription subscription) {
            }

            @Override
            public void onNext(ReadMessage readMessage) {
                RecordedEvent recordedEvent = readMessage.getEvent().getOriginalEvent();
                if (!recordedEvent.getEventType().startsWith("$")) {
                    return;
                }
                try {
                    System.out.println(new ObjectMapper().writeValueAsString(recordedEvent.getEventData()));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onError(Throwable throwable) {
            }

            @Override
            public void onComplete() {
            }
        });
    }

    private static void readFromStreamResolvingLinkTos(KurrentDBClient client) throws JsonProcessingException, ExecutionException, InterruptedException {
        // region read-from-all-stream-resolving-link-Tos
        ReadAllOptions options = ReadAllOptions.get()
                .forwards()
                .fromStart()
                .resolveLinkTos();

        ReadResult result = client.readAll(options)
                .get();

        // or using read reactive
        Publisher<ReadMessage> publisher = client.readAllReactive(options);

        // endregion read-from-all-stream-resolving-link-Tos
        for (ResolvedEvent resolvedEvent : result.getEvents()) {
            RecordedEvent recordedEvent = resolvedEvent.getOriginalEvent();
            System.out.println(new ObjectMapper().writeValueAsString(recordedEvent.getEventData()));
        }
    }
}
