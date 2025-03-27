package io.kurrent.dbclient;

import io.kurrent.dbclient.plugins.ClientCertificateAuthenticationTests;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PluginsTests require the enterprise edition of KurrentDB (>= 24.2.0).
 */
public class PluginsTests implements ClientCertificateAuthenticationTests {
    static private Database database;
    static private Logger logger;

    @BeforeAll
    public static void setup() {
        database = DatabaseFactory.spawnEnterpriseWithPluginsEnabled("UserCertificates");
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

