package io.kurrent.dbclient;

public class AppendStreamFailure {
    private final io.kurrentdb.protocol.streams.v2.AppendStreamFailure inner;

    AppendStreamFailure(io.kurrentdb.protocol.streams.v2.AppendStreamFailure inner) {
        this.inner = inner;
    }

    public String getStreamName() {
        return this.inner.getStream();
    }

    public void visit(MultiAppendStreamErrorVisitor visitor) {
        if (this.inner.getErrorCase() == io.kurrentdb.protocol.streams.v2.AppendStreamFailure.ErrorCase.STREAM_REVISION_CONFLICT) {
            visitor.onWrongExpectedRevision(this.inner.getStreamRevisionConflict().getStreamRevision());
            return;
        }

        if (this.inner.getErrorCase() == io.kurrentdb.protocol.streams.v2.AppendStreamFailure.ErrorCase.ACCESS_DENIED) {
            visitor.onAccessDenied(this.inner.getAccessDenied());
        }

        if (this.inner.getErrorCase() == io.kurrentdb.protocol.streams.v2.AppendStreamFailure.ErrorCase.STREAM_DELETED) {
            visitor.onStreamDeleted();
            return;
        }

       if (this.inner.getErrorCase() == io.kurrentdb.protocol.streams.v2.AppendStreamFailure.ErrorCase.TRANSACTION_MAX_SIZE_EXCEEDED) {
            visitor.onTransactionMaxSizeExceeded(this.inner.getTransactionMaxSizeExceeded().getMaxSize());
            return;
       }

       throw new IllegalArgumentException("Append failure does not match any known error type");
    }
}
