# Payment Orchestration Platform

A production-minded Spring Boot reference service that accepts idempotent payment requests, routes them through ordered providers, distinguishes retryable failures from hard declines, and records domain events through a transactional outbox.

## Why this project matters

Payment systems fail in ambiguous ways: callers retry, providers time out after charging, and event publication can fail after a database commit. This repository demonstrates the boundaries and invariants used to make those cases safe.

## Features

- Idempotent `POST /v1/payments` API backed by a database uniqueness constraint
- SHA-256 request fingerprints that reject unsafe idempotency-key reuse with HTTP 409
- Ordered primary/fallback routing with explicit failure taxonomy
- Per-provider circuit breakers with a single half-open recovery probe
- Optimistic locking for concurrent state changes
- Transactional outbox for reliable downstream event delivery
- Kafka relay with database row leasing (`SKIP LOCKED`) for safe horizontal scaling
- Signed provider webhooks with HMAC-SHA256 verification and replay protection
- Durable retry scheduling with capped exponential backoff and deterministic jitter
- Concurrent retry workers using PostgreSQL `SKIP LOCKED` leasing
- Prometheus business metrics, provider-latency histograms, and correlation IDs
- Flyway-managed PostgreSQL schema
- Bean Validation and consistent error responses
- Health, metrics, OpenAPI, Docker Compose, and GitHub Actions
- Unit tests for duplicate requests, failover, and hard-decline behavior
- Testcontainers integration tests for Flyway migrations, database constraints, and PostgreSQL leasing

## Stack

Java 17 · Spring Boot 3 · PostgreSQL · Kafka · Flyway · JPA · Maven · Docker · Testcontainers · JUnit 5 · Mockito

## Run locally

Prerequisites: Java 17, Docker, and Maven 3.9+.

```bash
docker compose up -d postgres kafka
mvn spring-boot:run
```

Open Swagger UI at `http://localhost:8080/docs` and health at `http://localhost:8080/actuator/health`.
Prometheus metrics are available at `http://localhost:8080/actuator/prometheus`. Every HTTP response includes `X-Correlation-Id`; callers may supply a valid ID to correlate logs across services.

## Example

```bash
curl -i http://localhost:8080/v1/payments \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: checkout-20260905-001' \
  -d '{"merchantId":"store-42","amount":49.99,"currency":"EUR"}'
```

Repeat the request with the same key to receive the original payment rather than creating a second charge.
Reusing that key with a different merchant, amount, or currency returns `409 IDEMPOTENCY_CONFLICT`.

## Failure simulation

The included adapters are deterministic and make the project easy to demo:

| Condition | Outcome |
|---|---|
| Amount above `9999` | Hard decline; routing stops |
| Amount divisible by `13` | Primary timeout; fallback succeeds |
| All other positive amounts | Primary succeeds |

## Provider webhooks

Provider callbacks are accepted at `POST /v1/provider-webhooks`. The signature is the lowercase HMAC-SHA256 hex digest of the exact request body using `WEBHOOK_SECRET`. Required headers are `X-Provider`, `X-Event-Id`, and `X-Signature`. The `(provider, event ID)` uniqueness constraint makes retries safe.

## Provider resilience

Three consecutive transient failures open a provider's circuit for 30 seconds. Requests bypass the unhealthy provider and continue through the ordered fallback chain. After the cool-down, exactly one request is admitted as a half-open probe; success closes the circuit, while another transient failure reopens it. Business declines never affect provider health. Both thresholds are configurable under `payments.providers.circuit-breaker`, and bypasses are exported as `payments.provider.circuit.open.total`.

## Design

See [architecture decisions](docs/architecture.md) for request flow, trade-offs, and production extensions.

## Roadmap

- OpenTelemetry traces and a Grafana dashboard
- Testcontainers integration suite and load-test profile

## License

MIT
