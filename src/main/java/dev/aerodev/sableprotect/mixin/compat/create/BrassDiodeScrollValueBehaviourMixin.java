package dev.aerodev.sableprotect.mixin.compat.create;

import com.simibubi.create.content.redstone.diodes.BrassDiodeScrollValueBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import dev.aerodev.sableprotect.claim.ClaimRole;
import dev.aerodev.sableprotect.protection.ProtectionHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BrassDiodeScrollValueBehaviour.class, remap = false)
public class BrassDiodeScrollValueBehaviourMixin {
    @Inject(
            method = "setValueSettings",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void sableProtect$cancelScrollSlider(Player player, ValueSettingsBehaviour.ValueSettings valueSetting, boolean ctrlHeld, CallbackInfo ci) {
        if (ci == null) {return;}

        if (!(player instanceof ServerPlayer)) {return;}

        final BrassDiodeScrollValueBehaviour behaviour = (BrassDiodeScrollValueBehaviour) (Object) this;

        final Level level = behaviour.getWorld();
        final BlockPos pos = behaviour.getPos();

        if (level == null) {return;}

        final ProtectionHelper.ClaimContext ctx = ProtectionHelper.getClaimContext(level, pos);
        if (ctx == null) return;

        if (ProtectionHelper.isAdminBypass((ServerPlayer) player)) return;

        if (ctx.claimData().isInteractionsProtected() && ctx.claimData().getRole(player.getUUID()) == ClaimRole.DEFAULT) {
            ci.cancel();
            ProtectionHelper.sendDeniedMessage((ServerPlayer) player);
        }
    }

    @Inject(
            method = "onShortInteract",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void sableProtect$cancelFilterChange(Player player, InteractionHand hand, Direction side, BlockHitResult hitResult, CallbackInfo ci) {
        if (ci == null) {return;}

        if (!(player instanceof ServerPlayer)) {return;}

        final BrassDiodeScrollValueBehaviour behaviour = (BrassDiodeScrollValueBehaviour) (Object) this;

        final Level level = behaviour.getWorld();
        final BlockPos pos = behaviour.getPos();

        if (level == null) {return;}

        final ProtectionHelper.ClaimContext ctx = ProtectionHelper.getClaimContext(level, pos);
        if (ctx == null) return;

        if (ProtectionHelper.isAdminBypass((ServerPlayer) player)) return;

        if (ctx.claimData().isInteractionsProtected() && ctx.claimData().getRole(player.getUUID()) == ClaimRole.DEFAULT) {
            ci.cancel();
            ProtectionHelper.sendDeniedMessage((ServerPlayer) player);
        }
    }
}
