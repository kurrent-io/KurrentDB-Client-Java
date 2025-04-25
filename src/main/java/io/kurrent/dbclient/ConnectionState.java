package io.kurrent.dbclient;

import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.io.File;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

class ConnectionState {
    private final static Logger logger = LoggerFactory.getLogger(ConnectionState.class);
    private final static int MAX_INBOUND_MESSAGE_LENGTH = 17 * 1_024 * 1_024; // 17MiB
    private final KurrentDBClientSettings settings;
    private final SslContext sslContext;
    private InetSocketAddress previous;
    private ManagedChannel currentChannel;

    // Indicates if the current channel passed all the connection pre-requisites to be used by the user
    // Not exhaustive list includes:
    // * If we managed to get a gossip seed from the channel
    // * If we managed to read the server features (if not, it was a not found error then it's not fatal, just old node version)
    private boolean confirmedChannel;

    ConnectionState(KurrentDBClientSettings settings) {
        this.settings = settings;

        if (settings.isTls()) {
            try {
                SslContextBuilder sslContextBuilder = GrpcSslContexts.forClient();

                if (settings.getDefaultClientCertificate() != null)
                    sslContextBuilder.keyManager(
                            new File(settings.getDefaultClientCertificate().getCertFile()),
                            new File(settings.getDefaultClientCertificate().getKeyFile()));

                if (settings.getTlsCaFile() != null)
                    sslContextBuilder.trustManager(new File(settings.getTlsCaFile()));

                if (!settings.isTlsVerifyCert())
                    sslContextBuilder.trustManager(InsecureTrustManagerFactory.INSTANCE);

                this.sslContext = sslContextBuilder.build();
            } catch (SSLException e) {
                logger.error("Exception when creating SSL context", e);
                throw new RuntimeException(e);
            }
        } else {
            this.sslContext = null;
        }
    }

    InetSocketAddress getLastConnectedEndpoint() {
        return this.confirmedChannel ? this.previous : null;
    }

    void confirmChannel() {
        this.confirmedChannel = true;
    }

    ManagedChannel getCurrentChannel() {
        return this.currentChannel;
    }

    KurrentDBClientSettings getSettings() {
        return this.settings;
    }

    void connect(InetSocketAddress addr) {
        this.closeChannel();

        NettyChannelBuilder builder = NettyChannelBuilder
                .forAddress(addr)
                .maxInboundMessageSize(MAX_INBOUND_MESSAGE_LENGTH)
                .intercept(settings.getInterceptors());

        if (this.sslContext == null) {
            builder.usePlaintext();
        } else {
            builder.sslContext(this.sslContext);
        }

        if (settings.getKeepAliveTimeout() <= 0)
            builder.keepAliveTimeout(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        else
            builder.keepAliveTimeout(settings.getKeepAliveTimeout(), TimeUnit.MILLISECONDS);

        if (settings.getKeepAliveInterval() <= 0)
            builder.keepAliveTime(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        else
            builder.keepAliveTime(settings.getKeepAliveInterval(), TimeUnit.MILLISECONDS);

        this.currentChannel = builder.build();
        this.confirmedChannel = false;
        this.previous = addr;
    }

    private void closeChannel() {
        if (this.currentChannel != null) {
            try {
                logger.trace("Shutting down existing gRPC channel [{}]", this.currentChannel);
                boolean terminated = this.currentChannel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
                if (!terminated) {
                    this.currentChannel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
                }
                logger.trace("Successful shutdown of gRPC channel [{}]", this.currentChannel);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                this.currentChannel = null;
            }
        }
    }

    public void shutdown() {
        this.closeChannel();
    }

    public void clear() {
        this.previous = null;
        this.confirmedChannel = false;
    }
}