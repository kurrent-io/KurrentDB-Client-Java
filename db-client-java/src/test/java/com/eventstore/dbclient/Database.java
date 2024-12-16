package com.eventstore.dbclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.Function;

public interface Database {
    Logger logger = LoggerFactory.getLogger(Database.class);

    ConnectionSettingsBuilder defaultSettingsBuilder();

    ClientTracker getClientTracker();

    void cleanup();

    default EventStoreDBClient newClient() {
        return connectWith(Function.identity());
    }

    default EventStoreDBClient connectWith(Function<ConnectionSettingsBuilder, ConnectionSettingsBuilder> mod) {
        return createClient(mod.apply(defaultSettingsBuilder()).buildConnectionSettings());
    }

    default EventStoreDBClient defaultClient() {
        return getClientTracker().getDefaultClient(this);
    }

    default EventStoreDBClient createClient(KurrentDBClientSettings settings) {
        return getClientTracker().createClient(settings);
    }

    default boolean isTargetingBelowOrEqual21_10() {
        try {
            Optional<ServerVersion> result = defaultClient().getServerVersion().get();
            return !result.isPresent() || result.get().isLessOrEqualThan(21, 10);
        } catch (Exception e) {
            logger.error("Error when retrieving the server version", e);
            throw new RuntimeException(e);
        }
    }

    default void dispose() {
        cleanup();
        getClientTracker().dispose();
    }
}
