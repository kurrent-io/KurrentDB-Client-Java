package io.kurrent.dbclient;

class OptionsWithPositionAndResolveLinkTosBase<T> extends OptionsWithBackPressure<T> {
    private StreamPosition<Position> position;

    protected OptionsWithPositionAndResolveLinkTosBase(OperationKind kind) {
        super(kind);
        this.position = StreamPosition.start();
    }

    protected OptionsWithPositionAndResolveLinkTosBase() {
        this(OperationKind.Regular);
    }

    StreamPosition<Position> getPosition() {
        return position;
    }

    /**
     * Starts from the beginning of the $all stream.
     */
    @SuppressWarnings("unchecked")
    public T fromStart() {
        this.position = StreamPosition.start();
        return (T)this;
    }

    /**
     * Starts from the end of the $all stream.
     */
    @SuppressWarnings("unchecked")
    public T fromEnd() {
        this.position = StreamPosition.end();
        return (T)this;
    }

    /**
     * Starts from the given transaction log position.
     * @param position transaction log position.
     */
    @SuppressWarnings("unchecked")
    public T fromPosition(Position position) {
        this.position = StreamPosition.position(position);
        return (T)this;
    }
}
