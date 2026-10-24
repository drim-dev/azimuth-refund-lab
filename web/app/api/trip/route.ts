import { FIXTURE } from "@/lib/contracts";
import { handleUpstream, passThrough, refundApiUrl, upstreamRequest } from "@/lib/upstream";
import { realizes } from "@/lib/azimuth";

export const dynamic = "force-dynamic";

export async function GET(): Promise<Response> {
  realizes("refunds/execution", "interrupted-processing-eventually-completes");
  return handleUpstream(async () =>
    passThrough(await upstreamRequest(refundApiUrl(`/api/v1/trips/${FIXTURE.tripId}`))),
  );
}
