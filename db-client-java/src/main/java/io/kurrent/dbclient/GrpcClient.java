package io.kurrent.dbclient;

import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

class GrpcClient {
    private static final Logger logger = LoggerFactory.getLogger(GrpcClient.class);
    private final AtomicBoolean closed;
    private final LinkedBlockingQueue<Msg> queue;
    private final KurrentDBClientSettings settings;

    GrpcClient(KurrentDBClientSettings settings, AtomicBoolean closed, LinkedBlockingQueue<Msg> queue) {
        this.settings = settings;
        this.closed = closed;
        this.queue = queue;
    }

    public boolean isShutdown() {
        return this.closed.get();
    }

    private void push(Msg msg) {
        try {
            if (this.closed.get()) {
                if (msg instanceof RunWorkItem) {
                    RunWorkItem args = (RunWorkItem) msg;
                    args.reportError(new ConnectionShutdownException());
                }

                if (msg instanceof Shutdown) {
                    ((Shutdown) msg).complete();
                }

                return;
            }

            this.queue.put(msg);
            logger.debug("Scheduled msg: {}", msg);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<WorkItemArgs> getWorkItemArgs() {
        final CompletableFuture<WorkItemArgs> result = new CompletableFuture<>();

        this.push(new RunWorkItem(UUID.randomUUID(), (args, error) -> {
            if (error != null) {
                result.completeExceptionally(error);
                return;
            }

            result.complete(args);
        }));

        return result;
    }

    public CompletableFuture<Optional<ServerVersion>> getServerVersion() {
        return runWithArgs(args -> CompletableFuture.completedFuture(args.getServerVersion()));
    }

    public <A> CompletableFuture<A> run(Function<ManagedChannel, CompletableFuture<A>> action) {
        return runWithArgs(args -> action.apply(args.getChannel()));
    }

    public <A> CompletableFuture<A> runWithArgs(Function<WorkItemArgs, CompletableFuture<A>> action) {
        return getWorkItemArgs().thenComposeAsync((args) -> {
            return action.apply(args).handleAsync((outcome, e) -> {
                if (e != null) {
                    if (e instanceof CompletionException) {
                        e = e.getCause();
                    }

                    if (e instanceof NotLeaderException) {
                        NotLeaderException ex = (NotLeaderException) e;
                        // TODO - Currently we don't retry on not leader exception but we might consider
                        // allowing this on a case-by-case basis.
                        this.push(new CreateChannel(args.getId(), ex.getLeaderEndpoint()));
                    }

                    if (e instanceof StatusRuntimeException) {
                        StatusRuntimeException ex = (StatusRuntimeException) e;

                        if (ex.getStatus().getCode().equals(Status.Code.UNAVAILABLE)) {
                            this.push(new CreateChannel(args.getId()));
                        }
                    }

                    logger.debug("RunWorkItem[{}] completed exceptionally: {}", args.getId(), e.toString());

                    if (e instanceof RuntimeException)
                        throw (RuntimeException)e;
                    else
                        throw new RuntimeException(e);
                }

                return outcome;
            });
        });
    }

    public CompletableFuture<Void> shutdown() {
        final CompletableFuture<Void> completion = new CompletableFuture<>();
        this.push(new Shutdown(completion::complete));
        return completion;
    }

    public KurrentDBClientSettings getSettings() {
        return this.settings;
    }
}