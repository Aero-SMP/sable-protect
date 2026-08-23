package dev.aerodev.sableprotect.mixin.compat.create;

import com.simibubi.create.foundation.block.ItemUseOverrides;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ItemUseOverrides.class, remap = false)
public class ItemUseOverridesMixin {
    @Inject(
            method = "onBlockActivated",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void sableProtect$cancelItemUseOverrides(
            final PlayerInteractEvent.RightClickBlock event,
            final CallbackInfo ci) {
        ci.cancel();
    }
}
