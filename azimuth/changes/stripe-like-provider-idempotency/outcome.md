# Outcome: Move duplicate-refund defect to provider adapter

Status: implemented

The provider simulator now always applies a supplied-key idempotency contract. It records every
request attempt for fixture diagnosis, creates one public refund operation and financial effect
for an identical repeated key, rejects changed payload under that key and treats different keys as
different operations. The unsafe mode moved to the worker: only the demo profile may generate a
new provider key per delivery.

The correct composed journey forced an interruption after provider success and observed two
outbound attempts with one RefundId, unchanged financial payload and one stable idempotency key.
The fixture-only public provider history reported one EUR 30 operation/effect, and the rider
eventually saw one succeeded refund. The untagged unsafe journey performed the same browser actions
but observed two keys and two EUR 30 operations/effects. Both journeys passed in fresh Compose
stacks.

Provider, worker, API, contract and web tests pass. The focused PIT execution generated one
key-selection branch mutant and killed it with the linked repeated-identity test. All progressive
teaching fixtures replay, the application and teaching mutation accounts agree, the accepted
Azimuth model has no holes, and the recorded assurance lifecycle accepts the clean challenge.

The change surfaced one additional runtime defect: Next.js had prerendered the unsafe-mode flag at
image build time. Making the page dynamic restored deployment-time mode selection; rerunning the
unsafe Compose journey then passed. Production adoption still requires qualification of a real
payment provider's key retention, conflict and replay semantics, as recorded in the verification
residual.
