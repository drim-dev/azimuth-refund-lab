import { handleUpstream, passThrough, upstreamRequest, workerUrl } from "@/lib/upstream";

interface RouteContext {
  params: Promise<{ action: string }>;
}

const actions = new Set(["arm", "release"]);

export async function POST(_request: Request, context: RouteContext): Promise<Response> {
  const { action } = await context.params;
  if (!actions.has(action)) {
    return Response.json({ title: "Unknown failpoint action", status: 404 }, { status: 404 });
  }

  return handleUpstream(async () =>
    passThrough(
      await upstreamRequest(
        workerUrl(`/demo/failpoints/after-provider-response/${encodeURIComponent(action)}`),
        { method: "POST" },
      ),
    ),
  );
}
