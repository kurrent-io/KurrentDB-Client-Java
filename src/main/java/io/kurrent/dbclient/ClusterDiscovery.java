package io.kurrent.dbclient;

import io.kurrent.dbclient.resolution.DeferredNodeResolution;
import io.kurrent.dbclient.resolution.DeprecatedNodeResolution;
import io.kurrent.dbclient.resolution.FixedSeedsNodeResolution;
import io.kurrent.dbclient.resolution.NodeResolution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class ClusterDiscovery implements Discovery {
    private static final Logger logger = LoggerFactory.getLogger(ClusterDiscovery.class);
    private final NodeSelector nodeSelector;
    private final NodeResolution resolution;

    ClusterDiscovery(KurrentDBClientSettings settings) {
        this.nodeSelector = new NodeSelector(settings.getNodePreference());

        if (settings.isDnsDiscover()) {
            if (settings.getFeatures().contains(ClientFeatureFlags.DNS_LOOKUP))
                this.resolution = new DeprecatedNodeResolution(settings.getHosts()[0]);
            else
                this.resolution = new DeferredNodeResolution(settings.getHosts()[0]);
        } else {
            this.resolution = new FixedSeedsNodeResolution(settings.getHosts());
        }
    }

    private static CompletableFuture<Optional<ClusterInfo.Member>> attemptDiscovery(NodeSelector selector, ConnectionState factory, InetSocketAddress seed) {
        factory.connect(seed);
        GossipClient client = new GossipClient(factory.getSettings(), factory.getCurrentChannel());
        return client.read().thenApply(info -> {
            if (factory.getLastConnectedEndpoint() != null) {
                info.getMembers().removeIf(member -> member.getHttpEndpoint().equals(factory.getLastConnectedEndpoint()));
            }

            return selector.determineBestFitNode(info);
        });
    }

    @Override
    public CompletableFuture<Void> run(ConnectionState state) {
        return CompletableFuture.runAsync(() -> discover(state));
    }

    void discover(ConnectionState state) {
        List<InetSocketAddress> candidates = resolution.resolve();

        if (candidates.size() > 1) {
            Collections.shuffle(candidates);

            if (state.getLastConnectedEndpoint() != null) {
                candidates.removeIf(candidate -> candidate.equals(state.getLastConnectedEndpoint()));
            }
        }

        for (InetSocketAddress seed : candidates) {
            logger.debug("Using seed node [{}] for cluster node discovery.", seed);
            try {
                Optional<ClusterInfo.Member> optionalMember = attemptDiscovery(this.nodeSelector, state, seed)
                        .get(state.getSettings().getGossipTimeout(), TimeUnit.MILLISECONDS);

                if (optionalMember.isPresent()) {
                    ClusterInfo.Member member = optionalMember.get();

                    if (!member.getHttpEndpoint().equals(state.getLastConnectedEndpoint())) {
                        state.connect(member.getHttpEndpoint());
                    }

                    logger.debug("Selected cluster node [{}] in state [{}] for connection attempt.", member.getHttpEndpoint(), member.getState());
                    return;
                }
            } catch (ExecutionException | TimeoutException e) {
                logger.error("Exception during the node selection process", e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        throw new NoClusterNodeFoundException();
    }
}