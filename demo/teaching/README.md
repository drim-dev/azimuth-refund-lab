# Teaching history

This directory separates a reproducible teaching sequence from the repository's actual history.
No Git tags exist yet, because creating commits and tags is an external publication boundary. The
future immutable tag names are declared in `stages.tsv`; `scripts/demo-stage checkout` fails rather
than guessing when a tag has not been materialized.

Three provenance labels are used:

- **constructed fixture** — a minimal, synthetic account that isolates one diagnostic. It did not
  occur as a commit in this repository.
- **authentic observation** — an output produced by an executable fixture. The bounded teaching
  fixture produces one surviving mutant under the weak response oracle and kills the same mutant
  under a direct financial-effect oracle. Separately, the application PIT run found eleven
  `TIMED_OUT` review items; it did not find a surviving mutant.
- **authentic current account** — the checked-in model, linkage, evidence and judgment from the
  present worktree.

The sequence is intentionally progressive:

1. a missing cover is rejected by the machine tier;
2. an example cannot satisfy the required universal evidence form;
3. structurally green but dishonest evidence is rejected by an agent judgment;
4. structurally green but toothless evidence is rejected by an agent judgment;
5. a bounded real mutation run shows one survivor under a weak oracle;
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
```

The conference cue interface is also accepted directly. `scripts/demo-stage demo-01-uncovered`
selects an offline state for the following `scripts/azimuth-check`; stage 5 selects the surviving
report for `scripts/pit-replay`, and stage 7 selects the killed report. This mirrors the speaker
guide without modifying or checking out the worktree.

`demo-stage checkout` is reserved for the future immutable Git history. It requires a clean
worktree and an existing annotated tag, and therefore cannot accidentally synthesize history.

Run `scripts/pit-replay --application` to show the separate real-application result: eleven PIT
timeouts, no killed mutant, and no survivor. It remains visible because it is evidence about an
unresolved mutation-harness limitation, not because it helps the teaching narrative.
