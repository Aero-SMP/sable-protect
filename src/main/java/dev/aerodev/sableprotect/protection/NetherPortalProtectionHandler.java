package dev.aerodev.sableprotect.protection;

import dev.aerodev.sableprotect.util.Lang;
import dev.aerodev.sableprotect.util.NoMansLand;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;

/**
 * Blocks nether portal travel into or out of No Man's Land. Because nether/overworld
 * coordinates are linked by an 8:1 scale, we project the entity's source position into
 * overworld coordinates and consult the same {@link NoMansLand} rectangle: this catches
 * both portals lit inside NML and portals in the nether that would emerge inside NML.
 *
 * <p>Cancels {@link EntityTravelToDimensionEvent} for the OW↔Nether pair only — End
 * portals and other dimension transits are unaffected.
 */
public class NetherPortalProtectionHandler {

    @SubscribeEvent
    public void onTravelToDimension(final EntityTravelToDimensionEvent event) {
        if (!NoMansLand.isEnabled()) return;

        final Entity entity = event.getEntity();
        final ResourceKey<Level> from = entity.level().dimension();
        final ResourceKey<Level> to = event.getDimension();

        final boolean owToNether = from.equals(Level.OVERWORLD) && to.equals(Level.NETHER);
        final boolean netherToOw = from.equals(Level.NETHER) && to.equals(Level.OVERWORLD);
        if (!owToNether && !netherToOw) return;

        // Project the source position into overworld coordinates: nether positions scale
        // by 8 to find the corresponding overworld destination.
        final double scale = netherToOw ? 8.0 : 1.0;
        final double projectedX = entity.getX() * scale;
        final double projectedZ = entity.getZ() * scale;

        if (!NoMansLand.contains(projectedX, projectedZ)) return;

        event.setCanceled(true);

        if (entity instanceof ServerPlayer player) {
            player.displayClientMessage(Lang.tr("sableprotect.nml.portal_blocked"), true);
            // Apply the dimension-changing delay so the player isn't immediately re-tested
            // on the next tick. They'll keep getting blocked if they remain in the portal,
            // but the cooldown gives them a window to step out before the next attempt.
            player.setPortalCooldown();
        }
    }
}
