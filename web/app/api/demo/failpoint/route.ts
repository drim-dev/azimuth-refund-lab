import { handleUpstream, passThrough, upstreamRequest, workerUrl } from "@/lib/upstream";

const FAILPOINT_PATH = "/demo/failpoints/after-provider-response";

export const dynamic = "force-dynamic";

export async function GET(): Promise<Response> {
  return handleUpstream(async () =>
    passThrough(await upstreamRequest(workerUrl(FAILPOINT_PATH))),
  );
}
