package dev.aerodev.sableprotect.mixin.compat.create;

import com.simibubi.create.content.kinetics.motor.KineticScrollValueBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBehaviour.ValueSettings;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = KineticScrollValueBehaviour.class, remap = false)
public class KineticScrollValueBehaviourMixin {
    @Inject(
            method = "setValueSettings",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )

    private static void sableProtect$cancelKineticScrollValueBehaviour(final Player player, ValueSettings valueSetting, boolean ctrlHeld, final CallbackInfo ci) {
        ci.cancel();
        //if (!(event.getEntity() instanceof ServerPlayer)) {return;}

        //final ProtectionHelper.ClaimContext ctx = ProtectionHelper.getClaimContext(event.getLevel(), event.getPos());
        //if (ctx == null) return;

        //if (ProtectionHelper.isAdminBypass((ServerPlayer) event.getEntity())) return;

        //if (ctx.claimData().isInteractionsProtected() && ctx.claimData().getRole(event.getEntity().getUUID()) == ClaimRole.DEFAULT) {
            //ci.cancel();
            //ProtectionHelper.sendDeniedMessage((ServerPlayer) event.getEntity());
        //}
    }
}
