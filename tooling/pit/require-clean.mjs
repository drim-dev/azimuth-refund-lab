#!/usr/bin/env node

import fs from "node:fs";

const [file] = process.argv.slice(2);
if (!file) fail("usage: require-clean.mjs <observation.json>");

let manifest;
try {
  manifest = JSON.parse(fs.readFileSync(file, "utf8"));
} catch (error) {
  fail(`observation ${file} is unavailable or invalid JSON: ${error.message}`);
}

if (!Array.isArray(manifest.observations) || manifest.observations.length !== 1) {
  fail("expected exactly one imported PIT observation");
}
const observation = manifest.observations[0];
if (observation.tool !== "PIT" || observation.kind !== "mutation-test") {
  fail("imported observation does not identify a PIT mutation challenge");
}
if (!Array.isArray(observation.bindings) || observation.bindings.length === 0) {
  fail("PIT observation has no claim-specific challenge binding");
}
if (observation.bindings.some((binding) => binding.role !== "challenge")) {
  fail("PIT observation contains a non-challenge binding");
}
const findings = observation.bindings.filter((binding) => binding.outcome !== "clean");
if (findings.length > 0) {
  fail(`${findings.length} PIT challenge binding(s) require agent review`);
}
process.stderr.write(`PIT challenge is clean for ${observation.bindings.length} binding(s)\n`);

function fail(message) {
  process.stderr.write(`require-clean-pit: ${message}\n`);
  process.exit(2);
}
