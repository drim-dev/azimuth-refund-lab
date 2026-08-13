package dev.drim.azimuth.refund.provider;

import static org.assertj.core.api.Assertions.assertThat;

import dev.drim.azimuth.refund.contracts.http.ProviderRefundRequest;
import dev.drim.azimuth.refund.contracts.http.ProviderRefundResponse;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.ObjectMapper;

@Testcontainers
@ActiveProfiles("demo")
@TestPropertySource(properties = "provider.demo.reapply-identical-requests=true")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProviderDuplicateApplicationComponentTest {
    @Container
    private static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer("postgres:17-alpine");

    private final HttpClient http = HttpClient.newHttpClient();

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper json;

    @Autowired
    private JdbcTemplate jdbc;

    @DynamicPropertySource
    static void configurePostgres(DynamicPropertyRegistry properties) {
        properties.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        properties.add("spring.datasource.username", POSTGRES::getUsername);
        properties.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @BeforeEach
    void clearProviderState() {
        jdbc.update("DELETE FROM provider_financial_effects");
        jdbc.update("DELETE FROM provider_refund_ledger");
    }

    @Test
    void identicalRepeatAppliesTheFinancialEffectAgainButKeepsOneIdempotencyRecord()
            throws Exception {
        UUID refundId = UUID.randomUUID();
        ProviderRefundRequest request = new ProviderRefundRequest(
                UUID.randomUUID(), new BigDecimal("30.00"), "EUR");

        HttpResponse<String> first = put(refundId, request);
        HttpResponse<String> repeated = put(refundId, request);

        assertThat(first.statusCode()).isEqualTo(200);
        assertThat(repeated.statusCode()).isEqualTo(200);
        assertThat(readResponse(repeated)).isEqualTo(readResponse(first));
        assertThat(count("provider_refund_ledger", refundId)).isEqualTo(1);
        assertThat(count("provider_financial_effects", refundId)).isEqualTo(2);

        HttpResponse<String> applications = get(
                "/provider/v1/refunds/" + refundId + "/applications");
        assertThat(applications.statusCode()).isEqualTo(200);
        assertThat(json.readTree(applications.body()).path("count").asInt()).isEqualTo(2);
    }

    private HttpResponse<String> put(UUID refundId, ProviderRefundRequest request)
            throws Exception {
        return http.send(
                HttpRequest.newBuilder(uri("/provider/v1/refunds/" + refundId))
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(request)))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> get(String path) throws Exception {
        return http.send(
                HttpRequest.newBuilder(uri(path)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private ProviderRefundResponse readResponse(HttpResponse<String> response) throws Exception {
        return json.readValue(response.body(), ProviderRefundResponse.class);
    }

    private int count(String table, UUID refundId) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM " + table + " WHERE refund_id = ?",
                Integer.class,
                refundId);
        return count == null ? 0 : count;
    }

    private URI uri(String path) {
        return URI.create("http://127.0.0.1:" + port + path);
    }
}
