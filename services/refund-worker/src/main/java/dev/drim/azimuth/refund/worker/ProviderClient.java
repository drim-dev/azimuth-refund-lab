package dev.drim.azimuth.refund.worker;

import dev.drim.azimuth.Azimuth.ImplementsMechanism;
import dev.drim.azimuth.Azimuth.Realizes;
import dev.drim.azimuth.refund.contracts.event.RefundRequestedV1;
import dev.drim.azimuth.refund.contracts.http.ProviderRefundRequest;
import dev.drim.azimuth.refund.contracts.http.ProviderRefundResponse;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
class ProviderClient {
    private final URI providerBaseUri;
    private final Duration requestTimeout;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    ProviderClient(
            @Value("${refund.provider.base-url}") URI providerBaseUri,
            @Value("${refund.provider.request-timeout}") Duration requestTimeout,
            ObjectMapper objectMapper
    ) {
        this.providerBaseUri = providerBaseUri;
        this.requestTimeout = requestTimeout;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(requestTimeout)
                .build();
    }

    @Realizes(spec = "refunds/provider-effects", scenario = "redelivery-after-provider-success")
    @ImplementsMechanism(spec = "refunds/provider-effects", mechanism = "stable-refund-identity")
    ProviderRefundResponse apply(RefundRequestedV1 requested) {
        ProviderRefundRequest providerRequest = new ProviderRefundRequest(
                requested.paymentId(), requested.amount(), requested.currency());
        URI target = providerBaseUri.resolve("/provider/v1/refunds/" + requested.refundId());

        try {
            HttpRequest request = HttpRequest.newBuilder(target)
                    .timeout(requestTimeout)
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofByteArray(
                            objectMapper.writeValueAsBytes(providerRequest)))
                    .build();
            HttpResponse<byte[]> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200) {
                throw new ProviderRequestFailedException(response.statusCode());
            }
            return objectMapper.readValue(response.body(), ProviderRefundResponse.class);
        } catch (IOException exception) {
            throw new ProviderRequestFailedException(exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ProviderRequestFailedException(exception);
        }
    }

    static final class ProviderRequestFailedException extends RuntimeException {
        ProviderRequestFailedException(int statusCode) {
            super("Provider refund request returned HTTP " + statusCode);
        }

        ProviderRequestFailedException(Exception cause) {
            super("Provider refund request did not complete", cause);
        }
    }
}
