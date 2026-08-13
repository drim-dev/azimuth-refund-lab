package dev.drim.azimuth.refund.provider;

enum ProviderRefundApplicationMode {
    IDEMPOTENT {
        @Override
        boolean recordsFinancialEffect(boolean createdIdempotencyRecord) {
            return createdIdempotencyRecord;
        }
    },
    REAPPLY_IDENTICAL_REQUESTS {
        @Override
        boolean recordsFinancialEffect(boolean createdIdempotencyRecord) {
            return true;
        }
    };

    abstract boolean recordsFinancialEffect(boolean createdIdempotencyRecord);
}
