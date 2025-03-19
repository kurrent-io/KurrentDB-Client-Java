package io.kurrent.dbclient;

import io.kurrent.dbclient.serialization.MessageSerializer;
import io.kurrent.dbclient.serialization.MessageSerializerBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Security;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class KurrentDBClientBase {
    final Logger logger = LoggerFactory.getLogger(KurrentDBClientBase.class);
    final private GrpcClient client;
    final protected MessageSerializer serializer;

    KurrentDBClientBase(KurrentDBClientSettings settings) {
        Discovery discovery;

        if (settings.getHosts().length == 1 && !settings.isDnsDiscover()) {
            discovery = new SingleNodeDiscovery(settings.getHosts()[0]);
        } else {
            discovery = new ClusterDiscovery(settings);
        }

        // Required to instruct Netty to use BouncyCastle for TLS
        Security.addProvider(new BouncyCastleProvider());
        ConnectionService service = new ConnectionService(settings, discovery);
        this.client = service.getHandle();

        CompletableFuture.runAsync(service, createConnectionLoopExecutor());

        serializer = MessageSerializerBuilder.get(settings.getSerializationSettings());
    }
    private Executor createConnectionLoopExecutor() {
        return Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "esdb-client-" + UUID.randomUUID());
            thread.setDaemon(true);
            return thread;
        });
    }


    /**
     * Closes a connection and cleans all its allocated resources.
     */
    public CompletableFuture<Void> shutdown() {
        return this.client.shutdown();
    }

    /**
     * Checks if this client instance has been shutdown.
     * After shutdown a client instance can no longer process new operations and
     * a new client instance has to be created.
     * @return {@code true} if client instance has been shutdown.
     */
    public boolean isShutdown() {
        return this.client.isShutdown();
    }

    public CompletableFuture<Optional<ServerVersion>> getServerVersion() {
        return client.getServerVersion();
    }

    GrpcClient getGrpcClient() {
        return client;
    }
}
