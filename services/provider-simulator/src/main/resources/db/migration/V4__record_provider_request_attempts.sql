CREATE TABLE provider_request_attempts (
    attempt_id UUID PRIMARY KEY,
    idempotency_key VARCHAR(255) NOT NULL,
    refund_id UUID NOT NULL,
    payment_id UUID NOT NULL,
    amount NUMERIC NOT NULL CHECK (amount > 0),
    currency VARCHAR(3) NOT NULL CHECK (currency ~ '^[A-Z]{3}$'),
    attempted_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX provider_request_attempts_refund_id_idx
    ON provider_request_attempts (refund_id);
