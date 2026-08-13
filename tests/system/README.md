# Composed system evidence

`refund-redelivery.spec.ts` crosses the browser, Next.js BFF, Refund API, RabbitMQ worker and
provider simulator. The provider ledger count is the financial oracle; the Refund API projection
is observed independently and may remain pending after the provider has applied the refund.

Install the pinned test dependency and browser once:

```sh
npm ci
npx playwright install chromium
```

Run the journey against an isolated, freshly built Compose stack:

```sh
npm run test:compose
```

The runner removes volumes before the journey and tears the stack down afterward. Set
`KEEP_REFUND_LAB_STACK=1` to retain a failed stack for diagnosis.
