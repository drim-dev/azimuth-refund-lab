# Teaching history

This directory separates a reproducible teaching sequence from the repository's actual history.
No Git tags exist yet, because creating commits and tags is an external publication boundary. The
future immutable tag names are declared in `stages.tsv`; `scripts/demo-stage checkout` fails rather
than guessing when a tag has not been materialized.

Three provenance labels are used:

- **constructed fixture** — a minimal, synthetic account that isolates one diagnostic. It did not
  occur as a commit in this repository.
- **authentic observation** — an output produced by an executable fixture. The bounded teaching
  fixture produces one surviving stable-key mutant under the weak status oracle and kills the same
  mutant under a direct attempt/key/effect oracle. The focused application PIT run also kills its
  stable-key branch mutant without a timeout.
- **authentic current account** — the checked-in model, linkage, evidence and judgment from the
  present worktree.

The sequence is intentionally progressive:

1. a missing cover is rejected by the machine tier;
2. an example cannot satisfy the required universal evidence form;
3. a structurally green graph becomes `dishonest-tag-judged` after an agent records its adverse
   judgment;
4. a structurally green graph becomes `toothless-evidence` after an agent records its adverse
   judgment;
5. a bounded real mutation challenge records one survivor and the resulting toothless judgment;
6. a changed account makes a formerly sound judgment stale;
7. the direct oracle kills that same teaching mutant, while the current worktree has a complete
   machine account and a current sound judgment.

Run the isolated fixtures without changing the worktree:

```sh
scripts/demo-stage list
scripts/demo-stage replay missing-cover
scripts/show-judgment-history
scripts/pit-replay
scripts/azimuth-check
scripts/teaching-acceptance-gate
```

The conference cue interface is also accepted directly. `scripts/demo-stage demo-01-uncovered`
selects an offline state for the following `scripts/azimuth-check`; stage 5 selects the surviving
report for `scripts/pit-replay`, and stage 7 selects the killed report. This mirrors the speaker
guide without modifying or checking out the worktree.

`scripts/azimuth-check` runs the selected account without normalizing its result. Missing links,
wrong forms, recorded adverse judgments and stale judgments therefore return nonzero exactly as
they would in CI. Stages 3 and 4 were structurally green before judgment; their stage-specific
models now contain the adverse, fingerprinted judgments that the deterministic tier enforces.
Use `scripts/azimuth-check --pre-review` on stages 3–5 to display the same selected linkage before
its judgment was recorded. This is a separate, declared model input rather than a request to ignore
an existing adverse verdict.

Selecting a stage does not rewrite `azimuth/model`. `scripts/demo-stage` prints the selected offline
model path; the current tree remains the authentic final account throughout fixture replay.

`scripts/teaching-acceptance-gate` applies one policy to every selected account: the Azimuth check
must pass, every required challenge must be present, and every challenge outcome must be `clean`.
The gate reads model and observation paths from `fixtures/stage-accounts.tsv`; it does not infer an
outcome from a stage name. Only the authentic final account opens the gate.

`demo-stage checkout` is reserved for the future immutable Git history. It requires a clean
worktree and an existing annotated tag, and therefore cannot accidentally synthesize history.

Run `scripts/pit-replay --application` to show the separate real-application result: one selected
worker key-policy mutant killed by one linked test, with no review items.

Run `demo/teaching/verify-stages` to check every expected raw diagnostic and acceptance outcome.
