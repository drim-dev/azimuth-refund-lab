import { FIXTURE } from "@/lib/contracts";
import { handleUpstream, passThrough, refundApiUrl, upstreamRequest } from "@/lib/upstream";

export const dynamic = "force-dynamic";

export async function GET(): Promise<Response> {
  return handleUpstream(async () =>
    passThrough(await upstreamRequest(refundApiUrl(`/api/v1/trips/${FIXTURE.tripId}`))),
  );
}
