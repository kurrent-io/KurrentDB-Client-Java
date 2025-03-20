package io.kurrent.dbclient;

import java.util.Objects;

/**
 * Holds a certificate and key for authenticated requests.
 */
public final class ClientCertificate {
    private final String certFile;
    private final String keyFile;

    public ClientCertificate(String certFile, String keyFile) {
        this.certFile = certFile;
        this.keyFile = keyFile;
    }

    /**
     * Certificate for user authentication.
     */
    public String getCertFile() {
        return certFile;
    }

    /**
     * Certificate key for user authentication.
     */
    public String getKeyFile() {
        return keyFile;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        ClientCertificate other = (ClientCertificate) obj;

        return Objects.equals(certFile, other.certFile)
                && Objects.equals(keyFile, other.keyFile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(certFile, keyFile);
    }
}
