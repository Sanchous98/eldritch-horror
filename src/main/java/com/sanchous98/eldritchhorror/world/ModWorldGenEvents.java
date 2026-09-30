package com.sanchous98.eldritchhorror.world;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.world.loc.Location;
import com.sanchous98.eldritchhorror.world.loc.Locations;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

/**
 * Server-side worldgen wiring. Keeps the Earth map server-authoritative: the biome registry is
 * captured here, before any chunk is generated.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class ModWorldGenEvents {

    /** Extra border room past the longitude wrap line so the seam warp fires first. */
    private static final int SEAM_MARGIN = 64;

    private ModWorldGenEvents() {
    }

    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        HolderLookup.Provider registries = event.getServer().registryAccess();
        HolderLookup<Biome> biomes = registries.lookupOrThrow(Registries.BIOME);
        EarthBiomeSource.setRegistry(biomes);
        // Load the map now so a missing/broken asset fails loudly on startup, not mid-chunk.
        EarthMaps.get();
        EldritchHorror.LOGGER.info("Earth world generator ready: {}x{} map at {} blocks/pixel",
                EarthMaps.PIXEL_WIDTH, EarthMaps.PIXEL_WIDTH / 2, EarthMap.BLOCKS_PER_PIXEL);
    }

    /**
     * Match the world border to the 2:1 map extent (Overworld only). This must run after the
     * server's levels exist — {@link ServerAboutToStartEvent} is too early ({@code overworld()}
     * is still {@code null} there).
     */
    @SubscribeEvent
    public static void onServerStartedSetBorder(
            net.neoforged.neoforge.event.server.ServerStartedEvent event) {
        var border = event.getServer().overworld().getWorldBorder();
        border.setCenter(0.0, 0.0);
        // Give the longitude seam a little margin *past* the wrap line so the seam warp in
        // BoundaryTravel fires before the border can stop the player. The world is a cylinder
        // east–west; the polar ends are bounded by Morok, not by a visible wall.
        border.setSize(2.0 * (EarthMap.HALF_WIDTH + SEAM_MARGIN));
        EldritchHorror.LOGGER.info("world border: X wraps at ±{} (warp), Morok past ±{} (no wall)",
                EarthMap.HALF_WIDTH, EarthMap.HALF_HEIGHT);
    }

    /**
     * Dev aid: if {@code -Deh.debugCity=<name>} is set, force-load a few chunks around that
     * curated city so city generation can be observed in the log / world without a client.
     */
    @SubscribeEvent
    public static void onServerStarted(net.neoforged.neoforge.event.server.ServerStartedEvent event) {
        maybeRenderCities(event);
        maybeForceload(event);
        String city = System.getProperty("eh.debugCity");
        if (city == null || city.isBlank()) {
            return;
        }
        var server = event.getServer();
        for (var c : com.sanchous98.eldritchhorror.world.city.Cities.all()) {
            if (!c.name().equalsIgnoreCase(city)) {
                continue;
            }
            int r = c.radius() + 16;
            var src = server.createCommandSourceStack();
            server.getCommands().performPrefixedCommand(src,
                    "forceload add " + (c.x() - r) + " " + (c.z() - r) + " "
                            + (c.x() + r) + " " + (c.z() + r));
            EldritchHorror.LOGGER.info("debug: forceloaded {} at ({}, {}) r={}", c.name(), c.x(), c.z(), r);
            return;
        }
        EldritchHorror.LOGGER.warn("debug: city '{}' not found", city);
    }

    /**
     * Dev aid: if {@code -Deh.renderCities} is set, schedule the server-side PNG renderer (see
     * {@link com.sanchous98.eldritchhorror.core.CityRenderer}). The value is {@code true}/{@code all}
     * for every city, a comma list of city names, or {@code batch=I/N} (equivalently {@code I/N})
     * to render only slice I of N — the renderer then walks one or two cities per tick so it can
     * never trip the server watchdog.
     *
     * <p>{@code -Deh.renderExit=true} shuts the server down when the selected slice is finished
     * (handy for a one-shot render run).
     */
    /**
     * Dev aid: {@code -Deh.forceload=name:radius,...} force-loads a disc around each named fixed
     * location so its generation can be exercised (and logged) without a client.
     */
    private static void maybeForceload(net.neoforged.neoforge.event.server.ServerStartedEvent event) {
        String prop = System.getProperty("eh.forceload");
        if (prop == null || prop.isBlank()) {
            return;
        }
        var src = event.getServer().createCommandSourceStack();
        for (String part : prop.split(",")) {
            String[] kv = part.split(":");
            String name = kv[0].trim();
            int r = kv.length > 1 ? Integer.parseInt(kv[1].trim()) : 96;
            boolean hit = false;
            for (Location loc : Locations.byId(name)) {
                int x = Locations.xOf(loc);
                int z = Locations.zOf(loc);
                event.getServer().getCommands().performPrefixedCommand(src,
                        "forceload add " + (x - r) + " " + (z - r) + " " + (x + r) + " " + (z + r));
                EldritchHorror.LOGGER.info("forceload: {} at ({}, {}) r={}", loc.id(), x, z, r);
                hit = true;
            }
            if (!hit) {
                EldritchHorror.LOGGER.warn("forceload: '{}' not found", name);
            }
        }
    }

    private static void maybeRenderCities(net.neoforged.neoforge.event.server.ServerStartedEvent event) {
        String prop = System.getProperty("eh.renderCities");
        if (prop == null || prop.isBlank()) {
            return;
        }
        boolean exit = Boolean.parseBoolean(System.getProperty("eh.renderExit", "false"));
        EldritchHorror.LOGGER.info("render: starting render session '{}' -> run/render (exitWhenDone={})",
                prop, exit);
        int count = com.sanchous98.eldritchhorror.core.CityRenderer.begin(
                event.getServer(), prop, exit);
        if (count == 0) {
            EldritchHorror.LOGGER.warn("render: no cities selected for '{}'", prop);
        }
    }
}
