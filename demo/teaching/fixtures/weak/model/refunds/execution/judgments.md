# Judgments: refunds/execution

## Claim: repeated-processing-does-not-repeat-refund
Verdict: toothless
Fingerprint: 23869222d777f607
Judged: 2026-08-13
Judge: constructed-fixture-agent

The test compares two successful responses but never observes their outbound provider keys or the
provider's public operation history. An implementation that creates a new key and financial effect
for every delivery still satisfies the test, so this evidence does not discriminate the relevant
wrong implementation.
