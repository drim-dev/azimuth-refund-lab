import { defineConfig } from "@playwright/test";

export default defineConfig({
  testDir: ".",
  testMatch: "*.spec.ts",
  fullyParallel: false,
  forbidOnly: true,
  retries: 0,
  workers: 1,
  timeout: 60_000,
  expect: {
    timeout: 10_000,
  },
  reporter: [["list"]],
  use: {
    baseURL: process.env.REFUND_LAB_URL ?? "http://127.0.0.1:3100",
    trace: "retain-on-failure",
    screenshot: "only-on-failure",
  },
});
