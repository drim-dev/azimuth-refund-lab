import fs from "node:fs";
import path from "node:path";

const keys = [
  "realizes",
  "covers",
  "mechanism_implementations",
  "mechanism_covers",
  "class_members",
  "enumerations",
  "artifacts",
  "observations",
];

const [output, ...inputs] = process.argv.slice(2);
if (!output || inputs.length === 0) {
  throw new Error("usage: merge-manifests.mjs <output> <manifest>...");
}

const union = Object.fromEntries(keys.slice(0, -1).map((key) => [key, []]));
for (const input of inputs) {
  const manifest = JSON.parse(fs.readFileSync(input, "utf8"));
  for (const key of keys) {
    if (manifest[key] === undefined && key === "observations") {
      continue;
    }
    if (!Array.isArray(manifest[key])) {
      throw new Error(`${input} has no array field ${key}`);
    }
    union[key] ??= [];
    union[key].push(...manifest[key]);
  }
}

for (const entries of Object.values(union)) {
  entries.sort((left, right) => JSON.stringify(left).localeCompare(JSON.stringify(right)));
}

fs.mkdirSync(path.dirname(path.resolve(output)), { recursive: true });
fs.writeFileSync(output, `${JSON.stringify(union, null, 2)}\n`);
