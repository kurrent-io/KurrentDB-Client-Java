package io.kurrent.dbclient;


import io.kurrent.dbclient.streams.*;
import io.kurrent.dbclient.streams.ReadStreamTests;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamsTests implements
        AppendTests,
        ReadStreamTests,
        SubscriptionTests,
        DeleteTests,
        DeadlineTests,
        InterceptorTests,
        MetadataTests,
        ClientLifecycleTests {
    static private Database database;
    static private Logger logger;

    @BeforeAll
    public static void setup() {
        database = DatabaseFactory.spawn();
        logger = LoggerFactory.getLogger(StreamsTests.class);
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
