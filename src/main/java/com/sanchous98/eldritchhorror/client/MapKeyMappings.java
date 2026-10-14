package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

/**
 * Registers the mod's key bindings, all rebindable in Options → Controls:
 * <ul>
 *   <li><b>open the world map</b> (default {@code M}) — while the world atlas is held ({@code design/22});</li>
 *   <li><b>open the quest journal</b> (default {@code J}) — {@link JournalScreen};</li>
 *   <li><b>open the codex</b> (default {@code C}) — {@link CodexScreen};</li>
 *   <li><b>open the skill tree</b> (default {@code K}) — {@link SkillScreen}.</li>
 * </ul>
 */
@EventBusSubscriber(modid = EldritchHorror.MODID, value = Dist.CLIENT)
public final class MapKeyMappings {

    /** Our controls category, shown in the key-bind screen. */
    static final KeyMapping.Category CATEGORY =
            new KeyMapping.Category(EldritchHorror.id("keys"));

    /** The map key; {@code null} until {@link #onRegisterKeyMappings} runs. */
    static KeyMapping openMap;
    /** The journal key; {@code null} until {@link #onRegisterKeyMappings} runs. */
    static KeyMapping openJournal;
    /** The codex key; {@code null} until {@link #onRegisterKeyMappings} runs. */
    static KeyMapping openCodex;
    /** The skill-tree key; {@code null} until {@link #onRegisterKeyMappings} runs. */
    static KeyMapping openSkills;

    private MapKeyMappings() {
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY);
        openMap = register(event, "key.eldritch_horror.map", InputConstants.KEY_M);
        openJournal = register(event, "key.eldritch_horror.journal", InputConstants.KEY_J);
        openCodex = register(event, "key.eldritch_horror.codex", InputConstants.KEY_C);
        openSkills = register(event, "key.eldritch_horror.skills", InputConstants.KEY_K);
    }

    /** Creates and registers one keyboard binding under {@link #CATEGORY}. */
    private static KeyMapping register(RegisterKeyMappingsEvent event, String name, int defaultKey) {
        KeyMapping mapping = new KeyMapping(name, InputConstants.Type.KEYBOARD, defaultKey, CATEGORY);
        event.register(mapping);
        return mapping;
    }
}
