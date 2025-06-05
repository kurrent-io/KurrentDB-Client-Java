package io.kurrent.dbclient.resolution;

import io.kurrent.dbclient.Endpoint;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DeprecatedNodeResolution implements NodeResolution {
    private final Endpoint address;

    public DeprecatedNodeResolution(Endpoint address) {
        this.address = address;
    }

    @Override
    public List<InetSocketAddress> resolve() {
        try {
            return Arrays.stream(InetAddress.getAllByName(address.getHost()))
                    .map(addr -> new InetSocketAddress(addr, address.getPort()))
                    .collect(Collectors.toList());
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}
