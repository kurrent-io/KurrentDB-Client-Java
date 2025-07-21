package io.kurrent.dbclient;

import io.kurrent.dbclient.proto.shared.Shared;
import io.kurrent.dbclient.proto.streams.StreamsOuterClass;
import com.google.protobuf.ByteString;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

final class GrpcUtils {
    static public <ReqT, RespT> ClientResponseObserver<ReqT, RespT> convertSingleResponse(
            CompletableFuture<RespT> dest) {

        return convertSingleResponse(dest, x -> x);
    }

    static public <ReqT, RespT, TargetT, ExceptionT extends Throwable> ClientResponseObserver<ReqT, RespT> convertSingleResponse(
            CompletableFuture<TargetT> dest, ThrowingFunction<RespT, TargetT, ExceptionT> converter) {

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
                if (t instanceof StatusRuntimeException) {
                    StatusRuntimeException e = (StatusRuntimeException) t;

                    if (e.getStatus().getCode().equals(Status.FAILED_PRECONDITION.getCode())) {
                        String reason = e.getTrailers().get(Metadata.Key.of("exception", Metadata.ASCII_STRING_MARSHALLER));
                        String streamName = e.getTrailers().get(Metadata.Key.of("stream-name", Metadata.ASCII_STRING_MARSHALLER));

                        if (reason != null && reason.equals("stream-deleted")) {
                            dest.completeExceptionally(new StreamDeletedException(streamName));
                            return;
                        }

                        if (reason != null && reason.equals("wrong-expected-version")) {
                            String expectedStr = e.getTrailers().get(Metadata.Key.of("expected-version", Metadata.ASCII_STRING_MARSHALLER));
                            String actualStr = e.getTrailers().get(Metadata.Key.of("actual-version", Metadata.ASCII_STRING_MARSHALLER));

                            // Some old versions of the server, actual-version is not provided.
                            if (actualStr == null || actualStr.isEmpty()) {
                                // Equivalent of NoStream internally.
                                actualStr = "-1";
                            }

                            try {
                                StreamState expected = StreamState.fromRawLong(Long.parseLong(expectedStr));
                                StreamState actual = StreamState.fromRawLong(Long.parseLong(actualStr));

                                dest.completeExceptionally(new WrongExpectedVersionException(streamName, expected, actual));
                                return;
                            } catch (NumberFormatException ex) {
                                // Nothing to do here as it would mean the server sent
                                // invalid expected version numbers.
                            }
                        }
                    }

                    String leaderHost = e.getTrailers().get(Metadata.Key.of("leader-endpoint-host", Metadata.ASCII_STRING_MARSHALLER));
                    String leaderPort = e.getTrailers().get(Metadata.Key.of("leader-endpoint-port", Metadata.ASCII_STRING_MARSHALLER));

                    if (leaderHost != null && leaderPort != null) {
                        NotLeaderException reason = new NotLeaderException(leaderHost, Integer.valueOf(leaderPort));
                        dest.completeExceptionally(reason);
                        return;
                    }
                }

                dest.completeExceptionally(t);
            }

            @Override
            public void onCompleted() {
            }
        };
    }

    static public StreamsOuterClass.ReadReq.Options.StreamOptions toStreamOptions(String streamName, StreamPosition<Long> revision) {
        StreamsOuterClass.ReadReq.Options.StreamOptions.Builder builder = StreamsOuterClass.ReadReq.Options.StreamOptions.newBuilder()
                .setStreamIdentifier(Shared.StreamIdentifier.newBuilder()
                        .setStreamName(ByteString.copyFromUtf8(streamName))
                        .build());

        if (revision.isEnd()) {
            return builder.setEnd(Shared.Empty.getDefaultInstance())
                    .build();
        }

        if (revision.isStart()) {
            return builder.setStart(Shared.Empty.getDefaultInstance())
                    .build();
        }

        return builder.setRevision(revision.getPositionOrThrow())
                .build();
    }

    static public <S extends AbstractAsyncStub<S>, O> S configureStub(S stub, KurrentDBClientSettings settings, OptionsBase<O> options) {
        return configureStub(stub, settings, options, null, true);
    }

    static public <S extends AbstractAsyncStub<S>, O> S configureStub(S stub, KurrentDBClientSettings settings, OptionsBase<O> options, long forceDeadlineInMs) {
        return configureStub(stub, settings, options, forceDeadlineInMs, true);
    }

    static public <S extends AbstractAsyncStub<S>, O> S configureStub(S stub, KurrentDBClientSettings settings, OptionsBase<O> options, Long forceDeadlineInMs, boolean forwardRequiresLeader) {
        S finalStub = stub;
        ConnectionMetadata metadata = new ConnectionMetadata();

        if (options.getKind() != OperationKind.Streaming) {
            long deadlineInMs = 10_000;

            if (forceDeadlineInMs != null) {
                deadlineInMs = forceDeadlineInMs;
            } else if (options.getDeadline() != null) {
                deadlineInMs = options.getDeadline();
            } else if (settings.getDefaultDeadline() != null) {
                deadlineInMs = settings.getDefaultDeadline();
            }

            finalStub = finalStub.withDeadlineAfter(deadlineInMs, TimeUnit.MILLISECONDS);
        }

        UserCredentials credentials = null;

        if (options.hasUserCredentials()) {
            credentials = options.getCredentials();
        } else if (settings.getDefaultCredentials() != null) {
            credentials = settings.getDefaultCredentials();
        }

        if (credentials != null) {
            metadata.authenticated(credentials);
        }

        if (forwardRequiresLeader && (options.isLeaderRequired() || settings.getNodePreference() == NodePreference.LEADER)) {
            metadata.requiresLeader();
        }

        metadata.headers(options.getHeaders());

        return finalStub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata.build()));
    }
}