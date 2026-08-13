const DEFAULT_REFUND_API_URL = "http://localhost:8080";
const DEFAULT_WORKER_URL = "http://localhost:8081";
const DEFAULT_PROVIDER_URL = "http://localhost:8082";

export function refundApiUrl(path: string): string {
  return joinUrl(process.env.REFUND_API_URL ?? DEFAULT_REFUND_API_URL, path);
}

export function workerUrl(path: string): string {
  return joinUrl(process.env.WORKER_URL ?? DEFAULT_WORKER_URL, path);
}

export function providerUrl(path: string): string {
  return joinUrl(process.env.PROVIDER_URL ?? DEFAULT_PROVIDER_URL, path);
}

export function upstreamRequest(url: string, init?: RequestInit): Promise<Response> {
  return fetch(url, {
    ...init,
    cache: "no-store",
    headers: {
      accept: "application/json",
      ...init?.headers,
    },
  });
}

export async function passThrough(response: Response): Promise<Response> {
  const contentType = response.headers.get("content-type") ?? "application/json";
  return new Response(await response.arrayBuffer(), {
    status: response.status,
    headers: {
      "cache-control": "no-store",
      "content-type": contentType,
    },
  });
}

export async function handleUpstream(request: () => Promise<Response>): Promise<Response> {
  try {
    return await request();
  } catch {
    return Response.json(
      {
        type: "about:blank",
        title: "Fixture service unavailable",
        status: 502,
        detail: "The demo could not reach one of its local services.",
      },
      { status: 502 },
    );
  }
}

function joinUrl(baseUrl: string, path: string): string {
  return `${baseUrl.replace(/\/$/, "")}/${path.replace(/^\//, "")}`;
}
