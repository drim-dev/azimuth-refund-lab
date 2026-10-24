package dev.drim.azimuth.refund.provider;

import static org.assertj.core.api.Assertions.assertThat;

import dev.drim.azimuth.Azimuth.CoversMechanism;
import dev.drim.azimuth.Azimuth.Oracle;
import dev.drim.azimuth.Azimuth.Quantification;
import dev.drim.azimuth.Azimuth.Scope;
import dev.drim.azimuth.refund.contracts.http.ProviderRefundRequest;
import dev.drim.azimuth.refund.contracts.http.ProviderRefundResponse;
import dev.drim.azimuth.refund.contracts.http.ProviderRefundSummary;
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
    private static final UUID REFUND_ID = UUID.fromString(
            "44444444-4444-4444-4444-444444444444");
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
    void clearProviderState() {
        jdbc.update("DELETE FROM provider_request_attempts");
        jdbc.update("DELETE FROM provider_financial_effects");
        jdbc.update("DELETE FROM provider_refund_ledger");
    }

    @Test
    @CoversMechanism(
            spec = "refunds/execution",
            mechanism = "provider-idempotency-contract",
            scope = Scope.component,
            quantification = Quantification.universal,
            oracle = Oracle.relational)
    void identicalKeyAndPayloadReturnOneStoredProviderOperation() throws Exception {
        String key = REFUND_ID.toString();
        ProviderRefundRequest request = request(REFUND_ID, PAYMENT_ID, "30.00", "EUR");

        HttpResponse<String> first = post(key, request);
        HttpResponse<String> repeated = post(key, request);

        assertThat(first.statusCode()).isEqualTo(200);
        assertThat(repeated.statusCode()).isEqualTo(200);
        assertThat(readResponse(repeated)).isEqualTo(readResponse(first));
        assertStoredRequest(key, request);
        assertThat(summary(REFUND_ID).operations()).containsExactly(readResponse(first));
        assertThat(summary(REFUND_ID).attempts()).hasSize(2);
        assertThat(summary(REFUND_ID).applicationCount()).isEqualTo(1);
    }

    @Test
    @CoversMechanism(
            spec = "refunds/execution",
            mechanism = "provider-idempotency-contract",
            scope = Scope.component,
            quantification = Quantification.universal,
            oracle = Oracle.relational)
    void concurrentIdenticalRequestsSettleOnOneProviderOperation() throws Exception {
        String key = REFUND_ID.toString();
        ProviderRefundRequest request = request(REFUND_ID, PAYMENT_ID, "30.00", "EUR");
        CountDownLatch start = new CountDownLatch(1);

        List<HttpResponse<String>> responses;
        try (ExecutorService callers = Executors.newFixedThreadPool(12)) {
            List<Future<HttpResponse<String>>> calls = IntStream.range(0, 12)
                    .mapToObj(ignored -> callers.submit(() -> {
                        start.await();
                        return postUnchecked(key, request);
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
        assertStoredRequest(key, request);
        assertThat(summary(REFUND_ID).applicationCount()).isEqualTo(1);
        assertThat(summary(REFUND_ID).attempts()).hasSize(12);
    }

    @ParameterizedTest
    @MethodSource("conflictingRequests")
    @CoversMechanism(
            spec = "refunds/execution",
            mechanism = "provider-idempotency-contract",
            scope = Scope.component,
            quantification = Quantification.universal,
            oracle = Oracle.relational)
    void changedPayloadUnderTheSameKeyIsAConflict(ProviderRefundRequest changed) throws Exception {
        String key = REFUND_ID.toString();
        ProviderRefundRequest original = request(REFUND_ID, PAYMENT_ID, "30.00", "EUR");
        assertThat(post(key, original).statusCode()).isEqualTo(200);

        HttpResponse<String> conflict = post(key, changed);

        assertThat(conflict.statusCode()).isEqualTo(409);
        assertThat(conflict.headers().firstValue("content-type").orElse(""))
                .startsWith("application/problem+json");
        assertThat(conflict.body()).contains("Provider refund conflict");
        assertStoredRequest(key, original);
        assertThat(summary(REFUND_ID).applicationCount()).isEqualTo(1);
    }

    @Test
    void differentKeysAreIndependentEvenForOneBusinessRefund() throws Exception {
        ProviderRefundRequest request = request(REFUND_ID, PAYMENT_ID, "30.00", "EUR");
        String firstKey = "delivery-1";
        String secondKey = "delivery-2";

        ProviderRefundResponse first = readResponse(post(firstKey, request));
        ProviderRefundResponse second = readResponse(post(secondKey, request));
        ProviderRefundSummary summary = summary(REFUND_ID);

        assertThat(first.providerOperationId()).isNotEqualTo(second.providerOperationId());
        assertThat(summary.operations()).extracting(ProviderRefundResponse::idempotencyKey)
                .containsExactly(firstKey, secondKey);
        assertThat(summary.attempts()).extracting(
                dev.drim.azimuth.refund.contracts.http.ProviderRefundAttempt::idempotencyKey)
                .containsExactly(firstKey, secondKey);
        assertThat(summary.applicationCount()).isEqualTo(2);
        assertThat(financialEffectCount(REFUND_ID)).isEqualTo(2);
    }

    @Test
    void missingBusinessRefundHasAnEmptyPublicHistory() throws Exception {
        ProviderRefundSummary summary = summary(UUID.randomUUID());

        assertThat(summary.operations()).isEmpty();
        assertThat(summary.attempts()).isEmpty();
        assertThat(summary.applicationCount()).isZero();
    }

    private static Stream<Arguments> conflictingRequests() {
        return Stream.of(
                Arguments.of(request(UUID.randomUUID(), PAYMENT_ID, "30.00", "EUR")),
                Arguments.of(request(REFUND_ID, UUID.randomUUID(), "30.00", "EUR")),
                Arguments.of(request(REFUND_ID, PAYMENT_ID, "30.01", "EUR")),
                Arguments.of(request(REFUND_ID, PAYMENT_ID, "30.00", "USD")));
    }

    private static ProviderRefundRequest request(
            UUID refundId,
            UUID paymentId,
            String amount,
            String currency
    ) {
        return new ProviderRefundRequest(refundId, paymentId, new BigDecimal(amount), currency);
    }

    private HttpResponse<String> post(String key, ProviderRefundRequest request)
            throws IOException, InterruptedException {
        HttpRequest httpRequest = HttpRequest.newBuilder(uri("/provider/v1/refunds"))
                .header("Content-Type", "application/json")
                .header("Idempotency-Key", key)
                .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(request)))
                .build();
        return http.send(httpRequest, HttpResponse.BodyHandlers.ofString());
    }

    private ProviderRefundSummary summary(UUID refundId) throws Exception {
        HttpResponse<String> response = http.send(
                HttpRequest.newBuilder(uri("/provider/v1/business-refunds/" + refundId)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(200);
        return json.readValue(response.body(), ProviderRefundSummary.class);
    }

    private HttpResponse<String> postUnchecked(String key, ProviderRefundRequest request) {
        try {
            return post(key, request);
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

    private void assertStoredRequest(String key, ProviderRefundRequest request) {
        StoredRequest stored = jdbc.queryForObject("""
                SELECT refund_id, payment_id, amount, currency
                FROM provider_refund_ledger
                WHERE idempotency_key = ?
                """, (resultSet, rowNumber) -> new StoredRequest(
                resultSet.getObject("refund_id", UUID.class),
                resultSet.getObject("payment_id", UUID.class),
                resultSet.getBigDecimal("amount"),
                resultSet.getString("currency")), key);

        assertThat(stored).isNotNull();
        assertThat(stored.refundId()).isEqualTo(request.refundId());
        assertThat(stored.paymentId()).isEqualTo(request.paymentId());
        assertThat(stored.amount()).isEqualByComparingTo(request.amount());
        assertThat(stored.currency()).isEqualTo(request.currency());
    }

    private int financialEffectCount(UUID refundId) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM provider_financial_effects WHERE refund_id = ?",
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

    private record StoredRequest(
            UUID refundId,
            UUID paymentId,
            BigDecimal amount,
            String currency
    ) {
    }
}
