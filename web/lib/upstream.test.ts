import { afterEach, describe, expect, it } from "vitest";
import { handleUpstream, providerUrl, refundApiUrl, workerUrl } from "./upstream";

describe("upstream boundaries", () => {
  afterEach(() => {
    delete process.env.REFUND_API_URL;
    delete process.env.WORKER_URL;
    delete process.env.PROVIDER_URL;
  });

  it("uses local service defaults and trims configured trailing slashes", () => {
    expect(refundApiUrl("/health")).toBe("http://localhost:8080/health");
    expect(workerUrl("/health")).toBe("http://localhost:8081/health");
    expect(providerUrl("/health")).toBe("http://localhost:8082/health");

    process.env.REFUND_API_URL = "http://refund-api:8080/";
    expect(refundApiUrl("/api/v1/trips/one")).toBe("http://refund-api:8080/api/v1/trips/one");
  });

  it("returns a bounded problem response when a fixture service is unreachable", async () => {
    const response = await handleUpstream(async () => {
      throw new TypeError("connection refused");
    });

    expect(response.status).toBe(502);
    await expect(response.json()).resolves.toMatchObject({
      title: "Fixture service unavailable",
      status: 502,
    });
  });
});
