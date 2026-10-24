export const FIXTURE = {
  riderId: "11111111-1111-1111-1111-111111111111",
  tripId: "22222222-2222-2222-2222-222222222222",
  paymentId: "33333333-3333-3333-3333-333333333333",
  refundAmount: 30,
  currency: "EUR",
} as const;

export type RefundStatus = "pending" | "succeeded";

export interface Money {
  amount: number;
  currency: string;
}

export interface RefundView {
  refundId: string;
  paymentId: string;
  amount: number;
  currency: string;
  status: RefundStatus;
  providerOperationId?: string;
  approvedAt: string;
  completedAt?: string;
}

export interface TripFinancialView {
  tripId: string;
  riderId: string;
  paymentId: string;
  capturedAmount: Money;
  originalFare: Money;
  waitTimeFee: Money;
  approvedRefundTotal: Money;
  finalCost: Money;
  refunds: RefundView[];
}

export type FailpointState = "idle" | "armed" | "paused" | "released";

export interface FailpointStatus {
  state: FailpointState;
  pausedRefundId: string | null;
  interruptionCount: number;
}

export interface ProviderRefundView {
  idempotencyKey: string;
  refundId: string;
  providerOperationId: string;
  outcome: "succeeded";
  appliedAt: string;
}

export interface ProviderLedgerView {
  refundId: string;
  attempts: Array<{
    idempotencyKey: string;
    refundId: string;
    paymentId: string;
    amount: number;
    currency: string;
    attemptedAt: string;
  }>;
  operations: ProviderRefundView[];
  applicationCount: number;
}

export interface ProblemDetail {
  title?: string;
  detail?: string;
  status?: number;
}
