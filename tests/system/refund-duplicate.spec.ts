import { expect, test, type APIRequestContext } from "@playwright/test";

interface TripFinancialView {
  refunds: Array<{ refundId: string; status: "pending" | "succeeded" }>;
}

interface FailpointStatus {
  state: "idle" | "armed" | "paused" | "released";
}

interface ProviderHistory {
  attempts: Array<{ idempotencyKey: string }>;
  operations: Array<{ idempotencyKey: string }>;
  applicationCount: number;
}

const POLL_INTERVAL_MS = 200;
const POLL_TIMEOUT_MS = 20_000;

test("unsafe worker changes the provider key on redelivery and repeats the refund", async ({
  page,
  request,
}) => {
  test.skip(process.env.REFUND_LAB_SCENARIO !== "duplicate");

  await page.goto("/");
  await page.getByRole("tab", { name: "Support console" }).click();
  await expect(page.getByText("Unsafe worker fixture")).toBeVisible();

  await page.getByRole("button", { name: "Arm interruption after provider response" }).click();
  await page.getByRole("button", { name: "Approve €30.00 refund" }).click();

  await pollJson<FailpointStatus>(
    request,
    "/api/demo/failpoint",
    (status) => status.state === "paused",
  );
  const pending = await pollJson<TripFinancialView>(
    request,
    "/api/trip",
    (trip) => trip.refunds[0]?.status === "pending",
  );
  const refundId = pending.refunds[0].refundId;
  const beforeRelease = await pollJson<ProviderHistory>(
    request,
    `/api/demo/provider-ledger/${refundId}`,
    (history) => history.applicationCount === 1,
  );
  expect(beforeRelease.attempts).toHaveLength(1);
  expect(beforeRelease.operations).toHaveLength(1);

  await page.getByRole("button", { name: "Refresh" }).click();
  await expect(page.getByText("Applications").locator("..")).toContainText("1");
  await page.getByRole("button", { name: "Interrupt processing and trigger redelivery" }).click();

  const duplicate = await pollJson<ProviderHistory>(
    request,
    `/api/demo/provider-ledger/${refundId}`,
    (history) => history.attempts.length === 2 && history.applicationCount === 2,
  );
  expect(duplicate.operations).toHaveLength(2);
  expect(duplicate.attempts[0].idempotencyKey).not.toBe(duplicate.attempts[1].idempotencyKey);

  await page.getByRole("button", { name: "Refresh" }).click();
  await expect(page.getByText("Applications").locator("..")).toContainText("2");
  await page.getByRole("tab", { name: "Rider view" }).click();
  await expect(page.locator(".receipt-lines .refund-line")).toContainText(
    "Approved refund succeeded−€30.00",
  );
});

async function pollJson<T>(
  request: APIRequestContext,
  path: string,
  predicate: (value: T) => boolean,
): Promise<T> {
  const deadline = Date.now() + POLL_TIMEOUT_MS;
  let latest: T | undefined;
  while (Date.now() < deadline) {
    const response = await request.get(path);
    if (response.ok()) {
      latest = (await response.json()) as T;
      if (predicate(latest)) {
        return latest;
      }
    }
    await new Promise((resolve) => setTimeout(resolve, POLL_INTERVAL_MS));
  }
  throw new Error(`Timed out polling ${path}; last body=${JSON.stringify(latest)}`);
}
