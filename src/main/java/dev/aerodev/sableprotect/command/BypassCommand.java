package dev.aerodev.sableprotect.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.aerodev.sableprotect.config.SableProtectConfig;
import dev.aerodev.sableprotect.permissions.Permissions;
import dev.aerodev.sableprotect.util.BypassHelper;
import dev.aerodev.sableprotect.util.Lang;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public final class BypassCommand {

    private BypassCommand() {}

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("bypass")
                .requires(src -> Permissions.has(
                        src,
                        Permissions.Nodes.BYPASS_USE,
                        SableProtectConfig.ADMIN_BYPASS_PERMISSION_LEVEL.get()))
                .executes(ctx -> {
                    final ServerPlayer player = ctx.getSource().getPlayerOrException();
                    final boolean enabled = BypassHelper.toggle(player);
                    player.displayClientMessage(
                            Lang.tr(
                                    enabled ? "sableprotect.bypass.enabled" : "sableprotect.bypass.disabled"),
                            false);
                    return 1;
                });
    }
}
