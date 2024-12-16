package io.kurrent.dbclient;

import com.eventstore.dbclient.proto.persistentsubscriptions.Persistent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class HttpUtils {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static Exception checkForError(int code) {
        switch (code) {
            case 401:
                return new RuntimeException("Access denied");
            case 404:
                return new ResourceNotFoundException();
            default:
                if (code >= 200 && code < 300)
                    return null;

                return new RuntimeException("Unexpected exception, code: " + code);
        }
    }

    public static ObjectMapper getObjectMapper() {
        return mapper;
    }

    public static String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String readContent(HttpURLConnection conn) throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            StringBuilder content = new StringBuilder();
            while ((line = in.readLine()) != null) {
                content.append(line);
            }

            return content.toString();
        }
    }

    public static PersistentSubscriptionToStreamInfo parseSubscriptionInfo(JsonNode node) {
        PersistentSubscriptionToStreamInfo info = new PersistentSubscriptionToStreamInfo();
        PersistentSubscriptionToStreamStats stats = new PersistentSubscriptionToStreamStats();
        List<PersistentSubscriptionConnectionInfo> connections = new ArrayList<>();

        info.setEventSource(node.get("eventStreamId").asText());
        info.setGroupName(node.get("groupName").asText());
        info.setStatus(node.get("status").asText());
        stats.setAveragePerSecond(node.get("averageItemsPerSecond").asInt());
        stats.setTotalItems(node.get("totalItemsProcessed").asInt());
        stats.setLastKnownEventRevision(node.get("lastKnownEventNumber").asInt());
        stats.setTotalInFlightMessages(node.get("totalInFlightMessages").asInt());

        if (node.get("readBufferCount") != null) {
            stats.setReadBufferCount(node.get("readBufferCount").asInt());
        }

        if (node.get("liveBufferCount") != null) {
            stats.setLiveBufferCount(node.get("liveBufferCount").asInt());
        }

        if (node.get("retryBufferCount") != null) {
            stats.setRetryBufferCount(node.get("retryBufferCount").asInt());
        }

        if (node.get("outstandingMessagesCount") != null) {
            stats.setOutstandingMessagesCount(node.get("outstandingMessagesCount").asInt());
        }

        if (node.get("parkedMessageCount") != null) {
            stats.setParkedMessageCount(node.get("parkedMessageCount").asInt());
        }

        if (node.get("countSinceLastMeasurement") != null) {
            stats.setCountSinceLastMeasurement(node.get("countSinceLastMeasurement").asInt());
        }

        if (node.get("lastKnownEventPosition") != null) {
            RevisionOrPosition result = parseRevisionOrPosition(node.get("lastKnownEventPosition").asText());

            stats.setLastKnownEventRevision(result.getRevision().get());
        }

        if (node.get("lastCheckpointedEventPosition") != null) {
            RevisionOrPosition result = parseRevisionOrPosition(node.get("lastCheckpointedEventPosition").asText());

            stats.setLastCheckpointedEventRevision(result.getRevision().get());
        }

        if (node.get("connections") != null) {
            for (JsonNode connNode : node.get("connections")) {
                PersistentSubscriptionConnectionInfo conn = new PersistentSubscriptionConnectionInfo();

                conn.setFrom(connNode.get("from").asText());
                conn.setUsername(connNode.get("username").asText());
                conn.setAverageItemsPerSecond(connNode.get("averageItemsPerSecond").asInt());
                conn.setTotalItems(connNode.get("totalItemsProcessed").asInt());
                conn.setCountSinceLastMeasurement(connNode.get("countSinceLastMeasurement").asInt());
                conn.setAvailableSlots(connNode.get("availableSlots").asInt());
                conn.setInFlightMessages(connNode.get("inFlightMessages").asInt());
                conn.setConnectionName(connNode.get("connectionName").asText());
                Map<String, Long> measures = new HashMap<>();

                if (connNode.get("extraStatistics") != null) {
                    for (JsonNode measure : connNode.get("extraStatistics")) {
                        measures.put(measure.get(0).asText(), measure.get(1).asLong());
                    }
                }

                conn.setExtraStatistics(measures);

                connections.add(conn);
            }
        }

        info.setStats(stats);
        info.setSettings(parseSettings(node.get("config")));
        info.setConnections(connections);

        return info;
    }

    private static PersistentSubscriptionToStreamSettings parseSettings(JsonNode node) {
        PersistentSubscriptionToStreamSettings config = null;
        if (node != null) {
            config = new PersistentSubscriptionToStreamSettings();
            config.setResolveLinkTos(node.get("resolveLinktos").asBoolean());

            long value;

            if (node.get("startFrom").isTextual()) {
                RevisionOrPosition result = parseRevisionOrPosition(node.get("startFrom").asText());

                value = result.getRevision().get();
            } else {
                value = node.get("startFrom").asLong();
            }

            if (value == 0) {
                config.setStartFrom(StreamPosition.start());
            } else if (value == -1) {
                config.setStartFrom(StreamPosition.end());
            } else {
                config.setStartFrom(StreamPosition.position(value));
            }

            config.setMessageTimeoutMs(node.get("messageTimeoutMilliseconds").asInt());
            config.setExtraStatistics(node.get("extraStatistics").asBoolean());
            config.setMaxRetryCount(node.get("maxRetryCount").asInt());
            config.setLiveBufferSize(node.get("liveBufferSize").asInt());
            config.setHistoryBufferSize(node.get("bufferSize").asInt());
            config.setReadBatchSize(node.get("readBatchSize").asInt());
            config.setCheckpointAfter(node.get("checkPointAfterMilliseconds").asInt());
            config.setCheckpointLowerBound(node.get("minCheckPointCount").asInt());
            config.setCheckpointUpperBound(node.get("maxCheckPointCount").asInt());
            config.setMaxSubscriberCount(node.get("maxSubscriberCount").asInt());
            config.setNamedConsumerStrategy(new NamedConsumerStrategy(node.get("namedConsumerStrategy").asText()));
        }

        return config;
    }

    private static PersistentSubscriptionConnectionInfo parseConnectionFromWire(Persistent.SubscriptionInfo.ConnectionInfo wire) {
        PersistentSubscriptionConnectionInfo info = new PersistentSubscriptionConnectionInfo();
        Map<String, Long> stats = new HashMap<>();

        info.setFrom(wire.getFrom());
        info.setUsername(wire.getUsername());
        info.setAverageItemsPerSecond(wire.getAverageItemsPerSecond());
        info.setTotalItems(wire.getTotalItems());
        info.setCountSinceLastMeasurement(wire.getCountSinceLastMeasurement());
        info.setAvailableSlots(wire.getAvailableSlots());
        info.setInFlightMessages(wire.getInFlightMessages());
        info.setConnectionName(wire.getConnectionName());

        for (Persistent.SubscriptionInfo.Measurement measurement : wire.getObservedMeasurementsList()) {
            stats.put(measurement.getKey(), measurement.getValue());
        }

        info.setExtraStatistics(stats);

        return info;
    }

    private static Position parsePosition(String input) {
        int commitIdx = input.indexOf("C:");
        int prepareIdx = input.indexOf("/P:");

        if (commitIdx != 0 || prepareIdx == -1) {
            throw new RuntimeException(String.format("Error when parsing a position string representation: '%s'", input));
        }

        long commit;

        try {
            commit = Long.parseLong(input.substring(2, prepareIdx));
        } catch (NumberFormatException e) {
            throw new RuntimeException(String.format("Error when parsing commit position: '%s'", input), e);
        }

        long prepare;

        try {
            prepare = Long.parseLong(input.substring(prepareIdx + 3));
        } catch (NumberFormatException e) {
            throw new RuntimeException(String.format("Error when parsing prepare position: '%s'", input), e);
        }

        return new Position(commit, prepare);
    }

    private static RevisionOrPosition parseRevisionOrPosition(String input) {
        RevisionOrPosition result = new RevisionOrPosition();
        try {
            result.setRevision(Long.parseLong(input));
        } catch (NumberFormatException e) {
            result.setPosition(parsePosition(input));
        }

        return result;
    }

    private static void populateStats(Persistent.SubscriptionInfo wire, PersistentSubscriptionStats stats) {
        stats.setAveragePerSecond(wire.getAveragePerSecond());
        stats.setTotalInFlightMessages(wire.getTotalInFlightMessages());
        stats.setTotalItems(wire.getTotalItems());
        stats.setParkedMessageCount(wire.getParkedMessageCount());
        stats.setOutstandingMessagesCount(wire.getOutstandingMessagesCount());
        stats.setReadBufferCount(wire.getReadBufferCount());
        stats.setLiveBufferCount(wire.getLiveBufferCount());
        stats.setRetryBufferCount(wire.getRetryBufferCount());
    }

    private static void populateSettings(Persistent.SubscriptionInfo wire, PersistentSubscriptionSettings setts) {
        setts.setHistoryBufferSize(wire.getBufferSize());
        setts.setExtraStatistics(wire.getExtraStatistics());
        setts.setNamedConsumerStrategy(new NamedConsumerStrategy(wire.getNamedConsumerStrategy()));
        setts.setLiveBufferSize(wire.getLiveBufferSize());
        setts.setCheckpointUpperBound(wire.getMaxCheckPointCount());
        setts.setCheckpointAfter(wire.getCheckPointAfterMilliseconds());
        setts.setMaxRetryCount(wire.getMaxRetryCount());
        setts.setMaxSubscriberCount(wire.getMaxSubscriberCount());
        setts.setMessageTimeoutMs(wire.getMessageTimeoutMilliseconds());
        setts.setCheckpointLowerBound(wire.getMinCheckPointCount());
        setts.setReadBatchSize(wire.getReadBatchSize());
        setts.setResolveLinkTos(wire.getResolveLinkTos());
    }

    public static PersistentSubscriptionInfo parseInfoFromWire(Persistent.SubscriptionInfo wire) {
        PersistentSubscriptionInfo info;
        RevisionOrPosition revOrPosition = parseRevisionOrPosition(wire.getStartFrom());
        RevisionOrPosition lastKnown = null;
        RevisionOrPosition lastCheckpointed = null;

        if (!wire.getLastKnownEventPosition().equals("")) {
            lastKnown = parseRevisionOrPosition(wire.getLastKnownEventPosition());
        }

        if (!wire.getLastCheckpointedEventPosition().equals("")) {
            lastCheckpointed = parseRevisionOrPosition(wire.getLastCheckpointedEventPosition());
        }

        if (wire.getEventSource().equals("$all")) {
            PersistentSubscriptionToAllInfo toAllInfo = new PersistentSubscriptionToAllInfo();
            PersistentSubscriptionToAllStats toAllStats = new PersistentSubscriptionToAllStats();
            PersistentSubscriptionToAllSettings toAllSettings = new PersistentSubscriptionToAllSettings();

            Position pos = revOrPosition.getPosition().get();

            if (pos.getPrepareUnsigned() == pos.getCommitUnsigned() && pos.getPrepareUnsigned() == -1) {
                toAllSettings.setStartFrom(StreamPosition.end());
            } else if (pos.getPrepareUnsigned() == pos.getCommitUnsigned() && pos.getPrepareUnsigned() == 0) {
                toAllSettings.setStartFrom(StreamPosition.start());
            } else {
                toAllSettings.setStartFrom(StreamPosition.position(revOrPosition.getPosition().get()));
            }

            populateStats(wire, toAllStats);
            populateSettings(wire, toAllSettings);

            if (lastKnown != null) {
                toAllStats.setLastKnownEventPosition(lastKnown.getPosition().get());
            }

            if (lastCheckpointed != null) {
                toAllStats.setLastCheckpointedEventPosition(lastCheckpointed.getPosition().get());
            }

            toAllInfo.setStats(toAllStats);
            toAllInfo.setSettings(toAllSettings);
            info = toAllInfo;
        } else {
            PersistentSubscriptionToStreamInfo toStreamInfo = new PersistentSubscriptionToStreamInfo();
            PersistentSubscriptionToStreamStats toStreamStats = new PersistentSubscriptionToStreamStats();
            PersistentSubscriptionToStreamSettings toStreamSettings = new PersistentSubscriptionToStreamSettings();

            long pos = revOrPosition.getRevision().get();

            if (pos == -1) {
                toStreamSettings.setStartFrom(StreamPosition.end());
            } else if (pos == 0) {
                toStreamSettings.setStartFrom(StreamPosition.start());
            } else {
                toStreamSettings.setStartFrom(StreamPosition.position(revOrPosition.getRevision().get()));
            }

            populateStats(wire, toStreamStats);
            populateSettings(wire, toStreamSettings);

            if (lastKnown != null) {
                toStreamStats.setLastKnownEventRevision(lastKnown.getRevision().get());
            }

            if (lastCheckpointed != null) {
                toStreamStats.setLastCheckpointedEventRevision(lastCheckpointed.getRevision().get());
            }

            toStreamInfo.setSettings(toStreamSettings);
            toStreamInfo.setStats(toStreamStats);
            info = toStreamInfo;
        }

        info.setEventSource(wire.getEventSource());
        info.setGroupName(wire.getGroupName());
        info.setStatus(wire.getStatus());

        List<PersistentSubscriptionConnectionInfo> connections = new ArrayList<>();

        for (Persistent.SubscriptionInfo.ConnectionInfo connWire : wire.getConnectionsList()) {
            connections.add(parseConnectionFromWire(connWire));
        }

        info.setConnections(connections);

        return info;
    }
}
