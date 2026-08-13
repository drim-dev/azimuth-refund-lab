# Conference rehearsal state

The live application has two deliberate machine-controlled beats:

1. `scripts/demo-failure` moves one approved refund to the boundary after provider success but
   before result publication. It requires a pristine fixture and stops with the provider ledger at
   one application while the rider projection is pending.
2. `scripts/demo-correct` releases that delivery, waits for RabbitMQ redelivery and requires the
   provider ledger to remain at one application while the rider projection converges to succeeded.

`scripts/rehearsal run` composes those beats after a project-scoped reset. The reset removes only
the named Compose project's volumes and starts the exact locally available images with
`--no-build`. Building during a talk would add package registries and changing source inputs to the
failure surface. The script never edits source, selects a teaching Git state, creates Git history,
or reports human observations.

## Current status

- Machine script syntax and source-input checksums: automated by `scripts/rehearsal check`.
- Runtime readiness: automated by `scripts/rehearsal readiness` against a pristine running stack.
- Failure/correction journey: automated by `scripts/rehearsal run` and the independent Playwright
  system test.
- Forty-five-minute human rehearsal: **pending**.
- Projector, display scaling and room-network rehearsal: **pending**.
- Manual-QA attestation for conference delivery: **pending**.

These pending items are completion conditions outside the repository's machine tier. A person must
perform and record them; a successful script run must not be rewritten as manual evidence.
