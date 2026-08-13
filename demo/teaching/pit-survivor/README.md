# Authentic surviving-mutant fixture

This bounded fixture is separate from the refund application's PIT run. It contains the same weak
oracle used by the teaching story: two duplicate responses are compared, but the authoritative
financial-effect count is never read.

PIT removes the idempotency condition in `RefundEffect.apply`. The response-only test still passes,
so the mutant survives. The strong test reads `applicationCount()` after two deliveries; the exact
same mutation is killed. `scripts/pit-replay --teaching-live` regenerates both reports. The default
offline replay validates and summarizes the checked-in XML and exact input hashes.

The fixture establishes one narrow proposition: this weak oracle accepts one generated wrong
implementation and the direct financial-effect oracle rejects it. It does not establish that the
real application contains the same defect.
