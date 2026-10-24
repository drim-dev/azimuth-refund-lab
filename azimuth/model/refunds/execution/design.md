# Design: refunds/execution

## Requirement: approved-refund-applied-once
Mechanism: stable-provider-idempotency-key
Enforcement: choke-point
Mechanism: provider-idempotency-contract
Enforcement: choke-point

`stable-provider-idempotency-key` maps the API-issued RefundId to the `Idempotency-Key` supplied on
every payment-provider call. Reprocessing the same approved refund therefore names one provider
operation. The request body still carries RefundId as business correlation; equality of the two
values in the correct adapter does not collapse their different responsibilities.

`provider-idempotency-contract` atomically creates one request/result under the supplied key,
records one financial effect only for the first creation, returns that result to an identical
repeat and rejects changed RefundId, payment, amount or currency under the same key. Different
keys denote independent operations. Implementation bindings are derived from annotated Java
methods.

The conference cold open makes the worker choose a new key per delivery, but only when the Spring
`demo` profile and an explicit unsafe property are active. Startup rejects that property in every
other profile. The provider remains conformant: it sees two keys and creates two operations. This
constructed failure mode is outside the production mechanism and starts through a separate
Compose overlay.

## Requirement: interrupted-refund-completes
Mechanism: confirmed-refund-result
Enforcement: choke-point
Mechanism: idempotent-result-projection
Enforcement: schema

After the payment provider returns success, the worker derives one stable result-event identity
from the RefundId, publishes a durable result and waits for broker confirmation before acknowledging
the request. A retry therefore republishes the same logical result. The Refund API records the
result identity once, advances the refund projection to `succeeded`, and exposes that projection to
the rider BFF. The BFF transports the current refund unchanged and the rider view polls pending
refunds until it renders the succeeded state and approved amount.

The interruption boundary—after provider success and before confirmed result publication—is a
design and verification concern. It is deliberately absent from the analyst-facing claim.

## Residue

The worker has no durable local inbox because the payment provider is the durable idempotency
authority. This is valid only while every retry supplies the same key and the provider retains the
operation for the required retry window. A provider without that contract requires reconciliation
or a different mechanism, not reuse of this judgment.
