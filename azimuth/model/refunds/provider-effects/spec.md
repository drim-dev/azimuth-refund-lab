# Spec: refunds/provider-effects

Financial effects performed by a payment provider after support approves a correction. The Refund
API owns approval and rider projection; this package owns the provider-side uniqueness predicate.

## Requirement: approved-refund-applied-once
Criticality: critical

One approved refund SHALL cause at most one successful durable provider-ledger application even
when delivery is repeated.

### Scenario: redelivery-after-provider-success
GIVEN a refund was approved with one stable RefundId
AND the provider has durably applied it
WHEN the worker is interrupted before publishing the result
AND RabbitMQ redelivers the request any number of times
THEN every provider request uses that same RefundId, payment, amount and currency
AND the provider ledger contains exactly one successful application for the approved amount
AND the rider eventually sees one succeeded refund
