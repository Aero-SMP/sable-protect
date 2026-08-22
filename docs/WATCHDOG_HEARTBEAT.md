# Watchdog heartbeat correction

## Problem

Minecraft 1.21.1's dedicated-server watchdog compares wall-clock time with
`MinecraftServer.nextTickTimeNanos`. That field is a scheduled deadline: it
advances by the target tick interval, not by the amount of real time consumed by
a completed tick.

If the server continues making progress below its target TPS, the scheduled
deadline falls behind wall time. The watchdog eventually reports the accumulated
scheduler debt as one long-running tick and forcibly stops the server. In this
failure mode, the completed-tick counter continues advancing and crash reports
sample different ordinary server-tick frames.

Increasing `max-tick-time` only delays that false positive. Setting it to `-1`
disables both false positives and useful detection of genuine deadlocks.

## Correction

`MinecraftServerWatchdogMixin` records `Util.getNanos()` at the head of every
`MinecraftServer.tickServer` invocation. When the watchdog asks
`getNextTickTime()`, the mixin returns the newer of:

- vanilla's scheduled tick deadline; and
- the most recently observed server-tick heartbeat.

The heartbeat fields are `volatile` because the server thread writes them while
the watchdog thread reads them. Before the first tick heartbeat, the mixin leaves
vanilla's value unchanged. Timestamp selection uses subtraction rather than a
plain numeric comparison so `System.nanoTime()` signed wraparound remains safe.

## Invariants

- A server that continuously begins new ticks cannot be killed solely because it
  is below 20 TPS.
- A tick that stops making progress leaves the heartbeat unchanged and can still
  exceed `max-tick-time`.
- Vanilla tick pacing, overload warnings, task scheduling, and catch-up behavior
  are not modified.
- The correction does not suppress exceptions or non-watchdog fail-stop paths,
  including Sable physics-worker failures.

## Configuration

Keep a positive `max-tick-time` in `server.properties`. A value of `60000` means
a real tick may remain stuck for 60 seconds before the watchdog terminates the
server. Larger values provide more tolerance for legitimately long operations.

Do not use `max-tick-time=-1` after this correction unless automatic recovery
from genuine main-thread hangs is intentionally unwanted.

## Validation

`WatchdogTimeReferenceTest` covers:

- ten simulated minutes at 4 TPS while vanilla scheduler debt grows beyond the
  watchdog timeout;
- a frozen heartbeat still crossing the timeout;
- preservation of a newer vanilla deadline; and
- signed `nanoTime` wraparound.

The packaged mixin must also be startup-tested against the complete server pack
so a mapping or injection mismatch fails during deployment rather than at the
next incident.
