
<a href="https://kurrent.io">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="KurrentLogo-White.png">
    <source media="(prefers-color-scheme: light)" srcset="KurrentLogo-Black.png">
    <img alt="Kurrent" src="KurrentLogo-Plum.png" height="50%" width="50%">
  </picture>
</a>

# KurrentDB Java Client

[![CI](https://github.com/kurrent-io/KurrentDB-Client-Java/actions/workflows/ci.yml/badge.svg)](https://github.com/kurrent-io/KurrentDB-Client-Java/actions/workflows/ci.yml)
[![LTS](https://github.com/kurrent-io/KurrentDB-Client-Java/actions/workflows/lts.yml/badge.svg)](https://github.com/kurrent-io/KurrentDB-Client-Java/actions/workflows/lts.yml)
[![Previous LTS](https://github.com/kurrent-io/KurrentDB-Client-Java/actions/workflows/previous-lts.yml/badge.svg)](https://github.com/kurrent-io/KurrentDB-Client-Java/actions/workflows/previous-lts.yml)

KurrentDB is a database that's engineered for modern software applications and event-driven architectures. Its
event-native design simplifies data modeling and preserves data integrity while the integrated streaming engine solves
distributed messaging challenges and ensures data consistency.

This repository contains an [KurrentDB](https://kurrent.io) Client SDK written in Java for use with languages on the
JVM. It is compatible with Java 8 and above.

## Access to binaries

Kurrent, Inc publishes GA (general availability) versions
to [Maven Central](https://search.maven.org/artifact/io.kurrent/kurrentdb-client).

## KurrentDB Server Compatibility

This client is compatible with version `20.6.1` upwards.

Server setup instructions can be found in
the [docs](https://developers.kurrent.io/server/v25.0/quick-start/installation), follow the docker setup for the
simplest configuration.

### Documentation

* [Samples](https://github.com/kurrent-io/KurrentDB-Client-Java/tree/trunk/src/test/java/io/kurrent/dbclient/samples)

## Communities

[Join our global community](https://www.kurrent.io/community) of developers.

- [Discuss](https://discuss.kurrent.io/)
- [Discord (Kurrent)](https://discord.gg/Phn9pmCw3t)
- [Discord (ddd-cqrs-es)](https://discord.com/invite/sEZGSHNNbH)

## Contributing

Development is done on the `main` branch.
We attempt to do our best to ensure that the history remains clean and to do so, we generally ask contributors to squash
their commits into a set or single logical commit.

- [Create an issue](https://github.com/kurrent-io/KurrentDB-Client-Java/issues)
- [Documentation](https://docs.kurrent.io/)
- [Contributing guide](https://github.com/kurrent-io/KurrentDB-Client-Java/blob/main/CONTRIBUTING.md)

### Running the tests

The client is built using [`Gradle 8.13`](https://gradle.org). Integration tests run against a server using Docker.

Tests are written using [TestContainers](https://www.testcontainers.org/) and require [Docker](https://www.docker.com/)
to be installed.

Specific docker images can be specified via the environment variable `KURRENTDB_IMAGE`.

## More resources

- [Release notes](https://kurrent.io/blog/release-notes)
- [Beginners Guide to Event Sourcing](https://kurrent.io/event-sourcing)
- [Articles](https://kurrent.io/blog)
- [Webinars](https://kurrent.io/webinars)
- [Contact us](https://kurrent.io/contact)
