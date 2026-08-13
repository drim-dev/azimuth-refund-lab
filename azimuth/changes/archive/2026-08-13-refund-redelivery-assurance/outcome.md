# Outcome: refund-redelivery-assurance

Status: accepted

## Result

The money-loss invariant is now an explicit critical claim with five Java realization sites, six
linked evidence sites, two mechanism bindings and five mechanism covers. The fresh union manifest
is hole-free. A controlled removal of all realization links produced an `unrealized` error, and a
controlled removal of all evidence links produced an `uncovered` error.

The agent tier judged the claim sound at fingerprint `4b7702ebc01753c6`. The judgment relies on
direct relational observations of provider-ledger state under sequential repetition, concurrency,
changed content and worker redelivery, plus a composed journey that observes provider truth while
the API projection is still pending.

## Verification performed

- `scripts/emit-azimuth` rebuilt JVM and TypeScript linkage and emitted a fresh union manifest;
- `azimuth check --manifest .azimuth/linkage.json` reported no holes;
- deleting required links from controlled manifest copies made the machine tier fail closed;
- affected Maven tests passed: contracts 4, worker 2 and provider 9;
- the composed Playwright journey passed from fresh Compose volumes;
- PIT 1.22 generated eight exact mutants in the linked provider idempotency path;
- the generic PIT adapter imported the native report as a challenge observation without claiming
  that timeouts were killed mutants;
- `azimuth judge` reported the critical claim as `judged sound` with the imported challenge present.

## Mutation experiment finding

All eight PIT mutation minions timed out while restarting the Spring/Testcontainers component
harness. PIT's summary counts timed-out mutants as killed, but the imported observation preserves
all eight as `TIMED_OUT` findings and contributes no positive evidence. The claim remains sound
because its direct component and composed evidence are independently sufficient; the judgment
records the mutation harness limitation explicitly.

This is a useful adverse validation of the extension boundary: Azimuth accepted a tool-neutral
challenge observation, exposed the finding to judgment and did not confuse a hardener result with
claim evidence.

## Departures

The approved plan expected a targeted mutation challenge but did not prescribe a green mutation
score. The real PIT run could not produce useful mutant outcomes through the Spring/Testcontainers
component harness; retaining the adverse observation is the honest departure. The experiment also
required the generic adapter to accept and fingerprint PIT 1.22's documented `partial` root
attribute while continuing to reject unknown report structure.

## Residual decisions

The PIT harness should be split into fast mutation-focused tests if mutation adequacy is to become a
required gate. The public repository remains pinned to an unpublished Azimuth source revision; its
local PIT importer uses the framework adapter fix for real PIT 1.22 `partial` reports until that fix
is committed and the lock can advance. Lifecycle observations, teaching-history Git states and
conference publication remain later changes in the approved exploration graph.
