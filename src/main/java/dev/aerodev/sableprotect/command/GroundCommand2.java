package dev.aerodev.sableprotect.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.aerodev.sableprotect.claim.ClaimRegistry;
import dev.aerodev.sableprotect.freeze.FreezeManager;
import dev.aerodev.sableprotect.freeze.PendingFetchManager;
import dev.aerodev.sableprotect.util.Lang;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

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
        player.displayClientMessage(Lang.tr("sableprotect.not_found", name), false);
        return 1;
    }
}
