# Constructed agent judgment: weak oracle

Verdict: `toothless`

Before judgment, the declarations make the relation graph structurally complete. The test compares
two succeeded statuses but never observes the outbound keys or the payment provider's public
operations. A wrong adapter can name two operations and receive success twice, so the evidence
does not discriminate the financially relevant failure.

The stage model records that assessment as a fingerprinted `toothless` judgment. A subsequent
deterministic check reports `toothless-evidence` and closes the CI gate.
