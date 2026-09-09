ALTER TABLE payments ADD COLUMN request_hash VARCHAR(64);
COMMENT ON COLUMN payments.request_hash IS 'SHA-256 of canonical payment inputs; null only for rows created before fingerprint rollout';
