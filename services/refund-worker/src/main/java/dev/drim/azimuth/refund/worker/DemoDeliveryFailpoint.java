package dev.drim.azimuth.refund.worker;

import java.util.UUID;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("demo")
class DemoDeliveryFailpoint implements DeliveryBoundary {
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition released = lock.newCondition();
    private FailpointState state = FailpointState.IDLE;
    private UUID pausedRefundId;
    private long interruptionCount;

    FailpointStatus arm() {
        lock.lock();
        try {
            if (state != FailpointState.IDLE) {
                throw new FailpointStateConflict("Failpoint is already " + state.wireName());
            }
            state = FailpointState.ARMED;
            return statusLocked();
        } finally {
            lock.unlock();
        }
    }

    FailpointStatus release() {
        lock.lock();
        try {
            if (state != FailpointState.PAUSED) {
                throw new FailpointStateConflict("Failpoint is " + state.wireName());
            }
            state = FailpointState.RELEASED;
            released.signalAll();
            return statusLocked();
        } finally {
            lock.unlock();
        }
    }

    FailpointStatus status() {
        lock.lock();
        try {
            return statusLocked();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void afterProviderResponse(UUID refundId) {
        lock.lock();
        try {
            if (state != FailpointState.ARMED) {
                return;
            }
            state = FailpointState.PAUSED;
            pausedRefundId = refundId;
            while (state == FailpointState.PAUSED) {
                released.await();
            }
            state = FailpointState.IDLE;
            pausedRefundId = null;
            interruptionCount++;
            throw new DeliveryRestartRequested();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            state = FailpointState.IDLE;
            pausedRefundId = null;
            throw new DeliveryRestartRequested();
        } finally {
            lock.unlock();
        }
    }

    private FailpointStatus statusLocked() {
        return new FailpointStatus(state.wireName(), pausedRefundId, interruptionCount);
    }

    enum FailpointState {
        IDLE,
        ARMED,
        PAUSED,
        RELEASED;

        String wireName() {
            return name().toLowerCase();
        }
    }

    record FailpointStatus(String state, UUID pausedRefundId, long interruptionCount) {
    }

    static final class FailpointStateConflict extends RuntimeException {
        FailpointStateConflict(String message) {
            super(message);
        }
    }
}
