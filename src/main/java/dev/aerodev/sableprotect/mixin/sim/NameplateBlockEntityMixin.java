package dev.aerodev.sableprotect.mixin.sim;

import com.simibubi.create.content.kinetics.motor.KineticScrollValueBehaviour;
import dev.aerodev.sableprotect.claim.ClaimRole;
import dev.aerodev.sableprotect.protection.PacketProtection;
import dev.aerodev.sableprotect.protection.ProtectionHelper;
import foundry.veil.api.network.handler.ServerPacketContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(targets = "dev.simulated_team.simulated.content.blocks.nameplate.NameplateScreenEntity", remap = false)
public class NameplateBlockEntityMixin {
    @Inject(
            method = "setName",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void sableProtect$cancelsRename(String name, boolean updateNameplates, @Nullable Player player, CallbackInfo ci) {
        if (player == null) {return;}
        if (!(player instanceof ServerPlayer)) {return;}

        BlockEntity blockEntity = (BlockEntity) (Object) this;

        final Level level = blockEntity.getLevel();
        final BlockPos pos = blockEntity.getBlockPos();

        final ProtectionHelper.ClaimContext ctx = ProtectionHelper.getClaimContext(level, pos);
        if (ctx == null) return;

        if (ProtectionHelper.isAdminBypass((ServerPlayer) player)) return;

        if (ctx.claimData().isInteractionsProtected() && ctx.claimData().getRole(player.getUUID()) == ClaimRole.DEFAULT) {
            ci.cancel();
            ProtectionHelper.sendDeniedMessage((ServerPlayer) player);
        }
    }
}
