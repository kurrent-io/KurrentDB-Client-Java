package io.kurrent.dbclient;

import java.util.HashMap;
import java.util.Map;

class OptionsBase<T> {
    private Long deadline;
    private final OperationKind kind;
    private UserCredentials credentials;
    private boolean requiresLeader;
    private Map<String, String> headers = new HashMap<>();

    protected OptionsBase() {
        this(OperationKind.Regular);
    }

    protected OptionsBase(OperationKind kind) {
        this.kind = kind;
    }

    boolean hasUserCredentials() {
        return this.credentials != null;
    }

    String getHttpCredentialString() {
        return this.credentials.basicAuthHeader();
    }

    /**
     * Sets user credentials for the request
     * @param credentials
     * @see UserCredentials
     * @return updated options
     */
    @SuppressWarnings("unchecked")
    public T authenticated(UserCredentials credentials) {
        this.credentials = credentials;
        return (T) this;
    }

    /**
     * Sets user credentials for the request
     * @param login
     * @param password
     * @return updated options
     */
    public T authenticated(String login, String password) {
        return authenticated(new UserCredentials(login, password));
    }

    /**
     * Requires the request to be performed by the leader of the cluster.
     * @return updated options
     */
    public T requiresLeader() {
        return requiresLeader(true);
    }

    /**
     * Do not require the request to be performed by the leader of the cluster.
     * @return updated options
     */
    public T notRequireLeader() {
        return requiresLeader(false);
    }

    /**
     * If true, requires the request to be performed by the leader of the cluster.
     * @param value
     * @return updated options
     */
    @SuppressWarnings("unchecked")
    public T requiresLeader(boolean value) {
        this.requiresLeader = value;
        return (T) this;
    }

    /**
     * A length of time (in milliseconds) to use for gRPC deadlines.
     * @param durationInMs
     * @return
     */
    @SuppressWarnings("unchecked")
    public T deadline(long durationInMs) {
        deadline = durationInMs;

        return (T)this;
    }

    /**
     * Adds a custom HTTP header that will be added to the request.
     */
    @SuppressWarnings("unchecked")
    public T header(String key, String value) {
        headers.put(key, value);
        return (T)this;
    }

    Long getDeadline() {
        return deadline;
    }

    OperationKind getKind() {
        return kind;
    }

    boolean isLeaderRequired() {
        return this.requiresLeader;
    }

    UserCredentials getCredentials() {
        return this.credentials;
    }

    Map<String, String> getHeaders() {
        return this.headers;
    }
}