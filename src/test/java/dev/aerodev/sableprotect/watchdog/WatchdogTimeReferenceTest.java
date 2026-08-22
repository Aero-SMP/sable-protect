package dev.aerodev.sableprotect.watchdog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class WatchdogTimeReferenceTest {

    private static final long NANOS_PER_MILLISECOND = 1_000_000L;

    @Test
    void preservesVanillaReferenceBeforeFirstHeartbeat() {
        assertEquals(5_000L, WatchdogTimeReference.latest(5_000L, 4_000L));
    }

    @Test
    void advancingFourTpsHeartbeatNeverLooksHung() {
        final long watchdogTimeout = 60_000L * NANOS_PER_MILLISECOND;
        final long tickDuration = 250L * NANOS_PER_MILLISECOND;
        final long simulationDuration = 10L * 60L * 1_000L * NANOS_PER_MILLISECOND;

        long scheduledTick = 0L;
        long heartbeat = 0L;
        for (long now = tickDuration; now <= simulationDuration; now += tickDuration) {
            scheduledTick += 50L * NANOS_PER_MILLISECOND;
            heartbeat = now;

            final long watchdogReference = WatchdogTimeReference.latest(scheduledTick, heartbeat);
            final long watchdogPollImmediatelyBeforeNextTick = now + tickDuration - 1L;
            assertFalse(watchdogPollImmediatelyBeforeNextTick - watchdogReference > watchdogTimeout);
        }

        assertTrue(simulationDuration - scheduledTick > watchdogTimeout,
                "vanilla's scheduled reference must accumulate enough debt to demonstrate the regression");
    }

    @Test
    void frozenTickStillExceedsWatchdogTimeout() {
        final long watchdogTimeout = 60_000L * NANOS_PER_MILLISECOND;
        final long heartbeat = 5_000L * NANOS_PER_MILLISECOND;
        final long scheduledTick = 4_000L * NANOS_PER_MILLISECOND;
        final long now = heartbeat + watchdogTimeout + 1L;

        final long watchdogReference = WatchdogTimeReference.latest(scheduledTick, heartbeat);
        assertTrue(now - watchdogReference > watchdogTimeout);
    }

    @Test
    void futureVanillaDeadlineRemainsAuthoritative() {
        assertEquals(8_000L, WatchdogTimeReference.latest(8_000L, 7_000L));
    }

    @Test
    void comparisonHandlesNanoTimeSignedWraparound() {
        final long beforeWrap = Long.MAX_VALUE - 5L;
        final long afterWrap = Long.MIN_VALUE + 5L;

        assertEquals(afterWrap, WatchdogTimeReference.latest(beforeWrap, afterWrap));
    }
}
