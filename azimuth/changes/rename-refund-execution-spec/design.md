# Design: Rename refund execution model package

The package identity follows the stable business responsibility—execution of an approved refund—
rather than the payment-provider implementation boundary. The former scenario is split because
financial uniqueness and rider-visible completion have different realization graphs and evidence
oracles.

Area realization obligations carry no role field in workspace format version 1. The intended
contributions therefore remain reviewable prose:

- `repeated-processing-does-not-repeat-refund`: the refund worker preserves the approved refund
  identity and payload; the payment-provider area makes repeated application idempotent;
- `interrupted-processing-eventually-completes`: the worker publishes a confirmed result, the
  Refund API records succeeded state, the rider BFF transports it, and the rider web renders it.

Approval and outbox creation are excluded: they make a refund eligible for execution but do not
establish either split predicate after approval.

No surface is declared. These are ordinary behavioral claims over required architectural areas,
not invariants quantified over every member of a derived route or handler class.

## Tool limitation

The pinned CLI parses additions and criticality transitions only. It cannot project a spec rename,
requirement replacement or scenario movement, so this change deliberately has no `specs/` delta.
`azimuth change check` is expected to report `change declares no supported intent delta`; current
model checks and the explicit proposal remain the machine and human records until replacement
deltas exist.
