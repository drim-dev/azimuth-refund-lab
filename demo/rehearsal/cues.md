# Live demo cues

## Start without consuming the story

Run `scripts/demo-start-duplicate`, then open the support console. The command leaves the fixture
pristine: no refund exists and the correct payment-provider simulator has no operation. In the
browser, arm the pause and approve EUR 30. The intended visible state is:

- approved refund: pending;
- worker failpoint: paused;
- provider attempts: 1;
- public provider operations: 1;
- final trip cost after correction: EUR 90.

The browser reads the simulator's fixture-only public transaction history. It does not inspect an
external provider's internal database. If the state differs, stop with `scripts/demo-stop` and use
the offline recording; do not improvise another approval or release.

## Show correction under redelivery

Click **Interrupt processing and trigger redelivery**. The intended visible state is:

- approved refund: succeeded;
- worker interruption count: 1;
- provider attempts: 2 with different idempotency keys;
- public provider operations: 2, totalling EUR 60;
- rider receipt: captured EUR 120, correction EUR 30, final cost EUR 90.

This is the deliberately unsafe worker, not a broken provider. The provider correctly deduplicates
one key; the worker incorrectly turns one business refund into two provider identities. The
correct-worker composed test later shows two attempts with one stable key and one operation.
