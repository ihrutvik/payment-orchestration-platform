CREATE TABLE refunds (
  id UUID PRIMARY KEY,
  payment_id UUID NOT NULL REFERENCES payments(id),
  idempotency_key VARCHAR(100) NOT NULL,
  amount NUMERIC(19,2) NOT NULL CHECK (amount > 0),
  currency VARCHAR(3) NOT NULL,
  status VARCHAR(32) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL,
  CONSTRAINT uk_refund_idempotency UNIQUE (idempotency_key)
);
CREATE INDEX idx_refunds_payment ON refunds(payment_id);
