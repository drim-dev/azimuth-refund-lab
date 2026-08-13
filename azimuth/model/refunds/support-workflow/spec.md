# Spec: refunds/support-workflow

Support correction of an incorrectly charged trip payment and honest presentation of the resulting
monetary history to the rider.

## Requirement: visible-refund-correction
Criticality: routine

An authenticated support agent SHALL be able to approve a refund against a captured payment, and
the rider SHALL be able to see the resulting refund status and monetary history.

### Scenario: approved-refund-is-visible
GIVEN a rider was charged EUR 120 including an incorrect EUR 30 wait-time fee
WHEN an authenticated support agent approves a EUR 30 refund
THEN the support view shows the refund status
AND the rider view shows the EUR 90 original fare, EUR 30 wait-time fee and approved EUR 30 refund
AND the rider view shows the final EUR 90 cost
