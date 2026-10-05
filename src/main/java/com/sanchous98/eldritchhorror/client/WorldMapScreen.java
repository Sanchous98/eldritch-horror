package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.registry.ModAttachments;
import com.sanchous98.eldritchhorror.world.EarthMap;
import com.sanchous98.eldritchhorror.world.city.Cities;
import com.sanchous98.eldritchhorror.world.city.City;
import com.sanchous98.eldritchhorror.world.loc.Location;
import com.sanchous98.eldritchhorror.world.loc.Locations;
import com.sanchous98.eldritchhorror.world.loc.city.CityLocation;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * The fullscreen world map (design/22-map-and-knowledge.md). Draws the baked Earth landmask scaled
 * to the 131072×65536 world, then overlays markers: the player's position, their home city, every
 * curated city (always known — it is Earth), and the sites the player has discovered (codex-gated).
 *
 * <p>Client-only and read-only: it renders synced data ({@code LORE}, {@code HOME_CITY}) and never
 * touches server state. The map image is {@code textures/gui/world_map.png}, a downscaled copy of
 * the landmask so a GUI-sized draw stays cheap.
 */
public final class WorldMapScreen extends Screen {

    private static final Identifier MAP_TEXTURE = EldritchHorror.id("textures/gui/world_map.png");
    private static final int TEX_W = 2048;
    private static final int TEX_H = 1024;

    /** World extent (blocks); mirrors {@link EarthMap}. */
    private static final int HALF_WIDTH = EarthMap.HALF_WIDTH;
    private static final int HALF_HEIGHT = EarthMap.HALF_HEIGHT;

    private int mapX;
    private int mapY;
    private int mapW;
    private int mapH;

    public WorldMapScreen() {
        super(Component.translatable("screen.eldritch_horror.world_map"));
    }

    @Override
    protected void init() {
        // Fit the 2:1 map into the screen with a small margin, preserving aspect.
        int margin = 24;
        int availW = this.width - margin * 2;
        int availH = this.height - margin * 2;
        this.mapW = Math.min(availW, availH * 2);
        this.mapH = this.mapW / 2;
        this.mapX = (this.width - this.mapW) / 2;
        this.mapY = (this.height - this.mapH) / 2;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        // The engine already draws the background before this method (Screen#extractRenderState is
        // invoked after extractBackground), so do not call extractBackground again here.
        // The Earth itself: the full texture stretched to the fitted rect (src = whole texture).
        g.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, MAP_TEXTURE,
                mapX, mapY, 0.0F, 0.0F, mapW, mapH, TEX_W, TEX_H, TEX_W, TEX_H);

        drawCities(g);
        drawHome(g);
        drawSites(g);
        drawPlayer(g);

        g.centeredText(this.font, this.title, this.width / 2, mapY - 14, 0xFFE0D0A0);
    }

    private void drawCities(GuiGraphicsExtractor g) {
        for (City city : Cities.all()) {
            marker(g, city.x(), city.z(), 0xFFE8C86A, 2);
        }
    }

    private void drawHome(GuiGraphicsExtractor g) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        String id = player.getData(ModAttachments.HOME_CITY.get());        if (id.isEmpty()) {
            return;
        }
        for (City city : Cities.all()) {
            if (city.id().equals(id)) {
                marker(g, city.x(), city.z(), 0xFF6AE86A, 3);
                return;
            }
        }
    }

    /** Sites are shown only once discovered (codex), per design/22 discovery. */
    private void drawSites(GuiGraphicsExtractor g) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        Set<String> known = player.getData(ModAttachments.LORE.get());        for (Location loc : Locations.all()) {
            if (loc instanceof CityLocation || !known.contains(loc.id())) {
                continue;
            }
            marker(g, Locations.xOf(loc), Locations.zOf(loc), 0xFFB060D0, 2);
        }
    }

    private void drawPlayer(GuiGraphicsExtractor g) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        int px = mapX + worldXToPx(player.getX());
        int py = mapY + worldZToPy(player.getZ());
        g.fill(px - 3, py - 3, px + 3, py + 3, 0xFF101010);
        g.fill(px - 2, py - 2, px + 2, py + 2, 0xFFFFFFFF);
    }

    /** One marker dot at world {@code (x,z)} clamped to the drawn map. */
    private void marker(GuiGraphicsExtractor g, int x, int z, int colour, int size) {
        int px = mapX + worldXToPx(x);
        int py = mapY + worldZToPy(z);
        g.fill(px - size, py - size, px + size, py + size, 0xFF101010);
        g.fill(px - size + 1, py - size + 1, px + size - 1, py + size - 1, colour);
    }

    private int worldXToPx(double worldX) {
        double frac = (worldX + HALF_WIDTH) / (2.0 * HALF_WIDTH);
        return (int) Math.round(Math.clamp(frac, 0.0, 1.0) * mapW);
    }

    private int worldZToPy(double worldZ) {
        double frac = (worldZ + HALF_HEIGHT) / (2.0 * HALF_HEIGHT);
        return (int) Math.round(Math.clamp(frac, 0.0, 1.0) * mapH);
    }
}
