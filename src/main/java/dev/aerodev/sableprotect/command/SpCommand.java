package dev.aerodev.sableprotect.command;

import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.mojang.logging.LogUtils;
import dev.aerodev.sableprotect.claim.ClaimRegistry;
import dev.aerodev.sableprotect.freeze.FreezeManager;
import dev.aerodev.sableprotect.freeze.PendingFetchManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.Map;

public final class SpCommand {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String ROOT = "sp";

    private SpCommand() {}

    public static void register(final RegisterCommandsEvent event,
                                final ClaimRegistry registry,
                                final FreezeManager freezeManager,
                                final PendingFetchManager pendingFetchManager) {
        // Nuke any existing /sp root before installing ours. Brigadier's RootCommandNode
        // merges same-name literals — if WorldEdit (or any other mod) has already
        // registered /sp as a superpickaxe alias, a naive register() leaves their Command
        // attached to the root literal and only adds our subcommands as children. Wiping
        // the node first guarantees our /sp fully replaces theirs, regardless of mod load
        // order. Combined with EventPriority.LOWEST on the listener, this should win
        // against any reasonable registration timing.
        removeExistingRoot(event.getDispatcher().getRoot(), ROOT);

        event.getDispatcher().register(
                Commands.literal(ROOT)
                        .then(ClaimCommand.register(registry))
                        .then(UnclaimCommand.register(registry))
                        .then(InfoCommand.register(registry))
                        .then(EditCommand.register(registry))
                        .then(MyClaimsCommand.register(registry))
                        .then(FetchCommand.register(registry, freezeManager, pendingFetchManager))
                        .then(GroundCommand.register(registry, freezeManager, pendingFetchManager))
                        .then(GroundCommand2.register(registry, freezeManager, pendingFetchManager))
                        .then(StealCommand.register(registry))
                        .then(DebugCommand.register())
                        .then(BypassCommand.register())
                        .then(ReloadCommand.register())
                        .then(ClaimUuidCommand.register(registry))
        );
    }

    /**
     * Reflectively remove a root literal from a Brigadier dispatcher root. Brigadier's
     * {@code CommandNode} stores its child index in three private maps ({@code children},
     * {@code literals}, {@code arguments}) and exposes no public removal API. The field
     * names have been stable across Brigadier versions, so reflection is acceptable; we
     * also fail silently if the layout ever changes — the worst outcome is the merge
     * behavior described above, not a crash.
     */
    private static void removeExistingRoot(final RootCommandNode<CommandSourceStack> root, final String name) {
        try {
            final CommandNode<CommandSourceStack> existing = root.getChild(name);
            if (existing == null) return;

            removeFromField(root, "children", name);
            removeFromField(root, "literals", name);
            removeFromField(root, "arguments", name);
            LOGGER.info("[sable-protect] Replaced an existing /{} command from another mod.", name);
        } catch (final Throwable t) {
            LOGGER.warn("[sable-protect] Could not pre-remove an existing /{} command; another mod's /{} may take precedence: {}",
                    name, name, t.toString());
        }
    }

    private static void removeFromField(final RootCommandNode<?> root, final String fieldName, final String key)
            throws ReflectiveOperationException {
        final Field field = CommandNode.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        final Object value = field.get(root);
        if (value instanceof Map<?, ?> map) {
            map.remove(key);
        }
    }
}
