package com.eventstore.dbclient;

import java.io.IOException;
import java.net.*;

/**
 * Utility class to parse a connection string.
 */
public final class KurrentDBConnectionString {
    KurrentDBConnectionString() {}

    /**
     * Parses a string representation of a client settings. Throws a runtime exception if the connection string is
     * malformed.
     * @return a client settings.
     */
    public static KurrentDBClientSettings parseOrThrow(String connectionString) {
        ConnectionSettingsBuilder builder = KurrentDBClientSettings.builder();

        try {
            URL url = new URL(null, connectionString, new EsdbUrlHandler());
            return ConnectionSettingsBuilder.parseFromUrl(builder, url);
        } catch (MalformedURLException e) {
            if (!connectionString.contains(","))
                throw new RuntimeException(e);

            // We are probably dealing with a connection string that has multiple gossip seeds with port, something
            // the URL standard doesn't support. We replace all commas to '/' so we can parse gossip seeds as URL path
            // segments.
            try {
                URL url = new URL(
                        null,
                        connectionString.replaceAll(",", "/"),
                        new EsdbUrlHandler());

                if (url.getHost().isEmpty())
                    throw new RuntimeException("Connection string doesn't have an host");

                builder.addHost(url.getHost(), url.getPort() == -1 ? 2_113 : url.getPort());

                for (String segment : url.getPath().split("/")) {
                    if (segment.isEmpty())
                        continue;

                    builder.parseGossipSeed(segment);
                }

                return ConnectionSettingsBuilder.parseFromUrl(builder, url);
            } catch (MalformedURLException ignored) {
                // In this case we return the first exception we received.
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Used to support 'esdb' and 'esdb+discover' URL protocol/scheme
     */
    public static class EsdbUrlHandler extends URLStreamHandler {
        @Override
        protected URLConnection openConnection(URL u) throws IOException {
            return null;
        }
    }
}
