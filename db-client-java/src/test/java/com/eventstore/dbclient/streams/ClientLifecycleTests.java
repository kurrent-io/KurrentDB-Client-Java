package io.kurrent.dbclient.streams;

import io.kurrent.dbclient.ConnectionAware;
import io.kurrent.dbclient.ConnectionShutdownException;
import io.kurrent.dbclient.KurrentDBClient;
import io.kurrent.dbclient.KurrentDBClientSettings;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public interface ClientLifecycleTests extends ConnectionAware {
    @Test
    default void testProvidesRunningStatus() {
        KurrentDBClient client = getDatabase().newClient();

        assertFalse(client.isShutdown());
    }

    @Test
    default void testProvidesShutdownStatusAfterManualShutdown() throws Throwable {
        KurrentDBClient client = getDatabase().newClient();

        client.shutdown().get();

        assertTrue(client.isShutdown());
    }

    @Test
    default void testProvidesShutdownStatusAfterAutomaticShutdown() throws Throwable {
        KurrentDBClientSettings settings = KurrentDBClientSettings.builder()
                .addHost("unknown.host.name", 2113)
                .buildConnectionSettings();
        KurrentDBClient client = KurrentDBClient.create(settings);

        try {
            client.readAll().get();
            fail();
        } catch (ExecutionException ex) {
            assertInstanceOf(ConnectionShutdownException.class, ex.getCause());
        }
        assertTrue(client.isShutdown());
    }
}
