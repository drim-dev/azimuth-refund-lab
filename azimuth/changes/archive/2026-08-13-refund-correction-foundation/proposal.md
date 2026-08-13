# Change: refund-correction-foundation

Status: accepted and complete

Exploration: joker-2026-progressive-verification-talk
Carries decisions: E1, E2, E3, E4

## Problem

The public reference repository has no executable application. The Joker demonstration needs a
small distributed refund workflow whose provider-side financial truth can diverge from an API
projection when a broker delivery is repeated after worker failure.

## Outcome

Provide the correct default refund workflow across the rider/support web experience, Refund API,
transactional outbox, RabbitMQ worker and separately persisted provider simulator. A deterministic
demo-only failpoint exposes the boundary after provider success and before result publication.

## Scope

In scope: versioned HTTP and event contracts, authenticated fixture support workflow, refund
capacity reservation, provider idempotency, durable request/result handoff, demo failpoint, local
Compose topology and composed system evidence.

Out of scope: production identity, a real payment processor, broker high availability, remote
deployment, Azimuth claim linkage, mutation challenges, lifecycle observations and public article
publication. Those belong to later changes in the approved graph.

## Affected claims

- Add routine claim `refunds/support-workflow#approved-refund-is-visible`.

The change deliberately follows the routine path. Ordinary automated tests remain project tests
outside Azimuth linkage until the assurance-account change raises the money-loss invariant.

## Completion conditions

- one command builds and tests the Java reactor and web application;
- one local Compose stack exposes rider, support and provider-ledger views;
- a support approval and its request outbox row commit atomically;
- a repeated request with the same refund identity returns one provider ledger operation;
- changed financial fields under the same refund identity are rejected;
- a result event advances Refund API state idempotently;
- the demo failpoint is unavailable without the `demo` profile;
- composed evidence crosses the BFF, API, broker, worker and provider ledger;
- current routine intent is distilled and the change is ready for acceptance.
