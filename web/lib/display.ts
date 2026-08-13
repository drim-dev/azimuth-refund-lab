import type { FailpointStatus, Money, ProviderLedgerView, RefundView } from "./contracts";

export function formatMoney(money: Money): string {
  return new Intl.NumberFormat("en", {
    style: "currency",
    currency: money.currency,
    minimumFractionDigits: 2,
  }).format(money.amount);
}

export function formatAmount(amount: number, currency: string): string {
  return formatMoney({ amount, currency });
}

export function formatMoment(value: string): string {
  return new Intl.DateTimeFormat("en", {
    dateStyle: "medium",
    timeStyle: "medium",
    timeZone: "UTC",
  }).format(new Date(value));
}

export type JourneyStage = "captured" | "api-pending" | "provider-applied" | "completed";

export function journeyStage(
  refund: RefundView | undefined,
  providerLedger: ProviderLedgerView | null,
  failpoint: FailpointStatus | null,
): JourneyStage {
  if (!refund) {
    return "captured";
  }
  if (refund.status === "succeeded") {
    return "completed";
  }
  if (providerLedger || failpoint?.state === "paused") {
    return "provider-applied";
  }
  return "api-pending";
}
