# Composed system evidence

`refund-redelivery.spec.ts` crosses the browser, Next.js BFF, Refund API, RabbitMQ worker and
provider simulator. It observes two outbound attempts with one RefundId, stable idempotency key and
unchanged payload; the simulator's fixture-only public history reports one provider operation and
one EUR 30 financial effect. The Refund API projection is observed independently and may remain
pending after the provider reports the first operation.

`refund-duplicate.spec.ts` drives the same browser sequence with an unsafe worker that changes its
provider key per delivery. The correct provider therefore treats the requests as two operations.
This test demonstrates the falsifier and deliberately has no `Covers` annotation.

Install the pinned test dependency and browser once:

```sh
npm ci
npx playwright install chromium
```

Run the journey against an isolated, freshly built Compose stack:

```sh
npm run test:compose
npm run test:compose:duplicate
```

The runner removes volumes before the journey and tears the stack down afterward. Set
`KEEP_REFUND_LAB_STACK=1` to retain a failed stack for diagnosis.
