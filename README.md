# Azimuth refund lab

A distributed refund reference application demonstrating progressive assurance with Azimuth.
Built for Joker 2026.

The fixture follows one financially visible failure: a worker applies a EUR 30 refund at a payment
provider, dies before publishing its result and acknowledging the RabbitMQ delivery, then receives
the request again. The correct system uses one stable refund identity so redelivery returns the
stored provider result rather than applying another refund.

The complete local stack contains a Next.js rider/support experience, a Spring Refund API,
RabbitMQ, a Spring refund worker, a separately persisted provider simulator and two PostgreSQL
instances. The provider ledger is intentionally outside the Refund API database so tests cannot
mistake a local projection for the financial truth.

## Verify locally

Run the complete build and component-test gate:

```sh
./scripts/verify
```

Start a fresh local stack with `./scripts/demo-reset`, then wait for it with
`./scripts/demo-doctor`. The rider and support lab is available at <http://localhost:3000>.

## Progressive assurance

Two accepted changes are preserved under `azimuth/changes/archive/`:

- the routine foundation follows the lightweight propose/apply/check/archive path and requires no
  claim tags;
- the critical redelivery invariant adds explicit realization, mechanism, evidence, challenge and
  judgment linkage.

Run `./scripts/emit-azimuth`, then `./scripts/azimuth-check` to rebuild and validate the assurance
account. `./scripts/demo-stage list` shows the constructed and authentic teaching states without
pretending that their future immutable Git tags already exist.

The real PIT run is deliberately retained as an adverse result: eight mutation minions timed out in
the Spring/Testcontainers harness. `./scripts/pit-replay --gate` shows why that observation requires
review and does not count as positive evidence.

## Lifecycle and conference operation

`./scripts/assurance-replay --recorded` replays the expected gate history without a service;
`--live` validates it against the local Azimuth assurance service. `./scripts/rehearsal run` executes
the deterministic failure and correction journey from prebuilt images. See `docs/rehearsal/` for
the offline procedure and the human rehearsal conditions that automation cannot attest.

No public Git tags or conference publication are implied by the working tree. Those are explicit
release actions after review. [The initiative status](docs/initiative-status.md) lists each
implemented layer and the remaining release or human-evidence boundary.
