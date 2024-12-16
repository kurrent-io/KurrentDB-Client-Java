package com.eventstore.dbclient.databases;

import com.eventstore.dbclient.*;

public class ExternallyCreatedCluster implements Database {
    final private boolean secure;
    final private ClientTracker clientTracker;

    public ExternallyCreatedCluster(boolean secure) {
        this.secure = secure;
        this.clientTracker = new ClientTracker();
    }

    @Override
    public ConnectionSettingsBuilder defaultSettingsBuilder() {
        return KurrentDBClientSettings
                .builder()
                .dnsDiscover(true)
                .defaultCredentials("admin", "changeit")
                .addHost("localhost", 2_113)
                .tls(secure)
                .tlsVerifyCert(false)
                .maxDiscoverAttempts(50)
                .defaultDeadline(60_000);
    }

    @Override
    public ClientTracker getClientTracker() {
        return clientTracker;
    }

    @Override
    public void cleanup() {
        // Nothing do to here.
    }
}
