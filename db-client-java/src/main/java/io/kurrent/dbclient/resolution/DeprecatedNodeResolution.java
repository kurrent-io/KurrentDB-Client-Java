package io.kurrent.dbclient.resolution;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DeprecatedNodeResolution implements NodeResolution {
    private final InetSocketAddress address;

    public DeprecatedNodeResolution(InetSocketAddress address) {
        this.address = address;
    }

    @Override
    public List<InetSocketAddress> resolve() {
        try {
            return Arrays.stream(InetAddress.getAllByName(address.getHostName()))
                    .map(addr -> new InetSocketAddress(addr, address.getPort()))
                    .collect(Collectors.toList());
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}
