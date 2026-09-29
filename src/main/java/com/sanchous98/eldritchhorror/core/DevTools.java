package com.sanchous98.eldritchhorror.core;

import com.sanchous98.eldritchhorror.EldritchHorror;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Dev conveniences, gated behind the {@code eh.dev} system property so they never run on a
 * normal server. Enable with {@code -Deh.dev=true} (the Docker dev-server service sets it).
 *
 * <ul>
 *   <li>Auto-op: the first time a player joins, they are given operator, so the dev server
 *       needs no console access for {@code /gamemode}, {@code /tp}, etc.</li>
 * </ul>
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class DevTools {

    private DevTools() {
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!devMode()) {
            return;
        }
        if (event.getEntity() instanceof ServerPlayer player) {
            var list = player.level().getServer().getPlayerList();
            var id = player.nameAndId();
            if (!list.isOp(id)) {
                list.op(id);
                EldritchHorror.LOGGER.info("dev: granted operator to {}", player.getGameProfile().name());
            }
        }
    }

    /** Dev mode if {@code -Deh.dev=true} (system property) or {@code EH_DEV=true} (env). */
    private static boolean devMode() {
        return Boolean.getBoolean("eh.dev") || "true".equalsIgnoreCase(System.getenv("EH_DEV"));
    }
}
