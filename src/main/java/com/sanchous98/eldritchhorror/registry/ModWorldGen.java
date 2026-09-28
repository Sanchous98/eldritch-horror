package com.sanchous98.eldritchhorror.registry;

import com.mojang.serialization.MapCodec;
import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.world.EarthBiomeSource;
import com.sanchous98.eldritchhorror.world.EarthChunkGenerator;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Worldgen type registrations. These registries hold {@link MapCodec}s (the serialisers for
 * generator/biome-source <em>types</em>), not instances — instances are created from the
 * datapack dimension JSON.
 */
public final class ModWorldGen {

    public static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS =
            DeferredRegister.create(Registries.CHUNK_GENERATOR, EldritchHorror.MODID);

    public static final DeferredRegister<MapCodec<? extends BiomeSource>> BIOME_SOURCES =
            DeferredRegister.create(Registries.BIOME_SOURCE, EldritchHorror.MODID);

    public static final DeferredHolder<MapCodec<? extends ChunkGenerator>, MapCodec<EarthChunkGenerator>>
            EARTH_GENERATOR = CHUNK_GENERATORS.register("earth", () -> EarthChunkGenerator.CODEC);

    public static final DeferredHolder<MapCodec<? extends BiomeSource>, MapCodec<EarthBiomeSource>>
            EARTH_BIOMES = BIOME_SOURCES.register("earth", () -> EarthBiomeSource.CODEC);

    private ModWorldGen() {
    }
}
