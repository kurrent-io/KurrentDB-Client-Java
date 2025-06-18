package io.kurrent.dbclient;


import io.grpc.ClientInterceptor;
import io.kurrent.dbclient.serialization.KurrentDBClientSerializationSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;

/**
 * Utility to create client settings programmatically.
 */
public class ConnectionSettingsBuilder {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionSettingsBuilder.class);
    private static final Set<String> SUPPORTED_PROTOCOLS = new HashSet<>(Arrays.asList(
            "esdb", "esdb+discover", "kurrent", "kurrent+discover", "kdb", "kdb+discover", "kurrentdb", "kurrentdb+discover"
    ));

    private boolean _dnsDiscover = false;
    private int _maxDiscoverAttempts = 3;
    private int _discoveryInterval = 500;
    private int _gossipTimeout = 3000;
    private NodePreference _nodePreference = NodePreference.LEADER;
    private boolean _tls = true;
    private boolean _tlsVerifyCert = true;
    private UserCredentials _defaultCredentials;
    private ClientCertificate _defaultClientCertificate;
    private LinkedList<Endpoint> _hosts = new LinkedList<>();
    private long _keepAliveTimeout = Consts.DEFAULT_KEEP_ALIVE_TIMEOUT_IN_MS;
    private long _keepAliveInterval = Consts.DEFAULT_KEEP_ALIVE_INTERVAL_IN_MS;
    private Long _defaultDeadline = null;
    private List<ClientInterceptor> _interceptors = new ArrayList<>();
    private String _tlsCaFile = null;
    private Set<String> _features = new HashSet<>();
    private KurrentDBClientSerializationSettings _serializationSettings;

    ConnectionSettingsBuilder() {}

    /**
     * Returns configured connection settings.
     * @see KurrentDBClientSettings
     * @return configured settings.
     */
    public KurrentDBClientSettings buildConnectionSettings() {
        return new KurrentDBClientSettings(_dnsDiscover,
                _maxDiscoverAttempts,
                _discoveryInterval,
                _gossipTimeout,
                _nodePreference,
                _tls,
                _tlsVerifyCert,
                _defaultCredentials,
                _defaultClientCertificate,
                _hosts.toArray(new Endpoint[0]),
                _keepAliveTimeout,
                _keepAliveInterval,
                _defaultDeadline,
                _interceptors,
                _tlsCaFile,
                _features,
                _serializationSettings
        );
    }

    /**
     * If DNS node discovery is enabled.
     */
    public ConnectionSettingsBuilder dnsDiscover(boolean dnsDiscover) {
        this._dnsDiscover = dnsDiscover;
        return this;
    }

    /**
     * How many times to attempt connection before throwing.
     */
    public ConnectionSettingsBuilder maxDiscoverAttempts(int maxDiscoverAttempts) {
        this._maxDiscoverAttempts = maxDiscoverAttempts;
        return this;
    }

    /**
     * How long to wait before retrying a new discovery process (in milliseconds).
     */
    public ConnectionSettingsBuilder discoveryInterval(int discoveryInterval) {
        this._discoveryInterval = discoveryInterval;
        return this;
    }

    /**
     * How long to wait for the gossip request to timeout (in seconds).
     */
    public ConnectionSettingsBuilder gossipTimeout(int gossipTimeout) {
        this._gossipTimeout = gossipTimeout;
        return this;
    }

    /**
     * Preferred node type when picking a node within a cluster.
     */
    public ConnectionSettingsBuilder nodePreference(NodePreference nodePreference) {
        this._nodePreference = nodePreference;
        return this;
    }

    /**
     * If secure mode is enabled.
     */
    public ConnectionSettingsBuilder tls(boolean tls) {
        this._tls = tls;
        return this;
    }

    /**
     * If secure mode is enabled, is certificate verification enabled.
     */
    public ConnectionSettingsBuilder tlsVerifyCert(boolean tlsVerifyCert) {
        this._tlsVerifyCert = tlsVerifyCert;
        return this;
    }

    /**
     * Default credentials used to authenticate requests.
     */
    public ConnectionSettingsBuilder defaultCredentials(String username, String password) {
        this._defaultCredentials = new UserCredentials(username, password);
        return this;
    }

    /**
     * Default credentials used to authenticate requests.
     */
    public ConnectionSettingsBuilder defaultCredentials(UserCredentials defaultCredentials) {
        this._defaultCredentials = defaultCredentials;
        return this;
    }

    /**
     * Client certificate used for user authentication.
     */
    public ConnectionSettingsBuilder defaultClientCertificate(String clientCertFile, String clientKeyFile) {
        this._defaultClientCertificate = new ClientCertificate(clientCertFile, clientKeyFile);
        return this;
    }

    /**
     * Client certificate used for user authentication.
     */
    public ConnectionSettingsBuilder defaultClientCertificate(ClientCertificate defaultClientCertificate) {
        this._defaultClientCertificate = defaultClientCertificate;
        return this;
    }

    /**
     * Adds an endpoint the client will use to connect.
     */
    public ConnectionSettingsBuilder addHost(String host, int port) {
        this._hosts.add(new Endpoint(host, port));
        return this;
    }

    /**
     * Adds an endpoint the client will use to connect.
     */
    public ConnectionSettingsBuilder addHost(InetSocketAddress host) {
        this._hosts.push(new Endpoint(host.getHostName(), host.getPort()));
        return this;
    }

    /**
     * The amount of time (in milliseconds) the sender of the keepalive ping waits for an acknowledgement.
     */
    public ConnectionSettingsBuilder keepAliveTimeout(long value) {
        if (value >= 0 && value < Consts.DEFAULT_KEEP_ALIVE_TIMEOUT_IN_MS) {
            logger.warn("Specified keepAliveTimeout of {} is less than recommended {}", value, Consts.DEFAULT_KEEP_ALIVE_TIMEOUT_IN_MS);
        }

        if (value == -1)
            value = Long.MAX_VALUE;

        this._keepAliveTimeout = value;

        return this;
    }

    /**
     * The amount of time (in milliseconds) to wait after which a keepalive ping is sent on the transport.
     */
    public ConnectionSettingsBuilder keepAliveInterval(long value) {
        if (value >= 0 && value < Consts.DEFAULT_KEEP_ALIVE_INTERVAL_IN_MS) {
            logger.warn("Specified keepAliveInterval of {} is less than recommended {}", value, Consts.DEFAULT_KEEP_ALIVE_INTERVAL_IN_MS);
        }

        if (value == -1)
            value = Long.MAX_VALUE;

        this._keepAliveInterval = value;

        return this;
    }

    /**
     * An optional length of time (in milliseconds) to use for gRPC deadlines.
     */
    public ConnectionSettingsBuilder defaultDeadline(long value) {
        this._defaultDeadline = value;
        return this;
    }

    /**
     * Register a gRPC interceptor every time a new gRPC channel is created.
     * @param interceptor
     */
    public ConnectionSettingsBuilder addInterceptor(ClientInterceptor interceptor) {
        this._interceptors.add(interceptor);
        return this;
    }

    /**
     * Client certificate for secure connection. Not required for enabling secure connection. Useful for self-signed
     * certificate that are not installed on the system trust store.
     * @param filepath path to a certificate file.
     */
    public ConnectionSettingsBuilder tlsCaFile(String filepath) {
        this._tlsCaFile = filepath;
        return this;
    }

    /**
     * Add feature flags.
     */
    public ConnectionSettingsBuilder features(String... features) {
        this._features.addAll(Arrays.asList(features));
        return this;
    }

    /**
     * Add feature flag.
     */
    public ConnectionSettingsBuilder feature(String feature) {
        this._features.add(feature);
        return this;
    }

    /**
     * Provides configuration options for messages serialization and deserialization in the KurrentDB client.
     * If null, default settings are used.
     */
    public ConnectionSettingsBuilder serialization(KurrentDBClientSerializationSettings serializationSettings) {
        this._serializationSettings = serializationSettings;
        return this;
    }

    void parseGossipSeed(String host) {
        String[] hostParts = host.split(":");

        switch (hostParts.length) {
            case 1:
                addHost(host, 2_113);
                break;
            case 2:
                try {
                    addHost(hostParts[0], Short.parseShort(hostParts[1]));
                } catch (NumberFormatException e) {
                    throw new RuntimeException(String.format("Invalid port number format: %s", hostParts[1]));
                }
                break;
            default:
                throw new RuntimeException(String.format("Invalid gossip seed format: %s", host));
        }
    }

    static KurrentDBClientSettings parseFromUrl(ConnectionSettingsBuilder builder, URL url) {
        if (!SUPPORTED_PROTOCOLS.contains(url.getProtocol()))
            throw new RuntimeException(String.format("Unknown URL scheme: %s", url.getProtocol()));

        if (url.getProtocol().contains("esdb"))
            logger.warn("The 'esdb' protocol is deprecated. Please use 'kurrentdb' instead.");

        builder.dnsDiscover(url.getProtocol().endsWith("+discover"));

        if (url.getUserInfo() != null && !url.getUserInfo().isEmpty()) {
            String[] splits = url.getUserInfo().split(":", 2);

            if (splits.length > 1) {
                try {
                    builder.defaultCredentials(URLDecoder.decode(splits[0], "utf-8"), URLDecoder.decode(splits[1], "utf-8"));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
            else
                builder.defaultCredentials(splits[0], "");
        }

        if (builder._hosts.isEmpty() && !url.getPath().isEmpty() && !url.getPath().equals("/"))
            throw new RuntimeException(String.format("Unsupported URL path: %s", url.getPath()));

        if (builder._hosts.isEmpty() && url.getHost().isEmpty())
            throw new RuntimeException("Connection string doesn't have an host");

        if (builder._hosts.isEmpty()) {
            if (!url.getHost().contains(",")) {
                builder.addHost(url.getHost(), url.getPort() == -1 ? 2_113 : url.getPort());
            } else {
                for (String hostPart : url.getHost().split(",")) {
                    builder.parseGossipSeed(hostPart);
                }
            }
        }

        if (url.getQuery() == null)
            return builder.buildConnectionSettings();

        String userCertFile = null;
        String userKeyFile = null;
        for (String param : url.getQuery().split("&")) {
            String[] entry = param.split("=");

            if (entry.length <= 1)
                continue;

            String value = entry[1].toLowerCase();
            switch (entry[0].toLowerCase()) {
                case "nodepreference":
                    switch (value) {
                        case "leader":
                            builder._nodePreference = NodePreference.LEADER;
                            break;
                        case "follower":
                            builder._nodePreference = NodePreference.FOLLOWER;
                            break;
                        case "readonlyreplica":
                            builder._nodePreference = NodePreference.READ_ONLY_REPLICA;
                            break;
                        case "random":
                            builder._nodePreference = NodePreference.RANDOM;
                            break;
                        default:
                            throw new RuntimeException(String.format("Unsupported node preference '%s'", value));
                    }
                    break;

                case "maxdiscoverattempts":
                    try {
                        int parsedValue = Integer.parseInt(value);

                        if (parsedValue < 0)
                            invalidParamFormat(entry[0], value);

                        builder._maxDiscoverAttempts = parsedValue;
                    } catch (NumberFormatException e) {
                        invalidParamFormat(entry[0], value);
                    }
                    break;

                case "discoveryinterval":
                    try {
                        int parsedValue = Integer.parseInt(value);

                        if (parsedValue < 0)
                            invalidParamFormat(entry[0], value);

                        builder._discoveryInterval = parsedValue;
                    } catch (NumberFormatException e) {
                        invalidParamFormat(entry[0], value);
                    }
                    break;

                case "gossiptimeout":
                    try {
                        int parsedValue = Integer.parseInt(value);

                        if (parsedValue < 0)
                            invalidParamFormat(entry[0], value);

                        builder._gossipTimeout = parsedValue;
                    } catch (NumberFormatException e) {
                        invalidParamFormat(entry[0], value);
                    }
                    break;

                case "dnsdiscover":
                    if (!value.equals("true") && !value.equals("false"))
                        invalidParamFormat(entry[0], value);

                    builder._dnsDiscover = value.equals("true");
                    break;

                case "tls":
                    if (!value.equals("true") && !value.equals("false"))
                        invalidParamFormat(entry[0], value);

                    builder._tls = value.equals("true");
                    break;

                case "tlsverifycert":
                    if (!value.equals("true") && !value.equals("false"))
                        invalidParamFormat(entry[0], value);

                    builder._tlsVerifyCert = value.equals("true");
                    break;

                case "keepalivetimeout":
                    try {
                        long parsedValue = Long.parseLong(value);
                        if (parsedValue >= 0 && parsedValue < Consts.DEFAULT_KEEP_ALIVE_TIMEOUT_IN_MS)
                            logger.warn("Specified keepAliveTimeout of {} is less than recommended {}", parsedValue, Consts.DEFAULT_KEEP_ALIVE_TIMEOUT_IN_MS);

                        if (parsedValue < -1)
                            invalidParamFormat(entry[0], value);

                        if (parsedValue == -1)
                            parsedValue = Long.MAX_VALUE;

                        builder._keepAliveTimeout = parsedValue;
                    } catch (NumberFormatException e) {
                        invalidParamFormat(entry[0], value);
                    }
                    break;

                case "keepaliveinterval":
                    try {
                        long parsedValue = Long.parseLong(value);
                        if (parsedValue >= 0 && parsedValue < Consts.DEFAULT_KEEP_ALIVE_INTERVAL_IN_MS)
                            logger.warn("Specified keepAliveInterval of {} is less than recommended {}", parsedValue, Consts.DEFAULT_KEEP_ALIVE_INTERVAL_IN_MS);

                        if (parsedValue < -1)
                            invalidParamFormat(entry[0], value);

                        if (parsedValue == -1)
                            parsedValue = Long.MAX_VALUE;

                        builder._keepAliveInterval = parsedValue;
                    } catch (NumberFormatException e) {
                        invalidParamFormat(entry[0], value);
                    }
                    break;

                case "defaultdeadline":
                    try {
                        long parsedValue = Long.parseLong(value);

                        if (parsedValue <= 0)
                            invalidParamFormat(entry[0], value);

                        builder._defaultDeadline = parsedValue;
                    } catch (NumberFormatException e) {
                        invalidParamFormat(entry[0], value);
                    }
                    break;

                case "tlscafile":
                    if (entry[1].isEmpty())
                        invalidParamFormat(entry[0], entry[1]);

                    builder._tlsCaFile = entry[1];
                    break;

                case "usercertfile":
                    if (entry[1].isEmpty())
                        invalidParamFormat(entry[0], entry[1]);

                    userCertFile = entry[1];
                    break;

                case "userkeyfile":
                    if (entry[1].isEmpty())
                        invalidParamFormat(entry[0], entry[1]);

                    userKeyFile = entry[1];
                    break;

                case "feature":
                    builder._features.add(value);
                    break;

                default:
                    logger.warn(String.format("Unknown setting '%s' is ignored", entry[0]));
                    break;
            }
        }

        if (userCertFile != null ^ userKeyFile != null)
            throw new RuntimeException("Invalid user certificate settings. Both 'userCertFile' and 'userKeyFile' must be provided.");

        if (userCertFile != null)
            builder.defaultClientCertificate(userCertFile, userKeyFile);

        return builder.buildConnectionSettings();
    }

    static void invalidParamFormat(String param, String value) {
        throw new RuntimeException(String.format("Invalid '%s' value format: '%s'", param, value));
    }
}
