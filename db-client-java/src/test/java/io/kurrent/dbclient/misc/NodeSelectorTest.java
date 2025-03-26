package io.kurrent.dbclient.misc;

import io.kurrent.dbclient.ClusterInfo;
import io.kurrent.dbclient.NodePreference;
import io.kurrent.dbclient.NodeSelector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toSet;

public class NodeSelectorTest {

    private ClusterInfo.Member leader;
    private ClusterInfo.Member follower1;
    private ClusterInfo.Member follower2;
    private ClusterInfo.Member replica1;
    private ClusterInfo.Member replica2;

    private ClusterInfo clusterInfo;

    @BeforeEach
    public void setUp() {
        leader = new ClusterInfo.Member(randomUUID(), true, ClusterInfo.MemberState.LEADER, null);
        follower1 = new ClusterInfo.Member(randomUUID(), true, ClusterInfo.MemberState.FOLLOWER, null);
        follower2 = new ClusterInfo.Member(randomUUID(), true, ClusterInfo.MemberState.FOLLOWER, null);
        replica1 = new ClusterInfo.Member(randomUUID(), true, ClusterInfo.MemberState.READ_ONLY_REPLICA, null);
        replica2 = new ClusterInfo.Member(randomUUID(), true, ClusterInfo.MemberState.READ_ONLY_REPLICA, null);

        clusterInfo = new ClusterInfo(asList(leader, follower1, follower2, replica1, replica2));
    }

    @Test
    public void shouldReturnEmptyPreferredNodeIfNodesAreInValidStates() {
        leader = new ClusterInfo.Member(randomUUID(), true, ClusterInfo.MemberState.RESIGNING_LEADER, null);
        follower1 = new ClusterInfo.Member(randomUUID(), true, ClusterInfo.MemberState.DISCOVER_LEADER, null);
        follower2 = new ClusterInfo.Member(randomUUID(), true, ClusterInfo.MemberState.DISCOVER_LEADER, null);
        clusterInfo = new ClusterInfo(asList(leader, follower1, follower2));
        NodeSelector sut = new NodeSelector(NodePreference.LEADER);

        Optional<ClusterInfo.Member> selectedNode = sut.determineBestFitNode(clusterInfo);

        Assertions.assertFalse(selectedNode.isPresent());
    }

    @Test
    public void shouldReturnEmptyPreferredNodeIfNodesAreNotAlive() {
        leader = new ClusterInfo.Member(randomUUID(), false, ClusterInfo.MemberState.LEADER, null);
        follower1 = new ClusterInfo.Member(randomUUID(), false, ClusterInfo.MemberState.FOLLOWER, null);
        follower2 = new ClusterInfo.Member(randomUUID(), false, ClusterInfo.MemberState.FOLLOWER, null);
        clusterInfo = new ClusterInfo(asList(leader, follower1, follower2));
        NodeSelector sut = new NodeSelector(NodePreference.LEADER);

        Optional<ClusterInfo.Member> selectedNode = sut.determineBestFitNode(clusterInfo);

        Assertions.assertFalse(selectedNode.isPresent());
    }

    @Test
    public void shouldSelectLeaderNodeOnNodePreferenceLeader() {
        NodeSelector sut = new NodeSelector(NodePreference.LEADER);

        Set<UUID> members = performMultipleNodeSelectionsWithSelector(sut);

        Assertions.assertEquals(1, members.size());
        Assertions.assertTrue(members.contains(leader.getInstanceId()));
    }

    @Test
    public void shouldRandomlySelectFollowerNodeOnNodePreferenceFollower() {
        NodeSelector sut = new NodeSelector(NodePreference.FOLLOWER);

        Set<UUID> members = performMultipleNodeSelectionsWithSelector(sut);

        Assertions.assertEquals(2, members.size());
        Assertions.assertTrue(members.containsAll(asList(follower1.getInstanceId(), follower2.getInstanceId())));
    }

    @Test
    public void shouldRandomlySelectReadOnlyReplicaNodeOnNodePreferenceReadOnlyReplica() {
        NodeSelector sut = new NodeSelector(NodePreference.READ_ONLY_REPLICA);

        Set<UUID> members = performMultipleNodeSelectionsWithSelector(sut);

        Assertions.assertEquals(2, members.size());
        Assertions.assertTrue(members.containsAll(asList(replica1.getInstanceId(), replica2.getInstanceId())));
    }

    @Test
    public void shouldRandomlySelectNodeOnNodePreferenceRandom() {
        NodeSelector sut = new NodeSelector(NodePreference.RANDOM);

        Set<UUID> members = performMultipleNodeSelectionsWithSelector(sut);

        Assertions.assertEquals(5, members.size());
    }

    private Set<UUID> performMultipleNodeSelectionsWithSelector(NodeSelector sut) {
        return IntStream.range(0, 100)
                .mapToObj(i -> sut.determineBestFitNode(clusterInfo).get())
                .map(ClusterInfo.Member::getInstanceId)
                .collect(toSet());
    }
}