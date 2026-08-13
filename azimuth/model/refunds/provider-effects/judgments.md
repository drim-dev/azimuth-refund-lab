# Judgments: refunds/provider-effects

## Claim: redelivery-after-provider-success
Verdict: sound
Fingerprint: 482570a75aae966b
Judged: 2026-08-13
Judge: codex

The claim is supported by a composed pair of component boundaries plus one real-process example.
`RefundWorkerComponentTest` forces the precise post-provider/pre-publication interruption against
real RabbitMQ, observes two provider requests, relates every financial field and RefundId across
them, and accepts one deterministic result only after redelivery. The provider test class enters
through real HTTP and PostgreSQL: sequential repeats and twelve concurrent calls return one stored
operation, while changed payment, amount or currency and two competing payloads produce one winner
and one conflict. Each case reads the provider ledger rather than the Refund API projection.

The Playwright journey independently establishes composition: while the worker is paused, provider
application count is one and the API is still pending; after release it observes one succeeded
refund, EUR 90 final rider cost, one recorded interruption and provider count still one. Deleting the
stable RefundId from the worker call, removing the provider conflict comparison, or replacing the
insert-on-conflict ledger path makes a distinct relational assertion fail. The implementation tags
name the request propagation, consumer boundary, ledger insert/comparison and provider choke point;
the two mechanism bindings are atomic and their component tests exercise their stated contracts.

The provider now separates idempotency records from financial effects. In the default mode, the
transaction appends an effect only when it creates the RefundId record, so the component oracle
asserts both one stored response and one money-moving row. The demo-only unsafe mode deliberately
appends an effect for an identical repeat and its component test observes two; startup rejects that
mode without the `demo` profile. This makes the conference falsifier concrete without weakening the
mechanism being judged.

The refreshed PIT challenge is adverse but does not overturn that reading. It generated eleven relevant
mutants and resolved seven exact realization/evidence subjects, then every mutation minion timed out
while restarting the Spring/Testcontainers harness. PIT reports those as detected, but the imported
observation correctly retains `TIMED_OUT` findings rather than calling them killed. I therefore give
the mutation result no positive weight and accept the named harness residual. The verdict rests on
the direct relational evidence above, within the finite failure and multiplicity classes recorded
in the verification plan; it does not claim correctness against a real provider or every
infrastructure failure.
