package io.kurrent.dbclient.samples.quick_start;

import io.kurrent.dbclient.*;
import io.kurrent.dbclient.samples.TestEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class QuickStart {
    public static void Run() throws ConnectionStringParsingException, ExecutionException, InterruptedException, JsonProcessingException {
        // region createClient
        KurrentDBClientSettings settings = KurrentDBConnectionString.parseOrThrow("{connectionString}");
        KurrentDBClient client = KurrentDBClient.create(settings);
        // endregion createClient

        // region createEvent
        TestEvent event = new TestEvent();
        JsonMapper jsonMapper = new JsonMapper();
        event.setId(UUID.randomUUID().toString());
        event.setImportantData("I wrote my first event!");

        EventData eventData = EventData
                .builderAsJson("TestEvent", jsonMapper.writeValueAsBytes(event))
                .build();
        // endregion createEvent

        // region appendEvents
        client.appendToStream("some-stream", eventData)
                .get();
        // endregion appendEvents

        // region overriding-user-credentials
        AppendToStreamOptions appendToStreamOptions = AppendToStreamOptions.get()
                .authenticated("admin", "changeit");

        client.appendToStream("some-stream", appendToStreamOptions, eventData)
                .get();
        // endregion overriding-user-credentials


        // region readStream
        ReadStreamOptions options = ReadStreamOptions.get()
                .forwards()
                .fromStart()
                .maxCount(10);

        ReadResult result = client.readStream("some-stream", options)
                .get();
        // endregion readStream
    }
}
