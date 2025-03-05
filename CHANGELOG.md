# Changelog
All notable changes to this project will be documented in this file.

## [Unreleased]

### Fixed
- Disable trace context extraction for deleted events [EventStoreDB-Client-Java#292](https://github.com/EventStore/EventStoreDB-Client-Java/pull/292)

### Fixes
- #291 [EventStoreDB-Client-Java#292](https://github.com/EventStore/EventStoreDB-Client-Java/pull/292)

## [5.4.3] - 2024-11-07
### Fixed
- Disable trace context extraction for deleted events. [EventStoreDB-Client-Java#292](https://github.com/EventStore/EventStoreDB-Client-Java/pull/292)

## [5.4.2] - 2024-10-16
### Added
- Support custom headers. [EventStoreDB-Client-Java#289](https://github.com/EventStore/EventStoreDB-Client-Java/pull/289)

### Changed
- Extract tracing metadata from Event. [EventStoreDB-Client-Java#284](https://github.com/EventStore/EventStoreDB-Client-Java/pull/284)

## [5.4.1] - 2024-07-16
### Changed
- Add user certificates and otel samples. [EventStoreDB-Client-Java#274](https://github.com/EventStore/EventStoreDB-Client-Java/pull/274)
- Support different runtime environments out-of-the-box. [EventStoreDB-Client-Java#279](https://github.com/EventStore/EventStoreDB-Client-Java/pull/279)
- Remove expectation oriented tests. [EventStoreDB-Client-Java#279](https://github.com/EventStore/EventStoreDB-Client-Java/pull/279)
- Fixed bug in the ClientTelemetry whereby injection logic forces all events to have JSON content type. [EventStoreDB-Client-Java#281](https://github.com/EventStore/EventStoreDB-Client-Java/pull/281)

### Fixed
- Use connection string in user certificate sample. [EventStoreDB-Client-Java#276](https://github.com/EventStore/EventStoreDB-Client-Java/pull/276)
- Fix connection service skipping discovery interval sleeps. [EventStoreDB-Client-Java#278](https://github.com/EventStore/EventStoreDB-Client-Java/pull/278)

## [5.4.0] - 2024-05-23
### Added
- new connection settings to provide an x.509 certificate for user authentication. [EventStoreDB-Client-Java#266](https://github.com/EventStore/EventStoreDB-Client-Java/pull/266)

### Changed
- Updated CI workflows and tests to pull eventstore docker images from cloud smith registry. [EventStoreDB-Client-Java#263](https://github.com/EventStore/EventStoreDB-Client-Java/pull/263)
- Added tracing instrumentation of Append and Subscribe (Catchup and Persistent) operations using the Open Telemetry APIs. [EventStoreDB-Client-Java#270](https://github.com/EventStore/EventStoreDB-Client-Java/pull/270)
- Updated everywhere to pull es-gencert-cli from Cloudsmith [EventStoreDB-Client-Java#271](https://github.com/EventStore/EventStoreDB-Client-Java/pull/271)

## [5.3.2] - 2024-03-05
### Changed
- Updated protobuf & protoc to 3.25.3 [EventStoreDB-Client-Java#268](https://github.com/EventStore/EventStoreDB-Client-Java/pull/268)

### Fixed
- https://github.com/EventStore/EventStoreDB-Client-Java/issues/267 [EventStoreDB-Client-Java#268](https://github.com/EventStore/EventStoreDB-Client-Java/pull/268)

## [5.3.1] - 2024-02-29
### Changed
- Updated gRPC client and Protoc to latest, which come with arm64 binaries, enabling compile on arm64 [EventStoreDB-Client-Java#265](https://github.com/EventStore/EventStoreDB-Client-Java/pull/265)

### Fixed
- parsing of server semver for CI, where the server version may have tagging [EventStoreDB-Client-Java#264](https://github.com/EventStore/EventStoreDB-Client-Java/pull/264)

## [5.3.0] - 2024-01-31
### Added
- Expose building a JSON payload event with raw bytes [EventStoreDB-Client-Java#258](https://github.com/EventStore/EventStoreDB-Client-Java/pull/258)

### Fixed
- Fix cluster discovery process. [EventStoreDB-Client-Java#261](https://github.com/EventStore/EventStoreDB-Client-Java/pull/261)

## [5.2.0] - 2023-11-10
### Added
- Support new subscription messages. [EventStoreDB-Client-Java#254](https://github.com/EventStore/EventStoreDB-Client-Java/pull/254)

## [5.1.1] - 2023-11-03
### Fixed
- Fix IllegalStateException when stopping a subscription. [EventStoreDB-Client-Java#250](https://github.com/EventStore/EventStoreDB-Client-Java/pull/250)

## [5.1.0] - 2023-10-31
### Added
- Support certificate file input in connection string and builder. [EventStoreDB-Client-Java#247](https://github.com/EventStore/EventStoreDB-Client-Java/pull/247)

### Changed
- Improve error reporting in SubscriptionListener. [EventStoreDB-Client-Java#245](https://github.com/EventStore/EventStoreDB-Client-Java/pull/245)
- Update dependencies. [EventStoreDB-Client-Java#246](https://github.com/EventStore/EventStoreDB-Client-Java/pull/246)

## [5.0.0] - 2023-10-23
### Changed
- Improve internal gRPC connection management for better error propagation. [EventStoreDB-Client-Java#226](https://github.com/EventStore/EventStoreDB-Client-Java/pull/226)
- Add secure and cluster tests. [EventStoreDB-Client-Java#236](https://github.com/EventStore/EventStoreDB-Client-Java/pull/236)
- Improve connection string parsing. [EventStoreDB-Client-Java#243](https://github.com/EventStore/EventStoreDB-Client-Java/pull/243)

### Added
- Support authenticated gossip read request. [EventStoreDB-Client-Java#235](https://github.com/EventStore/EventStoreDB-Client-Java/pull/235)

### Fixed
- Improve stream metadata serialization. [EventStoreDB-Client-Java#242](https://github.com/EventStore/EventStoreDB-Client-Java/pull/242)

## [4.3.0] - 2023-07-03
### Added
- Support user-provided gRPC client interceptors. [EventStoreDB-Client-Java#233](https://github.com/EventStore/EventStoreDB-Client-Java/pull/233)

## [4.2.0] - 2023-04-27

### Fixed
- Do not start discovery process on ABORT gRPC error. [EventStoreDB-Client-Java#219](https://github.com/EventStore/EventStoreDB-Client-Java/pull/219)
- Fix OptionBase authentication code. [EventStoreDB-Client-Java#221](https://github.com/EventStore/EventStoreDB-Client-Java/pull/221)

### Added
- Provide toString override for public types. [EventStoreDB-Client-Java#218](https://github.com/EventStore/EventStoreDB-Client-Java/pull/218)
- Implement `ExpectedRevision` raw long representation. [EventStoreDB-Client-Java#230](https://github.com/EventStore/EventStoreDB-Client-Java/pull/230)

### Changed
- Increase max inbound message length. [EventStoreDB-Client-Java#222](https://github.com/EventStore/EventStoreDB-Client-Java/pull/222)

## [4.1.1] - 2023-03-06

### Changed
- Stop using Jackson JsonMapper static instances. [EventStoreDB-Client-Java#217](https://github.com/EventStore/EventStoreDB-Client-Java/pull/217)

## [4.1.0] - 2023-02-24

### Added
- Add specific exceptions when delete stream operation fails. [EventStoreDB-Client-Java#208](https://github.com/EventStore/EventStoreDB-Client-Java/pull/208)
- Implement human-representation for `ExpectedVersion` types. [EventStoreDB-Client-Java#204](https://github.com/EventStore/EventStoreDB-Client-Java/pull/204)

### Fixed
- Fix server filtering sample code. [EventStoreDB-Client-Java#206](https://github.com/EventStore/EventStoreDB-Client-Java/pull/206)
- Fix `ConnectionSettingsBuilder` when dealing with keep-alive settings. [EventStoreDB-Client-Java#207](https://github.com/EventStore/EventStoreDB-Client-Java/pull/207)
- Fix `tombstoneStream` overload. [EventStoreDB-Client-Java#205](https://github.com/EventStore/EventStoreDB-Client-Java/pull/205)
- No longer store credentials unprotected in memory. [EventStoreDB-Client-Java#214](https://github.com/EventStore/EventStoreDB-Client-Java/pull/214)

### Changed
- Update gRPC and protobuf version. [EventStoreDB-Client-Java#213](https://github.com/EventStore/EventStoreDB-Client-Java/pull/213)

## [4.0.0] - 2022-11-01

### Fixed
- Fix next expected version when appending to a stream. [EventStoreDB-Client-Java#196](https://github.com/EventStore/EventStoreDB-Client-Java/pull/196)
- Fix condition causing subscribers not to be completed. [EventStoreDB-Client-Java#193](https://github.com/EventStore/EventStoreDB-Client-Java/pull/193)
- Shutdown `GossipClient` after usage. [EventStoreDB-Client-Java#186](https://github.com/EventStore/EventStoreDB-Client-Java/pull/186)
- Fix channel lifecycle behaviour. [EventStoreDB-Client-Java#184](https://github.com/EventStore/EventStoreDB-Client-Java/pull/184)
- Do not shutdown client on leader reconnect attempt. [EventStoreDB-Client-Java#182](https://github.com/EventStore/EventStoreDB-Client-Java/pull/182)
- Fix error signals to the `GrpcClient` based on a `CompletableFuture`. [EventStoreDB-Client-Java#182](https://github.com/EventStore/EventStoreDB-Client-Java/pull/182)

### Changed
- Fix next expected version when appending to a stream. [EventStoreDB-Client-Java#196](https://github.com/EventStore/EventStoreDB-Client-Java/pull/196)
- Add additional logging for connection handling. [EventStoreDB-Client-Java#181](https://github.com/EventStore/EventStoreDB-Client-Java/pull/181)
