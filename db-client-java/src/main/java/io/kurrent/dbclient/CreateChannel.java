package io.kurrent.dbclient;

import java.net.InetSocketAddress;
import java.util.StringJoiner;
import java.util.UUID;

class CreateChannel implements Msg {
    final InetSocketAddress channel;
    final UUID previousId;

    public CreateChannel(UUID previousId) {
        this.channel = null;
        this.previousId = previousId;
    }

    public CreateChannel(UUID previousId, InetSocketAddress endpoint) {
        this.channel = endpoint;
        this.previousId = previousId;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CreateChannel.class.getSimpleName() + "[", "]")
                .add("endpoint=" + (channel != null ? channel.toString() : "NOT_SET"))
                .toString();
    }

    @Override
    public void accept(ConnectionService connectionService) {
        connectionService.createChannel(this.previousId, this.channel);
    }
}