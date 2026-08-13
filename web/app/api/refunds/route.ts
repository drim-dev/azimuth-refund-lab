import { FIXTURE } from "@/lib/contracts";
import { handleUpstream, passThrough, refundApiUrl, upstreamRequest } from "@/lib/upstream";

export async function POST(): Promise<Response> {
  return handleUpstream(async () => {
    const response = await upstreamRequest(
      refundApiUrl(`/api/v1/payments/${FIXTURE.paymentId}/refunds`),
      {
        method: "POST",
        headers: {
          "content-type": "application/json",
          "X-Fixture-Role": "support",
        },
        body: JSON.stringify({ amount: FIXTURE.refundAmount, currency: FIXTURE.currency }),
      },
    );
    return passThrough(response);
  });
}
