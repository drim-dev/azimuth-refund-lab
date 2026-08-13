package dev.drim.azimuth.refund.worker;

final class DeliveryRestartRequested extends RuntimeException {
    DeliveryRestartRequested() {
        super("Demo failpoint requested delivery redelivery");
    }
}
