package io.kurrent.dbclient;

public final class ClientFeatureFlags {
    /**
     * Enables direct DNS name resolution, retrieving all IP addresses associated with a given hostname. This
     * functionality was initially implemented to support the now-deprecated TCP API. It is particularly useful in
     * scenarios involving clusters, where node discovery is enabled.
     */
    public static final String DNS_LOOKUP = "dns-lookup";
}
