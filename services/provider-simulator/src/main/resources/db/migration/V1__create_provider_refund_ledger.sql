CREATE TABLE provider_refund_ledger (
    refund_id UUID PRIMARY KEY,
    payment_id UUID NOT NULL,
    amount NUMERIC NOT NULL CHECK (amount > 0),
    currency VARCHAR(3) NOT NULL CHECK (currency ~ '^[A-Z]{3}$'),
    provider_operation_id UUID NOT NULL UNIQUE,
    outcome VARCHAR(16) NOT NULL CHECK (outcome = 'succeeded'),
    applied_at TIMESTAMPTZ NOT NULL
);
