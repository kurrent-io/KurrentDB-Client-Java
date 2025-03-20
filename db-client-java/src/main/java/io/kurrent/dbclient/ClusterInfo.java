package io.kurrent.dbclient;

import io.kurrent.dbclient.proto.gossip.GossipOuterClass;
import io.kurrent.dbclient.proto.shared.Shared;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClusterInfo {
    private final List<Member> members;

    public ClusterInfo(List<Member> members) {
        this.members = members;
    }

    public List<Member> getMembers() {
        return members;
    }

    static ClusterInfo fromWire(GossipOuterClass.ClusterInfo wire) {
        List<ClusterInfo.Member> members = new ArrayList<>();
        for (GossipOuterClass.MemberInfo member : wire.getMembersList()) {
            UUID instanceId;
            if (member.getInstanceId().hasStructured()) {
                Shared.UUID.Structured structured = member.getInstanceId().getStructured();
                instanceId = new UUID(structured.getMostSignificantBits(), structured.getLeastSignificantBits());
            } else {
                instanceId = UUID.fromString(member.getInstanceId().getString());
            }
            boolean isAlive = member.getIsAlive();
            MemberState state = MemberState.fromWire(member.getState());
            InetSocketAddress httpEndpoint = new InetSocketAddress(member.getHttpEndPoint().getAddress(), member.getHttpEndPoint().getPort());

            members.add(new Member(instanceId, isAlive, state, httpEndpoint));
        }

        return new ClusterInfo(members);
    }

    public enum MemberState {
        INITIALIZING, DISCOVER_LEADER, UNKNOWN, PRE_REPLICA, CATCHING_UP, CLONE,
        FOLLOWER, PRE_LEADER, LEADER, MANAGER, SHUTTING_DOWN, SHUT_DOWN, READ_ONLY_LEADERLESS,
        PRE_READ_ONLY_REPLICA, READ_ONLY_REPLICA, RESIGNING_LEADER;

        static MemberState fromWire(GossipOuterClass.MemberInfo.VNodeState state) {
            switch (state) {
                case Initializing:
                    return INITIALIZING;
                case DiscoverLeader:
                    return DISCOVER_LEADER;
                case PreReplica:
                    return PRE_REPLICA;
                case CatchingUp:
                    return CATCHING_UP;
                case Clone:
                    return CLONE;
                case Follower:
                    return FOLLOWER;
                case PreLeader:
                    return PRE_LEADER;
                case Leader:
                    return LEADER;
                case Manager:
                    return MANAGER;
                case ShuttingDown:
                    return SHUTTING_DOWN;
                case Shutdown:
                    return SHUT_DOWN;
                case ReadOnlyLeaderless:
                    return READ_ONLY_LEADERLESS;
                case PreReadOnlyReplica:
                    return PRE_READ_ONLY_REPLICA;
                case ReadOnlyReplica:
                    return READ_ONLY_REPLICA;
                case ResigningLeader:
                    return RESIGNING_LEADER;
            }
            return UNKNOWN;
        }
    }

    public static class Member {
        private final UUID instanceId;
        private final boolean isAlive;
        private final MemberState state;
        private final InetSocketAddress httpEndpoint;

        public Member(UUID instanceId, boolean isAlive, MemberState state, InetSocketAddress httpEndpoint) {
            this.instanceId = instanceId;
            this.isAlive = isAlive;
            this.state = state;
            this.httpEndpoint = httpEndpoint;
        }

        public UUID getInstanceId() {
            return instanceId;
        }

        public boolean isAlive() {
            return isAlive;
        }

        public MemberState getState() {
            return state;
        }

        public InetSocketAddress getHttpEndpoint() {
            return httpEndpoint;
        }
    }
}