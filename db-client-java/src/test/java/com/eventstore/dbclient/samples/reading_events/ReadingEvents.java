package io.kurrent.dbclient.samples.reading_events;

import io.kurrent.dbclient.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.concurrent.ExecutionException;

public class ReadingEvents {
    private static void readFromStream(KurrentDBClient client) throws ExecutionException, InterruptedException, JsonProcessingException {
        // region read-from-stream
        ReadStreamOptions options = ReadStreamOptions.get()
                .forwards()
                .fromStart();

        ReadResult result = client.readStream("some-stream", options)
                .get();

        // endregion read-from-stream

        // region iterate-stream
        for (ResolvedEvent resolvedEvent : result.getEvents()) {
            RecordedEvent recordedEvent = resolvedEvent.getOriginalEvent();
            System.out.println(new ObjectMapper().writeValueAsString(recordedEvent.getEventData()));
        }
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

        // endregion read-from-stream-position

        // region iterate-stream
        for (ResolvedEvent resolvedEvent : result.getEvents()) {
            RecordedEvent recordedEvent = resolvedEvent.getOriginalEvent();
            System.out.println(new ObjectMapper().writeValueAsString(recordedEvent.getEventData()));
        }
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
        // endregion reading-backwards
    }

    private static void readFromAllStream(KurrentDBClient client) throws JsonProcessingException, ExecutionException, InterruptedException {
        // region read-from-all-stream
        ReadAllOptions options = ReadAllOptions.get()
                .forwards()
                .fromStart();

        ReadResult result = client.readAll(options)
                .get();

        // endregion read-from-all-stream

        // region read-from-all-stream-iterate
        for (ResolvedEvent resolvedEvent : result.getEvents()) {
            RecordedEvent recordedEvent = resolvedEvent.getOriginalEvent();
            System.out.println(new ObjectMapper().writeValueAsString(recordedEvent.getEventData()));
        }
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
        // endregion ignore-system-events
    }

    private static void readFromAllStreamBackwards(KurrentDBClient client) throws JsonProcessingException, ExecutionException, InterruptedException {
        // region read-from-all-stream-backwards
        ReadAllOptions options = ReadAllOptions.get()
                .backwards()
                .fromEnd();

        ReadResult result = client.readAll(options)
                .get();

        // endregion read-from-all-stream-backwards

        // region read-from-all-stream-iterate
        for (ResolvedEvent resolvedEvent : result.getEvents()) {
            RecordedEvent recordedEvent = resolvedEvent.getOriginalEvent();
            System.out.println(new ObjectMapper().writeValueAsString(recordedEvent.getEventData()));
        }
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
    }

    private static void readFromStreamResolvingLinkTos(KurrentDBClient client) throws JsonProcessingException, ExecutionException, InterruptedException {
        // region read-from-all-stream-resolving-link-Tos
        ReadAllOptions options = ReadAllOptions.get()
                .forwards()
                .fromStart()
                .resolveLinkTos();

        ReadResult result = client.readAll(options)
                .get();

        // endregion read-from-all-stream-resolving-link-Tos
        for (ResolvedEvent resolvedEvent : result.getEvents()) {
            RecordedEvent recordedEvent = resolvedEvent.getOriginalEvent();
            System.out.println(new ObjectMapper().writeValueAsString(recordedEvent.getEventData()));
        }
    }
}
