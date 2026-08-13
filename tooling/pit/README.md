# PIT challenge: provider refund uniqueness

This package runs a bounded mutation challenge over the provider idempotency choke point. It is a
judgment input under D38 and D39, not evidence that the refund claim is true.

`run` requires a fresh `.azimuth/linkage.json`. It derives the exact PIT target and selected test
symbols from `Realizes` and `Covers`; no claim id is repeated in this package. The run fails when
the provider implementation, its linked provider evidence, the PIT report, or an upstream adapter
cannot be resolved exactly.

```sh
scripts/emit-azimuth
tooling/pit/run
```

During framework development, `AZIMUTH_SOURCE_DIR` may point at an Azimuth checkout containing the
generic PIT adapter. The public path remains the repository pin populated by `bootstrap-azimuth`;
the lock must advance before this package is published.

Generated native files remain under `services/provider-simulator/target/pit-reports/` and are
ignored by Git. `pit-selection.json` is the exact resolved input fingerprinted by the adapter and
is kept with the compact imported D39 `provider-refund-observation.json`.
`import` repeats only the fail-closed import and review gate when adapter development changes; it
does not execute PIT or silently regenerate the selection.

The checked-in `provider-refund-observation.json` is meaningful only for the exact linkage,
configuration, selection, PIT version and raw report that produced its fingerprint. Re-running the
challenge replaces it; a changed input therefore stales an earlier judgment.
