# Work packages: refund-redelivery-assurance

## Work package: linkage
Status: complete
Depends on: none
Owns: tooling/azimuth-jvm, tooling/azimuth-typescript, scripts/emit-azimuth, services/refund-worker, services/provider-simulator, tests/system
Objective: add language-native relations and produce one fresh linkage manifest
Evidence: extractor builds and machine-tier diagnostics on controlled manifest variants

## Work package: mutation
Status: complete
Depends on: linkage
Owns: tooling/pit
Objective: challenge provider idempotency and import the PIT report as an Azimuth observation
Evidence: a targeted PIT run plus strict generic importer tests

## Work package: judgment
Status: pending
Depends on: mutation
Owns: azimuth/model/refunds/provider-effects, azimuth/changes/refund-redelivery-assurance, artifacts/assurance
Objective: review current sources, evidence and challenge, then record a fingerprinted verdict
Evidence: judge worklist, current judgment and assurance snapshot
