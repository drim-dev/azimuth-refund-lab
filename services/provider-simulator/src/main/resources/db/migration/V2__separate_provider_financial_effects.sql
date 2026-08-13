CREATE TABLE provider_financial_effects (
    effect_id UUID PRIMARY KEY,
    refund_id UUID NOT NULL REFERENCES provider_refund_ledger (refund_id),
    payment_id UUID NOT NULL,
    amount NUMERIC NOT NULL CHECK (amount > 0),
    currency VARCHAR(3) NOT NULL CHECK (currency ~ '^[A-Z]{3}$'),
    applied_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX provider_financial_effects_refund_id_idx
    ON provider_financial_effects (refund_id);

INSERT INTO provider_financial_effects (
    effect_id,
    refund_id,
    payment_id,
    amount,
    currency,
    applied_at
)
SELECT
    provider_operation_id,
    refund_id,
    payment_id,
    amount,
    currency,
    applied_at
FROM provider_refund_ledger;
