# Change: rename-refund-execution-spec

Status: implemented

## Problem

The accepted package `refunds/provider-effects` exposed a payment-provider implementation boundary
as its domain identity and combined financial uniqueness with rider-visible completion in one
technical scenario. That shape was not analyst-readable, and the combined claim allowed a complete
provider/worker linkage to conceal missing realizations in the Refund API, rider BFF and web view.

## Outcome

Rename the package to `refunds/execution`, split the business predicates into at-most-once refund
application and eventual rider-visible completion, and require realizations in every architectural
area that establishes each predicate.

## Scope

In scope: accepted facets, Java and TypeScript relations, evidence bindings, area obligations,
teaching fixtures, lifecycle definitions, linkage generation and assurance export.

Out of scope: changing refund approval policy, adding a recovery deadline, altering runtime
behaviour, rewriting the archived change that originally introduced the old identity, and
finalizing or archiving this change.

## Affected claims

- Replace `refunds/provider-effects#redelivery-after-provider-success` with
  `refunds/execution#repeated-processing-does-not-repeat-refund`.
- Add `refunds/execution#interrupted-processing-eventually-completes` by splitting the rider-visible
  consequence out of the former mixed scenario.

## Completion conditions

- current analyst-facing claims contain no RabbitMQ, worker, payment-provider ledger or identifier
  vocabulary;
- at-most-once execution requires worker and payment-provider realizations;
- eventual visibility requires worker, Refund API, rider BFF and rider-web realizations;
- fresh JVM and TypeScript linkage passes `azimuth check` over both current claims;
- removing one realization from each obligated area produces `missing-required-realization`;
- the composed browser evidence covers rider-visible completion, while provider component evidence
  covers financial uniqueness;
- new judgments are produced only by the explicit agent-tier pass.
