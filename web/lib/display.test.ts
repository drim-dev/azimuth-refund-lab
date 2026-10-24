import { describe, expect, it } from "vitest";
import type { FailpointStatus, ProviderLedgerView, RefundView } from "./contracts";
import { formatAmount, journeyStage } from "./display";

const pendingRefund: RefundView = {
  refundId: "44444444-4444-4444-4444-444444444444",
  paymentId: "33333333-3333-3333-3333-333333333333",
  amount: 30,
  currency: "EUR",
  status: "pending",
  approvedAt: "2026-08-12T09:00:00Z",
};

const providerLedger: ProviderLedgerView = {
  refundId: pendingRefund.refundId,
  attempts: [{
    idempotencyKey: pendingRefund.refundId,
    refundId: pendingRefund.refundId,
    paymentId: pendingRefund.paymentId,
    amount: pendingRefund.amount,
    currency: pendingRefund.currency,
    attemptedAt: "2026-08-12T09:00:00Z",
  }],
  operations: [{
    idempotencyKey: pendingRefund.refundId,
    refundId: pendingRefund.refundId,
    providerOperationId: "66666666-6666-6666-6666-666666666666",
    outcome: "succeeded",
    appliedAt: "2026-08-12T09:00:01Z",
  }],
  applicationCount: 1,
};

describe("refund display", () => {
  it("formats the fixture amount as EUR", () => {
    expect(formatAmount(30, "EUR")).toBe("€30.00");
  });

  it("distinguishes provider truth from the pending API projection", () => {
    expect(journeyStage(undefined, null, null)).toBe("captured");
    expect(journeyStage(pendingRefund, null, null)).toBe("api-pending");
    expect(journeyStage(pendingRefund, providerLedger, null)).toBe("provider-applied");
    expect(journeyStage({ ...pendingRefund, status: "succeeded" }, providerLedger, null)).toBe(
      "completed",
    );
  });

  it("uses a paused delivery as evidence that the provider boundary was crossed", () => {
    const paused: FailpointStatus = {
      state: "paused",
      pausedRefundId: pendingRefund.refundId,
      interruptionCount: 0,
    };
    expect(journeyStage(pendingRefund, null, paused)).toBe("provider-applied");
  });
});
