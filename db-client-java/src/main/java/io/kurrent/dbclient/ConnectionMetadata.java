package io.kurrent.dbclient;

import io.grpc.Metadata;

import java.util.Map;

class ConnectionMetadata {
    private Metadata metadata;

    public ConnectionMetadata() {
        this.metadata = new Metadata();
    }

    public ConnectionMetadata authenticated(UserCredentials credentials) {
        this.metadata.put(Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER), credentials.basicAuthHeader());
        return this;
    }

    public boolean hasUserCredentials() {
        return this.metadata.containsKey(Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER));
    }

    public String getUserCredentials() {
        return this.metadata.get(Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER));
    }

    public ConnectionMetadata requiresLeader() {
        this.metadata.put(Metadata.Key.of("requires-leader", Metadata.ASCII_STRING_MARSHALLER), String.valueOf(true));
        return this;
    }

    public ConnectionMetadata headers(Map<String, String> headers) {
        for (Map.Entry<String, String> entry : headers.entrySet())
            this.metadata.put(Metadata.Key.of(entry.getKey(), Metadata.ASCII_STRING_MARSHALLER), entry.getValue());

        return this;
    }

    public Metadata build() {
        return this.metadata;
    }
}
