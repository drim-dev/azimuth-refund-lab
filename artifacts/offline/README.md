# Offline manifest

This directory commits only small, inspectable preparation inputs:

- `images.txt` names every image required by the Compose runtime;
- `source-inputs.sha256` fingerprints build and dependency-selection inputs.

It deliberately excludes Docker archives, Maven/npm caches, Playwright browsers, screenshots and
recordings. Those artifacts are large or machine-specific and belong in the conference operator's
offline kit. See `docs/rehearsal/offline.md` for preparation and verification.
