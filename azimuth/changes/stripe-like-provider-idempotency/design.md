# Design: Move duplicate-refund defect to provider adapter

The business RefundId and the provider idempotency key are distinct concepts even when the correct
mapping makes their text equal. The request body carries RefundId as business correlation; the
`Idempotency-Key` header identifies one provider operation. This preserves the ownership boundary:
our worker chooses the key, while the external provider decides whether two calls name one
operation.

The provider's internal idempotency store is keyed by the supplied idempotency key. It atomically
creates one stored request and financial effect, returns that stored result for an identical
repeat, and rejects a
different RefundId, payment, amount or currency under the same key. Different keys are independent
operations. The simulator therefore remains correct when the unsafe worker causes two refunds.

The normal worker maps `RefundId -> Idempotency-Key` deterministically. A demo-profile-only policy
can instead generate a key per call. Startup rejects that policy outside the demo profile. The
delivery failpoint remains after provider success and before result publication, so RabbitMQ
redelivery invokes the mapping twice at the financially ambiguous boundary.

Provider diagnostics enumerate request attempts and public refund operations by business RefundId.
This fixture-only reconciliation interface is not part of the Stripe-like write contract and does
not represent privileged access to a real provider's internal ledger.

Archived changes retain the earlier provider-owned falsifier as historical provenance. The
current design and teaching account explicitly revise it rather than silently rewriting history.
