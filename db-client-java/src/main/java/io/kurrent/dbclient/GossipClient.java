package io.kurrent.dbclient;

import com.eventstore.dbclient.proto.gossip.GossipGrpc;
import com.eventstore.dbclient.proto.gossip.GossipOuterClass;
import com.eventstore.dbclient.proto.shared.Shared;
import io.grpc.ManagedChannel;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

class GossipClient {
    private final ManagedChannel _channel;
    private final GossipGrpc.GossipStub _stub;

    public GossipClient(KurrentDBClientSettings settings, ManagedChannel channel) {
        _channel = channel;
        _stub = GrpcUtils.configureStub(GossipGrpc.newStub(_channel), settings, new GossipOption(), (long)settings.getGossipTimeout());
    }

    public void shutdown() {
        try {
            _channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    public CompletableFuture<ClusterInfo> read() {
        CompletableFuture<ClusterInfo> result = new CompletableFuture<>();

        _stub.read(Shared.Empty.getDefaultInstance(), convertSingleResponse(result, resp -> {
            List<ClusterInfo.Member> members = new ArrayList<>();

            for (GossipOuterClass.MemberInfo info: resp.getMembersList()) {
                UUID instanceId = null;

                if (info.hasInstanceId()) {
                    if (info.getInstanceId().hasStructured()) {
                        instanceId = new UUID(info.getInstanceId().getStructured().getMostSignificantBits(), info.getInstanceId().getStructured().getLeastSignificantBits());
                    } else {
                        instanceId = UUID.fromString(info.getInstanceId().getString());
                    }
                }

                ClusterInfo.MemberState state = ClusterInfo.MemberState.fromWire(info.getState());
                InetSocketAddress endpoint = new InetSocketAddress(info.getHttpEndPoint().getAddress(), info.getHttpEndPoint().getPort());

                ClusterInfo.Member member = new ClusterInfo.Member(instanceId, info.getIsAlive(), state, endpoint);
                members.add(member);
            }

            return new ClusterInfo(members);
        }));
        return result;
    }

    private <ReqT, RespT, TargetT> ClientResponseObserver<ReqT, RespT> convertSingleResponse(
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

    class GossipOption extends OptionsBase<Void> {}
}
