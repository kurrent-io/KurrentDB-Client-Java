package com.eventstore.dbclient.samples.authentication;

import com.eventstore.dbclient.KurrentDBClient;
import com.eventstore.dbclient.KurrentDBClientSettings;
import com.eventstore.dbclient.KurrentDBConnectionString;

public class UserCertificate {
    private static void tracing() {
        // region client-with-user-certificates
        KurrentDBClientSettings settings = KurrentDBConnectionString
                .parseOrThrow("esdb://admin:changeit@{endpoint}?tls=true&userCertFile={pathToCaFile}&userKeyFile={pathToKeyFile}");
        KurrentDBClient client = KurrentDBClient.create(settings);
        // endregion client-with-user-certificates
    }
}
