package dev.drim.azimuth.refund.contracts;

import static org.assertj.core.api.Assertions.assertThat;

import dev.drim.azimuth.refund.contracts.event.RefundEventNames;
import dev.drim.azimuth.refund.contracts.event.RefundRequestedV1;
import dev.drim.azimuth.refund.contracts.event.RefundResultV1;
import dev.drim.azimuth.refund.contracts.http.ApproveRefundRequest;
import dev.drim.azimuth.refund.contracts.http.ProviderRefundRequest;
import dev.drim.azimuth.refund.contracts.http.ProviderRefundAttempt;
import dev.drim.azimuth.refund.contracts.http.ProviderRefundResponse;
import dev.drim.azimuth.refund.contracts.http.ProviderRefundSummary;
import dev.drim.azimuth.refund.contracts.http.RefundView;
import dev.drim.azimuth.refund.contracts.http.TripFinancialView;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.yaml.YAMLFactory;

class WireCompatibilityTest {
    private static final ObjectMapper JSON = JsonMapper.builder().build();
    private static final ObjectMapper YAML = new ObjectMapper(new YAMLFactory());

    @Test
    void eventRecordsRoundTripTheirGoldenWireForms() throws IOException {
        RefundRequestedV1 requested = assertGoldenRoundTrip(
                "/contracts/events/refund-requested-v1.json", RefundRequestedV1.class);
        RefundResultV1 result = assertGoldenRoundTrip(
                "/contracts/events/refund-result-v1.json", RefundResultV1.class);

        assertThat(requested.eventType()).isEqualTo(RefundEventNames.REFUND_REQUESTED_V1);
        assertThat(requested.eventVersion()).isEqualTo(RefundEventNames.VERSION_1);
        assertThat(result.eventType()).isEqualTo(RefundEventNames.REFUND_RESULT_V1);
        assertThat(result.eventVersion()).isEqualTo(RefundEventNames.VERSION_1);
    }

    @Test
    void httpRecordsRoundTripTheirGoldenWireForms() throws IOException {
        assertGoldenRoundTrip(
                "/contracts/http/approve-refund-request.json", ApproveRefundRequest.class);
        assertGoldenRoundTrip("/contracts/http/refund-view.json", RefundView.class);
        assertGoldenRoundTrip(
                "/contracts/http/trip-financial-view.json", TripFinancialView.class);
        assertGoldenRoundTrip(
                "/contracts/http/provider-refund-request.json", ProviderRefundRequest.class);
        assertGoldenRoundTrip(
                "/contracts/http/provider-refund-attempt.json", ProviderRefundAttempt.class);
        assertGoldenRoundTrip(
                "/contracts/http/provider-refund-response.json", ProviderRefundResponse.class);
        assertGoldenRoundTrip(
                "/contracts/http/provider-refund-summary.json", ProviderRefundSummary.class);
    }

    @Test
    void openApiExamplesEqualTheGoldenHttpBodies() throws IOException {
        JsonNode refundApi = readYaml("/openapi/refund-api-v1.yaml");
        assertSchemaExample(
                refundApi,
                "ApproveRefundRequest",
                "/contracts/http/approve-refund-request.json");
        assertSchemaExample(refundApi, "RefundView", "/contracts/http/refund-view.json");
        assertSchemaExample(
                refundApi,
                "TripFinancialView",
                "/contracts/http/trip-financial-view.json");

        JsonNode providerApi = readYaml("/openapi/provider-api-v1.yaml");
        assertSchemaExample(
                providerApi,
                "ProviderRefundRequest",
                "/contracts/http/provider-refund-request.json");
        assertSchemaExample(
                providerApi,
                "ProviderRefundAttempt",
                "/contracts/http/provider-refund-attempt.json");
        assertSchemaExample(
                providerApi,
                "ProviderRefundResponse",
                "/contracts/http/provider-refund-response.json");
        assertSchemaExample(
                providerApi,
                "ProviderRefundSummary",
                "/contracts/http/provider-refund-summary.json");
    }

    @Test
    void asyncApiExamplesEqualTheGoldenEventBodies() throws IOException {
        JsonNode events = readYaml("/asyncapi/refund-events-v1.yaml");
        assertSchemaExample(
                events,
                "RefundRequestedV1",
                "/contracts/events/refund-requested-v1.json");
        assertSchemaExample(
                events,
                "RefundResultV1",
                "/contracts/events/refund-result-v1.json");
    }

    private static <T> T assertGoldenRoundTrip(String resource, Class<T> recordType)
            throws IOException {
        JsonNode golden = readJson(resource);
        T value = JSON.treeToValue(golden, recordType);
        JsonNode serialized = JSON.valueToTree(value);
        assertThat(serialized.toString()).isEqualTo(golden.toString());
        return value;
    }

    private static void assertSchemaExample(
            JsonNode document,
            String schemaName,
            String goldenResource
    ) throws IOException {
        JsonNode example = document.path("components")
                .path("schemas")
                .path(schemaName)
                .path("example");
        assertThat(example.isMissingNode()).isFalse();
        assertThat(example).isEqualTo(readJson(goldenResource));
    }

    private static JsonNode readJson(String resource) throws IOException {
        try (InputStream input = requiredResource(resource)) {
            return JSON.readTree(input);
        }
    }

    private static JsonNode readYaml(String resource) throws IOException {
        try (InputStream input = requiredResource(resource)) {
            return YAML.readTree(input);
        }
    }

    private static InputStream requiredResource(String resource) {
        InputStream input = WireCompatibilityTest.class.getResourceAsStream(resource);
        if (input == null) {
            throw new IllegalArgumentException("Missing classpath resource: " + resource);
        }
        return input;
    }
}
