package io.kurrent.dbclient;

import java.util.*;

public class NodeSelector {

    private static final Random random = new Random();
    private static final Set<ClusterInfo.MemberState> invalidStates;
    private static final Map<NodePreference,Comparator<ClusterInfo.Member>> memberComparators;

    static {
        invalidStates = new HashSet<ClusterInfo.MemberState>() {{
            add(ClusterInfo.MemberState.MANAGER);
            add(ClusterInfo.MemberState.SHUTTING_DOWN);
            add(ClusterInfo.MemberState.SHUT_DOWN);
            add(ClusterInfo.MemberState.UNKNOWN);
            add(ClusterInfo.MemberState.INITIALIZING);
            add(ClusterInfo.MemberState.RESIGNING_LEADER);
            add(ClusterInfo.MemberState.PRE_LEADER);
            add(ClusterInfo.MemberState.PRE_REPLICA);
            add(ClusterInfo.MemberState.PRE_READ_ONLY_REPLICA);
            add(ClusterInfo.MemberState.CLONE);
            add(ClusterInfo.MemberState.DISCOVER_LEADER);
        }};

        memberComparators= new HashMap<NodePreference, Comparator<ClusterInfo.Member>>()  {{
            put(NodePreference.LEADER, new MemberComparator(ClusterInfo.MemberState.LEADER));
            put(NodePreference.FOLLOWER, new MemberComparator(ClusterInfo.MemberState.FOLLOWER));
            put(NodePreference.READ_ONLY_REPLICA, new MemberComparator(ClusterInfo.MemberState.READ_ONLY_REPLICA));
            put(NodePreference.RANDOM, ((o1, o2) -> random.nextBoolean() ? -1 : 1));
        }};
    }

    private final Comparator<ClusterInfo.Member> memberComparator;

    public NodeSelector(NodePreference nodePreference) {
        this.memberComparator = memberComparators.get(nodePreference);
    }

    public Optional<ClusterInfo.Member> determineBestFitNode(ClusterInfo clusterInfo) {
        return clusterInfo.getMembers()
                .stream()
                .filter(ClusterInfo.Member::isAlive)
                .filter(m -> !invalidStates.contains(m.getState()))
                .sorted(memberComparator)
                .findFirst();
    }

    private static class MemberComparator implements Comparator<ClusterInfo.Member> {
        private final ClusterInfo.MemberState preferredState;

        private MemberComparator(ClusterInfo.MemberState preferredState) {
            this.preferredState = preferredState;
        }

        @Override
        public int compare(ClusterInfo.Member o1, ClusterInfo.Member o2) {
            if (o1.getState().equals(preferredState) && o2.getState().equals(preferredState)) {
                return random.nextBoolean() ? -1 : 0;
            } else if  (o1.getState().equals(preferredState) && !o2.getState().equals(preferredState)) {
                return -1;
            }
            return 1;
        }
    }
}
