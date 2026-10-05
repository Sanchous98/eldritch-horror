package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ClientHooks;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Installs the client-only implementations behind {@link ClientHooks}. Runs on the client setup
 * event, so on a dedicated server this class (and {@link WorldMapScreen}) is never loaded and the
 * common item's {@code use} falls through to a no-op.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID, value = Dist.CLIENT)
public final class ClientSetup {

    private ClientSetup() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ClientHooks.openWorldMap = () -> Minecraft.getInstance().gui.setScreen(new WorldMapScreen());
    }
}
