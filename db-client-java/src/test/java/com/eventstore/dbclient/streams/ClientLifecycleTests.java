package com.eventstore.dbclient.streams;

import com.eventstore.dbclient.ConnectionAware;
import com.eventstore.dbclient.ConnectionShutdownException;
import com.eventstore.dbclient.EventStoreDBClient;
import com.eventstore.dbclient.KurrentDBClientSettings;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public interface ClientLifecycleTests extends ConnectionAware {
    @Test
    default void testProvidesRunningStatus() {
        EventStoreDBClient client = getDatabase().newClient();

        assertFalse(client.isShutdown());
    }

    @Test
    default void testProvidesShutdownStatusAfterManualShutdown() throws Throwable {
        EventStoreDBClient client = getDatabase().newClient();

        client.shutdown().get();

        assertTrue(client.isShutdown());
    }

    @Test
    default void testProvidesShutdownStatusAfterAutomaticShutdown() throws Throwable {
        KurrentDBClientSettings settings = KurrentDBClientSettings.builder()
                .addHost("unknown.host.name", 2113)
                .buildConnectionSettings();
        EventStoreDBClient client = EventStoreDBClient.create(settings);

        try {
            client.readAll().get();
            fail();
        } catch (ExecutionException ex) {
            assertInstanceOf(ConnectionShutdownException.class, ex.getCause());
        }
        assertTrue(client.isShutdown());
    }
}
