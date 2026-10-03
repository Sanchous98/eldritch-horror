package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.corruption.CorruptionSystem;
import com.sanchous98.eldritchhorror.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jspecify.annotations.Nullable;

/**
 * Bounded spawner for {@link RisenHusk}. Vanilla mobs are removed ({@code world/MobSuppressor}), and
 * mod entities are always allowed by that suppressor (namespace check), so this pass is what makes
 * the husk appear in the world. It mirrors the established {@code world/CityPopulation} /
 * {@code corruption/TaintWorld} shape: server-authoritative, overworld-only, and it only ever
 * touches already-loaded chunks.
 *
 * <p>Every {@code RISEN_HUSK_SPAWN_INTERVAL_TICKS} (config; default 10 s) each player is topped up
 * towards a per-player cap. Husks spawn on a valid surface spot in the dark (vanilla monster
 * darkness test) or in a chunk whose taint is above the configured spread threshold — the
 * "dark/tainted/wild" attachment the design asks for. Position sampling is deterministic
 * ({@link RandomSource} seeded from the player id and the server tick; no {@code Math.random}).
 * Counts, radii and caps are all config-bounded.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class RisenHuskSpawner {

    /** Candidate attempts per desired spawn; unsuitable columns are simply retried. */
    private static final int PROBE_FACTOR = 16;

    private RisenHuskSpawner() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!ModConfig.ENABLE_RISEN_HUSK_SPAWNS.get()) {
            return;
        }
        int interval = ModConfig.RISEN_HUSK_SPAWN_INTERVAL_TICKS.get();
        if (event.getServer().getTickCount() % interval != 0) {
            return;
        }
        ServerLevel level = event.getServer().getLevel(Level.OVERWORLD);
        if (level == null) {
            return;
        }
        int perPass = ModConfig.RISEN_HUSK_SPAWN_COUNT.get();
        if (perPass <= 0) {
            return;
        }
        ServerChunkCache cache = level.getChunkSource();
        int tick = event.getServer().getTickCount();
        for (ServerPlayer player : level.players()) {
            spawnNear(level, cache, player, perPass, tick);
        }
    }

    /** Tops the player up towards the cap with up to {@code perPass} husks on valid loaded ground. */
    private static void spawnNear(ServerLevel level, ServerChunkCache cache, ServerPlayer player,
                                  int perPass, int tick) {
        int radius = ModConfig.RISEN_HUSK_SPAWN_RADIUS.get();
        int cap = ModConfig.RISEN_HUSK_SPAWN_CAP.get();
        if (cap <= 0) {
            return;
        }
        BlockPos origin = player.blockPosition();
        AABB box = player.getBoundingBox().inflate(radius);
        // getEntities never force-loads; only husks already in loaded chunks are counted.
        int present = level.getEntitiesOfClass(RisenHusk.class, box, husk -> husk.isAlive()).size();
        int budget = Math.min(perPass, cap - present);
        if (budget <= 0) {
            return;
        }

        RandomSource random = RandomSource.create(player.getUUID().getMostSignificantBits() ^ tick * 0x9E3779B97F4A7C15L);
        int spawned = 0;
        int attempts = budget * PROBE_FACTOR;
        for (int i = 0; i < attempts && spawned < budget; i++) {
            int x = origin.getX() + random.nextInt(radius * 2 + 1) - radius;
            int z = origin.getZ() + random.nextInt(radius * 2 + 1) - radius;
            LevelChunk chunk = cache.getChunkNow(x >> 4, z >> 4);
            if (chunk == null) {
                continue; // never load/generate a chunk just to spawn
            }
            boolean tainted = CorruptionSystem.getTaint(chunk) >= ModConfig.TAINT_SPREAD_THRESHOLD.get();
            BlockPos spot = surfaceSpot(level, x, z, tainted);
            if (spot == null) {
                continue;
            }
            // Context gate: dark (vanilla monster rules) or a tainted chunk.
            boolean dark = Monster.isDarkEnoughToSpawn(level, spot, random);
            if (!dark && !tainted) {
                continue;
            }
            RisenHusk husk = ModEntities.RISEN_HUSK.get()
                    .spawn(level, spot, EntitySpawnReason.EVENT);
            if (husk == null) {
                continue;
            }
            husk.setPersistenceRequired(); // finalizeSpawn already does; keep it explicit and safe
            spawned++;
        }
    }

    /**
     * A valid standing spot at {@code (x,z)}: ground below, two air blocks of body space. On open
     * surface it must see the sky; if {@code allowUnderground} (a tainted chunk) it may also be a
     * cave/indoor floor, so a tainted area genuinely spawns husks in the dark.
     */
    private static @Nullable BlockPos surfaceSpot(ServerLevel level, int x, int z, boolean allowUnderground) {
        if (!level.hasChunkAt(x, z)) {
            return null;
        }
        int y = level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
        BlockPos pos = new BlockPos(x, y, z);
        if (!level.isLoaded(pos)) {
            return null;
        }
        BlockState below = level.getBlockState(pos.below());
        boolean ground = below.isSolidRender()
                || below.getBlock() instanceof net.minecraft.world.level.block.PathBlock;
        if (!ground || !below.getFluidState().isEmpty()) {
            return null;
        }
        if (!level.getBlockState(pos).isAir() || !level.getBlockState(pos.above()).isAir()) {
            return null;
        }
        if (!allowUnderground && !level.canSeeSky(pos)) {
            return null;
        }
        return pos;
    }
}
