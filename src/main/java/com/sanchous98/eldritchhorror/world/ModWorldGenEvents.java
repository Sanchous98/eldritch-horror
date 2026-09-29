package com.sanchous98.eldritchhorror.world;

import com.sanchous98.eldritchhorror.EldritchHorror;
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
        border.setSize(2.0 * EarthMap.HALF_WIDTH);
        EldritchHorror.LOGGER.info("world border set to {}x{} blocks",
                2L * EarthMap.HALF_WIDTH, 2L * EarthMap.HALF_HEIGHT);
    }

    /**
     * Dev aid: if {@code -Deh.debugCity=<name>} is set, force-load a few chunks around that
     * curated city so city generation can be observed in the log / world without a client.
     */
    @SubscribeEvent
    public static void onServerStarted(net.neoforged.neoforge.event.server.ServerStartedEvent event) {
        maybeRenderCities(event);
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
     * Dev aid: if {@code -Deh.renderCities} is set to {@code true}/{@code all} or a comma list of
     * city names, render those cities to PNG on the server thread (see
     * {@link com.sanchous98.eldritchhorror.core.CityRenderer}). Inert unless the property is set.
     */
    private static void maybeRenderCities(net.neoforged.neoforge.event.server.ServerStartedEvent event) {
        String prop = System.getProperty("eh.renderCities");
        if (prop == null || prop.isBlank()) {
            return;
        }
        java.util.List<String> names = new java.util.ArrayList<>();
        if (!"true".equalsIgnoreCase(prop) && !"all".equalsIgnoreCase(prop)) {
            for (String part : prop.split(",")) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    names.add(trimmed);
                }
            }
        }
        EldritchHorror.LOGGER.info("render: rendering cities {} -> run/render",
                names.isEmpty() ? "ALL" : names);
        var rendered = com.sanchous98.eldritchhorror.core.CityRenderer.render(event.getServer(), names);
        EldritchHorror.LOGGER.info("render: done ({} png city pairs)", rendered.size());
    }
}
