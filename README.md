# Payment Orchestration Platform

A production-minded Spring Boot reference service that accepts idempotent payment requests, routes them through ordered providers, distinguishes retryable failures from hard declines, and records domain events through a transactional outbox.

## Why this project matters

Payment systems fail in ambiguous ways: callers retry, providers time out after charging, and event publication can fail after a database commit. This repository demonstrates the boundaries and invariants used to make those cases safe.

## Features

- Idempotent `POST /v1/payments` API backed by a database uniqueness constraint
- Ordered primary/fallback routing with explicit failure taxonomy
- Optimistic locking for concurrent state changes
- Transactional outbox for reliable downstream event delivery
- Kafka relay with database row leasing (`SKIP LOCKED`) for safe horizontal scaling
- Signed provider webhooks with HMAC-SHA256 verification and replay protection
- Flyway-managed PostgreSQL schema
- Bean Validation and consistent error responses
- Health, metrics, OpenAPI, Docker Compose, and GitHub Actions
- Unit tests for duplicate requests, failover, and hard-decline behavior

## Stack

Java 17 · Spring Boot 3 · PostgreSQL · Kafka · Flyway · JPA · Maven · Docker · JUnit 5 · Mockito

## Run locally

Prerequisites: Java 17, Docker, and Maven 3.9+.

```bash
docker compose up -d postgres kafka
mvn spring-boot:run
```

Open Swagger UI at `http://localhost:8080/docs` and health at `http://localhost:8080/actuator/health`.

## Example

```bash
curl -i http://localhost:8080/v1/payments \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: checkout-20260905-001' \
  -d '{"merchantId":"store-42","amount":49.99,"currency":"EUR"}'
```

Repeat the request with the same key to receive the original payment rather than creating a second charge.

## Failure simulation

The included adapters are deterministic and make the project easy to demo:

| Condition | Outcome |
|---|---|
| Amount above `9999` | Hard decline; routing stops |
| Amount divisible by `13` | Primary timeout; fallback succeeds |
| All other positive amounts | Primary succeeds |

## Provider webhooks

Provider callbacks are accepted at `POST /v1/provider-webhooks`. The signature is the lowercase HMAC-SHA256 hex digest of the exact request body using `WEBHOOK_SECRET`. Required headers are `X-Provider`, `X-Event-Id`, and `X-Signature`. The `(provider, event ID)` uniqueness constraint makes retries safe.

## Design

See [architecture decisions](docs/architecture.md) for request flow, trade-offs, and production extensions.

## Roadmap

- Scheduled retry worker with exponential backoff
- OpenTelemetry traces and Prometheus dashboard
- Testcontainers integration suite and load-test profile

## License

MIT
