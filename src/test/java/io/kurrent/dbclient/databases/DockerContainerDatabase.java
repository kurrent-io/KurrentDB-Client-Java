package io.kurrent.dbclient.databases;

import io.kurrent.dbclient.*;
import com.github.dockerjava.api.model.HealthCheck;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class DockerContainerDatabase extends GenericContainer<DockerContainerDatabase> implements Database {
    public static final String DEFAULT_IMAGE = "docker.cloudsmith.io/eventstore/kurrent-staging/kurrentdb:ci";

    public static class Builder {
        String image;
        boolean secure;
        boolean anonymous;
        Map<String, String> env;

        public Builder() {
            this.image = DEFAULT_IMAGE;
            this.env = new HashMap<>();
        }

        public Builder secure(boolean secure) {
            this.secure = secure;
            return this;
        }

        public Builder anonymous(boolean anonymous) {
            this.anonymous = anonymous;
            return this;
        }

        public Builder image(String image) {
            this.image = image;
            return this;
        }
        public Builder env(String envVar, String value) {
            this.env.put(envVar, value);
            return this;
        }

        public DockerContainerDatabase build() {
            return new DockerContainerDatabase(this);
        }
    }

    private final Builder builder;
    private final ClientTracker clientTracker;

    public DockerContainerDatabase(Builder builder) {
        super(builder.image);
        addExposedPorts(1113, 2113);

        withEnv("EVENTSTORE_RUN_PROJECTIONS", "ALL");
        withEnv("EVENTSTORE_START_STANDARD_PROJECTIONS", "true");
        withEnv("EVENTSTORE_ENABLE_ATOM_PUB_OVER_HTTP", "true");

        if (builder.secure) {
            verifyCertificatesExist();
            String certsDir = Paths.get(System.getProperty("user.dir"), "certs").toAbsolutePath().toString();

            withEnv("EVENTSTORE_CERTIFICATE_FILE", "/etc/eventstore/certs/node/node.crt");
            withEnv("EVENTSTORE_CERTIFICATE_PRIVATE_KEY_FILE", "/etc/eventstore/certs/node/node.key");
            withEnv("EVENTSTORE_TRUSTED_ROOT_CERTIFICATES_PATH", "/etc/eventstore/certs/ca");
            withFileSystemBind(certsDir, "/etc/eventstore/certs");
        } else {
            withEnv("EVENTSTORE_INSECURE", "true");
        }

        builder.env.forEach((envVar, value) -> withEnv(envVar, value));

        this.builder = builder;
        this.clientTracker = new ClientTracker();

        withCreateContainerCmdModifier(cmd -> cmd.withHealthcheck(new HealthCheck()
                .withInterval(1000000000L)
                .withTimeout(1000000000L)
                .withRetries(10)));

        waitingFor(Wait.forHealthcheck());

        start();
    }

    @Override
    public ConnectionSettingsBuilder defaultSettingsBuilder() {
        ConnectionSettingsBuilder settingsBuilder = KurrentDBClientSettings.builder().addHost(getHost(), getMappedPort(2113));

        if (!builder.anonymous)
            settingsBuilder.defaultCredentials("admin", "changeit");

        if (builder.secure)
            settingsBuilder.tls(true).tlsVerifyCert(false);
        else
            settingsBuilder.tls(false);

        return settingsBuilder;
    }

    @Override
    public ClientTracker getClientTracker() {
        return clientTracker;
    }

    @Override
    public void cleanup() {
        try {
            try {
                ExecResult checkDir = execInContainer("sh", "-c", "[ -d /var/log/eventstore ] && echo 'eventstore' || [ -d /var/log/kurrentdb ] && echo 'kurrentdb' || echo 'none'");
                String logDir = checkDir.getStdout().trim();
                
                if ("eventstore".equals(logDir)) {
                    logger().info("Collecting logs from /var/log/eventstore");
                    ExecResult result = execInContainer("tar", "-czvf", "/tmp/esdb_logs.tar.gz", "/var/log/eventstore");
                    if (result.getExitCode() == 0) {
                        copyFileFromContainer("/tmp/esdb_logs.tar.gz", "/tmp/esdb_logs.tar.gz");
                    } else {
                        logger().warn("Failed to compress logs: {}", result.getStderr());
                    }
                } else if ("kurrentdb".equals(logDir)) {
                    logger().info("Collecting logs from /var/log/kurrentdb");
                    ExecResult result = execInContainer("tar", "-czvf", "/tmp/esdb_logs.tar.gz", "/var/log/kurrentdb");
                    if (result.getExitCode() == 0) {
                        copyFileFromContainer("/tmp/esdb_logs.tar.gz", "/tmp/esdb_logs.tar.gz");
                    } else {
                        logger().warn("Failed to compress logs: {}", result.getStderr());
                    }
                } else {
                    logger().warn("No log directory found at /var/log/eventstore or /var/log/kurrentdb, skipping log collection");
                }
            } catch (Exception logException) {
                logger().warn("Could not collect container logs (this is not critical): {}", logException.getMessage());
            }
        } catch (Exception e) {
            logger().error("Error when cleanup docker container", e);
        } finally {
            stop();
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private static void verifyCertificatesExist() {
        String currentDir = System.getProperty("user.dir");
        String[][] files = {
                {"ca", "ca.crt"},
                {"ca", "ca.key"},
                {"node", "node.crt"},
                {"node", "node.key"},
        };

        for (String[] strings : files) {
            File file = Paths.get(currentDir, "certs", strings[0], strings[1]).toAbsolutePath().toFile();

            if (!file.exists())
                throw new RuntimeException(new FileNotFoundException(file.getAbsolutePath()));
        }
    }
}
