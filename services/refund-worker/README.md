# Refund worker

The worker consumes `refund.requested.v1`, applies the refund through the provider HTTP API and
publishes `refund.result.v1`. It acknowledges the request only after the result publication has a
positive publisher confirm and was not returned as unroutable.

Configuration is supplied through `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USERNAME`,
`RABBITMQ_PASSWORD`, `RABBITMQ_VIRTUAL_HOST`, `PROVIDER_BASE_URL`, `PROVIDER_REQUEST_TIMEOUT`,
`PUBLISH_CONFIRM_TIMEOUT` and `SERVER_PORT`. Defaults target the local Compose topology and expose
HTTP on port 8081.

The `demo` profile adds one failpoint at `/demo/failpoints/after-provider-response`:

- `POST /arm` arms one delivery;
- `GET /` reports `idle`, `armed`, `paused` or `released` and the paused RefundId;
- `POST /release` interrupts the paused delivery so RabbitMQ redelivers it.

No failpoint controller or failpoint behavior exists unless the `demo` profile is active.
