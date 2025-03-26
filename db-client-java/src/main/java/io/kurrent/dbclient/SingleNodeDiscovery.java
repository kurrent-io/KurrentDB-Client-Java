package io.kurrent.dbclient;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

class SingleNodeDiscovery implements Discovery {
    private final InetSocketAddress endpoint;

    SingleNodeDiscovery(InetSocketAddress endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public CompletableFuture<Void> run(ConnectionState state) {
        return CompletableFuture.runAsync(() -> state.connect(endpoint));
    }
}