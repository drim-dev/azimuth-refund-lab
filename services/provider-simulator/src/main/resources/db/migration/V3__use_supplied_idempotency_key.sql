ALTER TABLE provider_financial_effects
    DROP CONSTRAINT provider_financial_effects_refund_id_fkey;

ALTER TABLE provider_refund_ledger
    ADD COLUMN idempotency_key VARCHAR(255);

UPDATE provider_refund_ledger
SET idempotency_key = refund_id::text;

ALTER TABLE provider_refund_ledger
    ALTER COLUMN idempotency_key SET NOT NULL,
    DROP CONSTRAINT provider_refund_ledger_pkey,
    ADD PRIMARY KEY (idempotency_key);

CREATE INDEX provider_refund_ledger_refund_id_idx
    ON provider_refund_ledger (refund_id);

ALTER TABLE provider_financial_effects
    ADD COLUMN idempotency_key VARCHAR(255);

UPDATE provider_financial_effects effect
SET idempotency_key = ledger.idempotency_key
FROM provider_refund_ledger ledger
WHERE ledger.refund_id = effect.refund_id;

ALTER TABLE provider_financial_effects
    ALTER COLUMN idempotency_key SET NOT NULL,
    ADD CONSTRAINT provider_financial_effects_idempotency_key_fkey
        FOREIGN KEY (idempotency_key)
        REFERENCES provider_refund_ledger (idempotency_key);
