CREATE TABLE payment_retries (
  id UUID PRIMARY KEY,
  payment_id UUID NOT NULL REFERENCES payments(id),
  attempt_number INTEGER NOT NULL CHECK(attempt_number > 0),
  next_attempt_at TIMESTAMPTZ NOT NULL,
  reason VARCHAR(100) NOT NULL,
  completed BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMPTZ NOT NULL,
  completed_at TIMESTAMPTZ
);
CREATE INDEX idx_payment_retries_due ON payment_retries(next_attempt_at) WHERE completed = FALSE;
