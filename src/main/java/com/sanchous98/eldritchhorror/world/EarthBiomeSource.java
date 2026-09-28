package com.sanchous98.eldritchhorror.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

/**
 * Biome source for the Earth map: picks a vanilla biome from the baked Köppen layer, with an
 * ocean/coast override.
 *
 * <p>The biome ids live in the codec (so the level stem decodes without a registry). The biome
 * registry itself is supplied by {@link #setRegistry(HolderLookup)} once the server has loaded
 * its registries (see {@code ModWorldGen}); {@code possibleBiomes()} is memoised by the base
 * class on first use, which happens after that.
 */
public class EarthBiomeSource extends BiomeSource {

    public static final MapCodec<EarthBiomeSource> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.listOf().fieldOf("biomes").forGetter(EarthBiomeSource::biomeIds)
    ).apply(i, EarthBiomeSource::new));

    private static volatile HolderLookup<Biome> registry;

    private final List<String> biomeIds;

    public EarthBiomeSource(List<String> biomeIds) {
        this.biomeIds = List.copyOf(biomeIds);
    }

    /** Called once by the server when registries are available. */
    public static void setRegistry(HolderLookup<Biome> lookup) {
        registry = lookup;
    }

    public List<String> biomeIds() {
        return this.biomeIds;
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        HolderLookup<Biome> lookup = registry;
        if (lookup == null) {
            return Stream.empty();
        }
        return this.biomeIds.stream().map(id -> lookup.getOrThrow(key(id)));
    }

    @Override
    public BiomeResolver createResolver(Climate.Sampler sampler) {
        return this::getNoiseBiome;
    }

    /** @param quartX/quartY/quartZ quart coordinates (block / 4). */
    public Holder<Biome> getNoiseBiome(int quartX, int quartY, int quartZ) {
        HolderLookup<Biome> lookup = registry;
        if (lookup == null) {
            throw new IllegalStateException("EarthBiomeSource used before the biome registry was set");
        }
        int worldX = QuartPos.toBlock(quartX) + 8;
        int worldZ = QuartPos.toBlock(quartZ) + 8;
        EarthMap earth = EarthMaps.get();
        boolean land = earth.isLand(worldX, worldZ);
        int koppen = earth.koppenClass(worldX, worldZ);
        int depth = land ? 0
                : (int) Math.max(0, ElevationCurve.SEA_LEVEL
                        - ElevationCurve.toY(earth.elevationMetres(worldX, worldZ)));
        return lookup.getOrThrow(key(BiomeTable.forColumn(koppen, land, depth)));
    }

    private static ResourceKey<Biome> key(String id) {
        return ResourceKey.create(Registries.BIOME, Identifier.parse(id));
    }
}
