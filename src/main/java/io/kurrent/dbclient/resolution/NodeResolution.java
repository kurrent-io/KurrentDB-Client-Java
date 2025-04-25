package io.kurrent.dbclient.resolution;

import java.net.InetSocketAddress;
import java.util.List;

public interface NodeResolution {
    List<InetSocketAddress> resolve();
}
