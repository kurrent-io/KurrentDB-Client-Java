package io.kurrent.dbclient.samples.server_side_filtering;

import io.kurrent.dbclient.*;

import java.util.concurrent.CompletableFuture;

public class ServerSideFiltering {
    private static void excludeSystemEvents(KurrentDBClient client) {
        //region exclude-system
        SubscriptionListener listener = new SubscriptionListener() {
            @Override
            public void onEvent(Subscription subscription, ResolvedEvent event) {
                System.out.println("Received event"
                        + event.getOriginalEvent().getRevision()
                        + "@" + event.getOriginalEvent().getStreamId());
            }
        };
        String excludeSystemEventsRegex = "^[^\\$].*";

        SubscriptionFilter filter = SubscriptionFilter.newBuilder()
                .withEventTypeRegularExpression(excludeSystemEventsRegex)
                .build();

        SubscribeToAllOptions options = SubscribeToAllOptions.get()
                .filter(filter);

        client.subscribeToAll(
                listener,
                options
        );
        //endregion exclude-system
    }

    private static void eventTypePrefix(KurrentDBClient client) {
        //region event-type-prefix
        SubscriptionFilter filter = SubscriptionFilter.newBuilder()
                .addEventTypePrefix("customer-")
                .build();
        //endregion event-type-prefix

        SubscriptionListener listener = new SubscriptionListener() {
            @Override
            public void onEvent(Subscription subscription, ResolvedEvent event) {
                System.out.println("Received event"
                        + event.getOriginalEvent().getRevision()
                        + "@" + event.getOriginalEvent().getStreamId());
            }
        };

        SubscribeToAllOptions options = SubscribeToAllOptions.get()
                .filter(filter);

        client.subscribeToAll(
                listener,
                options
        );
    }

    private static void eventTypeRegex(KurrentDBClient client) {
        //region event-type-regex
        SubscriptionFilter filter = SubscriptionFilter.newBuilder()
                .withEventTypeRegularExpression("^user|^company")
                .build();
        //endregion event-type-regex

        SubscriptionListener listener = new SubscriptionListener() {
            @Override
            public void onEvent(Subscription subscription, ResolvedEvent event) {
                System.out.println("Received event"
                        + event.getOriginalEvent().getRevision()
                        + "@" + event.getOriginalEvent().getStreamId());
            }
        };

        SubscribeToAllOptions options = SubscribeToAllOptions.get()
                .filter(filter);

        client.subscribeToAll(
                listener,
                options
        );
    }

    private static void streamPrefix(KurrentDBClient client) {
        //region stream-prefix
        SubscriptionFilter filter = SubscriptionFilter.newBuilder()
                .addStreamNamePrefix("user-")
                .build();
        //endregion stream-prefix

        SubscriptionListener listener = new SubscriptionListener() {
            @Override
            public void onEvent(Subscription subscription, ResolvedEvent event) {
                System.out.println("Received event"
                        + event.getOriginalEvent().getRevision()
                        + "@" + event.getOriginalEvent().getStreamId());
            }
        };

        SubscribeToAllOptions options = SubscribeToAllOptions.get()
                .filter(filter);

        client.subscribeToAll(
                listener,
                options
        );
    }

    private static void streamRegex(KurrentDBClient client) {
        //region stream-regex
        SubscriptionFilter filter = SubscriptionFilter.newBuilder()
                .withStreamNameRegularExpression("^account|^savings")
                .build();
        //endregion stream-regex

        SubscriptionListener listener = new SubscriptionListener() {
            @Override
            public void onEvent(Subscription subscription, ResolvedEvent event) {
                System.out.println("Received event"
                        + event.getOriginalEvent().getRevision()
                        + "@" + event.getOriginalEvent().getStreamId());
            }
        };

        SubscribeToAllOptions options = SubscribeToAllOptions.get()
                .filter(filter);

        client.subscribeToAll(
                listener,
                options
        );
    }

    private static void checkpointCallback(KurrentDBClient client) {
        //region checkpoint
        String excludeSystemEventsRegex = "/^[^\\$].*/";

        SubscriptionFilter filter = SubscriptionFilter.newBuilder()
                .withEventTypeRegularExpression(excludeSystemEventsRegex)
                .withCheckpointer(
                        new Checkpointer() {
                            @Override
                            public CompletableFuture<Void> onCheckpoint(Subscription subscription, Position position) {
                                System.out.println("checkpoint taken at {p.PreparePosition}");
                                return CompletableFuture.completedFuture(null);
                            }
                        })
                .build();
        //endregion checkpoint

        SubscribeToAllOptions options = SubscribeToAllOptions.get()
                .filter(filter);

        SubscriptionListener listener = new SubscriptionListener() {
            @Override
            public void onEvent(Subscription subscription, ResolvedEvent event) {
                System.out.println("Received event"
                        + event.getOriginalEvent().getRevision()
                        + "@" + event.getOriginalEvent().getStreamId());
            }
        };

        client.subscribeToAll(
                listener,
                options
        );
    }

    private static void CheckpointCallbackWithInterval(KurrentDBClient client) {
        //region checkpoint-with-interval
        String excludeSystemEventsRegex = "/^[^\\$].*/";

        SubscriptionFilter filter = SubscriptionFilter.newBuilder()
                .withEventTypeRegularExpression(excludeSystemEventsRegex)
                .withCheckpointer(
                        new Checkpointer() {
                            @Override
                            public CompletableFuture<Void> onCheckpoint(Subscription subscription, Position position) {
                                System.out.println("checkpoint taken at {p.PreparePosition}");
                                return CompletableFuture.completedFuture(null);
                            }
                        },
                        1000)
                .build();
        //endregion checkpoint-with-interval

        SubscribeToAllOptions options = SubscribeToAllOptions.get()
                .filter(filter);

        SubscriptionListener listener = new SubscriptionListener() {
            @Override
            public void onEvent(Subscription subscription, ResolvedEvent event) {
                System.out.println("Received event"
                        + event.getOriginalEvent().getRevision()
                        + "@" + event.getOriginalEvent().getStreamId());
            }
        };

        client.subscribeToAll(
                listener,
                options
        );
    }
}
