package io.kurrent.dbclient;

import io.grpc.ClientInterceptor;
import io.kurrent.dbclient.serialization.KurrentDBClientSerializationSettings;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Set;

/**
 * Gathers all the settings related to a gRPC client with a KurrentDB database.
 * <i>KurrentDBClientSettings</i> supports a wide range of settings. If a setting is not mentioned in the connection
 * string, that setting default value is used.
 *
 * <ul>
 *     <li>maxDiscoverAttempts: 3</li>
 *     <li>discoveryInterval: 500</li>
 *     <li>gossipTimeout: 3</li>
 *     <li>nodePreference: leader</li>
 *     <li>tls: true</li>
 *     <li>tlsVerifyCert: true</li>
 *     <li>keepAliveTimeout: 10000</li>
 *     <li>keepAliveInterval: 10000</li>
 * </ul>
 */
public class KurrentDBClientSettings {
    private final boolean dnsDiscover;
    private final int maxDiscoverAttempts;
    private final int discoveryInterval;
    private final int gossipTimeout;
    private final NodePreference nodePreference;
    private final boolean tls;
    private final boolean tlsVerifyCert;
    private final UserCredentials defaultCredentials;
    private final ClientCertificate defaultClientCertificate;
    private final Endpoint[] hosts;
    private final long keepAliveTimeout;
    private final long keepAliveInterval;
    private final Long defaultDeadline;
    private final List<ClientInterceptor> interceptors;
    private final String tlsCaFile;
    private final Set<String> features;
    private final KurrentDBClientSerializationSettings serializationSettings;

    /**
     * If the dns discovery is enabled.
     */
    public boolean isDnsDiscover() {
        return dnsDiscover;
    }

    /**
     * How many times to attempt connection before throwing.
     */
    public int getMaxDiscoverAttempts() {
        return maxDiscoverAttempts;
    }

    /**
     * How long to wait before retrying a new discovery process (in milliseconds).
     */
    public int getDiscoveryInterval() {
        return discoveryInterval;
    }

    /**
     * How long to wait for the gossip request to time out (in seconds).
     */
    public int getGossipTimeout() {
        return gossipTimeout;
    }

    /**
     * Preferred node type when picking a node within a cluster.
     * @see NodePreference
     * @return selected node preference.
     */
    public NodePreference getNodePreference() {
        return nodePreference;
    }

    /**
     * If secure mode is enabled.
     */
    public boolean isTls() {
        return tls;
    }

    /**
     * If secure mode is enabled, is certificate verification enabled.
     */
    public boolean isTlsVerifyCert() {
        return tlsVerifyCert;
    }

    /**
     * Default credentials used to authenticate requests.
     * User credentials take precedence over any configured {@link ClientCertificate}.
     * @see UserCredentials
     * @return default credentials null if not defined
     */
    public UserCredentials getDefaultCredentials() {
        return defaultCredentials;
    }

    /**
     * Default certificate for user authentication.
     * If any {@link UserCredentials} are configured, the server will ignore the user certificate.
     * @see ClientCertificate
     * @return user certificate, otherwise null.
     */
    public ClientCertificate getDefaultClientCertificate() {
        return defaultClientCertificate;
    }

    /**
     * The list of endpoints that the client uses to connect.
     * @return hosts to connect to.
     */
    public Endpoint[] getHosts() {
        return hosts;
    }

    /**
     * The amount of time (in milliseconds) the sender of the keepalive ping waits for an acknowledgement.
     * If it does not receive an acknowledgement within this time, it will close the channel.
     */
    public long getKeepAliveTimeout() {
        return keepAliveTimeout;
    }

    /**
     * The amount of time (in milliseconds) to wait after which a keepalive ping is sent on the transport.
     * Use -1 to disable.
     * @return keepalive value in milliseconds.
     */
    public long getKeepAliveInterval() {
        return keepAliveInterval;
    }

    /**
     * An optional length of time (in milliseconds) to use for gRPC deadlines.
     * @return deadline value in milliseconds or null if not set.
     */
    public Long getDefaultDeadline() {
        return defaultDeadline;
    }

    /**
     * Registered gRPC interceptors.
     * @return list of registered gRPC client.
     */
    public List<ClientInterceptor> getInterceptors() {
        return interceptors;
    }

    /**
     * Client certificate for secure connection.
     * @return tls CA file if it was provided, otherwise null.
     */
    public String getTlsCaFile() {
        return tlsCaFile;
    }

    /**
     * Feature flags
     */
    public Set<String> getFeatures() { return features; }

    /**
     * Provides configuration options for messages serialization and deserialization in the KurrentDB client.
     * If null, default settings are used.
     */
    public KurrentDBClientSerializationSettings getSerializationSettings() { return serializationSettings; }

    KurrentDBClientSettings(
            boolean dnsDiscover,
            int maxDiscoverAttempts,
            int discoveryInterval,
            int gossipTimeout,
            NodePreference nodePreference,
            boolean tls,
            boolean tlsVerifyCert,
            UserCredentials defaultCredentials,
            ClientCertificate defaultClientCertificate,
            Endpoint[] hosts,
            long keepAliveTimeout,
            long keepAliveInterval,
            Long defaultDeadline,
            List<ClientInterceptor> interceptors,
            String tlsCaFile,
            Set<String> features,
            KurrentDBClientSerializationSettings serializationSettings
    ) {
        this.dnsDiscover = dnsDiscover;
        this.maxDiscoverAttempts = maxDiscoverAttempts;
        this.discoveryInterval = discoveryInterval;
        this.gossipTimeout = gossipTimeout;
        this.nodePreference = nodePreference;
        this.tls = tls;
        this.tlsVerifyCert = tlsVerifyCert;
        this.defaultCredentials = defaultCredentials;
        this.defaultClientCertificate = defaultClientCertificate;
        this.hosts = hosts;
        this.keepAliveTimeout = keepAliveTimeout;
        this.keepAliveInterval = keepAliveInterval;
        this.defaultDeadline = defaultDeadline;
        this.interceptors = interceptors;
        this.tlsCaFile = tlsCaFile;
        this.features = features;
        this.serializationSettings = serializationSettings;
    }

    /**
     * Return a connection settings builder configured with default properties.
     * @see ConnectionSettingsBuilder
     * @return a builder.
     */
    public static ConnectionSettingsBuilder builder() {
        return new ConnectionSettingsBuilder();
    }
}
