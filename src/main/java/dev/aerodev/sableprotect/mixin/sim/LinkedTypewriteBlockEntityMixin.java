package dev.aerodev.sableprotect.mixin.sim;
import dev.aerodev.sableprotect.claim.ClaimRole;
import dev.aerodev.sableprotect.protection.ProtectionHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(targets = "dev.simulated_team.simulated.content.blocks.redstone.linked_typewrite.LinkedTypewriteBlockEntity", remap = false)
public class LinkedTypewriteBlockEntityMixin {
    @Inject(
            method = "checkAndStartUsing",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void sableProtect$cancelsTypewriteFocus(UUID userID, CallbackInfoReturnable<Boolean> cir) {
        final LinkedTypewriteBlockEntity behaviour = (LinkedTypewriteBlockEntity) (Object) this;

        final Player player = this.level.getPlayerByUUID(userID);

        if (!(player instanceof ServerPlayer)) {return;}

        final ProtectionHelper.ClaimContext ctx = ProtectionHelper.getClaimContext(this.level, this.getBlockPos());
        if (ctx == null) return;

        if (ProtectionHelper.isAdminBypass((ServerPlayer) player)) return;

        if (ctx.claimData().isInteractionsProtected() && ctx.claimData().getRole(player.getUUID()) == ClaimRole.DEFAULT) {
            cir.setReturnValue(false);
            ProtectionHelper.sendDeniedMessage((ServerPlayer) player);
        }
    }
}
