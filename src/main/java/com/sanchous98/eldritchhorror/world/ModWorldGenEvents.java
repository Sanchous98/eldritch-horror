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
        EldritchHorror.LOGGER.info("Earth world generator ready: {}x{} map at 2 blocks/pixel",
                EarthMaps.PIXEL_WIDTH, EarthMaps.PIXEL_WIDTH / 2);
    }

    /**
     * Dev aid: if {@code -Deh.debugCity=<name>} is set, force-load a few chunks around that
     * curated city so city generation can be observed in the log / world without a client.
     */
    @SubscribeEvent
    public static void onServerStarted(net.neoforged.neoforge.event.server.ServerStartedEvent event) {
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
}
