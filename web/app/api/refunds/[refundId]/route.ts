import { handleUpstream, passThrough, refundApiUrl, upstreamRequest } from "@/lib/upstream";

interface RouteContext {
  params: Promise<{ refundId: string }>;
}

export async function GET(_request: Request, context: RouteContext): Promise<Response> {
  const { refundId } = await context.params;
  return handleUpstream(async () =>
    passThrough(await upstreamRequest(refundApiUrl(`/api/v1/refunds/${encodeURIComponent(refundId)}`))),
  );
}
