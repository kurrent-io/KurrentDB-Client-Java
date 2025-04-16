package io.kurrent.dbclient.samples.authentication;

import io.kurrent.dbclient.KurrentDBClient;
import io.kurrent.dbclient.KurrentDBClientSettings;
import io.kurrent.dbclient.KurrentDBConnectionString;

public class UserCertificate {
    private static void tracing() {
        // region client-with-user-certificates
        KurrentDBClientSettings settings = KurrentDBConnectionString
                .parseOrThrow("kurrentdb://admin:changeit@{endpoint}?tls=true&userCertFile={pathToCaFile}&userKeyFile={pathToKeyFile}");
        KurrentDBClient client = KurrentDBClient.create(settings);
        // endregion client-with-user-certificates
    }
}
