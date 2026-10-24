# Conference rehearsal state

The live application has two deliberate browser-controlled beats:

1. `scripts/demo-start-duplicate` starts a pristine unsafe-worker/correct-provider stack and stops
   before approving anything. The presenter opens the support console, arms the deterministic
   pause and approves EUR 30 in the browser.
2. After the first public provider operation is visible while the API is pending, the presenter
   releases the delivery. RabbitMQ redelivers it; the unsafe worker supplies another key and the
   correct provider reports a second EUR 30 operation.

`scripts/demo-run-duplicate` composes and asserts those beats without a presenter.
`scripts/rehearsal duplicate` calls that automated path. `scripts/rehearsal run` retains the
correct-worker journey as the positive evidence rehearsal. Each reset removes only the named
Compose project's volumes and starts the exact locally available images with
`--no-build`. Building during a talk would add package registries and changing source inputs to the
failure surface. The script never edits source, selects a teaching Git state, creates Git history,
or reports human observations.

## Current status

- Machine script syntax and source-input checksums: automated by `scripts/rehearsal check`.
- Runtime readiness: automated by `scripts/rehearsal readiness` against a pristine running stack.
- Correct and unsafe journeys: automated independently by `scripts/rehearsal run`,
  `scripts/rehearsal duplicate` and their Playwright system tests.
- Forty-five-minute human rehearsal: **pending**.
- Projector, display scaling and room-network rehearsal: **pending**.
- Manual-QA attestation for conference delivery: **pending**.

These pending items are completion conditions outside the repository's machine tier. A person must
perform and record them; a successful script run must not be rewritten as manual evidence.
