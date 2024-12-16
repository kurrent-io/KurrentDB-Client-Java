package com.eventstore.dbclient.persistentsubscriptions;

import com.eventstore.dbclient.ConnectionAware;
import com.eventstore.dbclient.KurrentDBPersistentSubscriptionsClient;
import org.junit.jupiter.api.Test;

public interface DeletePersistentSubscriptionToStreamTests extends ConnectionAware {
    @Test
    default void testDeletePersistentSub() throws Throwable {
        KurrentDBPersistentSubscriptionsClient client = getDefaultPersistentSubscriptionClient();
        String streamName = generateName();
        String groupName = generateName();

        client.createToStream(streamName, groupName)
                .get();

        client.deleteToStream(streamName, groupName)
                .get();
    }

    @Test
    default void testDeletePersistentSubToAll() throws Throwable {
        KurrentDBPersistentSubscriptionsClient client = getDefaultPersistentSubscriptionClient();
        String groupName = generateName();

        client.createToAll(groupName)
                .get();

        client.deleteToAll(groupName)
                .get();
    }
}
