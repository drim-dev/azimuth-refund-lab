# Verification: Move duplicate-refund defect to provider adapter

Provider component evidence uses real PostgreSQL and ranges over sequential repeats, concurrent
repeats, changed payload fields and different keys for one business refund. Worker component
evidence uses real RabbitMQ, forces the post-provider interruption and records both HTTP attempts,
including their method, path, key and body.

The canonical composed evidence must observe two provider attempts after redelivery, the same
business RefundId and idempotency key, exactly one EUR 30 financial effect, and eventual rider
success. The unsafe composed journey performs the same browser actions but expects two keys and two
effects; it is a falsifier fixture and must not carry a `Covers` relation.

PIT targets the lightweight worker key policy. A mutation that selects the per-delivery branch in
normal mode must be killed by linked evidence. The previous provider/Testcontainers timeout
observation remains historical but does not qualify the redesigned account.

## Residual: external-provider-contract
Accepted: the simulator executes the declared supplied-key semantics locally; production adoption
requires qualification against the selected payment provider's documented and observed contract

The change does not establish that Stripe or another real provider offers identical retention,
conflict or lookup behavior. Provider selection and production reconciliation remain required.
