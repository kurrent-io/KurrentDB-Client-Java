package io.kurrent.dbclient;

import io.kurrent.dbclient.databases.DockerContainerDatabase;
import io.kurrent.dbclient.databases.ExternallyCreatedCluster;

import java.util.Optional;

public class DatabaseFactory {
    public static Database spawn() {
        boolean secure = Boolean.parseBoolean(Optional.ofNullable(System.getenv("SECURE")).orElse("false"));
        boolean cluster = Boolean.parseBoolean(Optional.ofNullable(System.getenv("CLUSTER")).orElse("false"));

        if (cluster)
            return new ExternallyCreatedCluster(secure);

        return singleNodeBuilder()
                .secure(secure)
                .build();
    }

    public static Database spawnEnterpriseWithPluginsEnabled(String... pluginsToEnable) {
        boolean secure = Boolean.parseBoolean(Optional.ofNullable(System.getenv("SECURE")).orElse("false"));
        boolean cluster = Boolean.parseBoolean(Optional.ofNullable(System.getenv("CLUSTER")).orElse("false"));

        if (cluster)
            return new ExternallyCreatedCluster(secure);

        DockerContainerDatabase.Builder builder = singleNodeBuilder();

        for (String plugin : pluginsToEnable)
            builder.env(String.format("EventStore__Plugins__%s__Enabled", plugin), "true");

        return builder.secure(secure).build();
    }

    private static DockerContainerDatabase.Builder singleNodeBuilder() {
        return DockerContainerDatabase
                .builder()
                .image(Optional
                        .ofNullable(System.getenv("KURRENTDB_IMAGE"))
                        .orElse(DockerContainerDatabase.DEFAULT_IMAGE));
    }
}
