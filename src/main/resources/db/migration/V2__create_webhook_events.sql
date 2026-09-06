CREATE TABLE webhook_events (
  id UUID PRIMARY KEY,
  provider VARCHAR(100) NOT NULL,
  event_id VARCHAR(255) NOT NULL,
  event_type VARCHAR(100) NOT NULL,
  payload TEXT NOT NULL,
  received_at TIMESTAMPTZ NOT NULL,
  CONSTRAINT uk_webhook_provider_event UNIQUE(provider,event_id)
);
CREATE INDEX idx_payments_provider_reference ON payments(provider_reference);
