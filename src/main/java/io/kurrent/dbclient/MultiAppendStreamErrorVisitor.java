package io.kurrent.dbclient;

import io.kurrentdb.protocol.streams.v2.ErrorDetails;

public interface MultiAppendStreamErrorVisitor {
    default void onWrongExpectedRevision(long streamRevision) {}
    default void onAccessDenied(ErrorDetails.AccessDenied detail) {}
    default void onStreamDeleted() {}
    default void onTransactionMaxSizeExceeded(int maxSize) {}
}
