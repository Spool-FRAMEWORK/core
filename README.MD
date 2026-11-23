# Spool — Core

[![License](https://img.shields.io/badge/License-AGPL%203.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-21%2B-orange.svg)](https://openjdk.org/)
[![Maven](https://img.shields.io/badge/build-Maven-C71A36.svg)](https://maven.apache.org/)

**Core** is the shared foundation of the Spool framework. It defines the **`SpoolNode` orchestrator**, **event model**, **`Envelope` data contract**, **port interfaces**, and **in-memory adapters** used by all modules.

> You normally don't depend on Core directly — Crawler, Janitor, and Ingester bring it in transitively. This README is useful for understanding the contracts all modules share.

---

## Table of Contents

- [What it provides](#what-it-provides)
- [Installation](#installation)
- [SpoolNode — Assembling and Starting Modules](#spoolnode--assembling-and-starting-modules)
- [Event Bus](#event-bus)
- [Envelope Model](#envelope-model)
- [Error Router](#error-router)
- [In-Memory Adapters](#in-memory-adapters)
- [License](#license)

---

## What it provides

```
spool-core
│
├── model/spool/   SpoolNode, SpoolModule
├── model/vo/      Envelope, IdempotencyKey, MediaType
├── model/         EnvelopeStatus, Event
├── port/bus/      EventBus, EventPublisher, EventSubscriber, Handler
├── port/inbox/    InboxWriter, InboxUpdater, InboxStatusQuery, InboxEnvelopeResolver
├── utils/routing/ ErrorRouter, ChannelRouter
├── exception/     SpoolException hierarchy
└── adapter/memory InMemoryEventBus
```

---

## Installation

```xml
<dependency>
    <groupId>io.github.spool-framework</groupId>
    <artifactId>core</artifactId>
    <version>1.0.15-SNAPSHOT</version>
</dependency>
```

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/Spool-FRAMEWORK/*</url>
    </repository>
</repositories>
```

> Requires **Java 21+**.

---

## SpoolNode — Assembling and Starting Modules

`SpoolNode` is the central orchestrator. It registers modules, exposes a health endpoint, and manages the start/stop lifecycle.

```java
InMemoryEventBus bus = new InMemoryEventBus();

Crawler crawler = CrawlerBuilderFactory.poll(source)
        .source().ports(ports).and()
        .createWith(StandardNormalizer.JSON_ARRAY);

Ingester ingester = IngesterBuilderFactory.buffered()
        .from(bus).storesWith(writer).readWith(resolver).on(bus)
        .create();

// Register all modules and start — health endpoint on port 8080
SpoolNode node = SpoolNode.create()
        .register(ingester)   // start downstream first
        .register(crawler);

node.start();
// ...
node.stop();
```

You can also use a custom port or attach a `MetricsRegistry`:

```java
SpoolNode node = SpoolNode.create(9090);                              // custom health port
SpoolNode node = SpoolNode.create(new OpenTelemetryMetricsRegistry()); // with metrics
SpoolNode node = SpoolNode.create(9090, metricsRegistry);             // both
```

A running node can also take and drop the modules of a deployment as a group, without restarting, which is how a
node of a cluster runs what the Spool API assigns to it (ADR-031):

```java
node.add("orders", List.of(ingester, crawler)); // starts them, with their routes
node.remove("orders");                          // takes their routes out and stops them
```

A deployment the node already has, or one with a route the node already answers, is refused and leaves nothing
behind; so does one whose module fails to start.

Two nodes can be merged into one:

```java
SpoolNode combined = nodeA.and(nodeB);
combined.start();
```

---

## Event Bus

`InMemoryEventBus` implements both `EventPublisher` and `EventSubscriber` and is the default for local development.

```java
InMemoryEventBus bus = new InMemoryEventBus();

// Subscribe to an event type
bus.subscribe(ItemPersisted.class, event ->
        System.out.println("Persisted: " + event.idempotencyKey()));

// Publish an event
bus.publish(new ItemPersisted(...));
```

For production, swap `InMemoryEventBus` for a Kafka-backed implementation from `spool-infrastructure`.

---

## Envelope Model

`Envelope` is the core data carrier. It wraps the raw payload together with metadata, status, and retry information.

```java
public record Envelope(
    IdempotencyKey idempotencyKey,
    EventMetadata  metadata,
    MediaType      mediaType,
    byte[]         payload,
    EnvelopeStatus status,
    int            retries,
    Instant        capturedAt,
    Instant        updatedAt
) {}
```

### Envelope Status Lifecycle

```
CAPTURED ──► PERSISTED
    │
    └──► QUARANTINED  (failed validation or exceeded retries)
```

| Status | When |
|---|---|
| `CAPTURED` | Stored in the inbox by the Crawler. |
| `PERSISTED` | Written to the data lake: the janitor marks it when the ingester publishes `EnvelopePersisted`. |
| `QUARANTINED` | Failed validation or exceeded retry limit. |

---

## Error Router

`ErrorRouter` dispatches exceptions to type-specific handlers. First matching registration wins.

```java
ErrorRouter router = new ErrorRouter()
        .on(SourcePollException.class, (e, ctx) -> log.warn("Poll failed: {}", e.getMessage()))
        .on(InboxWriteException.class, (e, ctx) -> metrics.increment("inbox.failures"))
        .orElse((e, ctx) -> log.error("Unexpected error", e));
```

---

## In-Memory Adapters

Drop-in implementations for local development and testing. No external dependencies required.

| Class | Implements | Notes |
|---|---|---|
| `InMemoryEventBus` | `EventBus` | Thread-safe, synchronous dispatch via `ConcurrentHashMap`. |
| `InMemorySubscription` | `Subscription` | Runs a `Runnable` on cancel. Idempotent. |

---

## License

Distributed under the [GNU Affero General Public License v3.0](LICENSE).
