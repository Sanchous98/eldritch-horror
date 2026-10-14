package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.EldritchHorror;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Opens the three information screens — quest journal, codex, skill tree — from their key
 * bindings. Checked on the client tick and only while no other screen is open, so the keys keep
 * their normal meaning inside chat, inventories and the other screens. Client-only: none of the
 * screen classes are ever loaded on a dedicated server.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID, value = Dist.CLIENT)
public final class UiKeyHandler {

    private UiKeyHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.gui.screen() != null
                || MapKeyMappings.openJournal == null) {
            return;
        }
        if (MapKeyMappings.openJournal.consumeClick()) {
            minecraft.gui.setScreen(new JournalScreen());
        } else if (MapKeyMappings.openCodex.consumeClick()) {
            minecraft.gui.setScreen(new CodexScreen());
        } else if (MapKeyMappings.openSkills.consumeClick()) {
            minecraft.gui.setScreen(new SkillScreen());
        }
    }
}
