package com.sanchous98.eldritchhorror.world;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.world.city.Cities;
import com.sanchous98.eldritchhorror.world.city.City;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Actively populates the curated city districts with villagers so their streets feel busy. These
 * cities are custom structures on Earth terrain — vanilla village spawning never applies to them —
 * so without this pass the only allow-listed vanilla mob would never actually appear.
 *
 * <p>Server-authoritative and overworld-only. Roughly every {@link #INTERVAL_TICKS} (5 s) each
 * curated city near a player is topped up towards a population-scaled target. Everything is
 * bounded: only cities whose district reaches a player are considered, only already-loaded chunks
 * are ever touched (via {@link ServerChunkCache#getChunkNow}, so nothing is force-generated), the
 * spawn count per city per pass is capped by config, and the villagers are made persistent so they
 * never despawn once placed.
 *
 * <p>Placement is deterministic (a per-city seed mixed with the server tick; no {@code Math.random})
 * and only lands on the paved street surface: a hard, non-liquid block below with two blocks of
 * air above and open sky, which keeps villagers off water and out of enclosed building interiors.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class CityPopulation {

    /** Populate about every 5 seconds. */
    private static final int INTERVAL_TICKS = 100;

    /** Extra blocks beyond the district edge within which a city still counts as "near" a player. */
    private static final int MARGIN = 32;

    /** Target floor/ceiling: even a village-sized city gets a small crowd, a metropolis stays sane. */
    private static final int MIN_TARGET = 8;
    private static final int MAX_TARGET = 60;

    /** District half-extent bounds, mirrored from {@code CityLocation} ({@code clamp(…, 220, 380)}). */
    private static final int MIN_DISTRICT = 220;
    private static final int MAX_DISTRICT = 380;

    /** Candidate attempts per desired villager; unloaded/unsuitable columns are simply retried. */
    private static final int PROBE_FACTOR = 12;

    /** Deterministic profession pool; {@link VillagerType} is resolved from the biome at spawn. */
    private static final List<ResourceKey<VillagerProfession>> PROFESSIONS = List.of(
            VillagerProfession.NONE,
            VillagerProfession.FARMER,
            VillagerProfession.FISHERMAN,
            VillagerProfession.SHEPHERD,
            VillagerProfession.BUTCHER,
            VillagerProfession.CARTOGRAPHER,
            VillagerProfession.CLERIC,
            VillagerProfession.ARMORER,
            VillagerProfession.WEAPONSMITH,
            VillagerProfession.TOOLSMITH,
            VillagerProfession.LIBRARIAN,
            VillagerProfession.MASON,
            VillagerProfession.LEATHERWORKER,
            VillagerProfession.NITWIT);

    private CityPopulation() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!ModConfig.ENABLE_CITY_POPULATION.get()) {
            return;
        }
        if (event.getServer().getTickCount() % INTERVAL_TICKS != 0) {
            return;
        }
        ServerLevel level = event.getServer().getLevel(Level.OVERWORLD);
        if (level == null) {
            return;
        }
        int perPass = ModConfig.CITY_POPULATION_PER_TICK.get();
        if (perPass <= 0) {
            return;
        }
        List<City> cities = Cities.all();
        if (cities.isEmpty()) {
            return;
        }
        int density = ModConfig.CITY_POPULATION_DENSITY.get();
        int tick = event.getServer().getTickCount();
        ServerChunkCache cache = level.getChunkSource();
        // One city may be near several players; populate each once per pass.
        Set<City> done = new HashSet<>();
        for (ServerPlayer player : level.players()) {
            for (City city : cities) {
                if (done.contains(city) || !near(player, city)) {
                    continue;
                }
                done.add(city);
                populate(level, cache, city, perPass, density, tick);
            }
        }
    }

    /** True if {@code player} is within the city's district plus {@link #MARGIN} blocks. */
    private static boolean near(ServerPlayer player, City city) {
        int reach = district(city) + MARGIN;
        long dx = player.blockPosition().getX() - (long) city.x();
        long dz = player.blockPosition().getZ() - (long) city.z();
        return dx * dx + dz * dz <= (long) reach * reach;
    }

    /**
     * Tops {@code city} up towards its target: counts the villagers already in the district and,
     * if short, spawns up to the per-pass cap on valid loaded ground.
     */
    private static void populate(ServerLevel level, ServerChunkCache cache, City city,
                                 int perPass, int density, int tick) {
        int district = district(city);
        int target = target(city, density);

        // Counts only villagers in already-loaded chunks (getEntities never force-loads).
        AABB box = new AABB(city.x() - district, level.getMinY(), city.z() - district,
                city.x() + district, level.getMaxY(), city.z() + district);
        int need = target - level.getEntities(EntityTypes.VILLAGER, box, e -> true).size();
        int budget = Math.min(perPass, need);
        if (budget <= 0) {
            return;
        }

        // Deterministic per city and pass; no Math.random.
        long key = ((long) city.x() * 0x9E3779B97F4A7C15L) ^ ((long) city.z() * 0xC2B2AE3D27D4EB4FL);
        RandomSource random = RandomSource.create(key ^ tick);
        // Sample across the whole district (its chunks span district/16 each way).
        int chunkReach = Math.max(1, district >> 4);
        long r2 = (long) district * district;
        int spawned = 0;
        int attempts = budget * PROBE_FACTOR;
        for (int i = 0; i < attempts && spawned < budget; i++) {
            int cx = (city.x() >> 4) + random.nextInt(2 * chunkReach + 1) - chunkReach;
            int cz = (city.z() >> 4) + random.nextInt(2 * chunkReach + 1) - chunkReach;
            LevelChunk chunk = cache.getChunkNow(cx, cz);
            if (chunk == null) {
                continue; // never load/generate a chunk just to populate it
            }
            int x = (cx << 4) + random.nextInt(16);
            int z = (cz << 4) + random.nextInt(16);
            long dx = x - (long) city.x();
            long dz = z - (long) city.z();
            if (dx * dx + dz * dz > r2) {
                continue;
            }
            BlockPos spot = surfaceSpot(level, x, z);
            if (spot == null) {
                continue;
            }
            Villager villager = EntityTypes.VILLAGER.spawn(level, spot, EntitySpawnReason.STRUCTURE);
            if (villager == null) {
                continue;
            }
            style(villager, level, random);
            spawned++;
        }
    }

    /**
     * The paved street surface at {@code (x,z)}, or {@code null} if the column is unloaded, wet or
     * enclosed. Requires a hard ground block below, two air blocks at the spawn and open sky, so a
     * villager lands on the street rather than in water or inside a building's walled interior.
     */
    private static BlockPos surfaceSpot(ServerLevel level, int x, int z) {
        if (!level.hasChunkAt(x, z)) {
            return null;
        }
        int y = level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
        BlockPos pos = new BlockPos(x, y, z);
        if (!level.isLoaded(pos)) {
            return null;
        }
        BlockState below = level.getBlockState(pos.below());
        // Accept dirt_path too: PathBlock is 15/16 high, so isSolidRender() is false, yet it is
        // walkable ground (Nairobi's palette uses it for its streets).
        boolean ground = below.isSolidRender() || below.getBlock() instanceof net.minecraft.world.level.block.PathBlock;
        if (!ground || !below.getFluidState().isEmpty()) {
            return null; // no water, ice, leaves, fences or other soft cover beneath
        }
        if (!level.getBlockState(pos).isAir() || !level.getBlockState(pos.above()).isAir()) {
            return null; // a wall, crop or prop occupies the two-block body space
        }
        if (!level.canSeeSky(pos)) {
            return null; // indoors / walled courtyard: keep the crowd on the open streets
        }
        return pos;
    }

    /**
     * Gives a spawned villager a deterministic profession (varied crowd) and a biome-appropriate
     * type, then makes it persistent so it never despawns.
     */
    private static void style(Villager villager, ServerLevel level, RandomSource random) {
        Holder<VillagerProfession> profession =
                level.registryAccess().getOrThrow(PROFESSIONS.get(random.nextInt(PROFESSIONS.size())));
        Holder<VillagerType> type = level.registryAccess()
                .getOrThrow(VillagerType.byBiome(level.getBiome(villager.blockPosition())));
        villager.setVillagerData(villager.getVillagerData().withType(type).withProfession(profession));
        villager.setVillagerDataFinalized(true);
        villager.setPersistenceRequired();
    }

    /** Target villagers for a city: {@code clamp(population / density, MIN_TARGET, MAX_TARGET)}. */
    private static int target(City city, int density) {
        return Math.clamp(city.population() / Math.max(1, density), MIN_TARGET, MAX_TARGET);
    }

    /** The built district half-extent, matching {@code CityLocation#renderRadius}. */
    private static int district(City city) {
        int d = (int) Math.round(220.0 + Math.sqrt(Math.max(city.population(), 1)) / 40.0);
        return Math.clamp(d, MIN_DISTRICT, MAX_DISTRICT);
    }
}
