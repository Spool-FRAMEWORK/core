# Spool — Core

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://openjdk.org/)
[![Maven](https://img.shields.io/badge/build-Maven-C71A36.svg)](https://maven.apache.org/)

**Core** is the shared foundation of the Spool framework. It defines the **event model**, **port interfaces**, **exception hierarchy**, **error routing**, and **in-memory adapters** used by the Crawler, Publisher, and Ingester modules.

> You normally don't depend on Core directly — the module-specific artifacts (Crawler, Feeder, Ingester) bring it in transitively. This README is useful for understanding the contracts each module relies on.

---

## Table of Contents

- [Architecture](#architecture)
- [Package Overview](#package-overview)
- [Event Model](#event-model)
- [Ports (Interfaces)](#ports-interfaces)
- [Safe Decorators](#safe-decorators)
- [Error Router](#error-router)
- [Channel Router](#channel-router)
- [Exception Hierarchy](#exception-hierarchy)
- [In-Memory Adapters](#in-memory-adapters)
- [Serialization Factories](#serialization-factories)
- [Installation](#installation)
- [License](#license)

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         spool-core                              │
│                                                                 │
│  model/            Event, SpoolEvent, InboxItem, IdempotencyKey │
│                    InboxItemStored, ItemPublished, ItemPersisted │
│                    + failure events + InboxItemStatus            │
│                                                                 │
│  port/             EventBusEmitter, EventBusListener, EventBus  │
│                    InboxUpdater, PayloadDeserializer,            │
│                    RecordSerializer, Subscription                │
│                                                                 │
│  port/decorator/   SafeEventBusEmitter, SafeEventBusListener,   │
│                    SafeInboxUpdater                              │
│                                                                 │
│  control/          Handler<T>                                   │
│                                                                 │
│  utils/            ErrorRouter, ChannelRouter,                  │
│                    ChannelNamingConvention, NamingConvention,    │
│                    DomainEventMapping                            │
│                                                                 │
│  exception/        SpoolException hierarchy (13 types)          │
│                                                                 │
│  adapter/          InMemoryEventBus, InMemorySubscription       │
│                                                                 │
│  infrastructure/   PayloadDeserializerFactory,                  │
│   adapter/         RecordSerializerFactory, DomainMapperFactory │
└─────────────────────────────────────────────────────────────────┘
```

---

## Package Overview

| Package | Purpose |
|---|---|
| `software.spool.core.model` | Immutable event records and value objects shared across all modules. |
| `software.spool.core.port` | Port interfaces (hexagonal architecture) that modules depend on. |
| `software.spool.core.port.decorator` | `Safe*` decorators that normalise unchecked exceptions into typed `SpoolException` subclasses. |
| `software.spool.core.control` | The `Handler<T>` functional interface used by the event bus. |
| `software.spool.core.utils` | Cross-cutting utilities: error routing, channel routing, naming conventions. |
| `software.spool.core.exception` | Typed exception hierarchy rooted at `SpoolException`. |
| `software.spool.core.adapter` | In-memory implementations for local testing. |
| `software.spool.core.infrastructure.adapter` | Jackson-based factories for serializers and deserializers. |

---

## Event Model

All events implement `Event` (unique ID, causation/correlation IDs, timestamp). Framework events also implement `SpoolEvent`.

### Pipeline Events

```
Crawler                  Publisher                Ingester
───────                  ─────────                ────────
SourceItemCaptured  ──►  InboxItemStored  ──►  ItemPublished  ──►  ItemPersisted
```

| Event | Emitted By | Description |
|---|---|---|
| `SourceItemCaptured` | Crawler | A record was captured and serialized from the source. |
| `InboxItemStored` | Crawler | A record was written to the inbox. |
| `ItemPublished` | Publisher | An inbox item was published to the event bus. |
| `InboxItemConsumed` | Ingester | An inbox item was consumed for ingestion. |
| `ItemPersisted` | Ingester | An item was persisted to the data lake. |

### Failure Events

| Event | Description |
|---|---|
| `SourceFetchFailed` | The source could not be fetched. |
| `SourceItemCaptureFailed` | A single record failed to capture/serialize. |
| `InboxItemStoreFailed` | Writing to the inbox failed. |
| `ItemPublishFailed` | Publishing to the event bus failed. |
| `InboxItemConsumptionFailed` | Consuming an inbox item failed. |
| `DataLakePersistFailed` | Persisting a batch to the data lake failed. |

### Value Objects

| Type | Description |
|---|---|
| `IdempotencyKey` | Deduplication identifier. Can be created from a string or derived via SHA-256 digest. |
| `InboxItem` | An inbox entry with status, payload, key, and timestamp. |
| `InboxItemStatus` | Enum: `UNPUBLISHED → PUBLISHING → PUBLISHED → INGESTED → PERSISTED`. |

### Building Events

All SpoolEvent records use a fluent builder:

```java
ItemPublished event = ItemPublished.builder()
        .from(inboxItemStored)   // copies correlationId, causationId, idempotencyKey
        .payload(serializedData)
        .build();
```

---

## Ports (Interfaces)

Ports define the contracts that modules program against. Provide your own implementations for production infrastructure.

| Port | Direction | Method | Description |
|---|---|---|---|
| `EventBusEmitter` | Output | `emit(Event)` | Publishes an event. |
| `EventBusListener` | Input | `on(Class<E>, Handler<E>)` | Subscribes to an event type. Returns a `Subscription`. |
| `EventBus` | Both | — | Combines emitter and listener. |
| `InboxUpdater` | Output | `update(IdempotencyKey, InboxItemStatus)` | Changes the status of an inbox item. |
| `PayloadDeserializer<T>` | Input | `deserialize(String)` | Converts a raw string payload into a typed object. |
| `RecordSerializer<T>` | Output | `serialize(T)` | Converts a typed record into a string. |
| `Subscription` | — | `cancel()`, `isActive()` | Handle for unsubscribing from events. |
| `Handler<T>` | — | `handle(T)` | Functional interface for event handlers. |

---

## Safe Decorators

Each `Safe*` decorator wraps a port and normalises any unchecked exception into the corresponding typed `SpoolException`. Exceptions that are already a `SpoolException` are rethrown as-is.

| Decorator | Wraps | Throws |
|---|---|---|
| `SafeEventBusEmitter` | `EventBusEmitter` | `EventBusEmitException` |
| `SafeEventBusListener` | `EventBusListener` | `EventBusListenException` |
| `SafeInboxUpdater` | `InboxUpdater` | `InboxUpdateException` |

```java
// Wrap any emitter to get typed exceptions
EventBusEmitter safe = SafeEventBusEmitter.of(myEmitter);
```

---

## Error Router

`ErrorRouter` dispatches exceptions to type-specific handlers in registration order (first match wins).

```java
ErrorRouter router = new ErrorRouter()
        .on(SourcePollException.class, (e, event) -> log.warn("Poll failed", e))
        .on(SerializationException.class, (e, event) -> log.error("Serialization failed", e))
        .orElse((e, event) -> log.error("Unexpected error", e));

// Dispatch an exception
router.dispatch(exception);           // without context
router.dispatch(exception, event);    // with event context
```

---

## Channel Router

`ChannelRouter` maps event types to named channels. When no explicit route exists, a default name is derived from the event class using the configured `ChannelNamingConvention`.

```java
ChannelRouter router = ChannelRouter.defaults()  // DOT_CASE convention
        .route(OrderReceived.class, "orders")
        .route(PaymentProcessed.class, "payments");

String ch = router.resolve(event);
// "orders", "payments", or e.g. "inbox.item.stored" (DOT_CASE default)
```

### Naming Conventions

| Convention | Example (`OrderReceived`) |
|---|---|
| `ChannelNamingConvention.DOT_CASE` | `order.received` |
| `ChannelNamingConvention.SNAKE_CASE` | `order_received` |
| `ChannelNamingConvention.KEBAB_CASE` | `order-received` |

---

## Exception Hierarchy

```
RuntimeException
 └─ SpoolException (abstract)
     ├─ SourcePollException
     ├─ SourceOpenException
     ├─ DeserializationException
     ├─ SerializationException
     ├─ SplitException
     ├─ InboxWriteException
     ├─ InboxUpdateException
     ├─ InboxReadException
     ├─ EventBusEmitException
     ├─ EventBusListenException
     └─ DuplicateEventException

 RuntimeException
  └─ SpoolContextException  (wraps exception + SpoolEvent context)
```

Each exception type can be handled independently by the `ErrorRouter`.

---

## In-Memory Adapters

Provided for testing and local development:

| Class | Implements | Notes |
|---|---|---|
| `InMemoryEventBus` | `EventBus` | Thread-safe, synchronous dispatch via `ConcurrentHashMap`. |
| `InMemorySubscription` | `Subscription` | Runs a `Runnable` on cancel. Idempotent. |

```java
InMemoryEventBus bus = new InMemoryEventBus();

bus.on(ItemPublished.class, event -> System.out.println("Published: " + event.payload()));
bus.emit(ItemPublished.builder().idempotencyKey(key).payload("data").build());
```

---

## Serialization Factories

### PayloadDeserializerFactory

Ready-made deserializers backed by Jackson:

| Method | Output Type | Description |
|---|---|---|
| `json()` | `JsonNode` | Parse any JSON string. |
| `jsonArray()` | `JsonNode` | Parse and validate JSON array. |
| `yaml()` | `JsonNode` | Parse any YAML string. |
| `yamlArray()` | `JsonNode` | Parse and validate YAML sequence. |
| `textLines()` | `List<String>` | Split into trimmed non-empty lines. |
| `csv()` | `List<Map<String, String>>` | Parse CSV with header row. |

### RecordSerializerFactory

| Method | Input Type | Description |
|---|---|---|
| `jsonNode()` | `JsonNode` | Serialize to compact JSON. |
| `record()` | Any `<T>` | Serialize any object to JSON. |
| `map()` | `Map<String, Object>` | Serialize a map to JSON. |

### DomainMapperFactory

JSON-to-Java object deserializers with naming strategy support:

| Method | Naming Strategy |
|---|---|
| `camelCase(Class<D>)` | `camelCase` (Jackson default) |
| `snakeCase(Class<D>)` | `snake_case` |
| `pascalCase(Class<D>)` | `PascalCase` |
| `kebabCase(Class<D>)` | `kebab-case` |

```java
PayloadDeserializer<MyDto> deserializer = DomainMapperFactory.snakeCase(MyDto.class);
MyDto dto = deserializer.deserialize("{\"user_name\": \"alice\"}");
```

---

## Installation

### Maven (GitHub Packages)

```xml
<dependency>
    <groupId>io.github.spool-framework</groupId>
    <artifactId>core</artifactId>
    <version>1.0.15-SNAPSHOT</version>
</dependency>
```

Configure the GitHub Packages repository in your `pom.xml` or `settings.xml`:

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/Spool-FRAMEWORK/*</url>
    </repository>
</repositories>
```

> Requires **Java 17+**.

---

## License

Distributed under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).
