# Azimuth refund lab

A distributed refund reference application demonstrating progressive assurance with Azimuth.
Built for Joker 2026.

The fixture follows one financially visible failure: a worker asks a payment provider to apply a
EUR 30 refund, dies before publishing its result and acknowledging the RabbitMQ delivery, then
receives the request again. The correct worker supplies one stable provider idempotency key derived
from the business RefundId, so the provider returns the first operation instead of applying a
second refund. The deliberate failure changes the key on every delivery while the provider itself
continues to honor the idempotency contract correctly.

The complete local stack contains a Next.js rider/support experience, a Spring Refund API,
RabbitMQ, a Spring refund worker, a separately persisted provider simulator and two PostgreSQL
instances. The simulator persists provider operations outside the Refund API database so tests
cannot mistake an application-owned projection for the financial effect. Browser evidence uses a
fixture-only public provider history endpoint, not privileged access to provider storage.

## Verify locally

Run the complete build and component-test gate:

```sh
./scripts/verify
```

For the conference browser journey, run `./scripts/demo-start-duplicate`. It starts a pristine
unsafe-worker/correct-provider stack and leaves every action for the presenter: open the support
console, arm the pause, approve EUR 30, inspect the pending state and first provider operation,
release the delivery, then return to the rider view. The lab is available at
<http://localhost:3100>. `./scripts/demo-run-duplicate` drives and asserts the same unsafe journey
for rehearsal or CI; it is not the live-talk entry point. Finish either journey with
`./scripts/demo-stop`, which removes only the lab containers, network and volumes and retains the
built images.

## Progressive assurance

Two accepted changes are preserved under `azimuth/changes/archive/`:

- the routine foundation follows the lightweight propose/apply/check/archive path and requires no
  claim tags;
- the critical redelivery invariant adds explicit realization, mechanism, evidence, challenge and
  judgment linkage.

Run `./scripts/emit-azimuth`, then `./scripts/azimuth-check` to rebuild and validate the assurance
account. `./scripts/demo-stage list` shows the constructed and authentic teaching states without
pretending that their future immutable Git tags already exist.

The focused PIT run selects the worker's provider-key mapping from Azimuth linkage. Its one relevant
branch mutant is killed by the linked repeated-identity test. `./scripts/pit-replay
--application-gate` verifies the checked-in execution; the challenge is a narrow judgment input,
not proof of the whole distributed claim.

## Lifecycle and conference operation

`./scripts/assurance-replay --recorded` replays the expected gate history without a service;
`--live` validates it against the local Azimuth assurance service. `./scripts/rehearsal run`
executes the correct redelivery journey from prebuilt images; `./scripts/rehearsal duplicate`
asserts the deliberate unsafe-worker failure. See `docs/rehearsal/` for
the offline procedure and the human rehearsal conditions that automation cannot attest.

No public Git tags or conference publication are implied by the working tree. Those are explicit
release actions after review. [The initiative status](docs/initiative-status.md) lists each
implemented layer and the remaining release or human-evidence boundary.
