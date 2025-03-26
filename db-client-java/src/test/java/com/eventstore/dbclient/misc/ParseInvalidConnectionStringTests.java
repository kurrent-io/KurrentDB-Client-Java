package com.eventstore.dbclient.misc;

import com.eventstore.dbclient.ConnectionStringParsingException;
import com.eventstore.dbclient.EventStoreDBConnectionString;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;


public class ParseInvalidConnectionStringTests {
    public static Stream<Arguments> invalidConnectionStrings() {
        return Stream.of(
                Arguments.of("localhost"),
                Arguments.of("https://console.eventstore.cloud/"),
                Arguments.of("esbd+discovery://localhost"),
                Arguments.of("esdb://my:great@username:UyeXx8$^PsOo4jG88FlCauR1Coz25q@host?nodePreference=follower&tlsVerifyCert=false"),
                Arguments.of("esdb://host1,host2:200:300?tlsVerifyCert=false"),
                Arguments.of("esdb://localhost/&tlsVerifyCert=false"),
                Arguments.of("esdb://localhost?tlsVerifyCert=false?nodePreference=follower"),
                Arguments.of("esdb://localhost?tlsVerifyCert=false&nodePreference=any"),
                Arguments.of("esdb://localhost?tlsVerifyCert=if you feel like it"),
                Arguments.of("esdb://localhost?keepAliveInterval=-3"),
                Arguments.of("esdb://localhost?keepAliveInterval=sdfksjsfl"),
                Arguments.of("esdb://localhost?keepAliveTimeout=sdfksjsfl"),
                Arguments.of("esdb://localhost?keepAliveTimeout=-3"),
                Arguments.of("esdb://localhost?nodePreference=read_only_replica"),
                Arguments.of("esdb://localhost?userCertFile=/path/to/cert"),
                Arguments.of("esdb://localhost?userKeyFile=/path/to/key"),
                Arguments.of("esdb://localhost:65536,localhost:2113")
       );
    }

    @ParameterizedTest
    @MethodSource("invalidConnectionStrings")
    public void test(String input) throws ConnectionStringParsingException {
        Assertions.assertThrows(RuntimeException.class, () -> {
            EventStoreDBConnectionString.parseOrThrow(input);
        });
    }
}
