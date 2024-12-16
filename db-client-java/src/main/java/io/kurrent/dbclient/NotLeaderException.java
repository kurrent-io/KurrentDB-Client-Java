package io.kurrent.dbclient;

import java.net.InetSocketAddress;

/**
 * When a request needing a leader node was executed on a follower node.
 * In this case the connection will reconnect automatically to the leader node. However, the request causing that
 * exception needs to be retried if the user really wants it to be carried out.
 */
public class NotLeaderException extends Exception {
    private final InetSocketAddress leaderEndpoint;

    NotLeaderException(String host, int port) {
        leaderEndpoint = new InetSocketAddress(host, port);
    }

    public InetSocketAddress getLeaderEndpoint() {
        return leaderEndpoint;
    }
}
