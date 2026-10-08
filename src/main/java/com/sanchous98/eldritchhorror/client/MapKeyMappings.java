package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

/**
 * Registers the mod's key bindings. Currently one: <b>open the world map</b> (default {@code M}),
 * which opens the fullscreen map when the player is holding the world atlas — the key equivalent of
 * right-clicking it ({@code design/22}). The key is rebindable in Options → Controls.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID, value = Dist.CLIENT)
public final class MapKeyMappings {

    /** Our controls category, shown in the key-bind screen. */
    static final KeyMapping.Category CATEGORY =
            new KeyMapping.Category(EldritchHorror.id("keys"));

    /** The map key; {@code null} until {@link #onRegisterKeyMappings} runs. */
    static KeyMapping openMap;

    private MapKeyMappings() {
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY);
        openMap = new KeyMapping("key.eldritch_horror.map",
                InputConstants.Type.KEYBOARD, InputConstants.KEY_M, CATEGORY);
        event.register(openMap);
    }
}
