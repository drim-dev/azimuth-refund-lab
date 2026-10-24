import type { ProviderLedgerView } from "@/lib/contracts";
import { handleUpstream, passThrough, providerUrl, upstreamRequest } from "@/lib/upstream";

interface RouteContext {
  params: Promise<{ refundId: string }>;
}

export async function GET(_request: Request, context: RouteContext): Promise<Response> {
  const { refundId } = await context.params;
  return handleUpstream(async () => {
    const response = await upstreamRequest(
      providerUrl(`/provider/v1/business-refunds/${encodeURIComponent(refundId)}`),
    );
    if (!response.ok) {
      return passThrough(response);
    }

    const history = (await response.json()) as ProviderLedgerView;
    return Response.json(history, { headers: { "cache-control": "no-store" } });
  });
}
