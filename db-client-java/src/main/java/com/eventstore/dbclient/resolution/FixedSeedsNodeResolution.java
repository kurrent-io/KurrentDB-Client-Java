package com.eventstore.dbclient.resolution;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FixedSeedsNodeResolution implements NodeResolution {
    private final InetSocketAddress[] seeds;

    public FixedSeedsNodeResolution(InetSocketAddress[] seeds) {
        this.seeds = seeds;
    }

    @Override
    public List<InetSocketAddress> resolve() {
        return new ArrayList<>(Arrays.asList(seeds));
    }
}
