# Offline rehearsal procedure

The offline package is a procedure, not a committed binary bundle. Large Docker archives,
dependency caches, browser binaries and recordings stay outside Git.

## Prepare while online

1. Build every application image from the intended immutable revision while online. A successful
   `docker compose build` is a preparation condition, not part of the live rehearsal.
2. Run the composed Playwright test and `scripts/rehearsal run`.
3. Run `scripts/rehearsal offline-check` to verify the exact runtime names in
   `artifacts/offline/images.txt` and the source inputs in
   `artifacts/offline/source-inputs.sha256`.
4. Export the listed images to an external archive with `docker image save`. Record that archive's
   SHA-256 beside the archive, not in this repository.
5. Retain the Maven repository, npm caches, Playwright Chromium bundle, slides, screenshots and a
   short demo recording on the presentation machine. The running demo needs only the saved runtime
   images; rebuilding or reinstalling during the talk is not part of the recovery path.

## Prove the offline path

Disable networking before rehearsal. Load the external image archive if the images are absent,
then run:

```text
scripts/rehearsal offline-check
scripts/demo-start-duplicate
# perform the browser journey
scripts/demo-stop
```

`scripts/demo-start-duplicate` and both rehearsal journeys deliberately start with `--no-build`.
If an image is absent, they fail instead of contacting a registry or rebuilding different source.
`scripts/demo-reset` remains a developer convenience and is not the offline conference recovery
path.

## Refreshing the manifest

When a listed source input intentionally changes, regenerate its line with `shasum -a 256` on
macOS or `sha256sum` on Linux and review the diff. When Compose adds or renames an image, update
`images.txt` and repeat the disabled-network rehearsal. Never accept a checksum mismatch merely to
make the rehearsal green.
