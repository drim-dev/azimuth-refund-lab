import { afterEach, describe, expect, it, vi } from "vitest";
import { GET as getTrip } from "@/app/api/trip/route";
import { POST as approveRefund } from "@/app/api/refunds/route";
import { POST as controlFailpoint } from "@/app/api/demo/failpoint/[action]/route";
import { GET as getProviderLedger } from "@/app/api/demo/provider-ledger/[refundId]/route";

const refund = {
  refundId: "44444444-4444-4444-4444-444444444444",
  paymentId: "33333333-3333-3333-3333-333333333333",
  amount: 30,
  currency: "EUR",
  status: "pending",
  approvedAt: "2026-08-12T09:00:00Z",
};

const fetchMock = vi.fn<(input: RequestInfo | URL, init?: RequestInit) => Promise<Response>>();

afterEach(() => {
  vi.unstubAllGlobals();
  fetchMock.mockReset();
  delete process.env.REFUND_API_URL;
  delete process.env.WORKER_URL;
  delete process.env.PROVIDER_URL;
});

describe("Refund BFF routes", () => {
  it("loads the fixed seeded trip through the Refund API", async () => {
    fetchMock.mockResolvedValue(Response.json({ tripId: "fixture-trip" }));
    vi.stubGlobal("fetch", fetchMock);

    const response = await getTrip();

    expect(response.status).toBe(200);
    expect(fetchMock).toHaveBeenCalledWith(
      "http://localhost:8080/api/v1/trips/22222222-2222-2222-2222-222222222222",
      expect.objectContaining({ cache: "no-store" }),
    );
  });

  it("adds the authenticated support role and fixed correction amount", async () => {
    fetchMock.mockResolvedValue(Response.json(refund, { status: 202 }));
    vi.stubGlobal("fetch", fetchMock);

    const response = await approveRefund();

    expect(response.status).toBe(202);
    expect(fetchMock).toHaveBeenCalledWith(
      "http://localhost:8080/api/v1/payments/33333333-3333-3333-3333-333333333333/refunds",
      expect.objectContaining({
        method: "POST",
        body: JSON.stringify({ amount: 30, currency: "EUR" }),
        headers: expect.objectContaining({ "X-Fixture-Role": "support" }),
      }),
    );
  });

  it("exposes only the two bounded worker controls", async () => {
    fetchMock.mockResolvedValue(Response.json({ state: "armed" }, { status: 202 }));
    vi.stubGlobal("fetch", fetchMock);

    const armed = await controlFailpoint(new Request("http://web/api/demo/failpoint/arm"), {
      params: Promise.resolve({ action: "arm" }),
    });
    const rejected = await controlFailpoint(new Request("http://web/api/demo/failpoint/reset"), {
      params: Promise.resolve({ action: "reset" }),
    });

    expect(armed.status).toBe(202);
    expect(rejected.status).toBe(404);
    expect(fetchMock).toHaveBeenCalledTimes(1);
    expect(fetchMock).toHaveBeenCalledWith(
      "http://localhost:8081/demo/failpoints/after-provider-response/arm",
      expect.objectContaining({ method: "POST", cache: "no-store" }),
    );
  });

  it("combines the provider operation and application count", async () => {
    fetchMock
      .mockResolvedValueOnce(
        Response.json({
          refundId: refund.refundId,
          providerOperationId: "66666666-6666-6666-6666-666666666666",
          outcome: "succeeded",
          appliedAt: "2026-08-12T09:00:01Z",
        }),
      )
      .mockResolvedValueOnce(Response.json({ refundId: refund.refundId, count: 1 }));
    vi.stubGlobal("fetch", fetchMock);

    const response = await getProviderLedger(
      new Request(`http://web/api/demo/provider-ledger/${refund.refundId}`),
      { params: Promise.resolve({ refundId: refund.refundId }) },
    );

    expect(response.status).toBe(200);
    await expect(response.json()).resolves.toMatchObject({
      refund: { outcome: "succeeded" },
      applicationCount: 1,
    });
    expect(fetchMock).toHaveBeenCalledTimes(2);
  });

  it("preserves an upstream problem status", async () => {
    fetchMock.mockResolvedValue(
      Response.json({ title: "Refund capacity exceeded" }, { status: 409 }),
    );
    vi.stubGlobal("fetch", fetchMock);

    const response = await approveRefund();

    expect(response.status).toBe(409);
    await expect(response.json()).resolves.toMatchObject({ title: "Refund capacity exceeded" });
  });
});
