package io.kurrent.dbclient;

import io.kurrent.dbclient.persistentsubscriptions.*;
import io.kurrent.dbclient.persistentsubscriptions.PersistentSubscriptionToAllWithFilterTests;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistentSubscriptionsTests implements
        CreatePersistentSubscriptionTests,
        PersistentSubscriptionManagementTests,
        DeletePersistentSubscriptionToStreamTests,
        UpdatePersistentSubscriptionToStreamTests,
        SubscribePersistentSubscriptionToStreamTests,
        PersistentSubscriptionToAllWithFilterTests {
    static private Database database;
    static private Logger logger;

    @BeforeAll
    public static void setup() {
        database = DatabaseFactory.spawn();
        logger = LoggerFactory.getLogger(PersistentSubscriptionsTests.class);
    }

    @Override
    public Database getDatabase() {
        return database;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @AfterAll
    public static void cleanup() {
        database.dispose();
    }
}
