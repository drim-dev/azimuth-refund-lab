# Spec: refunds/provider-effects

## Requirement: approved-refund-applied-once
Criticality: critical

One approved refund SHALL cause at most one durable provider-ledger application when delivery is
repeated.

### Scenario: redelivery-after-provider-success
GIVEN a refund was approved with one stable RefundId
WHEN the delivery is repeated
THEN the provider ledger contains exactly one successful application
