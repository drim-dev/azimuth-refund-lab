# Judgments: refunds/execution

## Claim: repeated-processing-does-not-repeat-refund
Verdict: sound
Fingerprint: 4e518218228769c9
Judged: 2026-08-13
Judge: Codex

The worker owns the mapping between the business RefundId and the provider's idempotency key. Its
normal policy deterministically uses the RefundId, and every provider call carries that key plus
the same RefundId, payment, amount and currency. The provider owns a separate contract: it
atomically creates one operation for a supplied key, returns that operation for an identical
repeat and rejects changed financial content under the same key. These realizations establish
different parts of the predicate in both obligated areas; neither is tagged merely for appearing
on the call path.

Provider component evidence exercises sequential and twelve-way concurrent identical requests,
changed payload fields under one key, and different keys for one business RefundId against real
PostgreSQL. Worker evidence ranges over 1,000 generated RefundIds with twenty calls each, then
forces the post-provider interruption and directly records both outbound HTTP attempts. The
composed browser test observes two attempts with the same RefundId, stable key and payload, one
public provider operation and one EUR 30 financial effect, and eventual rider success. The
separate unsafe journey changes only the worker key and observes two provider operations; because
it is a falsifier rather than support, it carries no `Covers` annotation.

The universal declaration rests on the stable mapping, provider contract and recorded equivalence
classes, not an assertion that a finite test enumerates every delivery count. That boundary is
explicit in `finite-delivery-range`; real-provider retention and replay semantics remain explicit
in `external-provider-contract`. The focused PIT challenge generated one branch-inversion mutant
at the key-selection choke point, selected the linked repeated-identity test and killed it. This
supports only the claim that the evidence rejects that generated wrong mapping. Changing the key
per delivery, accepting a changed payload under one key or creating an unconditional second
operation makes a direct relation fail.

## Claim: interrupted-processing-eventually-completes
Verdict: sound
Fingerprint: 1b7c90f4fe2d06d5
Judged: 2026-08-13
Judge: Codex

The worker consumer negatively acknowledges an interrupted delivery, publishes one persistent
stable result after processing resumes, waits for broker confirmation, and only then acknowledges
the request. The Refund API records the result identity once and advances the refund projection.
The rider BFF exposes that projection, while the rider view refreshes a pending refund and renders
its completed identity, amount, currency, and final cost. Each obligated area therefore establishes
a named part of the cross-component predicate.

The API component test publishes the same result twice and relates one receipt to one completed
projection. The composed browser test forces the payment-success/pre-publication interruption,
observes one public provider operation while the API remains pending, resumes delivery, records
the second outbound attempt and then relates rider-visible completion to the original RefundId,
amount and currency. An early request acknowledgement, unconfirmed or non-persistent result
publication, non-idempotent result projection, missing BFF projection, or rider view that stays
pending makes one of those observations fail. The example declaration is honest: the evidence
covers the selected retryable boundary, not every infrastructure failure or an unbounded recovery
deadline, as recorded in the verification residual.
