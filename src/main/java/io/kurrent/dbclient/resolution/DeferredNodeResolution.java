package io.kurrent.dbclient.resolution;

import io.kurrent.dbclient.Endpoint;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;

public class DeferredNodeResolution implements NodeResolution {
    private final Endpoint address;

    public DeferredNodeResolution(Endpoint address) {
        this.address = address;
    }

    @Override
    public List<InetSocketAddress> resolve() {
        return Collections.singletonList(new InetSocketAddress(address.getHost(), address.getPort()));
    }
}
