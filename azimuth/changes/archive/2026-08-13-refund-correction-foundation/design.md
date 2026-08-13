# Design: refund-correction-foundation

## Authority boundaries

The Refund API owns approval, remaining refundable capacity and the user-visible projection. The
provider simulator owns successful financial effects in a separate PostgreSQL instance. Tests query
the provider ledger directly because an API projection cannot establish what the provider applied.

## Durable handoff

The Refund API reserves capacity and writes `refund.requested.v1` to an outbox in one transaction.
A relay publishes with confirms. The worker consumes with manual acknowledgement, calls the provider
using the stable RefundId, publishes `refund.result.v1` with confirms and acknowledges only after
publication. The API records result message identity and the state transition in one transaction.

Delivery remains at least once. Outbox retries and worker failure may repeat messages. Stable
financial identity and durable inbox/outbox state make repeated effects harmless.

## Provider idempotency

The provider ledger has one row per RefundId. The first request records the amount, currency,
outcome and provider operation id. An identical repeat returns that stored result. Reusing the same
RefundId with a different amount, currency or payment identity is a conflict rather than a second
operation.

## Demo failpoint

A one-shot controller under the `demo` Spring profile pauses the worker after the provider response
and before result publication. Releasing it interrupts the delivery so RabbitMQ redelivers. The
business request and event schemas carry no failpoint field.

## Rejected alternatives

One database for API and provider was rejected because a cross-database failure is the subject of
the demonstration. A worker database was rejected because the provider already owns the durable
idempotency record. JPA was rejected for the critical reservation paths because explicit SQL makes
conditional updates and constraints directly reviewable.

## Residue

Authentication is a visible fixture role rather than production identity. RabbitMQ is single-node.
The provider is simulated. The change establishes correct behavior but does not yet claim Azimuth
assurance linkage; the next change will introduce the critical invariant and evaluate the evidence.
