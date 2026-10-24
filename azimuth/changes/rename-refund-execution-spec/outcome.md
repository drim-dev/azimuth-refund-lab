# Outcome: Rename refund execution model package

Status: implemented

## Result

The current model now names the production capability `refunds/execution` and separates financial
uniqueness from eventual rider-visible completion. Realization obligations make the worker and
payment-provider contributions mandatory for the first claim, and the worker, Refund API, rider
BFF and rider-web contributions mandatory for the second. Fresh Java and TypeScript linkage has no
machine-tier holes, and both critical claims carry current agent-tier judgments.

The assurance export contains two claim contracts under the new identity. The recorded lifecycle
replay remains deterministic after its definitions and expected trace were rebound to the new
at-most-once contract.

## Validation

- `azimuth check` over fresh union linkage and the PIT observation: four claims, no holes;
- temporary removal of every obligated area relation: six of six cases rejected with
  `missing-required-realization` for the exact area;
- Maven reactor tests for contracts, Refund API, worker and payment provider: 21 tests passed;
- web unit tests: three files and ten tests passed; TypeScript and production build passed;
- composed Docker/Playwright journey: one test passed and Compose removed its containers, network
  and volumes;
- all five teaching fixtures retained their intended result, including the deliberately green
  dishonest and weak-oracle stages and the rejected stale judgment;
- recorded assurance lifecycle replay matched its eleven expected evaluations;
- PIT replay retained 11 timeouts as an adverse, inconclusive challenge rather than positive
  evidence.

## Departures and residue

The current Azimuth change-delta grammar cannot express removal, identity replacement or scenario
movement. This change therefore records the replacement in its proposal and edits the sole current
model directly. `azimuth change check rename-refund-execution-spec` cannot validate that semantic
delta and reports accepted-state errors; this is a tooling limitation, not a successful change
check.

No surface was introduced: the claims are cross-area business predicates, not invariants quantified
over an enumerated population of sites. Area realization obligations are the applicable machine
mechanism.

The archived introducing change retains `refunds/provider-effects` as historical provenance. Its
proposal, work-package ownership and intent delta describe what was accepted at that time and were
not rewritten as though the later rename had already happened.

`demo-stage replay final-account` reaches the green current Azimuth account, then stops because the
committed teaching PIT provenance hashes do not match the committed `RefundEffect.java` and
`WeakResponseOracleTest.java` inputs. Neither file nor the PIT record changed in this migration.
The application PIT observation replays successfully; repairing the independent teaching-record
provenance requires a fresh teaching PIT execution rather than rewriting its receipt here.
