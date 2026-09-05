CREATE TABLE payments (
  id UUID PRIMARY KEY,
  idempotency_key VARCHAR(100) NOT NULL,
  merchant_id VARCHAR(255) NOT NULL,
  amount NUMERIC(19,2) NOT NULL CHECK (amount > 0),
  currency CHAR(3) NOT NULL,
  status VARCHAR(32) NOT NULL,
  provider VARCHAR(100), provider_reference VARCHAR(255), failure_code VARCHAR(100),
  attempts INTEGER NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL, updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT uk_payment_idempotency UNIQUE (idempotency_key)
);
CREATE TABLE outbox_events (
  id UUID PRIMARY KEY, aggregate_type VARCHAR(50) NOT NULL, aggregate_id UUID NOT NULL,
  event_type VARCHAR(100) NOT NULL, payload TEXT NOT NULL, created_at TIMESTAMPTZ NOT NULL, published_at TIMESTAMPTZ
);
CREATE INDEX idx_outbox_unpublished ON outbox_events(created_at) WHERE published_at IS NULL;
