package dev.drim.azimuth.refund.worker;

import static org.assertj.core.api.Assertions.assertThat;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import dev.drim.azimuth.Azimuth.Covers;
import dev.drim.azimuth.Azimuth.CoversMechanism;
import dev.drim.azimuth.Azimuth.Oracle;
import dev.drim.azimuth.Azimuth.Quantification;
import dev.drim.azimuth.Azimuth.Scope;
import dev.drim.azimuth.refund.contracts.event.RefundEventNames;
import dev.drim.azimuth.refund.contracts.event.RefundMessaging;
import dev.drim.azimuth.refund.contracts.event.RefundOutcome;
import dev.drim.azimuth.refund.contracts.event.RefundRequestedV1;
import dev.drim.azimuth.refund.contracts.event.RefundResultV1;
import dev.drim.azimuth.refund.contracts.http.ProviderRefundRequest;
import dev.drim.azimuth.refund.contracts.http.ProviderRefundResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.rabbitmq.RabbitMQContainer;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("demo")
@Testcontainers
class RefundWorkerComponentTest {
    private static final UUID REFUND_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID PAYMENT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID PROVIDER_OPERATION_ID =
            UUID.fromString("66666666-6666-6666-6666-666666666666");
    private static final Instant APPLIED_AT = Instant.parse("2026-08-12T09:00:01Z");
    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private static final AtomicInteger PROVIDER_CALLS = new AtomicInteger();
    private static final List<ProviderAttempt> PROVIDER_ATTEMPTS = new CopyOnWriteArrayList<>();
    private static final ObjectMapper PROVIDER_JSON = new ObjectMapper();
    private static final HttpServer PROVIDER = startProvider();

    @Container
    static final RabbitMQContainer RABBITMQ = new RabbitMQContainer("rabbitmq:4.1-alpine");

    @DynamicPropertySource
    static void applicationProperties(DynamicPropertyRegistry properties) {
        properties.add("spring.rabbitmq.host", RABBITMQ::getHost);
        properties.add("spring.rabbitmq.port", RABBITMQ::getAmqpPort);
        properties.add("spring.rabbitmq.username", RABBITMQ::getAdminUsername);
        properties.add("spring.rabbitmq.password", RABBITMQ::getAdminPassword);
        properties.add("refund.provider.base-url", () -> "http://localhost:" + PROVIDER.getAddress().getPort());
    }

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @LocalServerPort
    int workerPort;

    @BeforeEach
    void clearBoundaries() {
        rabbitTemplate.execute(channel -> {
            channel.queuePurge(RefundMessaging.WORKER_QUEUE);
            channel.queuePurge(RefundMessaging.API_RESULT_QUEUE);
            return null;
        });
        PROVIDER_CALLS.set(0);
        PROVIDER_ATTEMPTS.clear();
    }

    @AfterAll
    static void stopProvider() {
        PROVIDER.stop(0);
    }

    @Test
    @Covers(
            spec = "refunds/execution",
            scenario = "repeated-processing-does-not-repeat-refund",
            scope = Scope.component,
            quantification = Quantification.universal,
            oracle = Oracle.relational)
    @CoversMechanism(
            spec = "refunds/execution",
            mechanism = "stable-provider-idempotency-key",
            scope = Scope.component,
            quantification = Quantification.universal,
            oracle = Oracle.relational)
    void releasedDeliveryIsRedeliveredAndEmitsOneStableResult() throws Exception {
        assertThat(postFailpoint("arm").statusCode()).isEqualTo(202);

        RefundRequestedV1 requested = new RefundRequestedV1(
                UUID.fromString("55555555-5555-5555-5555-555555555555"),
                RefundEventNames.REFUND_REQUESTED_V1,
                RefundEventNames.VERSION_1,
                Instant.parse("2026-08-12T09:00:00Z"),
                REFUND_ID,
                PAYMENT_ID,
                new BigDecimal("30.00"),
                "EUR",
                "support-fixture");
        publishRequested(requested);

        await(Duration.ofSeconds(10), () -> failpointStatus().contains("\"state\":\"paused\""));
        assertThat(PROVIDER_CALLS).hasValue(1);
        assertThat(receiveResult(Duration.ofMillis(200))).isNull();

        assertThat(postFailpoint("release").statusCode()).isEqualTo(202);

        Message resultMessage = receiveResult(Duration.ofSeconds(10));
        assertThat(resultMessage).isNotNull();
        RefundResultV1 result = objectMapper.readValue(resultMessage.getBody(), RefundResultV1.class);
        UUID expectedEventId = UUID.nameUUIDFromBytes(
                (RefundEventNames.REFUND_RESULT_V1 + ":" + REFUND_ID)
                        .getBytes(java.nio.charset.StandardCharsets.UTF_8));

        assertThat(result.eventId()).isEqualTo(expectedEventId);
        assertThat(result.refundId()).isEqualTo(REFUND_ID);
        assertThat(result.providerOperationId()).isEqualTo(PROVIDER_OPERATION_ID);
        assertThat(result.outcome()).isEqualTo(RefundOutcome.SUCCEEDED);
        assertThat(result.appliedAt()).isEqualTo(APPLIED_AT);
        assertThat(resultMessage.getMessageProperties().getReceivedDeliveryMode())
                .isEqualTo(MessageDeliveryMode.PERSISTENT);
        await(Duration.ofSeconds(5), () -> PROVIDER_CALLS.get() == 2);
        ProviderRefundRequest expectedRequest = new ProviderRefundRequest(
                REFUND_ID, PAYMENT_ID, new BigDecimal("30.00"), "EUR");
        assertThat(PROVIDER_ATTEMPTS).containsExactly(
                new ProviderAttempt("POST", REFUND_ID.toString(), expectedRequest),
                new ProviderAttempt("POST", REFUND_ID.toString(), expectedRequest));
        assertThat(failpointStatus()).contains("\"interruptionCount\":1");
        assertThat(receiveResult(Duration.ofMillis(300))).isNull();
    }

    private void publishRequested(RefundRequestedV1 requested) throws IOException {
        MessageProperties properties = new MessageProperties();
        properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        properties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
        rabbitTemplate.send(
                RefundMessaging.EXCHANGE,
                RefundMessaging.REQUESTED_ROUTING_KEY,
                new Message(objectMapper.writeValueAsBytes(requested), properties));
    }

    private Message receiveResult(Duration timeout) {
        try {
            rabbitTemplate.setReceiveTimeout(timeout.toMillis());
            return rabbitTemplate.receive(RefundMessaging.API_RESULT_QUEUE);
        } finally {
            rabbitTemplate.setReceiveTimeout(0);
        }
    }

    private HttpResponse<String> postFailpoint(String action) throws IOException, InterruptedException {
        return HTTP.send(
                HttpRequest.newBuilder(workerUri("/demo/failpoints/after-provider-response/" + action))
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private String failpointStatus() {
        try {
            return HTTP.send(
                    HttpRequest.newBuilder(workerUri("/demo/failpoints/after-provider-response")).GET().build(),
                    HttpResponse.BodyHandlers.ofString()).body();
        } catch (IOException | InterruptedException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private URI workerUri(String path) {
        return URI.create("http://localhost:" + workerPort + path);
    }

    private static void await(Duration timeout, BooleanSupplier condition) throws InterruptedException {
        Instant deadline = Instant.now().plus(timeout);
        while (Instant.now().isBefore(deadline)) {
            if (condition.getAsBoolean()) {
                return;
            }
            Thread.sleep(25);
        }
        throw new AssertionError("Condition was not satisfied within " + timeout);
    }

    private static HttpServer startProvider() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            server.createContext("/provider/v1/refunds", RefundWorkerComponentTest::handleRefund);
            server.start();
            return server;
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static void handleRefund(HttpExchange exchange) throws IOException {
        PROVIDER_CALLS.incrementAndGet();
        String idempotencyKey = exchange.getRequestHeaders().getFirst("Idempotency-Key");
        ProviderRefundRequest request = PROVIDER_JSON.readValue(
                exchange.getRequestBody(), ProviderRefundRequest.class);
        PROVIDER_ATTEMPTS.add(new ProviderAttempt(
                exchange.getRequestMethod(), idempotencyKey, request));
        ProviderRefundResponse response = new ProviderRefundResponse(
                idempotencyKey,
                REFUND_ID,
                PROVIDER_OPERATION_ID,
                RefundOutcome.SUCCEEDED,
                APPLIED_AT);
        byte[] body = PROVIDER_JSON.writeValueAsBytes(response);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }

    private record ProviderAttempt(
            String method,
            String idempotencyKey,
            ProviderRefundRequest request
    ) {
    }
}
