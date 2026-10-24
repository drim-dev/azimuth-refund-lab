# Authentic surviving-mutant fixture

This bounded fixture is separate from the refund application's PIT run. Both stages execute one
recognizable `RefundRedeliveryComposedTest` lineage. The weak stage compares two succeeded statuses
but never observes the keys, provider operations or financial-effect count.

PIT inverts the stable-key branch in `WorkerProviderAdapter.keyFor`. The status-only method still
passes, so the mutant survives. The direct method observes two attempts with one key and one
provider operation; the exact same mutation is killed. `scripts/pit-replay --teaching-live`
regenerates both reports. The default offline replay validates and summarizes the checked-in XML
and exact input hashes.

The fixture establishes one narrow proposition: this weak oracle accepts one generated wrong
implementation and the direct financial-effect oracle rejects it. It does not establish that the
real application contains the same defect.
