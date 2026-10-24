# Change: stripe-like-provider-idempotency

Status: implemented

## Problem

The deliberate duplicate-refund mode currently makes the payment-provider simulator violate its
own idempotency contract. A production application normally does not control that implementation:
it supplies an idempotency key to an external provider such as Stripe. The current failure
therefore assigns the defect to the wrong ownership boundary and does not test whether the worker
preserves the business refund identity in the provider request.

## Outcome

Make the provider simulator enforce a Stripe-like supplied-key contract in every mode. The normal
worker derives the provider idempotency key from the stable RefundId; the demo-only unsafe worker
creates a new key for each delivery. Recreate evidence and the mutation challenge around that
integration boundary, and separate the browser-driven cold open from its automated assertion.

## Scope

In scope: provider HTTP and persistence contracts, worker provider-adapter behavior, component and
composed evidence, the demo-only falsifier, presenter commands, Support-console diagnostics,
mutation selection, teaching accounts, lifecycle definitions and current judgments.

Out of scope: changing the analyst-facing refund claims, changing approval policy or result-event
semantics, claiming conformance by a real payment provider, rewriting archived changes, publishing
teaching tags, and finalizing or archiving this change.

## Affected claims

- Collateral redesign and review:
  `refunds/execution#repeated-processing-does-not-repeat-refund`.
- Collateral evidence refresh:
  `refunds/execution#interrupted-processing-eventually-completes`.

## Completion conditions

- the provider atomically deduplicates identical payloads under one supplied idempotency key,
  rejects changed payload under that key, and treats two different keys as two operations;
- the correct worker sends the same RefundId, payment, amount, currency and provider idempotency
  key on both attempts after the forced interruption and redelivery;
- canonical composed evidence directly observes two attempts, one EUR 30 provider financial
  effect and eventual rider-visible success;
- the untagged unsafe journey observes different provider keys and two EUR 30 effects without
  representing the provider as defective;
- presenter start, automated run/assert and cleanup commands have distinct names and behavior;
- PIT challenges the worker key mapping through a bounded harness that produces a conclusive
  result rather than treating timeouts as killed mutants;
- fresh linkage, agent judgments, assurance export, lifecycle replay, component tests and both
  Compose journeys agree with the new mechanism.
