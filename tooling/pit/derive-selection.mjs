#!/usr/bin/env node

import fs from "node:fs";
import path from "node:path";

const [linkageFile, outputFile] = process.argv.slice(2);
if (!linkageFile || !outputFile) {
  fail("usage: derive-selection.mjs <linkage.json> <selection.json>");
}

const linkage = readJson(linkageFile, "linkage manifest");
const realizes = relationArray(linkage.realizes, "realizes");
const covers = relationArray(linkage.covers, "covers");
const targetSites = ["dev.drim.azimuth.refund.worker.ProviderIdempotencyKeys.forRefund"];
const targetClasses = [
  "dev.drim.azimuth.refund.worker.ProviderIdempotencyKeys",
];
const targetRelations = targetSites.map((site) => {
  const matches = realizes.filter((relation) => relation.lang === "java" && relation.site === site);
  if (matches.length !== 1) {
    fail(`expected exactly one JVM Realizes site ${site}, found ${matches.length}`);
  }
  return matches[0];
});
const target = targetRelations[0];
for (const relation of targetRelations.slice(1)) {
  if (relation.spec !== target.spec || relation.scenario !== target.scenario) {
    fail("PIT target sites do not realize one claim");
  }
}
const selected = covers
  .filter((relation) => relation.lang === "java")
  .filter((relation) => relation.spec === target.spec && relation.scenario === target.scenario)
  .filter((relation) => relation.site.startsWith(
    "dev.drim.azimuth.refund.worker.ProviderIdempotencyKeysTest."))
  .sort((left, right) => left.site.localeCompare(right.site));

if (selected.length === 0) {
  fail(`claim linked from ${targetSites[0]} has no worker key-selection Covers site`);
}
if (new Set(selected.map((relation) => relation.site)).size !== selected.length) {
  fail("worker key-selection Covers sites are not unique");
}

const selection = {
  schema: "azimuth-pit-selection/1",
  target_classes: targetClasses,
  selected_tests: selected.map((relation) => ({
    site: relation.site,
    pit_names: [pitDisplayName(relation.site), pitEngineName(relation.site)],
  })),
};

fs.mkdirSync(path.dirname(path.resolve(outputFile)), { recursive: true });
fs.writeFileSync(outputFile, `${JSON.stringify(selection, null, 2)}\n`);
process.stderr.write(
  `${targetClasses.length} class: selected ${selected.length} linked worker key-selection test(s)`
    + ` → ${outputFile}\n`,
);

function pitDisplayName(site) {
  const separator = site.lastIndexOf(".");
  if (separator <= 0 || separator === site.length - 1) {
    fail(`cannot convert JVM method site ${site} to a PIT test name`);
  }
  return `${site.slice(separator + 1)}(${site.slice(0, separator)})`;
}

function pitEngineName(site) {
  const separator = site.lastIndexOf(".");
  if (separator <= 0 || separator === site.length - 1) {
    fail(`cannot convert JVM method site ${site} to a PIT engine name`);
  }
  const className = site.slice(0, separator);
  const methodName = site.slice(separator + 1);
  return `${className}.[engine:junit-jupiter]/[class:${className}]/[method:${methodName}()]`;
}

function relationArray(value, name) {
  if (!Array.isArray(value)) fail(`linkage manifest ${name} must be an array`);
  return value.map((relation, index) => {
    if (!relation || typeof relation !== "object" || Array.isArray(relation)) {
      fail(`linkage manifest ${name}[${index}] must be an object`);
    }
    for (const field of ["spec", "scenario", "site", "lang"]) {
      if (typeof relation[field] !== "string" || relation[field].length === 0) {
        fail(`linkage manifest ${name}[${index}].${field} must be a non-empty string`);
      }
    }
    return relation;
  });
}

function readJson(file, description) {
  try {
    return JSON.parse(fs.readFileSync(file, "utf8"));
  } catch (error) {
    fail(`${description} ${file} is unavailable or invalid JSON: ${error.message}`);
  }
}

function fail(message) {
  process.stderr.write(`derive-pit-selection: ${message}\n`);
  process.exit(2);
}
