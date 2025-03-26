package io.kurrent.dbclient;

import java.util.concurrent.CompletableFuture;

/**
 * Callback type when a checkpoint is reached.
 */
@FunctionalInterface
public interface Checkpointer {
    /**
     * Called everytime a checkpoint is reached.
     * @param subscription Subscription handle.
     * @param position Transaction log position.
     */
    CompletableFuture<Void> onCheckpoint(Subscription subscription, Position position);
}
