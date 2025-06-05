package io.kurrent.dbclient;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

class SingleNodeDiscovery implements Discovery {
    private final Endpoint endpoint;

    SingleNodeDiscovery(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public CompletableFuture<Void> run(ConnectionState state) {
        return CompletableFuture.runAsync(() -> state.connect(new InetSocketAddress(this.endpoint.getHost(), this.endpoint.getPort())));
    }
}