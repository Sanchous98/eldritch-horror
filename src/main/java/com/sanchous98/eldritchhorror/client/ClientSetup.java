package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.EldritchHorror;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Client-only item interactions that must not load a client class on the dedicated server.
 *
 * <p>Right-clicking the world atlas opens {@link WorldMapScreen}. The check is by item id, so no
 * client class is referenced from common code, and this handler exists only on the client
 * ({@code Dist.CLIENT}), keeping the server free of GUI classes entirely.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID, value = Dist.CLIENT)
public final class ClientSetup {

    private static final Identifier ATLAS_ID = EldritchHorror.id("world_atlas");

    private ClientSetup() {
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        ItemStack stack = event.getItemStack();
        if (!stack.isEmpty() && ATLAS_ID.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()))) {
            Minecraft.getInstance().gui.setScreen(new WorldMapScreen());
        }
    }
}
