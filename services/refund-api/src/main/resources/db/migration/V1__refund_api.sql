CREATE TABLE trips (
    trip_id uuid PRIMARY KEY,
    rider_id uuid NOT NULL
);

CREATE TABLE payments (
    payment_id uuid PRIMARY KEY,
    trip_id uuid NOT NULL UNIQUE REFERENCES trips (trip_id),
    captured_amount numeric(19, 2) NOT NULL CHECK (captured_amount >= 0),
    original_fare numeric(19, 2) NOT NULL CHECK (original_fare >= 0),
    wait_time_fee numeric(19, 2) NOT NULL CHECK (wait_time_fee >= 0),
    refundable_remaining numeric(19, 2) NOT NULL CHECK (refundable_remaining >= 0),
    currency char(3) NOT NULL,
    CHECK (captured_amount = original_fare + wait_time_fee),
    CHECK (refundable_remaining <= captured_amount)
);

CREATE TABLE refunds (
    refund_id uuid PRIMARY KEY,
    payment_id uuid NOT NULL REFERENCES payments (payment_id),
    amount numeric(19, 2) NOT NULL CHECK (amount > 0),
    currency char(3) NOT NULL,
    status varchar(16) NOT NULL CHECK (status IN ('pending', 'succeeded')),
    approved_by varchar(100) NOT NULL,
    approved_at timestamptz NOT NULL,
    provider_operation_id uuid,
    completed_at timestamptz,
    CHECK ((status = 'pending' AND provider_operation_id IS NULL AND completed_at IS NULL)
        OR (status = 'succeeded' AND provider_operation_id IS NOT NULL AND completed_at IS NOT NULL))
);

CREATE INDEX refunds_payment_id_idx ON refunds (payment_id, approved_at, refund_id);

CREATE TABLE refund_request_outbox (
    event_id uuid PRIMARY KEY,
    refund_id uuid NOT NULL UNIQUE REFERENCES refunds (refund_id),
    event_type varchar(64) NOT NULL,
    payload jsonb NOT NULL,
    created_at timestamptz NOT NULL,
    published_at timestamptz
);

CREATE INDEX refund_request_outbox_unpublished_idx
    ON refund_request_outbox (created_at) WHERE published_at IS NULL;

CREATE TABLE refund_result_inbox (
    event_id uuid PRIMARY KEY,
    refund_id uuid NOT NULL,
    received_at timestamptz NOT NULL
);

INSERT INTO trips (trip_id, rider_id)
VALUES ('22222222-2222-2222-2222-222222222222', '11111111-1111-1111-1111-111111111111');

INSERT INTO payments (
    payment_id, trip_id, captured_amount, original_fare, wait_time_fee,
    refundable_remaining, currency
)
VALUES (
    '33333333-3333-3333-3333-333333333333',
    '22222222-2222-2222-2222-222222222222',
    120.00, 90.00, 30.00, 120.00, 'EUR'
);
