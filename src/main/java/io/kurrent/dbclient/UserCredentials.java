package io.kurrent.dbclient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Holds a login and a password for authenticated requests.
 */
public final class UserCredentials {
    private final String username;
    private final String base64Encoded;

    public UserCredentials(String username, String password) {
        this.username = username;

        byte[] credentialsBytes = String.format("%s:%s", username, password).getBytes(StandardCharsets.US_ASCII);
        base64Encoded = String.format("Basic %s", Base64.getEncoder().encodeToString(credentialsBytes));
    }

    String getUsername() { return username; }

    String basicAuthHeader() {
        return base64Encoded;
    }
}
