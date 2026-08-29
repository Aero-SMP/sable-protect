package dev.aerodev.sableprotect.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.aerodev.sableprotect.claim.ClaimData;
import dev.aerodev.sableprotect.claim.ClaimRegistry;
import dev.aerodev.sableprotect.claim.ClaimRole;
import dev.aerodev.sableprotect.config.SableProtectConfig;
import dev.aerodev.sableprotect.freeze.FreezeManager;
import dev.aerodev.sableprotect.freeze.PendingFetchManager;
import dev.aerodev.sableprotect.util.Lang;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import dev.ryanhcode.sable.api.entity.EntitySubLevelUtil;
import dev.ryanhcode.sable.api.physics.PhysicsPipeline;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.Pose3d;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import org.apache.logging.log4j.core.jmx.Server;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.UUID;

public final class GroundCommand2 {

    private GroundCommand2() {}

    public static LiteralArgumentBuilder<CommandSourceStack> register(
            final ClaimRegistry registry, final FreezeManager freezeManager,
            final PendingFetchManager pendingFetchManager) {
        return Commands.literal("ground2")
                .then(Commands.argument("name", StringArgumentType.string())
                        .suggests((ctx, builder) -> {
                            final ServerPlayer player = ctx.getSource().getPlayerOrException();
                            for (final UUID id : registry.getOwnedBy(player.getUUID())) {
                                final String n = registry.getNameByUuid(id);
                                if (n != null) builder.suggest(n);
                            }
                            for (final UUID id : registry.getMemberOf(player.getUUID())) {
                                final String n = registry.getNameByUuid(id);
                                if (n != null) builder.suggest(n);
                            }
                            return builder.buildFuture();
                        })
                        .executes(ctx -> {
                            final ServerPlayer player = ctx.getSource().getPlayerOrException();
                            final String name = StringArgumentType.getString(ctx, "name");
                            return execute(player, name, registry, freezeManager, pendingFetchManager);
                        }));
    }

    private static int execute(final ServerPlayer player, final String name,
                               final ClaimRegistry registry, final FreezeManager freezeManager,
                               final PendingFetchManager pendingFetchManager) {
        final UUID subLevelId = registry.getSubLevelByName(name);
        if (subLevelId == null) { // Checks if sub-level exists
            player.displayClientMessage(Lang.tr("sableprotect.not_found", name), false);
            return 0;
        }

        final ClaimData data = registry.getClaim(subLevelId);
        if (data == null) { // Checks if sub-level has claim data
            player.displayClientMessage(Lang.tr("sableprotect.not_found", name), false);
            return 0;
        }

        if (data.getRole(player.getUUID()) == ClaimRole.DEFAULT) { //Checks if player is allowed to ground sub-level
            player.displayClientMessage(Lang.tr("sableprotect.ground.not_authorized"), false);
            return 0;
        }

        if (freezeManager.isFrozen(subLevelId)) { //Checks if sub-level is frozen
            player.displayClientMessage(Lang.tr("sableprotect.fetch.already_frozen", name), false);
            return 0;
        }

        final ServerSubLevel subLevel = UnclaimCommand.findSubLevel(player, subLevelId);
        if (subLevel != null) {
            return groundSublevel(player, name, subLevel, freezeManager);
        }
        //Loads Chunk that sub-level is in, then continues as normal, will continue working soon
        return 0;
    }

    private static int groundSublevel(final ServerPlayer player, final String name,
                                     final ServerSubLevel subLevel,
                                     final FreezeManager freezeManager) {
        final ServerLevel level = subLevel.getLevel();
        final Pose3d pose = subLevel.logicalPose();
        final Vector3dc currentPos = pose.position();
        final MinecraftServer server = level.getServer();

        //Checks if a player is aboard the sub-level before grounding
        ServerPlayer playerAboard = findPlayerAboard(server, subLevel);
        if (playerAboard != null) {
            player.displayClientMessage(Lang.tr("sableprotect.ground.crew_present", playerAboard.getGameProfile().getName()), false);
            return 0;
        }

        final Vector3d destination = computeGroundDestination(level, currentPos.x(), currentPos.z());

        final ServerSubLevelContainer container = SubLevelContainer.getContainer(level);
        if (container == null) {
            player.displayClientMessage(Lang.tr("sableprotect.fetch.failed"), false);
            return 0;
        }

        final PhysicsPipeline pipeline = container.physicsSystem().getPipeline();
        pipeline.resetVelocity(subLevel);
        final int durationSeconds = SableProtectConfig.FREEZE_DURATION_SECONDS.get();
        final long durationTicks = (long) (durationSeconds * server.tickRateManager().tickrate());

        //Starts the animation, then freezes the sub-level

        SubLevelAssemblyHelper.animateTo(subLevel.getUniqueId(),  BlockPos.containing(destination.x, destination.y, destination.z), callback -> {
            final long currentTick = level.getServer().getTickCount();
            Pose3d newpose = subLevel.logicalPose();

            if (!freezeManager.freeze(subLevel, new Vector3d(newpose.position().x, newpose.position().y, newpose.position().z), new Quaterniond(newpose.orientation()), durationTicks, currentTick)) {
                player.displayClientMessage(Lang.tr("sableprotect.fetch.freeze_unavailable"), false);
                return;
            }
            player.displayClientMessage(
                    Lang.tr("sableprotect.ground.success", name,
                            Component.literal((int) destination.x + ", " + (int) destination.y + ", " + (int) destination.z)
                                    .withStyle(ChatFormatting.AQUA),
                            durationSeconds),
                    false);
        });
        return 1;

    }

    private static ServerPlayer findPlayerAboard(MinecraftServer server, ServerSubLevel target) {
        UUID targetId = target.getUniqueId();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            SubLevel related = Sable.HELPER.getTrackingSubLevel(player);

            if (related == null) {
                related = Sable.HELPER.getContaining(player);
            }

            if (related != null && related.getUniqueId().equals((targetId))) {
                return player;
            }
        }
        return null;
    }

    private static Vector3d computeGroundDestination(final Level level, final double x, final double z) {
        // Force-load the chunk so the heightmap query gives a real surface instead of
        // bottom-of-world for unloaded chunks (see vanilla's {@code ServerLevel#getHeight}).
        if (level instanceof net.minecraft.server.level.ServerLevel server) {
            try {
                server.getChunkSource().getChunk(((int) Math.floor(x)) >> 4, ((int) Math.floor(z)) >> 4,
                        net.minecraft.world.level.chunk.status.ChunkStatus.FULL, true);
            } catch (final Throwable ignored) {}
        }
        final BlockPos surface = level.getHeightmapPos(
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                new BlockPos((int) Math.floor(x), 0, (int) Math.floor(z)));
        return new Vector3d(x, surface.getY(), z);
    }

}
