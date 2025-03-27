package io.kurrent.dbclient;

class ServerInfo {
    private final ServerVersion version;
    private final int features;

    ServerInfo(ServerVersion version, int features) {
        this.version = version;
        this.features = features;
    }

    public boolean supportFeature(int feature) {
        return (features & feature) != 0;
    }
    public ServerVersion getServerVersion() { return version; }
}
