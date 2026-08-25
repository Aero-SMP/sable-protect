package dev.aerodev.sableprotect.mixin.sim;

import dev.aerodev.sableprotect.SableProtectMod;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterBlock;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterBlockEntity;
import dev.aerodev.sableprotect.claim.ClaimRole;
import dev.aerodev.sableprotect.protection.ProtectionHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LinkedTypewriterBlock.class, remap = false)
public class LinkedTypewriteBlockMixin {
    @Inject(
            method = "useItemOn",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void sableProtect$cancelsTypewriteFocus(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult, CallbackInfoReturnable<ItemInteractionResult> cir) {
        SableProtectMod.LOGGER.info("Mixin catched!");

        final LinkedTypewriterBlockEntity behaviour = (LinkedTypewriterBlockEntity) (Object) this;

        if (level == null) {
            SableProtectMod.LOGGER.info("level = null");
        }

        if (player == null) {
            SableProtectMod.LOGGER.info("Player = null");
        }

        if (!(player instanceof ServerPlayer)) {return;}

        final ProtectionHelper.ClaimContext ctx = ProtectionHelper.getClaimContext(level, blockPos);
        if (ctx == null) return;

        if (ProtectionHelper.isAdminBypass((ServerPlayer) player)) return;

        if (ctx.claimData().isInteractionsProtected() && ctx.claimData().getRole(player.getUUID()) == ClaimRole.DEFAULT) {
            //behaviour.disconnectUser();
            SableProtectMod.LOGGER.info("Attemping to cancel focus");
            cir.setReturnValue(ItemInteractionResult.FAIL);
            ProtectionHelper.sendDeniedMessage((ServerPlayer) player);
        }
    }
}
