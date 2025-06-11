package io.kurrent.dbclient;

public interface MultiAppendStreamErrorVisitor {
    default void onWrongExpectedRevision(long streamRevision) {}
    default void onAccessDenied(String reason) {}
    default void onStreamDeleted() {}
    default void onTransactionMaxSizeExceeded(int maxSize) {}
}
