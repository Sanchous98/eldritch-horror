package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.corruption.CorruptionSystem;
import com.sanchous98.eldritchhorror.registry.ModAttachments;
import com.sanchous98.eldritchhorror.sanity.SanitySystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * The mod HUD: sanity and corruption replace the vanilla hunger and experience bars.
 *
 * <p>The vanilla {@code FOOD_LEVEL} and {@code EXPERIENCE_LEVEL} layers are replaced with no-ops
 * (NeoForge {@code RegisterGuiLayersEvent.replaceLayer} — there is no remove), and two custom
 * layers are drawn in their slots:
 * <ul>
 *   <li><b>Sanity</b> — where the food bar was (a row of 10 pips over the hotbar).</li>
 *   <li><b>Corruption</b> — where the experience bar was (a filled bar, coloured by stage).</li>
 * </ul>
 *
 * <p>Reads straight from the synced player attachments, so it is pure rendering — no gameplay
 * state (see {@code design/27-systems-framework.md}).
 */
@EventBusSubscriber(modid = EldritchHorror.MODID, value = Dist.CLIENT)
public final class ClientHud {

    private static final Identifier SANITY_ID = EldritchHorror.id("hud/sanity");
    private static final Identifier CORRUPTION_ID = EldritchHorror.id("hud/corruption");

    private ClientHud() {
    }

    @SubscribeEvent
    public static void onRegisterLayers(RegisterGuiLayersEvent event) {
        // Hide vanilla hunger and experience.
        event.replaceLayer(VanillaGuiLayers.FOOD_LEVEL, (g, dt) -> { });
        event.replaceLayer(VanillaGuiLayers.EXPERIENCE_LEVEL, (g, dt) -> { });

        // Corruption bar where the XP bar was (above the hotbar, full width).
        event.registerAbove(VanillaGuiLayers.HOTBAR, CORRUPTION_ID, ClientHud::drawCorruption);
        // Sanity pips where the food bar was (right of centre).
        event.registerAbove(VanillaGuiLayers.HOTBAR, SANITY_ID, ClientHud::drawSanity);
    }

    private static void drawSanity(GuiGraphicsExtractor g, net.minecraft.client.DeltaTracker dt) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        double sanity = player.getData(ModAttachments.SANITY.get());
        double frac = clamp01(sanity / SanitySystem.max(player));
        int pips = (int) Math.ceil(frac * 10.0);

        int right = g.guiWidth() / 2 + 91;
        int top = g.guiHeight() - 39;
        int size = 8;
        for (int i = 0; i < 10; i++) {
            int x = right - (i + 1) * (size + 1);
            int y = top;
            // dark background pip
            g.fill(RenderPipelines.GUI, x, y, x + size, y + size, 0xCC202028);
            if (i < pips) {
                g.fill(RenderPipelines.GUI, x + 1, y + 1, x + size - 1, y + size - 1, sanityColour(frac));
            }
        }
    }

    private static void drawCorruption(GuiGraphicsExtractor g, net.minecraft.client.DeltaTracker dt) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        double corruption = player.getData(ModAttachments.CORRUPTION.get());
        double frac = clamp01(corruption / CorruptionSystem.DEFAULT_MAX);

        int width = 182;
        int x = g.guiWidth() / 2 - width / 2;
        int y = g.guiHeight() - 29; // exactly the vanilla experience-bar slot
        g.fill(RenderPipelines.GUI, x, y, x + width, y + 5, 0xCC101018);
        int filled = (int) (width * frac);
        if (filled > 0) {
            g.fill(RenderPipelines.GUI, x, y, x + filled, y + 5, corruptionColour(frac));
        }
    }

    private static int sanityColour(double frac) {
        // calm blue → uneasy violet → danger red
        if (frac > 0.7) {
            return 0xFF7FB0E0;
        }
        if (frac > 0.4) {
            return 0xFFB080D0;
        }
        if (frac > 0.2) {
            return 0xFFD08060;
        }
        return 0xFFD04040;
    }

    private static int corruptionColour(double frac) {
        // dormant grey-violet → claimed deep purple
        if (frac > 0.7) {
            return 0xFF7A2FB0;
        }
        if (frac > 0.4) {
            return 0xFF9A4FC0;
        }
        if (frac > 0.1) {
            return 0xFF6E5AA0;
        }
        return 0xFF4A4A60;
    }

    private static double clamp01(double v) {
        return Math.clamp(v, 0.0, 1.0);
    }
}
