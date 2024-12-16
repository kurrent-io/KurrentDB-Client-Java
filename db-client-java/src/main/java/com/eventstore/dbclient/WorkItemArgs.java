package com.eventstore.dbclient;

import io.grpc.ManagedChannel;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;

class WorkItemArgs {
    private final UUID id;
    private final ManagedChannel channel;
    private final InetSocketAddress endpoint;
    private final ServerInfo info;

    public WorkItemArgs(UUID id, ManagedChannel channel, InetSocketAddress endpoint, ServerInfo info) {
        this.id = id;
        this.channel = channel;
        this.endpoint = endpoint;
        this.info = info;
    }

    public UUID getId() {
        return id;
    }

    public ManagedChannel getChannel() {
        return channel;
    }

    public InetSocketAddress getEndpoint() {
        return endpoint;
    }

    public Optional<ServerVersion> getServerVersion() {
        return Optional.ofNullable(info).map(ServerInfo::getServerVersion);
    }

    public boolean supportFeature(int feature) {
        return info != null && info.supportFeature(feature);
    }

    public <A> HttpURLConnection getHttpConnection(OptionsBase<A> options, KurrentDBClientSettings settings, String path) {
        try {
            HttpURLConnection conn = (HttpURLConnection) getURL(settings.isTls(), this.endpoint, path).openConnection();
            conn.setRequestProperty("Accept", ContentType.JSON);
            String creds = options.getHttpCredentialString();

            if (creds == null && settings.getDefaultCredentials() != null) {
                creds = settings.getDefaultCredentials().basicAuthHeader();
            }

            if (creds != null) {
                conn.setRequestProperty("Authorization", creds);
            }

            return conn;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static URL getURL(boolean secure, InetSocketAddress endpoint, String path) {
        String protocol = secure ? "https" : "http";
        try {
            return new URL(protocol + "://" + endpoint.getHostName() + ":" + endpoint.getPort() + path);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}