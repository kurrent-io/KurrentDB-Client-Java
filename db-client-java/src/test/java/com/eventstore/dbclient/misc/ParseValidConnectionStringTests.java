package com.eventstore.dbclient.misc;

import java.net.InetSocketAddress;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.eventstore.dbclient.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ParseValidConnectionStringTests {
    private final JsonMapper mapper = new JsonMapper();


    public static Stream<Arguments> validConnectionStrings() {
        return Stream.of(
                Arguments.of(
                        "esdb://localhost",
                        "{\"dnsDiscover\":false,\"maxDiscoverAttempts\":3,\"discoveryInterval\":500,\"gossipTimeout\":3000,\"nodePreference\":\"leader\",\"tls\":true,\"tlsVerifyCert\":true,\"throwOnAppendFailure\":true,\"hosts\":[{\"address\":\"localhost\",\"port\":2113}]}"
                ),
                Arguments.of(
                        "esdb://localhost:2114",
                        "{\"dnsDiscover\":false,\"maxDiscoverAttempts\":3,\"discoveryInterval\":500,\"gossipTimeout\":3000,\"nodePreference\":\"leader\",\"tls\":true,\"tlsVerifyCert\":true,\"throwOnAppendFailure\":true,\"hosts\":[{\"address\":\"localhost\",\"port\":2114}]}"
                ),
                Arguments.of(
                        "esdb://user:pass@localhost:2114",
                        "{\"dnsDiscover\":false,\"maxDiscoverAttempts\":3,\"discoveryInterval\":500,\"gossipTimeout\":3000,\"nodePreference\":\"leader\",\"tls\":true,\"tlsVerifyCert\":true,\"throwOnAppendFailure\":true,\"defaultCredentials\":{\"login\":\"user\",\"password\":\"pass\"},\"hosts\":[{\"address\":\"localhost\",\"port\":2114}]}"
                ),
                Arguments.of(
                        "esdb://user:pass@localhost:2114/",
                        "{\"dnsDiscover\":false,\"maxDiscoverAttempts\":3,\"discoveryInterval\":500,\"gossipTimeout\":3000,\"nodePreference\":\"leader\",\"tls\":true,\"tlsVerifyCert\":true,\"throwOnAppendFailure\":true,\"defaultCredentials\":{\"login\":\"user\",\"password\":\"pass\"},\"hosts\":[{\"address\":\"localhost\",\"port\":2114}]}"
                ),
                Arguments.of(
                        "esdb://user:pass@localhost:2114/?tlsVerifyCert=false",
                        "{\"dnsDiscover\":false,\"maxDiscoverAttempts\":3,\"discoveryInterval\":500,\"gossipTimeout\":3000,\"nodePreference\":\"leader\",\"tls\":true,\"tlsVerifyCert\":false,\"throwOnAppendFailure\":true,\"defaultCredentials\":{\"login\":\"user\",\"password\":\"pass\"},\"hosts\":[{\"address\":\"localhost\",\"port\":2114}]}"
                ),
                Arguments.of(
                        "esdb://user:pass@localhost:2114?tlsVerifyCert=false",
                        "{\"dnsDiscover\":false,\"maxDiscoverAttempts\":3,\"discoveryInterval\":500,\"gossipTimeout\":3000,\"nodePreference\":\"leader\",\"tls\":true,\"tlsVerifyCert\":false,\"throwOnAppendFailure\":true,\"defaultCredentials\":{\"login\":\"user\",\"password\":\"pass\"},\"hosts\":[{\"address\":\"localhost\",\"port\":2114}]}"
                ),
                Arguments.of(
                        "esdb://user:pass@localhost:2114?tls=false",
                        "{\"dnsDiscover\":false,\"maxDiscoverAttempts\":3,\"discoveryInterval\":500,\"gossipTimeout\":3000,\"nodePreference\":\"leader\",\"tls\":false,\"tlsVerifyCert\":true,\"throwOnAppendFailure\":true,\"defaultCredentials\":{\"login\":\"user\",\"password\":\"pass\"},\"hosts\":[{\"address\":\"localhost\",\"port\":2114}]}"
                ),
                Arguments.of(
                        "esdb://host1,host2,host3",
                        "{\"dnsDiscover\":false,\"maxDiscoverAttempts\":3,\"discoveryInterval\":500,\"gossipTimeout\":3000,\"nodePreference\":\"leader\",\"tls\":true,\"tlsVerifyCert\":true,\"throwOnAppendFailure\":true,\"hosts\":[{\"address\":\"host1\",\"port\":2113},{\"address\":\"host2\",\"port\":2113},{\"address\":\"host3\",\"port\":2113}]}"
                ),
                Arguments.of(
                        "esdb://host1:1234,host2:4321,host3:3231",
                        "{\"dnsDiscover\":false,\"maxDiscoverAttempts\":3,\"discoveryInterval\":500,\"gossipTimeout\":3000,\"nodePreference\":\"leader\",\"tls\":true,\"tlsVerifyCert\":true,\"throwOnAppendFailure\":true,\"hosts\":[{\"address\":\"host1\",\"port\":1234},{\"address\":\"host2\",\"port\":4321},{\"address\":\"host3\",\"port\":3231}]}"
                ),
                Arguments.of(
                        "esdb://bubaqp2rh41uf5akmj0g-0.mesdb.eventstore.cloud:2113,bubaqp2rh41uf5akmj0g-1.mesdb.eventstore.cloud:2113,bubaqp2rh41uf5akmj0g-2.mesdb.eventstore.cloud:2113",
                        "{\"dnsDiscover\":false,\"maxDiscoverAttempts\":3,\"discoveryInterval\":500,\"gossipTimeout\":3000,\"nodePreference\":\"leader\",\"tls\":true,\"tlsVerifyCert\":true,\"throwOnAppendFailure\":true,\"hosts\":[{\"address\":\"bubaqp2rh41uf5akmj0g-0.mesdb.eventstore.cloud\",\"port\":2113},{\"address\":\"bubaqp2rh41uf5akmj0g-1.mesdb.eventstore.cloud\",\"port\":2113},{\"address\":\"bubaqp2rh41uf5akmj0g-2.mesdb.eventstore.cloud\",\"port\":2113}]}"
                ),
                Arguments.of(
                        "esdb://user:pass@host1:1234,host2:4321,host3:3231?nodePreference=follower",
                        "{\"dnsDiscover\":false,\"maxDiscoverAttempts\":3,\"discoveryInterval\":500,\"gossipTimeout\":3000,\"nodePreference\":\"follower\",\"tls\":true,\"tlsVerifyCert\":true,\"throwOnAppendFailure\":true,\"defaultCredentials\":{\"login\":\"user\",\"password\":\"pass\"},\"hosts\":[{\"address\":\"host1\",\"port\":1234},{\"address\":\"host2\",\"port\":4321},{\"address\":\"host3\",\"port\":3231}]}"
                ),
                Arguments.of(
                        "esdb://host1,host2,host3?tls=false",
                        "{\"dnsDiscover\":false,\"maxDiscoverAttempts\":3,\"discoveryInterval\":500,\"gossipTimeout\":3000,\"nodePreference\":\"leader\",\"tls\":false,\"tlsVerifyCert\":true,\"throwOnAppendFailure\":true,\"hosts\":[{\"address\":\"host1\",\"port\":2113},{\"address\":\"host2\",\"port\":2113},{\"address\":\"host3\",\"port\":2113}]}"
                ),
                Arguments.of(
                        "esdb://127.0.0.1:21573?tls=false",
                        "{\"dnsDiscover\":false,\"maxDiscoverAttempts\":3,\"discoveryInterval\":500,\"gossipTimeout\":3000,\"nodePreference\":\"leader\",\"tls\":false,\"tlsVerifyCert\":true,\"throwOnAppendFailure\":true,\"hosts\":[{\"address\":\"127.0.0.1\",\"port\":21573}]}"
                ),
                Arguments.of(
                        "esdb://host1,host2,host3?tlsVerifyCert=false",
                        "{\"dnsDiscover\":false,\"maxDiscoverAttempts\":3,\"discoveryInterval\":500,\"gossipTimeout\":3000,\"nodePreference\":\"leader\",\"tls\":true,\"tlsVerifyCert\":false,\"throwOnAppendFailure\":true,\"hosts\":[{\"address\":\"host1\",\"port\":2113},{\"address\":\"host2\",\"port\":2113},{\"address\":\"host3\",\"port\":2113}]}"
                ),
                Arguments.of(
                        "esdb+discover://user:pass@host?nodePreference=follower&tlsVerifyCert=false",
                        "{\"dnsDiscover\":true,\"maxDiscoverAttempts\":3,\"discoveryInterval\":500,\"gossipTimeout\":3000,\"nodePreference\":\"follower\",\"tls\":true,\"tlsVerifyCert\":false,\"throwOnAppendFailure\":true,\"defaultCredentials\":{\"login\":\"user\",\"password\":\"pass\"},\"hosts\":[{\"address\":\"host\",\"port\":2113}]}"
                ),
                Arguments.of(
                        "esdb://my%3Agreat%40username:UyeXx8%24%5EPsOo4jG88FlCauR1Coz25q@host?nodePreference=follower&tlsVerifyCert=false",
                        "{\"dnsDiscover\":false,\"maxDiscoverAttempts\":3,\"discoveryInterval\":500,\"gossipTimeout\":3000,\"nodePreference\":\"follower\",\"tls\":true,\"tlsVerifyCert\":false,\"throwOnAppendFailure\":true,\"defaultCredentials\":{\"login\":\"my:great@username\",\"password\":\"UyeXx8$^PsOo4jG88FlCauR1Coz25q\"},\"hosts\":[{\"address\":\"host\",\"port\":2113}]}"
                ),
                Arguments.of(
                        "esdb://host?maxDiscoverAttempts=200&discoveryInterval=1000&gossipTimeout=1&nodePreference=leader&tls=false&tlsVerifyCert=false&throwOnAppendFailure=false",
                        "{\"dnsDiscover\":false,\"maxDiscoverAttempts\":200,\"discoveryInterval\":1000,\"gossipTimeout\":1,\"nodePreference\":\"leader\",\"tls\":false,\"tlsVerifyCert\":false,\"throwOnAppendFailure\":false,\"hosts\":[{\"address\":\"host\",\"port\":2113}]}"
                ),
                Arguments.of(
                        "esdb://host?MaxDiscoverAttempts=200&discoveryinterval=1000&GOSSIPTIMEOUT=1&nOdEpReFeReNcE=leader&TLS=false&TlsVerifyCert=false&THROWOnAppendFailure=false",
                        "{\"dnsDiscover\":false,\"maxDiscoverAttempts\":200,\"discoveryInterval\":1000,\"gossipTimeout\":1,\"nodePreference\":\"leader\",\"tls\":false,\"tlsVerifyCert\":false,\"throwOnAppendFailure\":false,\"hosts\":[{\"address\":\"host\",\"port\":2113}]}"
                ),
                Arguments.of(
                        "esdb://localhost?keepAliveTimeout=20&keepAliveInterval=10",
                        "{\"dnsDiscover\":false,\"maxDiscoverAttempts\":3,\"discoveryInterval\":500,\"gossipTimeout\":3000,\"nodePreference\":\"leader\",\"tls\":true,\"tlsVerifyCert\":true,\"throwOnAppendFailure\":true,\"hosts\":[{\"address\":\"localhost\",\"port\":2113}], \"keepAliveTimeout\": \"20\", \"keepAliveInterval\": \"10\"}"
                ),
                Arguments.of(
                        "esdb://localhost?keepAliveTimeout=20&keepAliveInterval=10&nodePreference=readOnlyReplica",
                        "{\"dnsDiscover\":false,\"maxDiscoverAttempts\":3,\"discoveryInterval\":500,\"gossipTimeout\":3000,\"nodePreference\":\"readOnlyReplica\",\"tls\":true,\"tlsVerifyCert\":true,\"throwOnAppendFailure\":true,\"hosts\":[{\"address\":\"localhost\",\"port\":2113}], \"keepAliveTimeout\": \"20\", \"keepAliveInterval\": \"10\"}"
                ),
                Arguments.of(
                        "esdb://127.0.0.1:21573?defaultDeadline=60000",
                        "{\"dnsDiscover\":false,\"maxDiscoverAttempts\":3,\"discoveryInterval\":500,\"gossipTimeout\":3000,\"nodePreference\":\"leader\",\"tls\":true,\"tlsVerifyCert\":true,\"throwOnAppendFailure\":true,\"hosts\":[{\"address\":\"127.0.0.1\",\"port\":21573}], \"defaultDeadline\": 60000}"
                ),
                Arguments.of(
                        "esdb://127.0.0.1:21573?tlsCaFile=/path/to/cert",
                        "{\"dnsDiscover\":false,\"maxDiscoverAttempts\":3,\"discoveryInterval\":500,\"gossipTimeout\":3000,\"nodePreference\":\"leader\",\"tls\":true,\"tlsVerifyCert\":true,\"throwOnAppendFailure\":true,\"hosts\":[{\"address\":\"127.0.0.1\",\"port\":21573}], \"tlsCaFile\": \"/path/to/cert\"}"
                ),
                Arguments.of(
                        "esdb://127.0.0.1:21573?userCertFile=/path/to/cert&userKeyFile=/path/to/key",
                        "{\"dnsDiscover\":false,\"maxDiscoverAttempts\":3,\"discoveryInterval\":500,\"gossipTimeout\":3000,\"nodePreference\":\"leader\",\"tls\":true,\"tlsVerifyCert\":true,\"throwOnAppendFailure\":true,\"hosts\":[{\"address\":\"127.0.0.1\",\"port\":21573}], \"defaultClientCertificate\": {\"clientCertFile\": \"/path/to/cert\", \"clientKeyFile\": \"/path/to/key\"}}"
                ),
                Arguments.of(
                        "esdb://localhost?feature=foobar",
                        "{\"dnsDiscover\":false,\"maxDiscoverAttempts\":3,\"discoveryInterval\":500,\"gossipTimeout\":3000,\"nodePreference\":\"leader\",\"tls\":true,\"tlsVerifyCert\":true,\"throwOnAppendFailure\":true,\"hosts\":[{\"address\":\"localhost\",\"port\":2113}], \"features\": \"foobar\"}"
                ),
                Arguments.of(
                        "esdb://localhost?feature=foobar&feature=baz",
                        "{\"dnsDiscover\":false,\"maxDiscoverAttempts\":3,\"discoveryInterval\":500,\"gossipTimeout\":3000,\"nodePreference\":\"leader\",\"tls\":true,\"tlsVerifyCert\":true,\"throwOnAppendFailure\":true,\"hosts\":[{\"address\":\"localhost\",\"port\":2113}], \"features\": [\"foobar\", \"baz\"]}"
                )
        );
    }

    public void assertEquals(EventStoreDBClientSettings settings, EventStoreDBClientSettings other) {
        Assertions.assertEquals(settings.isDnsDiscover(), other.isDnsDiscover());
        Assertions.assertEquals(settings.getMaxDiscoverAttempts(), other.getMaxDiscoverAttempts());
        Assertions.assertEquals(settings.getDiscoveryInterval(), other.getDiscoveryInterval());
        Assertions.assertEquals(settings.getGossipTimeout(), other.getGossipTimeout());
        Assertions.assertEquals(settings.getNodePreference(), other.getNodePreference());
        Assertions.assertEquals(settings.isTls(), other.isTls());
        Assertions.assertEquals(settings.isTlsVerifyCert(), other.isTlsVerifyCert());
        Assertions.assertEquals(settings.getKeepAliveTimeout(), other.getKeepAliveTimeout());
        Assertions.assertEquals(settings.getKeepAliveInterval(), other.getKeepAliveInterval());
        Assertions.assertEquals(settings.getDefaultDeadline(), other.getDefaultDeadline());
        Assertions.assertEquals(settings.getTlsCaFile(), other.getTlsCaFile());
        Assertions.assertEquals(settings.getDefaultClientCertificate(), other.getDefaultClientCertificate());

        Assertions.assertEquals(settings.getHosts().length, other.getHosts().length);
        IntStream.range(0, settings.getHosts().length).forEach((i) -> {
            Assertions.assertEquals(settings.getHosts()[i].getHostName(), other.getHosts()[i].getHostName());
            Assertions.assertEquals(settings.getHosts()[i].getPort(), other.getHosts()[i].getPort());
        });
    }

    @ParameterizedTest
    @MethodSource("validConnectionStrings")
    public void test(String connectionString, String json) throws ConnectionStringParsingException, JsonProcessingException {

        EventStoreDBClientSettings expectedSettings = this.parseJson(json);
        EventStoreDBClientSettings parsedSettings = EventStoreDBConnectionString.parseOrThrow(connectionString);

        this.assertEquals(expectedSettings, parsedSettings);
    }

    private EventStoreDBClientSettings parseJson(String input) throws JsonProcessingException {
        ConnectionSettingsBuilder builder = EventStoreDBClientSettings.builder();
        JsonNode tree = mapper.readTree(input);

        if (tree.get("dnsDiscover") != null)
            builder.dnsDiscover(tree.get("dnsDiscover").asBoolean());

        if (tree.get("maxDiscoverAttempts") != null)
            builder.maxDiscoverAttempts(tree.get("maxDiscoverAttempts").asInt());

        if (tree.get("discoveryInterval") != null)
            builder.discoveryInterval(tree.get("discoveryInterval").asInt());

        if (tree.get("gossipTimeout") != null)
            builder.gossipTimeout(tree.get("gossipTimeout").asInt());

        if (tree.get("tls") != null)
            builder.tls(tree.get("tls").asBoolean());

        if (tree.get("tlsVerifyCert") != null)
            builder.tlsVerifyCert(tree.get("tlsVerifyCert").asBoolean());

        if (tree.get("keepAliveTimeout") != null)
            builder.keepAliveTimeout(Long.parseLong(tree.get("keepAliveTimeout").asText()));

        if (tree.get("keepAliveInterval") != null)
            builder.keepAliveInterval(Long.parseLong(tree.get("keepAliveInterval").asText()));

        if (tree.get("nodePreference") != null) {
            NodePreference pref;

            switch (tree.get("nodePreference").asText().toLowerCase()) {
                case "leader":
                    pref = NodePreference.LEADER;
                    break;
                case "follower":
                    pref = NodePreference.FOLLOWER;
                    break;
                case "random":
                    pref = NodePreference.RANDOM;
                    break;
                case "readonlyreplica":
                    pref = NodePreference.READ_ONLY_REPLICA;
                    break;
                default:
                    throw new RuntimeException(String.format("Unsupported node pref value: '%s'", tree.get("nodePreference").asText()));
            }

            builder.nodePreference(pref);
        }

        if (tree.get("defaultDeadline") != null) {
            builder.defaultDeadline(tree.get("defaultDeadline").asLong());
        }

        if (tree.get("tlsCaFile") != null)
            builder.tlsCaFile(tree.get("tlsCaFile").asText());

        if (tree.get("defaultClientCertificate") != null) {
            JsonNode defaultClientCertificate = tree.get("defaultClientCertificate");

            builder.defaultClientCertificate(
                    defaultClientCertificate.get("clientCertFile").asText(),
                    defaultClientCertificate.get("clientKeyFile").asText());
        }

        tree.get("hosts").elements().forEachRemaining((host) -> {
            builder.addHost(new InetSocketAddress(host.get("address").asText(), host.get("port").asInt()));
        });

        if (tree.get("features") != null) {
           JsonNode features = tree.get("features");

           if (features.isArray())
               features.elements().forEachRemaining(feature -> builder.feature(feature.asText()));
           else
               builder.feature(features.asText());
        }

        return builder.buildConnectionSettings();
    }

    @Test
    public void testPortNumbers() {
        //Test gossip seeds with port numbers higher than 32000.
        EventStoreDBClientSettings settings = EventStoreDBConnectionString.parseOrThrow("esdb://localhost:4500,localhost:50000?feature=foobar&feature=baz");
        Assertions.assertNotNull(settings);
        Assertions.assertEquals(2,settings.getHosts().length);
    }
}
