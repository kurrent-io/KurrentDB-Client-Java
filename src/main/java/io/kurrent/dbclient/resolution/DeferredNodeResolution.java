package io.kurrent.dbclient.resolution;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;

public class DeferredNodeResolution implements NodeResolution {
    private final InetSocketAddress address;

    public DeferredNodeResolution(InetSocketAddress address) {
        this.address = address;
    }

    @Override
    public List<InetSocketAddress> resolve() {
        return Collections.singletonList(address);
    }
}
