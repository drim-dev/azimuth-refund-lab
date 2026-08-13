# Work packages: refund-correction-foundation

## Work package: contracts
Status: complete
Depends on: none
Owns: contracts
Objective: freeze versioned HTTP and AMQP records plus golden wire examples
Evidence: Maven contract tests validate JSON compatibility and schema examples

## Work package: refund-api
Status: complete
Depends on: contracts
Owns: services/refund-api
Objective: own approval, capacity reservation, request outbox, result inbox and visible projection
Evidence: component tests use real PostgreSQL and RabbitMQ through HTTP and AMQP boundaries

## Work package: provider
Status: complete
Depends on: contracts
Owns: services/provider-simulator
Objective: own the durable provider ledger and stable RefundId idempotency contract
Evidence: component tests use real PostgreSQL and exercise repeat and conflicting provider calls

## Work package: worker
Status: complete
Depends on: contracts
Owns: services/refund-worker
Objective: consume requests, call the provider, publish results and expose the demo-only failpoint
Evidence: component tests use real RabbitMQ and provider HTTP boundaries with manual acknowledgement

## Work package: web
Status: complete
Depends on: contracts
Owns: web
Objective: expose rider and authenticated fixture support workflows through a Next.js BFF
Evidence: unit and route tests plus a production Next.js build

## Work package: system
Status: complete
Depends on: refund-api, provider, worker, web
Owns: tests/system
Objective: verify the complete monetary history and crash/redelivery journey against the provider ledger
Evidence: Playwright and API journey over the local Compose stack with bounded polling
