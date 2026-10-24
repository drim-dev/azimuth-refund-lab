# Spec: refunds/execution

Execution of an approved refund, from the decision to return money until the completed refund is
visible to the rider. Approval policy belongs to the support workflow; this package begins after a
refund has been approved.

## Requirement: approved-refund-applied-once
Criticality: critical

One approved refund SHALL return the approved amount no more than once, even when its processing
is repeated.

### Scenario: repeated-processing-does-not-repeat-refund
GIVEN one refund was approved for a rider
WHEN its processing is interrupted and repeated any number of times
THEN the rider receives the approved amount no more than once

## Requirement: interrupted-refund-completes
Criticality: critical

An approved refund SHALL resume after an interruption and eventually appear as completed to the
rider.

### Scenario: interrupted-processing-eventually-completes
GIVEN a refund was approved for a rider
AND the approved amount was returned
WHEN processing is interrupted before completion is visible
AND processing resumes
THEN the rider eventually sees one completed refund for the approved amount
