package dev.drim.azimuth.refund.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import dev.drim.azimuth.refund.contracts.event.RefundEventNames;
import dev.drim.azimuth.refund.contracts.event.RefundMessaging;
import dev.drim.azimuth.refund.contracts.event.RefundOutcome;
import dev.drim.azimuth.refund.contracts.event.RefundRequestedV1;
import dev.drim.azimuth.refund.contracts.event.RefundResultV1;
import dev.drim.azimuth.refund.contracts.http.RefundStatus;
import dev.drim.azimuth.refund.contracts.http.RefundView;
import dev.drim.azimuth.refund.contracts.http.TripFinancialView;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.ObjectMapper;

@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "refund.outbox.relay-delay=25ms",
        "refund.outbox.confirm-timeout=5s"
})
class RefundApiComponentTest {
    private static final UUID PAYMENT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID TRIP_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Container
    static final GenericContainer<?> POSTGRES = new GenericContainer<>(DockerImageName.parse("postgres:18-alpine"))
            .withExposedPorts(5432)
            .withEnv("POSTGRES_DB", "refunds")
            .withEnv("POSTGRES_USER", "refunds")
            .withEnv("POSTGRES_PASSWORD", "refunds");

    @Container
    static final GenericContainer<?> RABBIT = new GenericContainer<>(DockerImageName.parse("rabbitmq:4.2-alpine"))
            .withExposedPorts(5672)
            .withEnv("RABBITMQ_DEFAULT_USER", "refunds")
            .withEnv("RABBITMQ_DEFAULT_PASS", "refunds");

    @DynamicPropertySource
    static void dependencyProperties(DynamicPropertyRegistry properties) {
        properties.add("spring.datasource.url", () -> "jdbc:postgresql://" + POSTGRES.getHost() + ":"
                + POSTGRES.getMappedPort(5432) + "/refunds");
        properties.add("spring.datasource.username", () -> "refunds");
        properties.add("spring.datasource.password", () -> "refunds");
        properties.add("spring.rabbitmq.host", RABBIT::getHost);
        properties.add("spring.rabbitmq.port", () -> RABBIT.getMappedPort(5672));
        properties.add("spring.rabbitmq.username", () -> "refunds");
        properties.add("spring.rabbitmq.password", () -> "refunds");
    }

    @LocalServerPort
    int port;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    RabbitTemplate rabbit;

    @Autowired
    RabbitAdmin rabbitAdmin;

    @Autowired
    ObjectMapper objectMapper;

    private final HttpClient http = HttpClient.newHttpClient();

    @BeforeEach
    void resetFixture() {
        rabbitAdmin.purgeQueue(RefundMessaging.WORKER_QUEUE, false);
        rabbitAdmin.purgeQueue(RefundMessaging.API_RESULT_QUEUE, false);
        jdbc.update("DELETE FROM refund_result_inbox");
        jdbc.update("DELETE FROM refund_request_outbox");
        jdbc.update("DELETE FROM refunds");
        jdbc.update("UPDATE payments SET refundable_remaining = 120.00 WHERE payment_id = ?", PAYMENT_ID);
    }

    @Test
    void approvalReservesCapacityAndPublishesTheDurableRequest() throws Exception {
        HttpResponse<String> approval = approve("30.00", true);

        assertThat(approval.statusCode()).isEqualTo(202);
        RefundView refund = objectMapper.readValue(approval.body(), RefundView.class);
        assertThat(refund.status()).isEqualTo(RefundStatus.PENDING);
        assertThat(refund.amount()).isEqualByComparingTo("30.00");

        Message published = rabbit.receive(RefundMessaging.WORKER_QUEUE, 5_000);
        assertThat(published).isNotNull();
        RefundRequestedV1 request = objectMapper.readValue(published.getBody(), RefundRequestedV1.class);
        assertThat(request.refundId()).isEqualTo(refund.refundId());
        assertThat(request.paymentId()).isEqualTo(PAYMENT_ID);
        assertThat(request.approvedBy()).isEqualTo("support-fixture");

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> assertThat(jdbc.queryForObject(
                "SELECT published_at IS NOT NULL FROM refund_request_outbox WHERE refund_id = ?",
                Boolean.class,
                refund.refundId()
        )).isTrue());
        assertThat(jdbc.queryForObject(
                "SELECT refundable_remaining FROM payments WHERE payment_id = ?",
                BigDecimal.class,
                PAYMENT_ID
        )).isEqualByComparingTo("90.00");

        HttpResponse<String> tripResponse = get("/api/v1/trips/" + TRIP_ID);
        assertThat(tripResponse.statusCode()).isEqualTo(200);
        TripFinancialView trip = objectMapper.readValue(tripResponse.body(), TripFinancialView.class);
        assertThat(trip.originalFare().amount()).isEqualByComparingTo("90.00");
        assertThat(trip.waitTimeFee().amount()).isEqualByComparingTo("30.00");
        assertThat(trip.approvedRefundTotal().amount()).isEqualByComparingTo("30.00");
        assertThat(trip.finalCost().amount()).isEqualByComparingTo("90.00");
    }

    @Test
    void rejectedApprovalDoesNotReserveCapacityOrCreateAnOutboxEntry() throws Exception {
        HttpResponse<String> unauthenticated = approve("30.00", false);
        HttpResponse<String> overCapacity = approve("121.00", true);

        assertThat(unauthenticated.statusCode()).isEqualTo(401);
        assertThat(overCapacity.statusCode()).isEqualTo(409);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM refunds", Long.class)).isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM refund_request_outbox", Long.class)).isZero();
        assertThat(jdbc.queryForObject(
                "SELECT refundable_remaining FROM payments WHERE payment_id = ?",
                BigDecimal.class,
                PAYMENT_ID
        )).isEqualByComparingTo("120.00");
    }

    @Test
    void resultRedeliveryAdvancesTheProjectionOnce() throws Exception {
        RefundView pending = objectMapper.readValue(approve("30.00", true).body(), RefundView.class);
        assertThat(rabbit.receive(RefundMessaging.WORKER_QUEUE, 5_000)).isNotNull();

        UUID resultEventId = UUID.randomUUID();
        UUID providerOperationId = UUID.randomUUID();
        Instant appliedAt = Instant.now();
        RefundResultV1 result = new RefundResultV1(
                resultEventId,
                RefundEventNames.REFUND_RESULT_V1,
                RefundEventNames.VERSION_1,
                appliedAt,
                pending.refundId(),
                providerOperationId,
                RefundOutcome.SUCCEEDED,
                appliedAt
        );
        publishResult(result);
        publishResult(result);

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            HttpResponse<String> response = get("/api/v1/refunds/" + pending.refundId());
            assertThat(response.statusCode()).isEqualTo(200);
            RefundView completed = objectMapper.readValue(response.body(), RefundView.class);
            assertThat(completed.status()).isEqualTo(RefundStatus.SUCCEEDED);
            assertThat(completed.providerOperationId()).isEqualTo(providerOperationId);
            assertThat(jdbc.queryForObject(
                    "SELECT count(*) FROM refund_result_inbox WHERE event_id = ?",
                    Long.class,
                    resultEventId
            )).isEqualTo(1L);
        });
    }

    @Test
    void healthReportsRealDependenciesAvailable() throws Exception {
        HttpResponse<String> response = get("/actuator/health");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"status\":\"UP\"");
    }

    private HttpResponse<String> approve(String amount, boolean authenticated) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(uri("/api/v1/payments/" + PAYMENT_ID + "/refunds"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"amount\":" + amount + ",\"currency\":\"EUR\"}"));
        if (authenticated) {
            request.header("X-Fixture-Role", "support");
        }
        return http.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> get(String path) throws Exception {
        return http.send(HttpRequest.newBuilder(uri(path)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    private void publishResult(RefundResultV1 result) throws Exception {
        MessageProperties properties = new MessageProperties();
        properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        properties.setMessageId(result.eventId().toString());
        Message message = new Message(
                objectMapper.writeValueAsString(result).getBytes(StandardCharsets.UTF_8),
                properties
        );
        rabbit.send(RefundMessaging.EXCHANGE, RefundMessaging.RESULT_ROUTING_KEY, message);
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }
}
