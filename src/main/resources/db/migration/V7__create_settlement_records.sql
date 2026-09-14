CREATE TABLE settlement_records (
  id UUID PRIMARY KEY,
  provider VARCHAR(100) NOT NULL,
  external_record_id VARCHAR(255) NOT NULL,
  provider_reference VARCHAR(255) NOT NULL,
  amount NUMERIC(19,2) NOT NULL CHECK (amount > 0),
  currency VARCHAR(3) NOT NULL,
  status VARCHAR(32) NOT NULL,
  payment_id UUID REFERENCES payments(id),
  received_at TIMESTAMPTZ NOT NULL,
  CONSTRAINT uk_settlement_provider_record UNIQUE (provider,external_record_id)
);
CREATE INDEX idx_settlement_status_received ON settlement_records(status,received_at);
CREATE INDEX idx_settlement_provider_reference ON settlement_records(provider_reference);
