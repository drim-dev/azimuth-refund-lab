package dev.drim.azimuth.refund.provider;

import static org.assertj.core.api.Assertions.assertThat;

import dev.drim.azimuth.Azimuth.Covers;
import dev.drim.azimuth.Azimuth.CoversMechanism;
import dev.drim.azimuth.Azimuth.Oracle;
import dev.drim.azimuth.Azimuth.Quantification;
import dev.drim.azimuth.Azimuth.Scope;
import dev.drim.azimuth.refund.contracts.http.ProviderRefundRequest;
import dev.drim.azimuth.refund.contracts.http.ProviderRefundResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.ObjectMapper;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProviderRefundComponentTest {
    private static final UUID PAYMENT_ID = UUID.fromString(
            "33333333-3333-3333-3333-333333333333");

    @Container
    private static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer("postgres:17-alpine");

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

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
    void clearLedger() {
        jdbc.update("DELETE FROM provider_financial_effects");
        jdbc.update("DELETE FROM provider_refund_ledger");
    }

    @Test
    @Tag("mutation")
    @Covers(
            spec = "refunds/provider-effects",
            scenario = "redelivery-after-provider-success",
            scope = Scope.component,
            quantification = Quantification.universal,
            oracle = Oracle.relational)
    @CoversMechanism(
            spec = "refunds/provider-effects",
            mechanism = "provider-refund-ledger",
            scope = Scope.component,
            quantification = Quantification.universal,
            oracle = Oracle.relational)
    void identicalRepeatReturnsTheStoredResponseAndOneLedgerOperation() throws Exception {
        UUID refundId = UUID.randomUUID();
        ProviderRefundRequest request = request(PAYMENT_ID, "30.00", "EUR");

        HttpResponse<String> first = put(refundId, request);
        HttpResponse<String> repeated = put(refundId, request);

        assertThat(first.statusCode()).isEqualTo(200);
        assertThat(repeated.statusCode()).isEqualTo(200);
        assertThat(readResponse(repeated)).isEqualTo(readResponse(first));
        assertLedgerMatches(refundId, request);
        assertThat(applicationCount(refundId)).isEqualTo(1);
        assertThat(idempotencyRecordCount(refundId)).isEqualTo(1);
    }

    @Test
    void storedRefundAndItsApplicationCountAreAvailableOverHttp() throws Exception {
        UUID refundId = UUID.randomUUID();
        ProviderRefundRequest request = request(PAYMENT_ID, "30.00", "EUR");
        ProviderRefundResponse applied = readResponse(put(refundId, request));
        assertThat(put(refundId, request).statusCode()).isEqualTo(200);

        HttpResponse<String> stored = get("/provider/v1/refunds/" + refundId);
        HttpResponse<String> applications = get(
                "/provider/v1/refunds/" + refundId + "/applications");

        assertThat(stored.statusCode()).isEqualTo(200);
        assertThat(readResponse(stored)).isEqualTo(applied);
        assertThat(applications.statusCode()).isEqualTo(200);
        assertThat(json.readTree(applications.body()).path("refundId").asString())
                .isEqualTo(refundId.toString());
        assertThat(json.readTree(applications.body()).path("count").asInt()).isEqualTo(1);
        assertThat(applicationCount(refundId)).isEqualTo(1);
        assertThat(idempotencyRecordCount(refundId)).isEqualTo(1);
    }

    @Test
    void missingRefundReturnsNotFoundAndZeroApplications() throws Exception {
        UUID refundId = UUID.randomUUID();

        HttpResponse<String> missing = get("/provider/v1/refunds/" + refundId);
        HttpResponse<String> applications = get(
                "/provider/v1/refunds/" + refundId + "/applications");

        assertThat(missing.statusCode()).isEqualTo(404);
        assertThat(missing.headers().firstValue("content-type").orElse(""))
                .startsWith("application/problem+json");
        assertThat(missing.body()).contains("Provider refund not found");
        assertThat(applications.statusCode()).isEqualTo(200);
        assertThat(json.readTree(applications.body()).path("refundId").asString())
                .isEqualTo(refundId.toString());
        assertThat(json.readTree(applications.body()).path("count").asInt()).isZero();
    }

    @Test
    @Tag("mutation")
    @Covers(
            spec = "refunds/provider-effects",
            scenario = "redelivery-after-provider-success",
            scope = Scope.component,
            quantification = Quantification.universal,
            oracle = Oracle.relational)
    @CoversMechanism(
            spec = "refunds/provider-effects",
            mechanism = "provider-refund-ledger",
            scope = Scope.component,
            quantification = Quantification.universal,
            oracle = Oracle.relational)
    void concurrentIdenticalRepeatsReturnOneStoredOperation() throws Exception {
        UUID refundId = UUID.randomUUID();
        ProviderRefundRequest request = request(PAYMENT_ID, "30.00", "EUR");
        CountDownLatch start = new CountDownLatch(1);

        List<HttpResponse<String>> responses;
        try (ExecutorService callers = Executors.newFixedThreadPool(12)) {
            List<Future<HttpResponse<String>>> calls = IntStream.range(0, 12)
                    .mapToObj(ignored -> callers.submit(() -> {
                        start.await();
                        return putUnchecked(refundId, request);
                    }))
                    .toList();
            start.countDown();
            responses = calls.stream().map(ProviderRefundComponentTest::await).toList();
        }

        assertThat(responses).allSatisfy(response -> assertThat(response.statusCode()).isEqualTo(200));
        List<ProviderRefundResponse> results = responses.stream()
                .map(this::readResponseUnchecked)
                .toList();
        assertThat(results).containsOnly(results.getFirst());
        assertLedgerMatches(refundId, request);
        assertThat(applicationCount(refundId)).isEqualTo(1);
        assertThat(idempotencyRecordCount(refundId)).isEqualTo(1);
    }

    @ParameterizedTest
    @MethodSource("conflictingRequests")
    @Tag("mutation")
    @Covers(
            spec = "refunds/provider-effects",
            scenario = "redelivery-after-provider-success",
            scope = Scope.component,
            quantification = Quantification.universal,
            oracle = Oracle.relational)
    @CoversMechanism(
            spec = "refunds/provider-effects",
            mechanism = "provider-refund-ledger",
            scope = Scope.component,
            quantification = Quantification.universal,
            oracle = Oracle.relational)
    void changedFinancialFieldIsAConflict(ProviderRefundRequest changedRequest) throws Exception {
        UUID refundId = UUID.randomUUID();
        ProviderRefundRequest original = request(PAYMENT_ID, "30.00", "EUR");
        assertThat(put(refundId, original).statusCode()).isEqualTo(200);

        HttpResponse<String> conflict = put(refundId, changedRequest);

        assertThat(conflict.statusCode()).isEqualTo(409);
        assertThat(conflict.headers().firstValue("content-type").orElse(""))
                .startsWith("application/problem+json");
        assertThat(conflict.body()).contains("Provider refund conflict");
        assertLedgerMatches(refundId, original);
        assertThat(applicationCount(refundId)).isEqualTo(1);
        assertThat(idempotencyRecordCount(refundId)).isEqualTo(1);
    }

    @Test
    @Tag("mutation")
    @Covers(
            spec = "refunds/provider-effects",
            scenario = "redelivery-after-provider-success",
            scope = Scope.component,
            quantification = Quantification.universal,
            oracle = Oracle.relational)
    @CoversMechanism(
            spec = "refunds/provider-effects",
            mechanism = "provider-refund-ledger",
            scope = Scope.component,
            quantification = Quantification.universal,
            oracle = Oracle.relational)
    void competingPayloadsSettleOnOneRefundIdentity() throws Exception {
        UUID refundId = UUID.randomUUID();
        ProviderRefundRequest first = request(PAYMENT_ID, "30.00", "EUR");
        ProviderRefundRequest second = request(UUID.randomUUID(), "31.00", "USD");
        CountDownLatch start = new CountDownLatch(1);

        List<HttpResponse<String>> responses;
        try (ExecutorService callers = Executors.newFixedThreadPool(2)) {
            List<Future<HttpResponse<String>>> calls = List.of(
                    callers.submit(() -> {
                        start.await();
                        return putUnchecked(refundId, first);
                    }),
                    callers.submit(() -> {
                        start.await();
                        return putUnchecked(refundId, second);
                    }));
            start.countDown();
            responses = calls.stream().map(ProviderRefundComponentTest::await).toList();
        }

        assertThat(responses).extracting(HttpResponse::statusCode).containsExactlyInAnyOrder(200, 409);
        ProviderRefundRequest winner = responses.get(0).statusCode() == 200 ? first : second;
        assertLedgerMatches(refundId, winner);
        assertThat(applicationCount(refundId)).isEqualTo(1);
        assertThat(idempotencyRecordCount(refundId)).isEqualTo(1);
    }

    @Test
    void healthIsAvailableOverHttp() throws Exception {
        HttpRequest request = HttpRequest.newBuilder(uri("/actuator/health")).GET().build();

        HttpResponse<String> response = http.send(
                request,
                HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"status\":\"UP\"");
    }

    private static Stream<Arguments> conflictingRequests() {
        return Stream.of(
                Arguments.of(request(UUID.randomUUID(), "30.00", "EUR")),
                Arguments.of(request(PAYMENT_ID, "30.01", "EUR")),
                Arguments.of(request(PAYMENT_ID, "30.00", "USD")));
    }

    private static ProviderRefundRequest request(UUID paymentId, String amount, String currency) {
        return new ProviderRefundRequest(paymentId, new BigDecimal(amount), currency);
    }

    private HttpResponse<String> put(UUID refundId, ProviderRefundRequest request)
            throws IOException, InterruptedException {
        HttpRequest httpRequest = HttpRequest.newBuilder(
                        uri("/provider/v1/refunds/" + refundId))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(request)))
                .build();
        return http.send(httpRequest, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> get(String path) throws IOException, InterruptedException {
        return http.send(
                HttpRequest.newBuilder(uri(path)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> putUnchecked(UUID refundId, ProviderRefundRequest request) {
        try {
            return put(refundId, request);
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception);
        }
    }

    private ProviderRefundResponse readResponse(HttpResponse<String> response) throws IOException {
        return json.readValue(response.body(), ProviderRefundResponse.class);
    }

    private ProviderRefundResponse readResponseUnchecked(HttpResponse<String> response) {
        try {
            return readResponse(response);
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private void assertLedgerMatches(UUID refundId, ProviderRefundRequest request) {
        LedgerOracle row = jdbc.queryForObject("""
                SELECT payment_id, amount, currency
                FROM provider_refund_ledger
                WHERE refund_id = ?
                """, (resultSet, rowNumber) -> new LedgerOracle(
                resultSet.getObject("payment_id", UUID.class),
                resultSet.getBigDecimal("amount"),
                resultSet.getString("currency")), refundId);

        assertThat(row).isNotNull();
        assertThat(row.paymentId()).isEqualTo(request.paymentId());
        assertThat(row.amount()).isEqualByComparingTo(request.amount());
        assertThat(row.currency()).isEqualTo(request.currency());
    }

    private int applicationCount(UUID refundId) {
        return countRows("provider_financial_effects", refundId);
    }

    private int idempotencyRecordCount(UUID refundId) {
        return countRows("provider_refund_ledger", refundId);
    }

    private int countRows(String table, UUID refundId) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM " + table + " WHERE refund_id = ?",
                Integer.class,
                refundId);
        return count == null ? 0 : count;
    }

    private URI uri(String path) {
        return URI.create("http://127.0.0.1:" + port + path);
    }

    private static <T> T await(Future<T> future) {
        try {
            return future.get();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception);
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    private record LedgerOracle(UUID paymentId, BigDecimal amount, String currency) {
    }
}
