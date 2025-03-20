package io.kurrent.dbclient.persistentsubscriptions;

import io.kurrent.dbclient.*;
import org.junit.jupiter.api.Test;

public interface UpdatePersistentSubscriptionToStreamTests extends ConnectionAware {
    @Test
    default void testUpdatePersistentSub() throws Throwable {
        String streamName = generateName();
        String groupName = generateName();

        KurrentDBPersistentSubscriptionsClient client = getDefaultPersistentSubscriptionClient();
        client.createToStream(streamName, groupName)
                .get();

        UpdatePersistentSubscriptionToStreamOptions updated = UpdatePersistentSubscriptionToStreamOptions.get()
                .checkpointAfterInMs(5_000)
                .startFrom(2);

        client.updateToStream(streamName, groupName, updated)
                .get();
    }

    @Test
    default void testUpdatePersistentSubToAll() throws Throwable {
        String groupName = generateName();
        KurrentDBPersistentSubscriptionsClient client = getDefaultPersistentSubscriptionClient();

        client.createToAll(groupName)
                    .get();
        UpdatePersistentSubscriptionToAllOptions updatedSettings = UpdatePersistentSubscriptionToAllOptions.get()
                .checkpointAfterInMs(5_000)
                .startFrom(3,4);

        client.updateToAll(groupName, updatedSettings)
                .get();
    }
}
