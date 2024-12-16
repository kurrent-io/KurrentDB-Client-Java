package io.kurrent.dbclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Main gRPC connection management service.
 */
class ConnectionService implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(ConnectionService.class);
    private final GrpcClient client;
    private final AtomicBoolean closed;
    private final LinkedBlockingQueue<Msg> queue;
    private final Discovery discovery;
    private final KurrentDBClientSettings settings;
    private final ConnectionState connection;
    private UUID channelId = UUID.randomUUID();
    private ServerInfo serverInfo = null;

    ConnectionService(KurrentDBClientSettings settings, Discovery discovery) {
        this.settings = settings;
        this.discovery = discovery;
        this.connection = new ConnectionState(settings);
        this.queue = new LinkedBlockingQueue<>();
        this.closed = new AtomicBoolean(false);
        this.client = new GrpcClient(settings, this.closed, this.queue);
    }

    GrpcClient getHandle() {
        return this.client;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Msg msg = this.queue.take();
                logger.debug("Current msg: {}", msg);
                msg.accept(this);
            } catch (Exception e) {
                if (!this.closed.get())
                    this.forceExit(e);

                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            logger.error("Thread is interrupted", e);
            throw new RuntimeException(e);
        }
    }

    private boolean loadServerFeatures() {
        try {
            this.serverInfo = ServerFeatures
                    .getSupportedFeatures(this.settings, this.connection.getCurrentChannel())
                    .orElse(null);

            return true;
        } catch (ServerFeatures.RetryableException e) {
            logger.warn("An exception happened when fetching server supported features. Retrying connection attempt.", e);
            return false;
        }
    }

    private void drainPendingRequests() {
        logger.debug("Draining pending requests...");
        ArrayList<Msg> msgs = new ArrayList<>();
        this.queue.drainTo(msgs);

        for (Msg msg : msgs) {
            msg.accept(this);
        }

        logger.debug("Drainage completed successfully");
    }

    private void forceExit(Exception e) {
        this.shutdown(new Shutdown(x -> {
            if (e != null)
                logger.error("Unexpected error", e);
        }));
    }

    private void forceExit() {
        this.forceExit(null);
    }

    public void createChannel(UUID previousId, InetSocketAddress candidate) {
        if (this.closed.get()) {
            logger.warn("Channel creation request ignored, the connection to endpoint [{}] is already closed", this.connection.getLastConnectedEndpoint());
            return;
        }

        if (!this.channelId.equals(previousId)) {
            logger.debug("Skipping connection attempt as new connection to endpoint [{}] has already been created.", this.connection.getLastConnectedEndpoint());
            return;
        }

        for (int attempts = 1; attempts <= this.settings.getMaxDiscoverAttempts() + 1; attempts++) {
            if (attempts > this.settings.getMaxDiscoverAttempts()) {
                logger.error("Maximum discovery attempt count reached: {}", settings.getMaxDiscoverAttempts());
                this.forceExit();
                return;
            }

            logger.debug("Start connection attempt ({}/{})", attempts, settings.getMaxDiscoverAttempts());

            // Node selection.
            if (candidate != null) {
                this.connection.connect(candidate);
                logger.debug("Prepared channel to proposed leader candidate [{}]", candidate);
            } else {
                try {
                    // TODO - Should we consider a discovery timeout?
                    this.discovery.run(this.connection).get();

                    if (this.loadServerFeatures()) {
                        this.channelId = UUID.randomUUID();
                        this.connection.confirmChannel();
                        logger.info("Connection to endpoint [{}] created successfully", this.connection.getLastConnectedEndpoint());
                        break;
                    }
                } catch (InterruptedException e) {
                    forceExit(e);
                } catch (ExecutionException e) {
                    logger.error("Error when running discovery process", e);
                    // In this case, it's better to clear any connection internal state like the previous selected node
                    // that has failed. It's possible that node might still be the best candidate if it manages to
                    // recover in the meantime.
                    this.connection.clear();
                }
            }

            // In case a candidate was provided, but we failed to connect to it.
            // We force a new discovery process in that case.
            candidate = null;
            this.sleep(this.settings.getDiscoveryInterval());
        }
    }

    public void process(RunWorkItem args) {
        if (this.closed.get()) {
            logger.warn("Receive a command request but the connection to endpoint [{}] is already closed", this.connection.getLastConnectedEndpoint());
            args.getItem().accept(null, new ConnectionShutdownException());
            return;
        }

        // It's possible we haven't connected yet.
        if (this.connection.getCurrentChannel() == null) {
            logger.debug("Channel is not resolved yet, connecting...");

            try {
                this.createChannel(this.channelId, null);
            } catch (RuntimeException e) {
                args.getItem().accept(null, e);
                throw e;
            }
        }

        WorkItemArgs workArgs = new WorkItemArgs(
                this.channelId,
                this.connection.getCurrentChannel(),
                this.connection.getLastConnectedEndpoint(),
                this.serverInfo);

        args.getItem().accept(workArgs, null);
    }

    public void shutdown(Shutdown args) {
        if (this.closed.get()) {
            args.complete();
            return;
        }

        logger.info("Received a shutdown request, closing connection to endpoint [{}]", this.connection.getLastConnectedEndpoint());
        this.closed.set(true);
        this.connection.shutdown();
        this.drainPendingRequests();
        logger.info("Connection to endpoint [{}] was closed successfully", this.connection.getLastConnectedEndpoint());
        args.complete();
        throw new ConnectionShutdownException();
    }
}