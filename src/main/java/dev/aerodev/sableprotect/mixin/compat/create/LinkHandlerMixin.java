package dev.aerodev.sableprotect.mixin.compat.create;


import com.simibubi.create.content.redstone.link.LinkHandler;
import com.simibubi.create.foundation.block.ItemUseOverrides;
import dev.aerodev.sableprotect.SableProtectMod;
import dev.aerodev.sableprotect.claim.ClaimData;
import dev.aerodev.sableprotect.claim.ClaimRole;
import dev.aerodev.sableprotect.protection.ProtectionHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LinkHandler.class, remap = false)
public class LinkHandlerMixin {
    @Inject(
            method = "onBlockActivated",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void sableProtect$cancelItemFrequencySet(
            final PlayerInteractEvent.RightClickBlock event,
            final CallbackInfo ci) {
        if (!(event.getEntity() instanceof ServerPlayer)) {return;}

        final ProtectionHelper.ClaimContext ctx = ProtectionHelper.getClaimContext(event.getLevel(), event.getPos());
        if (ctx == null) return;

        if (ProtectionHelper.isAdminBypass((ServerPlayer) event.getEntity())) return;

        if (ctx.claimData().isInteractionsProtected() && ctx.claimData().getRole(event.getEntity().getUUID()) == ClaimRole.DEFAULT) {
            ci.cancel();
            ProtectionHelper.sendDeniedMessage((ServerPlayer) event.getEntity());
        }
    }
}
