# Architecture decisions

## Request flow

```mermaid
flowchart TD
  Client[API client] --> API[Payment API]
  API --> Idem{Existing key?}
  Idem -->|yes| Result[Return original result]
  Idem -->|no| Router[Ordered provider router]
  Router --> Primary[Primary provider]
  Primary -->|transient error| Fallback[Fallback provider]
  Primary -->|success or hard decline| Store[(PostgreSQL)]
  Fallback --> Store
  Store --> Outbox[Transactional outbox]
  Outbox --> Relay[Leased relay workers]
  Relay --> Kafka[(Kafka)]
```

## ADR-001: Idempotency is a database invariant

`Idempotency-Key` has a unique constraint. Application lookup makes normal retries cheap; the constraint remains the final guard against concurrent duplicate requests.

## ADR-002: Provider fallback is ordered

Only transient errors and soft declines may advance to another provider. Hard declines stop immediately, preventing repeated authorization attempts and customer confusion.

## ADR-003: Domain state and events commit together

The payment update and outbox row share one transaction. Relay workers lock small batches using `FOR UPDATE SKIP LOCKED`, publish them to Kafka, and mark them published in the same relay transaction. A broker failure rolls the transaction back so the row is retried. Delivery is at least once, so consumers must deduplicate by event identity.

## ADR-004: Webhooks are authenticated and replay-safe

The service verifies an HMAC-SHA256 signature over the untouched request body using constant-time comparison. It stores the provider event before applying state and enforces uniqueness on `(provider, event ID)`. Provider retries therefore return success without repeating the state transition.

## ADR-005: Retries are durable work, not sleeping threads

Exhausted transient provider attempts create a PostgreSQL retry row with capped exponential backoff and deterministic jitter. Workers lease due rows with `SKIP LOCKED`, so multiple instances can process concurrently without duplicate leases. Success or a hard decline terminates retrying; reaching the configured limit creates a terminal failure event.

## ADR-006: Operational signals reflect business outcomes

Prometheus counters expose created payments, idempotent replays, provider outcomes, and final payment states. Provider latency uses histograms suitable for p95/p99 alerts. Correlation IDs are accepted only from a bounded safe character set, returned to clients, and placed in logging context for cross-service investigation.

## Production extensions

- Persist request fingerprints and reject key reuse with a different body.
- Lease outbox rows using `FOR UPDATE SKIP LOCKED`, publish to Kafka, then mark published.
- Replace demo providers with isolated HTTP clients using timeouts, circuit breakers, and signed requests.
- Encrypt provider tokens and minimize PCI scope through hosted fields/tokenization.
- Add retry scheduling with exponential backoff, jitter, and a terminal dead-letter path.
