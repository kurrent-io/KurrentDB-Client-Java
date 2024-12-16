package com.eventstore.dbclient.persistentsubscriptions;

import com.eventstore.dbclient.*;
import org.junit.jupiter.api.Test;

public interface CreatePersistentSubscriptionTests extends ConnectionAware {
    @Test
    default void testCreatePersistentSub() throws Throwable {
        KurrentDBPersistentSubscriptionsClient client = getDefaultPersistentSubscriptionClient();

        client.createToStream(generateName(), generateName())
                .get();

        client.createToStream(generateName(), generateName(), CreatePersistentSubscriptionToStreamOptions.get().startFrom(2))
                .get();
    }

    @Test
    default void testCreatePersistentSubToAll() throws Throwable {
        KurrentDBPersistentSubscriptionsClient client = getDefaultPersistentSubscriptionClient();

        client.createToAll(generateName())
                .get();

        client.createToAll(generateName(), CreatePersistentSubscriptionToAllOptions.get().startFrom(1, 2))
                .get();
    }
}
