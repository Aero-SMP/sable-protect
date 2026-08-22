package dev.aerodev.sableprotect.watchdog;

/**
 * Selects the newer of vanilla's scheduled tick deadline and an observed server
 * tick heartbeat.
 *
 * <p>The subtraction-based comparison preserves {@link System#nanoTime()}'s
 * wraparound semantics as long as the two timestamps are less than roughly 292
 * years apart.</p>
 */
public final class WatchdogTimeReference {

    private WatchdogTimeReference() {
    }

    public static long latest(final long scheduledTickNanos, final long tickHeartbeatNanos) {
        return tickHeartbeatNanos - scheduledTickNanos > 0L
                ? tickHeartbeatNanos
                : scheduledTickNanos;
    }
}
