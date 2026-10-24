import { expect, test, type APIRequestContext } from "@playwright/test";
import { covers } from "@azimuth/tags";

interface Money {
  amount: number;
  currency: string;
}

interface RefundView {
  refundId: string;
  amount: number;
  currency: string;
  status: "pending" | "succeeded";
}

interface TripFinancialView {
  capturedAmount: Money;
  finalCost: Money;
  refunds: RefundView[];
}

interface FailpointStatus {
  state: "idle" | "armed" | "paused" | "released";
  pausedRefundId: string | null;
  interruptionCount: number;
}

interface ProviderLedgerView {
  refundId: string;
  attempts: ProviderAttempt[];
  operations: Array<{
    idempotencyKey: string;
    refundId: string;
    providerOperationId: string;
    outcome: "succeeded";
    appliedAt: string;
  }>;
  applicationCount: number;
}

interface ProviderAttempt {
  idempotencyKey: string;
  refundId: string;
  paymentId: string;
  amount: number;
  currency: string;
  attemptedAt: string;
}

const POLL_INTERVAL_MS = 200;
const POLL_TIMEOUT_MS = 20_000;

test("a worker interruption redelivers one refund without repeating its financial effect", async ({
  page,
  request,
}) => {
  test.skip(process.env.REFUND_LAB_SCENARIO === "duplicate");

  covers(
    "refunds/execution",
    "repeated-processing-does-not-repeat-refund",
    "e2e",
    "example",
    "relational",
  );
  covers(
    "refunds/execution",
    "interrupted-processing-eventually-completes",
    "e2e",
    "example",
    "relational",
  );

  await page.goto("/");
  await expect(page.getByRole("heading", { name: "Central Station → Riverside" })).toBeVisible();
  await expect(page.getByText("Captured payment").locator("..")).toContainText("€120.00");

  await page.getByRole("tab", { name: "Support console" }).click();
  await page.getByRole("button", { name: "Arm interruption after provider response" }).click();
  await expect(page.getByText("Worker failpoint").locator("..")).toContainText("armed");

  await page.getByRole("button", { name: "Approve €30.00 refund" }).click();

  const paused = await pollJson<FailpointStatus>(
    request,
    "/api/demo/failpoint",
    (status) => status.state === "paused",
  );
  const pendingTrip = await pollJson<TripFinancialView>(
    request,
    "/api/trip",
    (trip) => trip.refunds.at(-1)?.status === "pending",
  );
  const pendingRefund = expectSingleRefund(pendingTrip);
  expect(paused.pausedRefundId).toBe(pendingRefund.refundId);

  const ledgerWhileApiIsPending = await pollJson<ProviderLedgerView>(
    request,
    `/api/demo/provider-ledger/${pendingRefund.refundId}`,
    (ledger) => ledger.applicationCount === 1,
  );
  expect(ledgerWhileApiIsPending.refundId).toBe(pendingRefund.refundId);
  expect(ledgerWhileApiIsPending.operations).toHaveLength(1);
  expect(ledgerWhileApiIsPending.operations[0]).toMatchObject({
    idempotencyKey: pendingRefund.refundId,
    refundId: pendingRefund.refundId,
    outcome: "succeeded",
  });
  expect(ledgerWhileApiIsPending.attempts).toHaveLength(1);
  expectProviderAttempt(ledgerWhileApiIsPending.attempts[0], pendingRefund.refundId);
  expect(pendingTrip.finalCost).toEqual({ amount: 90, currency: "EUR" });

  await page.getByRole("button", { name: "Refresh" }).click();
  await expect(page.getByText("Applications").locator("..")).toContainText("1");
  await expect(page.getByText("Worker failpoint").locator("..")).toContainText("paused");

  await page.getByRole("button", { name: "Interrupt processing and trigger redelivery" }).click();

  const completedTrip = await pollJson<TripFinancialView>(
    request,
    "/api/trip",
    (trip) => trip.refunds.at(-1)?.status === "succeeded",
  );
  const completedRefund = expectSingleRefund(completedTrip);
  expect(completedRefund).toMatchObject({
    refundId: pendingRefund.refundId,
    amount: 30,
    currency: "EUR",
    status: "succeeded",
  });
  expect(completedTrip.capturedAmount).toEqual({ amount: 120, currency: "EUR" });
  expect(completedTrip.finalCost).toEqual({ amount: 90, currency: "EUR" });

  const finalLedger = await pollJson<ProviderLedgerView>(
    request,
    `/api/demo/provider-ledger/${pendingRefund.refundId}`,
    (ledger) => ledger.attempts.length === 2,
  );
  expect(finalLedger.attempts).toHaveLength(2);
  expectProviderAttempt(finalLedger.attempts[0], pendingRefund.refundId);
  expectProviderAttempt(finalLedger.attempts[1], pendingRefund.refundId);
  expect(finalLedger.attempts.map((attempt) => attempt.idempotencyKey)).toEqual([
    pendingRefund.refundId,
    pendingRefund.refundId,
  ]);
  expect(finalLedger.operations).toHaveLength(1);
  expect(finalLedger.operations[0].idempotencyKey).toBe(pendingRefund.refundId);
  expect(finalLedger.applicationCount).toBe(1);

  const interrupted = await pollJson<FailpointStatus>(
    request,
    "/api/demo/failpoint",
    (status) => status.state === "idle" && status.interruptionCount === 1,
  );
  expect(interrupted.pausedRefundId).toBeNull();

  await page.getByRole("tab", { name: "Rider view" }).click();
  await expect(page.locator(".receipt-lines .total")).toContainText("Final cost€90.00");
  await expect(page.locator(".receipt-lines .refund-line")).toContainText(
    "Approved refund succeeded−€30.00",
  );
  await expect(
    page.getByText("The €30.00 correction is complete. Your final ride cost is €90.00."),
  ).toBeVisible();
});

function expectSingleRefund(trip: TripFinancialView): RefundView {
  expect(trip.refunds).toHaveLength(1);
  return trip.refunds[0];
}

function expectProviderAttempt(attempt: ProviderAttempt, refundId: string): void {
  expect(attempt).toMatchObject({
    idempotencyKey: refundId,
    refundId,
    paymentId: "33333333-3333-3333-3333-333333333333",
    amount: 30,
    currency: "EUR",
  });
}

async function pollJson<T>(
  request: APIRequestContext,
  path: string,
  predicate: (value: T) => boolean,
): Promise<T> {
  const deadline = Date.now() + POLL_TIMEOUT_MS;
  let latest: T | undefined;
  let latestStatus: number | undefined;

  while (Date.now() < deadline) {
    const response = await request.get(path);
    latestStatus = response.status();
    if (response.ok()) {
      latest = (await response.json()) as T;
      if (predicate(latest)) {
        return latest;
      }
    }
    await new Promise((resolve) => setTimeout(resolve, POLL_INTERVAL_MS));
  }

  throw new Error(
    `Timed out polling ${path}; last status=${latestStatus}, last body=${JSON.stringify(latest)}`,
  );
}
