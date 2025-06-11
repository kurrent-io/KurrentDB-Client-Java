package io.kurrent.dbclient;

public class AppendStreamFailure {
    private final io.kurrentdb.v2.AppendStreamFailure inner;

    AppendStreamFailure(io.kurrentdb.v2.AppendStreamFailure inner) {
        this.inner = inner;
    }

    public String getStreamName() {
        return this.inner.getStream();
    }

    public void visit(MultiAppendStreamErrorVisitor visitor) {
        if (this.inner.getErrorCase() == io.kurrentdb.v2.AppendStreamFailure.ErrorCase.WRONG_EXPECTED_REVISION) {
            visitor.onWrongExpectedRevision(this.inner.getWrongExpectedRevision().getStreamRevision());
            return;
        }

        if (this.inner.getErrorCase() == io.kurrentdb.v2.AppendStreamFailure.ErrorCase.ACCESS_DENIED) {
            visitor.onAccessDenied(this.inner.getAccessDenied().getReason());
        }

        if (this.inner.getErrorCase() == io.kurrentdb.v2.AppendStreamFailure.ErrorCase.STREAM_DELETED) {
            visitor.onStreamDeleted();
            return;
        }

       if (this.inner.getErrorCase() == io.kurrentdb.v2.AppendStreamFailure.ErrorCase.TRANSACTION_MAX_SIZE_EXCEEDED) {
            visitor.onTransactionMaxSizeExceeded(this.inner.getTransactionMaxSizeExceeded().getMaxSize());
            return;
       }

       throw new IllegalArgumentException("Append failure does not match any known error type");
    }
}
