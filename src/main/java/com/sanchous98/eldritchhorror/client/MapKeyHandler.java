package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.EldritchHorror;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Opens the world map when the map key is pressed while the world atlas is held. Client-only; the
 * map screen class is never loaded server-side.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID, value = Dist.CLIENT)
public final class MapKeyHandler {

    private static final Identifier ATLAS = EldritchHorror.id("world_atlas");

    private MapKeyHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (MapKeyMappings.openMap == null || !MapKeyMappings.openMap.consumeClick()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        // The key mirrors a right-click on the atlas, so require it in hand (either hand).
        for (InteractionHand hand : InteractionHand.values()) {
            if (ATLAS.equals(BuiltInRegistries.ITEM.getKey(minecraft.player.getItemInHand(hand).getItem()))) {
                minecraft.gui.setScreen(new WorldMapScreen());
                return;
            }
        }
    }
}
