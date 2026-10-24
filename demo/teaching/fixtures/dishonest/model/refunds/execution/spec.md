# Spec: refunds/execution

## Requirement: approved-refund-applied-once
Criticality: critical

One approved refund SHALL return the approved amount no more than once, even when its processing
is repeated.

### Scenario: repeated-processing-does-not-repeat-refund
GIVEN one refund was approved for a rider
WHEN its processing is interrupted and repeated any number of times
THEN the rider receives the approved amount no more than once
