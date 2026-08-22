package dev.aerodev.sableprotect.mixin.compat.vanilla;

import dev.aerodev.sableprotect.watchdog.WatchdogTimeReference;
import java.util.function.BooleanSupplier;
import net.minecraft.Util;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes the vanilla watchdog measure lack of server-tick progress instead of
 * accumulated scheduler debt.
 */
@Mixin(MinecraftServer.class)
public abstract class MinecraftServerWatchdogMixin {

    @Unique
    private volatile long sableProtect$watchdogHeartbeatNanos;

    @Unique
    private volatile boolean sableProtect$watchdogHeartbeatSeen;

    @Inject(method = "tickServer", at = @At("HEAD"))
    private void sableProtect$recordWatchdogHeartbeat(
            final BooleanSupplier haveTime,
            final CallbackInfo callbackInfo) {
        sableProtect$watchdogHeartbeatNanos = Util.getNanos();
        sableProtect$watchdogHeartbeatSeen = true;
    }

    @Inject(method = "getNextTickTime", at = @At("RETURN"), cancellable = true)
    private void sableProtect$correctWatchdogTimeReference(
            final CallbackInfoReturnable<Long> callbackInfo) {
        if (!sableProtect$watchdogHeartbeatSeen) {
            return;
        }

        callbackInfo.setReturnValue(WatchdogTimeReference.latest(
                callbackInfo.getReturnValue(),
                sableProtect$watchdogHeartbeatNanos));
    }
}
