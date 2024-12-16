package com.eventstore.dbclient;

import io.grpc.ManagedChannel;

import java.net.InetSocketAddress;
import java.util.HashMap;

class ClientTelemetryTags extends HashMap<String, String> {
    ClientTelemetryTags() {
        super();
    }

    ClientTelemetryTags(ClientTelemetryTags tags) {
        super(tags);
    }

    static Builder builder() {
        return new Builder();
    }

    static class Builder {
        final ClientTelemetryTags tags;

        Builder() {
            tags = new ClientTelemetryTags();
        }

        Builder withServerTagsFromGrpcChannel(ManagedChannel channel) {
            if (channel == null) return this;

            String[] authorityParts = channel.authority().split(":");

            return withServerTags(authorityParts[0], authorityParts[1]);
        }

        Builder withServerTagsFromClientSettings(KurrentDBClientSettings settings) {
            if (settings == null || !settings.isDnsDiscover()) return this;

            InetSocketAddress dns = settings.getHosts()[0];

            return withServerTags(dns.getAddress().toString(), String.valueOf(dns.getPort()));
        }

        private Builder withServerTags(String address, String port) {
            return withRequiredTag(ClientTelemetryAttributes.Server.ADDRESS, address)
                    .withRequiredTag(ClientTelemetryAttributes.Server.PORT, String.valueOf(port));
        }

        Builder withOptionalDatabaseUserTag(UserCredentials userCredentials) {
            if (userCredentials == null) return this;

            return withOptionalTag(ClientTelemetryAttributes.Database.USER, userCredentials.getUsername());
        }

        Builder withRequiredTag(String key, String value) {
            if (key == null) throw new NullPointerException("Required tag key is null.");
            if (value == null) throw new NullPointerException("Required tag value is null.");

            return withOptionalTag(key, value);
        }

        Builder withOptionalTag(String key, String value) {
            if (value == null) return this;
            tags.put(key, value);
            return this;
        }

        ClientTelemetryTags build() {
            return tags;
        }
    }
}
