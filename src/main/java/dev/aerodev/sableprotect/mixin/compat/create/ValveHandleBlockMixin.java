package dev.aerodev.sableprotect.mixin.compat.create;

import com.simibubi.create.content.kinetics.crank.ValveHandleBlock;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ValveHandleBlock.class, remap = false)
public class ValveHandleBlockMixin {
    @Inject(
            method = "clicked",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void sableProtect$clicked(Level level, BlockPos pos, BlockState blockState, Player player, InteractionHand hand, CallbackInfoReturnable<Boolean> cir) {
        if (!(player instanceof ServerPlayer)) {return;}

        final ProtectionHelper.ClaimContext ctx = ProtectionHelper.getClaimContext(level, pos);
        if (ctx == null) return;

        if (ProtectionHelper.isAdminBypass((ServerPlayer) player)) return;

        if (ctx.claimData().isInteractionsProtected() && ctx.claimData().getRole(player.getUUID()) == ClaimRole.DEFAULT) {
            cir.setReturnValue(false);
            ProtectionHelper.sendDeniedMessage((ServerPlayer) player);
        }
    }
}
