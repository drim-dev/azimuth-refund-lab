#!/usr/bin/env node

import crypto from "node:crypto";
import fs from "node:fs";
import path from "node:path";

const [policy, projectRoot, ...manifestPaths] = process.argv.slice(2);
if (!policy || !projectRoot || manifestPaths.length === 0) {
  fail("usage: check-challenges.mjs <optional|required> <project-root> <manifest>...");
}
if (policy !== "optional" && policy !== "required") {
  fail(`unknown challenge policy: ${policy}`);
}

const challenges = [];
for (const manifestPath of manifestPaths) {
  const document = readJson(manifestPath);
  for (const observation of document.observations ?? []) {
    verifyReportIntegrity(observation);
    for (const binding of observation.bindings ?? []) {
      if (binding.role === "challenge") challenges.push({ observation, binding });
    }
  }
}

if (policy === "required" && challenges.length === 0) {
  fail("required challenge observation is missing");
}

let findings = 0;
for (const { observation, binding } of challenges) {
  const label = `${observation.id} -> ${binding.spec}#${binding.scenario}`;
  process.stdout.write(`challenge ${label}: ${binding.outcome}\n`);
  if (binding.outcome !== "clean") findings += 1;
}

if (findings > 0) {
  fail(`${findings} challenge binding(s) require review`);
}
process.stdout.write(`challenge gate: ${challenges.length} current binding(s) accepted\n`);

function verifyReportIntegrity(observation) {
  const expected = observation.payload?.report_sha256;
  if (!expected) return;
  const reportPath = path.resolve(projectRoot, observation.report);
  const actual = crypto.createHash("sha256").update(fs.readFileSync(reportPath)).digest("hex");
  if (actual !== expected) {
    fail(`challenge report changed after observation ${observation.id} was recorded`);
  }
}

function readJson(file) {
  try {
    return JSON.parse(fs.readFileSync(file, "utf8"));
  } catch (error) {
    fail(`cannot read challenge manifest ${file}: ${error.message}`);
  }
}

function fail(message) {
  process.stderr.write(`challenge gate: CLOSED (${message})\n`);
  process.exit(1);
}
