import type { ProviderApplicationCount, ProviderRefundView } from "@/lib/contracts";
import { handleUpstream, passThrough, providerUrl, upstreamRequest } from "@/lib/upstream";

interface RouteContext {
  params: Promise<{ refundId: string }>;
}

export async function GET(_request: Request, context: RouteContext): Promise<Response> {
  const { refundId } = await context.params;
  return handleUpstream(async () => {
    const path = `/provider/v1/refunds/${encodeURIComponent(refundId)}`;
    const [refundResponse, applicationsResponse] = await Promise.all([
      upstreamRequest(providerUrl(path)),
      upstreamRequest(providerUrl(`${path}/applications`)),
    ]);

    if (!refundResponse.ok) {
      return passThrough(refundResponse);
    }
    if (!applicationsResponse.ok) {
      return passThrough(applicationsResponse);
    }

    const refund = (await refundResponse.json()) as ProviderRefundView;
    const applications = (await applicationsResponse.json()) as ProviderApplicationCount;
    return Response.json(
      { refund, applicationCount: applications.count },
      { headers: { "cache-control": "no-store" } },
    );
  });
}
