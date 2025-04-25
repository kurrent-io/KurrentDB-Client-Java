package io.kurrent.dbclient.plugins;

import io.kurrent.dbclient.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

public interface ClientCertificateAuthenticationTests extends ConnectionAware {
    @Test
    default void testClientCertificateAuthenticationWithValidCertificate() {
        Assertions.assertDoesNotThrow(() -> {
            KurrentDBClient client = getDatabase()
                    .createClient(getDatabase()
                            .defaultSettingsBuilder()
                            .defaultClientCertificate(clientCertificate("admin"), userKey("admin"))
                            .defaultCredentials(null)
                            .buildConnectionSettings());

            client.readAll().get();
        });
    }

    static String clientCertificate(String user) {
        return buildCertPath(user, "crt");
    }

    static String userKey(String user) {
        return buildCertPath(user, "key");
    }

    static String buildCertPath(String user, String extension) {
        String certsPath = Paths.get(System.getProperty("user.dir"), "certs").toAbsolutePath().toString();
        return String.format("%s/user-%s/user-%s.%s", certsPath, user, user, extension);
    }
}
