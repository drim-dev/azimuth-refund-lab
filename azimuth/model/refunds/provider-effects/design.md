# Design: refunds/provider-effects

## Requirement: approved-refund-applied-once
Mechanism: stable-refund-identity
Enforcement: schema
Mechanism: provider-refund-ledger
Enforcement: choke-point

`stable-refund-identity` carries one API-issued RefundId unchanged through repeated worker calls and
uses it to derive the logical result identity. `provider-refund-ledger` atomically stores one request
and response under RefundId, records the financial effect only when that idempotency record is first
created, returns the stored response to identical repeats and rejects changed content. Financial
effects live in a separate append-only table so evidence does not confuse idempotency state with
money movement. Implementation bindings are derived from annotated Java methods.

The conference cold open can deliberately reapply an identical request, but only when both the
Spring `demo` profile and an explicit unsafe property are active. Startup rejects that property in
every other profile. This constructed failure mode is outside the production mechanism and is
started through a separate Compose overlay.

## Residue

The worker has no durable local inbox because the provider is the durable idempotency authority.
This is valid only while every retry uses the same RefundId and the provider's operation remains
queryable. A future provider without such an idempotency contract requires reconciliation or a
different mechanism, not reuse of this judgment.
