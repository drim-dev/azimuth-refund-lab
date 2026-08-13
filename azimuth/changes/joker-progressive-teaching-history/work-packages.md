# Work packages: joker-progressive-teaching-history

## Work package: provider-falsifier
Status: complete
Depends on: none
Owns: services/provider-simulator, compose.failure.yaml
Objective: make duplicate financial effects real only in an explicit demo profile
Evidence: default and unsafe real-PostgreSQL component tests

## Work package: teaching-states
Status: complete
Depends on: provider-falsifier
Owns: demo/teaching, scripts/demo-stage, scripts/azimuth-check, scripts/pit-replay,
  scripts/show-judgment-history
Objective: provide deterministic structural, semantic, mutation and staleness states
Evidence: every state and exact expected diagnostic is replayed offline

## Work package: immutable-history
Status: pending
Depends on: teaching-states
Owns: Git commits and annotated tags named in demo/teaching/stages.tsv
Objective: publish immutable presentation revisions after review and explicit authorization
Evidence: every tag resolves to the recorded commit and provenance metadata
