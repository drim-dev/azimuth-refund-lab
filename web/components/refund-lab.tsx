"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import { realizes } from "@/lib/azimuth";
import type {
  FailpointStatus,
  ProblemDetail,
  ProviderLedgerView,
  RefundView,
  TripFinancialView,
} from "@/lib/contracts";
import { FIXTURE } from "@/lib/contracts";
import { formatAmount, formatMoment, formatMoney, journeyStage } from "@/lib/display";

type ViewName = "rider" | "support";
type DemoAction = "arm" | "release";

const stages = [
  { id: "captured", label: "Payment captured", detail: "€120.00 recorded" },
  { id: "api-pending", label: "Refund reserved", detail: "Refund API: pending" },
  { id: "provider-applied", label: "Provider applied", detail: "Public operation recorded" },
  { id: "completed", label: "Rider updated", detail: "Final cost: €90.00" },
] as const;

export function RefundLab({ unsafeWorkerMode = false }: { unsafeWorkerMode?: boolean }) {
  const [activeView, setActiveView] = useState<ViewName>("rider");
  const [trip, setTrip] = useState<TripFinancialView | null>(null);
  const [failpoint, setFailpoint] = useState<FailpointStatus | null>(null);
  const [providerLedger, setProviderLedger] = useState<ProviderLedgerView | null>(null);
  const [busy, setBusy] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [notice, setNotice] = useState("Loading fixture state…");

  const refund = useMemo(() => latestRefund(trip?.refunds ?? []), [trip]);
  const currentStage = journeyStage(refund, providerLedger, failpoint);

  function selectViewFromKeyboard(event: React.KeyboardEvent<HTMLButtonElement>) {
    if (!["ArrowLeft", "ArrowRight", "Home", "End"].includes(event.key)) {
      return;
    }
    event.preventDefault();
    const nextView = event.key === "ArrowLeft" || event.key === "Home" ? "rider" : "support";
    setActiveView(nextView);
    window.requestAnimationFrame(() => document.getElementById(`${nextView}-tab`)?.focus());
  }

  const refresh = useCallback(async (quiet = false) => {
    if (!quiet) {
      setBusy("refresh");
    }
    try {
      const [tripResponse, failpointResponse] = await Promise.all([
        fetch("/api/trip", { cache: "no-store" }),
        fetch("/api/demo/failpoint", { cache: "no-store" }),
      ]);
      const tripView = await readJson<TripFinancialView>(tripResponse);
      const workerState = failpointResponse.ok
        ? await readJson<FailpointStatus>(failpointResponse)
        : null;
      const currentRefund = latestRefund(tripView.refunds);
      let ledger: ProviderLedgerView | null = null;
      if (currentRefund) {
        const ledgerResponse = await fetch(
          `/api/demo/provider-ledger/${encodeURIComponent(currentRefund.refundId)}`,
          { cache: "no-store" },
        );
        if (ledgerResponse.ok) {
          ledger = await readJson<ProviderLedgerView>(ledgerResponse);
        }
      }

      setTrip(tripView);
      setFailpoint(workerState);
      setProviderLedger(ledger);
      setError(null);
      setNotice(currentRefund ? `Refund is ${currentRefund.status}.` : "Ready for support approval.");
    } catch (reason) {
      setError(errorMessage(reason));
      setNotice("Fixture state could not be refreshed.");
    } finally {
      if (!quiet) {
        setBusy(null);
      }
    }
  }, []);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  useEffect(() => {
    if (refund?.status !== "pending") {
      return;
    }
    const timer = window.setInterval(() => void refresh(true), 1200);
    return () => window.clearInterval(timer);
  }, [refund?.status, refresh]);

  async function approveRefund() {
    setBusy("approve");
    setError(null);
    setNotice("Submitting the €30.00 correction…");
    try {
      const response = await fetch("/api/refunds", { method: "POST" });
      const approved = await readJson<RefundView>(response);
      setNotice(`Refund ${shortId(approved.refundId)} is pending.`);
      await refresh(true);
    } catch (reason) {
      setError(errorMessage(reason));
      setNotice("The refund was not approved.");
    } finally {
      setBusy(null);
    }
  }

  async function changeFailpoint(action: DemoAction) {
    setBusy(action);
    setError(null);
    try {
      const response = await fetch(`/api/demo/failpoint/${action}`, { method: "POST" });
      const state = await readJson<FailpointStatus>(response);
      setFailpoint(state);
      setNotice(
        action === "arm"
          ? "Worker will pause after the provider applies the next refund."
          : "Paused delivery released; RabbitMQ can redeliver it.",
      );
      await refresh(true);
    } catch (reason) {
      setError(errorMessage(reason));
      setNotice("The demo control did not change.");
    } finally {
      setBusy(null);
    }
  }

  return (
    <main>
      <header className="masthead">
        <a className="brand" href="#main-content" aria-label="Refund lab home">
          <span className="brand-mark" aria-hidden="true">A</span>
          <span>Azimuth <b>Refund Lab</b></span>
        </a>
        <div className="environment"><span aria-hidden="true">●</span> Local fixture</div>
      </header>

      <section className="hero" aria-labelledby="page-title">
        <p className="eyebrow">Trip payment · 12 August 2026</p>
        <h1 id="page-title">One correction, every boundary visible.</h1>
        <p>
          Follow a €30.00 wait-time fee refund from support approval to the payment provider and
          back to the rider’s final cost.
        </p>
      </section>

      <div className="shell" id="main-content">
        <div className="tabs" role="tablist" aria-label="Fixture perspectives">
          <button
            id="rider-tab"
            role="tab"
            aria-selected={activeView === "rider"}
            aria-controls="rider-panel"
            tabIndex={activeView === "rider" ? 0 : -1}
            onClick={() => setActiveView("rider")}
            onKeyDown={selectViewFromKeyboard}
          >
            Rider view
          </button>
          <button
            id="support-tab"
            role="tab"
            aria-selected={activeView === "support"}
            aria-controls="support-panel"
            tabIndex={activeView === "support" ? 0 : -1}
            onClick={() => setActiveView("support")}
            onKeyDown={selectViewFromKeyboard}
          >
            Support console
          </button>
        </div>

        <div className="live-region" role="status" aria-live="polite">{notice}</div>
        {error && <div className="error-banner" role="alert"><b>Action needed.</b> {error}</div>}

        {activeView === "rider" ? (
          <RiderPanel trip={trip} refund={refund} stage={currentStage} />
        ) : (
          <SupportPanel
            trip={trip}
            refund={refund}
            failpoint={failpoint}
            providerLedger={providerLedger}
            stage={currentStage}
            busy={busy}
            onApprove={approveRefund}
            onFailpoint={changeFailpoint}
            onRefresh={() => refresh()}
            unsafeWorkerMode={unsafeWorkerMode}
          />
        )}
      </div>
    </main>
  );
}

interface RiderPanelProps {
  trip: TripFinancialView | null;
  refund: RefundView | undefined;
  stage: ReturnType<typeof journeyStage>;
}

function RiderPanel({ trip, refund, stage }: RiderPanelProps) {
  realizes("refunds/execution", "interrupted-processing-eventually-completes");
  return (
    <section id="rider-panel" role="tabpanel" aria-labelledby="rider-tab" className="panel-grid">
      <article className="card receipt-card">
        <div className="card-heading">
          <div><p className="eyebrow">Ride complete</p><h2>Central Station → Riverside</h2></div>
          <StatusBadge status={refund?.status ?? "captured"} />
        </div>
        <dl className="receipt-lines">
          <div><dt>Original fare</dt><dd>{moneyOrFallback(trip?.originalFare, 90)}</dd></div>
          <div><dt>Wait-time fee</dt><dd>{moneyOrFallback(trip?.waitTimeFee, 30)}</dd></div>
          <div className="captured"><dt>Captured payment</dt><dd>{moneyOrFallback(trip?.capturedAmount, 120)}</dd></div>
          {refund && (
            <div className="refund-line">
              <dt>Approved refund <small>{refund.status}</small></dt>
              <dd>−{formatAmount(refund.amount, refund.currency)}</dd>
            </div>
          )}
          <div className="total"><dt>Final cost</dt><dd>{moneyOrFallback(trip?.finalCost, refund ? 90 : 120)}</dd></div>
        </dl>
        <p className="card-note">
          {refund?.status === "succeeded"
            ? "The €30.00 correction is complete. Your final ride cost is €90.00."
            : refund
              ? "Your €30.00 correction is processing. This page updates automatically."
              : "Support has not approved a correction for this trip yet."}
        </p>
      </article>
      <Journey stage={stage} />
    </section>
  );
}

interface SupportPanelProps {
  trip: TripFinancialView | null;
  refund: RefundView | undefined;
  failpoint: FailpointStatus | null;
  providerLedger: ProviderLedgerView | null;
  stage: ReturnType<typeof journeyStage>;
  busy: string | null;
  onApprove: () => Promise<void>;
  onFailpoint: (action: DemoAction) => Promise<void>;
  onRefresh: () => Promise<void>;
  unsafeWorkerMode: boolean;
}

function SupportPanel(props: SupportPanelProps) {
  const { trip, refund, failpoint, providerLedger, stage, busy, unsafeWorkerMode } = props;
  const latestProviderOperation = providerLedger?.operations.at(-1);
  const canArm = failpoint?.state === "idle" && !refund;
  const canRelease = failpoint?.state === "paused";

  return (
    <section id="support-panel" role="tabpanel" aria-labelledby="support-tab" className="support-layout">
      <div className="support-main">
        <article className="card">
          <div className="card-heading">
            <div><p className="eyebrow">Authenticated fixture role</p><h2>Incorrect wait-time fee</h2></div>
            <span className="role-chip">Support</span>
          </div>
          <div className="case-summary">
            <div><span>Payment</span><code>{shortId(trip?.paymentId ?? FIXTURE.paymentId)}</code></div>
            <div><span>Captured</span><strong>{moneyOrFallback(trip?.capturedAmount, 120)}</strong></div>
            <div><span>Refund due</span><strong>€30.00</strong></div>
          </div>
          {refund ? (
            <div className="approved-state">
              <StatusBadge status={refund.status} />
              <div>
                <b>€30.00 refund approved</b>
                <p>Refund {shortId(refund.refundId)} · {formatMoment(refund.approvedAt)}</p>
              </div>
            </div>
          ) : (
            <button className="primary-action" disabled={busy !== null} onClick={() => void props.onApprove()}>
              {busy === "approve" ? "Approving…" : "Approve €30.00 refund"}
            </button>
          )}
        </article>

        <Journey stage={stage} />
      </div>

      <aside className="card demo-card" aria-labelledby="demo-heading">
        <p className="eyebrow">Presenter controls</p>
        <h2 id="demo-heading">Delivery boundary</h2>
        <p className="muted">Pause once after provider success, before the API receives the result.</p>
        {unsafeWorkerMode && (
          <p className="scenario-warning" role="status">
            Unsafe worker fixture: each delivery creates a new provider idempotency key.
          </p>
        )}

        <div className="control-status">
          <span>Worker failpoint</span>
          <StatusBadge status={failpoint?.state ?? "unavailable"} />
        </div>
        <div className="button-row">
          <button
            className="secondary-action"
            disabled={!canArm || busy !== null}
            onClick={() => void props.onFailpoint("arm")}
          >
            {busy === "arm" ? "Arming interruption…" : "Arm interruption after provider response"}
          </button>
          <button
            className="danger-action"
            disabled={!canRelease || busy !== null}
            onClick={() => void props.onFailpoint("release")}
          >
            {busy === "release"
              ? "Interrupting processing…"
              : "Interrupt processing and trigger redelivery"}
          </button>
        </div>
        {failpoint?.pausedRefundId && (
          <p className="technical-line">Paused refund <code>{shortId(failpoint.pausedRefundId)}</code></p>
        )}
        <p className="technical-line">Interruptions <b>{failpoint?.interruptionCount ?? 0}</b></p>

        <hr />

        <div className="ledger-heading">
          <div><p className="eyebrow">Fixture-only reconciliation</p><h3>Provider refund history</h3></div>
          <button className="text-action" disabled={busy !== null} onClick={() => void props.onRefresh()}>
            Refresh
          </button>
        </div>
        {providerLedger ? (
          <dl className="ledger-details">
            <div><dt>Outcome</dt><dd><StatusBadge status={latestProviderOperation?.outcome ?? "unavailable"} /></dd></div>
            <div><dt>Latest operation</dt><dd><code>{shortId(latestProviderOperation?.providerOperationId ?? "—")}</code></dd></div>
            <div><dt>Applied</dt><dd>{latestProviderOperation ? formatMoment(latestProviderOperation.appliedAt) : "—"}</dd></div>
            <div><dt>Applications</dt><dd><b>{providerLedger.applicationCount}</b></dd></div>
            <div className="ledger-keys">
              <dt>Idempotency keys</dt>
              <dd>{providerLedger.attempts.map((attempt, index) => (
                <code key={`${attempt.idempotencyKey}-${index}`}>{shortId(attempt.idempotencyKey)}</code>
              ))}</dd>
            </div>
          </dl>
        ) : (
          <div className="empty-ledger"><span aria-hidden="true">○</span><p>No public provider transaction recorded.</p></div>
        )}
      </aside>
    </section>
  );
}

function Journey({ stage }: { stage: ReturnType<typeof journeyStage> }) {
  const activeIndex = stages.findIndex((item) => item.id === stage);
  return (
    <article className="card journey-card" aria-labelledby="journey-heading">
      <div className="card-heading"><div><p className="eyebrow">Live transaction</p><h2 id="journey-heading">Correction journey</h2></div></div>
      <ol className="journey">
        {stages.map((item, index) => {
          const state = index < activeIndex ? "complete" : index === activeIndex ? "current" : "upcoming";
          return (
            <li key={item.id} className={state} aria-current={state === "current" ? "step" : undefined}>
              <span className="step-mark" aria-hidden="true">{state === "complete" ? "✓" : index + 1}</span>
              <div><b>{item.label}</b><span>{item.detail}</span></div>
              <small>{state}</small>
            </li>
          );
        })}
      </ol>
    </article>
  );
}

function StatusBadge({ status }: { status: string }) {
  const positive = ["succeeded", "completed", "captured"].includes(status);
  return (
    <span className={`status-badge status-${status}`}>
      <span aria-hidden="true">{positive ? "✓" : status === "paused" ? "Ⅱ" : "●"}</span>
      {status.replaceAll("-", " ")}
    </span>
  );
}

function latestRefund(refunds: RefundView[]): RefundView | undefined {
  return refunds.at(-1);
}

function moneyOrFallback(money: TripFinancialView["capturedAmount"] | undefined, amount: number) {
  return money ? formatMoney(money) : formatAmount(amount, "EUR");
}

function shortId(value: string): string {
  return value.length > 13 ? `${value.slice(0, 8)}…${value.slice(-4)}` : value;
}

async function readJson<T>(response: Response): Promise<T> {
  if (!response.ok) {
    let problem: ProblemDetail = {};
    try {
      problem = (await response.json()) as ProblemDetail;
    } catch {
      // Some demo boundaries return an empty or plain-text response on failure.
    }
    throw new Error(problem.detail ?? problem.title ?? `Request failed with status ${response.status}.`);
  }
  return (await response.json()) as T;
}

function errorMessage(reason: unknown): string {
  return reason instanceof Error ? reason.message : "An unexpected error occurred.";
}
