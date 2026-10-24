# PIT challenge: worker provider-key stability

This package runs a bounded mutation challenge over the worker's idempotency-key mapping. It is a
judgment input under D38 and D39, not evidence that the distributed refund claim is true.

`run` requires a fresh `.azimuth/linkage.json`. It derives the exact PIT target and selected test
symbols from `Realizes` and `Covers`; no claim id is repeated in this package. The run fails when
the linked key-selection implementation, its repeated-identity test, the PIT report, or an upstream
adapter cannot be resolved exactly.

```sh
scripts/emit-azimuth
tooling/pit/run
```

During framework development, `AZIMUTH_SOURCE_DIR` may point at an Azimuth checkout containing the
generic PIT adapter. The public path remains the repository pin populated by `bootstrap-azimuth`;
the lock must advance before this package is published.

Generated native files remain under `services/refund-worker/target/pit-reports/` and are
ignored by Git. `pit-selection.json` is the exact resolved input fingerprinted by the adapter and
is kept with the compact imported D39 `worker-idempotency-observation.json`.
`import` repeats only the fail-closed import and review gate when adapter development changes; it
does not execute PIT or silently regenerate the selection.

The checked-in `worker-idempotency-observation.json` is meaningful only for the exact linkage,
configuration, selection, PIT version and raw report that produced its fingerprint. Re-running the
challenge replaces it; a changed input therefore stales an earlier judgment.
