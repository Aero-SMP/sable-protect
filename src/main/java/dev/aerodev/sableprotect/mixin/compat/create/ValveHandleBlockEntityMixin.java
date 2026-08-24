package dev.aerodev.sableprotect.mixin.compat.create;

import com.simibubi.create.content.kinetics.crank.ValveHandleBlockEntity;
import com.simibubi.create.content.kinetics.motor.KineticScrollValueBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBehaviour.ValueSettings;
import dev.aerodev.sableprotect.claim.ClaimRole;
import dev.aerodev.sableprotect.protection.ProtectionHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(value = ValveHandleBlockEntity.ValveHandleScrollValueBehaviour.class, remap = false)
public class ValveHandleBlockEntityMixin {
    @Inject(
            method = "setValueSettings",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void sableProtect$cancelsetValueSettings(Player player, ValueSettings valueSetting, boolean ctrlHeld, CallbackInfo ci) {
        if (ci == null) {return;}

        if (!(player instanceof ServerPlayer)) {return;}

        final ValveHandleBlockEntity.ValveHandleScrollValueBehaviour behaviour = (ValveHandleBlockEntity.ValveHandleScrollValueBehaviour) (Object) this;

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
