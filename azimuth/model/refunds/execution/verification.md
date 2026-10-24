# Verification: refunds/execution

## Claim: repeated-processing-does-not-repeat-refund
Scope: component
Quantification: universal
Oracle: relational

Worker evidence forces the boundary after provider response and before result publication, then
relates two observed outbound requests to one RefundId, payment, amount, currency and supplied
idempotency key. Provider mechanism evidence ranges over sequential and concurrent identical
requests against real PostgreSQL, changed payload under one key, and different keys for one
business refund. It observes one public provider operation and one financial effect for a repeated
key without treating provider persistence as application-owned state.

The composed browser example crosses the web app, BFF, Refund API, RabbitMQ, worker and simulator.
Its fixture-only provider reconciliation endpoint exposes public request history and refund
operations: the test directly observes two attempts with the same stable key, one EUR 30 operation
and financial effect, and eventual rider success. A separate untagged unsafe journey sends two
different keys and observes two operations/effects. It demonstrates the falsifier and is not
positive evidence for the claim.

PIT challenges the lightweight worker key-selection branch. The checked-in execution generated
one relevant mutant, ran one selected linked test, and killed the branch inversion in under one
second. This hardener supports the claim narrowly: it establishes that the selected evidence
rejects this generated wrong key mapping, not that mutation score proves correctness.

## Residual: finite-delivery-range
Accepted: the component pair covers concurrent and sequential multiplicity while the composed
example covers the forced post-provider interruption; expand when another delivery/failure class
is introduced

The word “any” describes the invariant, not an infinite test run. Evidence is considered universal
because the supplied-key contract and stable-key mapping are exercised across the selected
equivalence classes and inspected directly, with the finite boundary recorded here.

## Residual: external-provider-contract
Accepted: the simulator executes the supplied-key semantics locally; qualify the selected payment
provider's documented retention, conflict and replay behavior before production adoption

The local provider is a contract simulator. Its fixture-only request history and refund summary are
not evidence that a real provider exposes identical diagnostics or retains keys for the required
window. Production qualification and reconciliation remain separate operational obligations.

## Claim: interrupted-processing-eventually-completes
Scope: e2e
Quantification: example
Oracle: relational
Residual: the composed journey samples the financially dangerous interruption after payment
success rather than every infrastructure failure or recovery duration
Accepted: component evidence isolates confirmed result publication and idempotent projection; add
another composed equivalence class when a materially different interruption can prevent completion

The composed browser journey forces the post-payment/pre-publication interruption, observes the
refund as pending while the payment provider reports one public refund operation, resumes the
delivery, and relates the final rider-visible succeeded refund to the same identity, amount and
currency. Component evidence separately checks confirmed result publication and idempotent API
projection; it supports diagnosis but does not replace the composed rider oracle.

The evidence does not quantify over every infrastructure failure or unbounded recovery time. It
establishes eventual visibility for the selected retryable boundary without turning “eventually”
into a fixed product deadline.
