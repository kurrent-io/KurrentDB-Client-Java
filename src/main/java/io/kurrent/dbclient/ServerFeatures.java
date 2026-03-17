package io.kurrent.dbclient;

import io.kurrent.dbclient.proto.serverfeatures.ServerFeaturesGrpc;
import io.kurrent.dbclient.proto.serverfeatures.Serverfeatures;
import io.kurrent.dbclient.proto.shared.Shared;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

class ServerFeatures {
    public static Optional<ServerInfo> getSupportedFeatures(KurrentDBClientSettings settings, ManagedChannel channel) {
        final ServerFeaturesGrpc.ServerFeaturesStub stub = ServerFeaturesGrpc.newStub(channel);
        try {
            return Optional.ofNullable(getSupportedFeaturesInternal(stub).get(settings.getGossipTimeout(), TimeUnit.MILLISECONDS));
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted when fetching server features", e);
        } catch (TimeoutException e) {
            throw new RetryableException(e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof StatusRuntimeException) {
                StatusRuntimeException error = (StatusRuntimeException) e.getCause();
                if (error.getStatus().getCode() == Status.Code.NOT_FOUND || error.getStatus().getCode() == Status.Code.UNIMPLEMENTED) {
                    return Optional.empty();
                } else if (error.getStatus().getCode() == Status.Code.UNAVAILABLE) {
                    throw new RetryableException(e);
                }
            }
            throw new RuntimeException("Error when fetching server features", e);
        }
    }

    private static CompletableFuture<ServerInfo> getSupportedFeaturesInternal(ServerFeaturesGrpc.ServerFeaturesStub stub) {
        CompletableFuture<ServerInfo> result = new CompletableFuture<>();

        stub.getSupportedMethods(Shared.Empty.getDefaultInstance(), convertSingleResponse(result, resp -> {
            int major = 0, minor = 0, patch = 0;

            String semVer = resp.getEventStoreServerVersion().replaceAll("[^.\\d]+", "");
            String[] splits = semVer.split("\\.");
            for (int idx = 0; idx < splits.length; idx++) {
                if (idx > 2) {
                    break;
                }

                int value = Integer.parseInt(splits[idx]);
                switch (idx) {
                    case 0:
                        major = value;
                        break;
                    case 1:
                        minor = value;
                        break;
                    default:
                        patch = value;
                        break;
                }
            }

            ServerVersion version = new ServerVersion(major, minor, patch);

            int features = FeatureFlags.NOTHING;
            for (Serverfeatures.SupportedMethod method : resp.getMethodsList()) {
                if (method.getMethodName().equals("batchappend") && method.getServiceName().equals("event_store.client.streams.streams")) {
                    features |= FeatureFlags.BATCH_APPEND;
                } else if (method.getServiceName().equals("event_store.client.persistent_subscriptions.persistentsubscriptions")) {
                    switch (method.getMethodName()) {
                        case "create":
                            for (String feat : method.getFeaturesList()) {
                                if (feat.equals("all")) {
                                    features |= FeatureFlags.PERSISTENT_SUBSCRIPTION_TO_ALL;
                                }
                            }
                            break;
                        case "getinfo":
                            features |= FeatureFlags.PERSISTENT_SUBSCRIPTION_GET_INFO;
                            break;
                        case "replayparked":
                            features |= FeatureFlags.PERSISTENT_SUBSCRIPTION_REPLAY;
                            break;
                        case "list":
                            features |= FeatureFlags.PERSISTENT_SUBSCRIPTION_LIST;
                            break;
                        case "restartsubsystem":
                            features |= FeatureFlags.PERSISTENT_SUBSCRIPTION_RESTART_SUBSYSTEM;
                            break;
                        default:
                            break;
                    }
                } else if (method.getMethodName().equals("appendsession")) {
                    features |= FeatureFlags.MULTI_STREAM_APPEND;
                } else if (method.getMethodName().equals("appendrecords") && method.getServiceName().equals("kurrentdb.protocol.v2.streams.streamsservice")) {
                    features |= FeatureFlags.APPEND_RECORDS;
                }
            }

            return new ServerInfo(version, features);
        }));

        return result;
    }

    private static <ReqT, RespT, TargetT> ClientResponseObserver<ReqT, RespT> convertSingleResponse(
            CompletableFuture<TargetT> dest, Function<RespT, TargetT> converter) {
        return new ClientResponseObserver<ReqT, RespT>() {
            @Override
            public void beforeStart(ClientCallStreamObserver<ReqT> requestStream) {
            }

            @Override
            public void onNext(RespT value) {
                try {
                    TargetT converted = converter.apply(value);
                    dest.complete(converted);
                } catch (Throwable e) {
                    dest.completeExceptionally(e);
                }
            }

            @Override
            public void onError(Throwable t) {
                dest.completeExceptionally(t);
            }

            @Override
            public void onCompleted() {
            }
        };
    }

    static class RetryableException extends RuntimeException {
        public RetryableException(Throwable cause) {
            super(cause);
        }
    }
}