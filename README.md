# Payment Orchestration Platform

A production-minded Spring Boot reference service that accepts idempotent payment requests, routes them through ordered providers, distinguishes retryable failures from hard declines, and records domain events through a transactional outbox.

## Why this project matters

Payment systems fail in ambiguous ways: callers retry, providers time out after charging, and event publication can fail after a database commit. This repository demonstrates the boundaries and invariants used to make those cases safe.

## Features

- Idempotent `POST /v1/payments` API backed by a database uniqueness constraint
- Ordered primary/fallback routing with explicit failure taxonomy
- Optimistic locking for concurrent state changes
- Transactional outbox for reliable downstream event delivery
- Flyway-managed PostgreSQL schema
- Bean Validation and consistent error responses
- Health, metrics, OpenAPI, Docker Compose, and GitHub Actions
- Unit tests for duplicate requests, failover, and hard-decline behavior

## Stack

Java 17 · Spring Boot 3 · PostgreSQL · Flyway · JPA · Maven · Docker · JUnit 5 · Mockito

## Run locally

Prerequisites: Java 17, Docker, and Maven 3.9+.

```bash
docker compose up -d postgres
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

## Design

See [architecture decisions](docs/architecture.md) for request flow, trade-offs, and production extensions.

## Roadmap

- Outbox publisher with Kafka and at-least-once delivery
- Provider webhooks with signature validation and deduplication
- Scheduled retry worker with exponential backoff
- OpenTelemetry traces and Prometheus dashboard
- Testcontainers integration suite and load-test profile

## License

MIT
