# Verification: refunds/provider-effects

## Claim: redelivery-after-provider-success
Scope: component
Quantification: universal
Oracle: relational

Worker evidence forces the exact boundary after provider response and before result publication,
then relates both observed provider requests and the sole result to one RefundId and financial
payload. Provider evidence ranges sequential and concurrent identical repeats against real
PostgreSQL, relates every response to the same stored operation, and asserts both one idempotency
record and one authoritative financial-effect row directly. A real composed example establishes
that the two mechanisms connect and that the rider projection eventually converges.

A separate demo-profile component test establishes that the presentation fixture can make the
dangerous alternative real: the same two requests retain one idempotency record but append two
financial effects. That test demonstrates the falsifier and is not positive evidence for the claim.

PIT challenges the provider idempotency branch. A surviving or uncovered mutation in the selected
uniqueness path is an adverse observation requiring review; mutation score alone is not evidence.

The refreshed targeted PIT execution generated eleven relevant mutants, but every mutated minion
timed out while restarting the Spring/Testcontainers component harness. PIT's summary counts
timeouts as “killed”; the Azimuth adapter preserves all eleven as `TIMED_OUT` review items instead. This is an
adverse hardener result about harness suitability, not evidence against the claim and not a killed
mutation claim. The direct component and composed evidence must stand on its own until a mutation
harness can execute these subjects reliably.

## Residual: finite-delivery-range
Accepted: the component pair covers concurrent and sequential multiplicity while the composed
example covers the forced post-provider interruption; expand when another delivery/failure class is
introduced

The word “any” describes the invariant, not an infinite test run. Evidence is considered universal
because the durable uniqueness constraint and stable-key protocol are exercised across the selected
equivalence classes and inspected directly, with the finite boundary recorded here.

## Residual: pit-component-harness-timeout
Accepted: the eleven generated uniqueness-path mutants are preserved as adverse review items; build a
lighter mutation execution seam before using PIT as positive support for this claim

PIT forks mutation minions, while the selected component class starts Spring and PostgreSQL through
Testcontainers. Coverage succeeds, but each mutated minion exceeds the bounded five-second timeout.
Raising the timeout merely delays the same result and is not reclassified as success.
