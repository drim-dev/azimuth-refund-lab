# Change: refund-redelivery-assurance

Status: accepted and complete

Exploration: joker-2026-progressive-verification-talk
Carries decisions: E2, E3, E4, E5, E6, E7

## Problem

The working refund path has ordinary tests but no explicit account of its money-loss invariant. A
green test suite cannot show which code realizes the invariant, which evidence has the required
form, or whether an agent has reviewed the evidence rather than trusting its name and tags.

## Outcome

Introduce one critical claim for redelivery after provider success, bind its two reusable mechanisms
to Java symbols, link independent component and composed evidence, challenge the evidence with PIT,
and record a fingerprinted judgment.

## Scope

In scope: JVM and TypeScript linkage packages, deterministic manifest emission, stable-identity and
provider-ledger mechanism declarations, honest `Realizes`/`Covers` links, PIT configuration and a
generic PIT observation, verification plan, judgment and an assurance snapshot.

Out of scope: pedagogical broken Git states, public article/slides, remote CI observations,
production monitoring and deployment gates. Those remain later changes in the approved graph.

## Affected claims

- Add critical claim `refunds/provider-effects#redelivery-after-provider-success`.

## Completion conditions

- the worker demonstrably repeats the same RefundId after the forced interruption;
- the provider ledger accepts any tested identical concurrency/repeat pattern as one operation and
  rejects changed financial content under that identity;
- the composed journey observes provider truth while the API is pending and count one after
  redelivery;
- deleting a required realization or cover produces a machine-tier hole;
- PIT mutates the provider uniqueness path and its report is imported without pretending to be
  Stryker.NET;
- the agent tier reads source, evidence and challenge result and records a current verdict;
- a fresh manifest leaves the accepted model hole-free.
