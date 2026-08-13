# Outcome: refund-correction-foundation

Status: accepted

## Result

The reference repository now runs a correct distributed refund journey across a Next.js BFF,
Refund API, transactional request outbox, RabbitMQ worker and separately persisted provider ledger.
The worker's demo profile can pause deterministically after provider success and before result
publication, then force redelivery without repeating the financial effect.

The routine requirement acquired no `Realizes`, `Covers`, verification or judgment artifacts. Its
ordinary project tests include 19 Java contract/component cases, 10 web cases and one composed
Playwright journey; Azimuth does not claim those tests as evidence for a routine scenario.

## Verification performed

- `./mvnw -B test`: all five reactor modules passed;
- web unit tests, strict type check and Next production build passed;
- all four production container images built;
- `npm run test:compose` under `tests/system`: one bounded browser journey passed from fresh volumes;
- the composed journey observed provider application count one while the API was pending, released
  the interrupted delivery, and observed a succeeded refund, EUR 90 final cost and count still one;
- the locked public Azimuth revision bootstrapped locally and reported version 0.1.0.

## Departures

PostgreSQL 18 requires volumes mounted at `/var/lib/postgresql`, not the legacy data subdirectory.
Unnecessary host database port mappings were removed after the first Compose startup exposed both
that compatibility issue and a local 5432 collision. Neither departure changes a public contract.

The locked Azimuth source revision contains TypeScript package manifests without npm lockfiles, so
the bootstrap uses a non-publishing local `npm install` rather than `npm ci`. The framework revision
itself remains pinned and checked after checkout.

## Residual decisions

The support role is an explicit fixture header, not production authentication. RabbitMQ remains a
single node and the payment provider is a simulator. The critical money-loss invariant, mutation
challenge and judgment deliberately belong to the next approved change rather than being implied by
this routine change.
