# Design: refund-redelivery-assurance

## Requirement: approved-refund-applied-once
Mechanism: stable-refund-identity
Enforcement: schema
Mechanism: provider-refund-ledger
Enforcement: choke-point

The API assigns RefundId once and publishes it from a transactional outbox. The worker passes that
identity unchanged as the provider idempotency key, derives one result-event identity from it, and
acknowledges only after confirmed result publication. Repeated delivery is expected rather than
treated as an exactly-once transport guarantee.

The provider ledger owns the financial effect in a separate database. RefundId is its primary key;
the first call stores the request and response atomically, an identical repeat returns the stored
response, and changed financial content under the same identity is a conflict.

## Evidence composition

Worker component evidence forces the post-provider interruption and shows two identical calls plus
one stable result. Provider component evidence ranges sequential and concurrent repeats and reads
the ledger directly. The composed Playwright example proves those mechanisms connect across the
real BFF, API, broker, worker and provider database. No single substituted component test is
credited for the whole distributed predicate.

## Residue

The universal range is finite and selected: concurrent provider calls, sequential repeats and one
forced failure boundary. It is not a proof over every infrastructure failure or an exactly-once
delivery claim. Provider availability, ambiguous external responses and reconciliation with a real
processor remain outside this fixture.
