package io.kurrent.dbclient;

class OptionsWithResolveLinkTosBase<T> extends OptionsBase<T> {
    private boolean resolveLinkTos;

    protected OptionsWithResolveLinkTosBase(OperationKind kind) {
        super(kind);
        this.resolveLinkTos = false;
    }

    protected OptionsWithResolveLinkTosBase() {
        this(OperationKind.Regular);
    }

    boolean shouldResolveLinkTos() {
        return this.resolveLinkTos;
    }

    /**
     * Whether the subscription should resolve linkTo events to their linked events. Default: false.
     */
    @SuppressWarnings("unchecked")
    public T resolveLinkTos(boolean value) {
        this.resolveLinkTos = value;
        return (T)this;
    }

    /**
     * Resolve linkTo events to their linked events.
     */
    public T resolveLinkTos() {
        return this.resolveLinkTos(true);
    }

    /**
     * Don't resolve linkTo events to their linked events.
     */
    public T notResolveLinkTos() {
        return this.resolveLinkTos(false);
    }
}
