package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.corruption.CorruptionAPI;
import com.sanchous98.eldritchhorror.corruption.CorruptionSystem;
import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.sanity.SanityAPI;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.block.PathBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

/**
 * Shared, bounded support for the bestiary: a deterministic per-player spawner and the
 * {@link DreadAura} tick. Both mirror the risen husk's rules exactly —
 * server-authoritative, overworld-only, deterministic {@link RandomSource} seeded from the player
 * id and server tick (never {@code Math.random}), and only ever touching already-loaded chunks
 * ({@code getChunkNow} / {@code getEntitiesOfClass}, which never force-load or generate).
 *
 * <p>Generalises {@link RisenHuskSpawner} so each new mob supplies only its gate, caps and config;
 * no duplicated scan loop.
 */
public final class BestiarySupport {

    /** Candidate attempts per desired spawn; unsuitable columns are simply retried. */
    public static final int PROBE_FACTOR = 16;

    /** How far above the terrain surface a flying spawn may appear. */
    public static final int AIR_CLEARANCE = 4;

    private BestiarySupport() {
    }

    /** Per-mob spawn context gate: dark/tainted, or family-specific (e.g. taint only). */
    @FunctionalInterface
    public interface SpawnGate {
        boolean allows(ServerLevel level, BlockPos spot, boolean dark, boolean tainted);
    }

    /** Air-spawn gate for flying mobs: the spot is above the terrain, so only darkness applies. */
    @FunctionalInterface
    public interface AirGate {
        boolean allows(ServerLevel level, BlockPos spot, boolean dark);
    }

    /**
     * Tops {@code player} up towards {@code cap} with up to {@code perPass} mobs of {@code type} on
     * valid loaded ground, respecting {@code gate}. Counts only already-loaded entities nearby, so
     * it neither force-loads chunks nor double-counts a horde.
     */
    public static <T extends Mob> void topUp(ServerLevel level, ServerChunkCache cache, ServerPlayer player,
                                             EntityType<T> type, Class<T> typeClass,
                                             int radius, int cap, int perPass, int tick,
                                             SpawnGate gate) {
        if (cap <= 0 || perPass <= 0) {
            return;
        }
        BlockPos origin = player.blockPosition();
        AABB box = player.getBoundingBox().inflate(radius);
        int present = level.getEntitiesOfClass(typeClass, box, mob -> mob.isAlive()).size();
        int budget = Math.min(perPass, cap - present);
        if (budget <= 0) {
            return;
        }

        RandomSource random = RandomSource.create(
                player.getUUID().getMostSignificantBits() ^ tick * 0x9E3779B97F4A7C15L);
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
            boolean dark = Monster.isDarkEnoughToSpawn(level, spot, random);
            if (!gate.allows(level, spot, dark, tainted)) {
                continue;
            }
            T mob = type.spawn(level, spot, EntitySpawnReason.EVENT);
            if (mob == null) {
                continue;
            }
            mob.setPersistenceRequired();
            spawned++;
        }
    }

    /**
     * A valid standing spot at {@code (x,z)}: ground below (solid or path), two air blocks of body
     * space and no fluid. On open surface it must see the sky; if {@code allowUnderground} (a
     * tainted chunk) it may also be a cave/indoor floor, so a tainted area genuinely spawns in the
     * dark. Same conservative gate as {@code CityPopulation.surfaceSpot}.
     */
    public static BlockPos surfaceSpot(ServerLevel level, int x, int z, boolean allowUnderground) {
        if (!level.hasChunkAt(x, z)) {
            return null;
        }
        int y = level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
        // On the open surface, use the heightmap column. Tainted chunks may spawn in the dark, so
        // scan downward from the surface for the first valid floor (a cave or indoor ground).
        int lowest = allowUnderground ? level.getMinY() + 1 : y;
        for (int cy = y; cy >= lowest; cy--) {
            BlockPos candidate = new BlockPos(x, cy, z);
            if (!level.isLoaded(candidate)) {
                break; // never read unloaded columns
            }
            BlockState below = level.getBlockState(candidate.below());
            boolean ground = below.isSolidRender() || below.getBlock() instanceof PathBlock;
            if (ground && below.getFluidState().isEmpty()
                    && level.getBlockState(candidate).isAir()
                    && level.getBlockState(candidate.above()).isAir()) {
                if (allowUnderground || level.canSeeSky(candidate)) {
                    return candidate;
                }
            }
        }
        return null;
    }

    /**
     * A valid standing spot at {@code (x,z)} in a loaded <b>cave</b>: like {@link #surfaceSpot} but
     * it scans downward from {@code maxY} and only accepts a floor that does not see the sky. Used by
     * the deep-cave presence (Nyogtha); loaded chunks only, never force-loads.
     */
    public static BlockPos undergroundSpot(ServerLevel level, int x, int z, int maxY) {
        if (!level.hasChunkAt(x, z)) {
            return null;
        }
        for (int cy = maxY; cy >= level.getMinY() + 1; cy--) {
            BlockPos candidate = new BlockPos(x, cy, z);
            if (!level.isLoaded(candidate)) {
                break; // never read unloaded columns
            }
            BlockState below = level.getBlockState(candidate.below());
            boolean ground = below.isSolidRender() || below.getBlock() instanceof PathBlock;
            if (ground && below.getFluidState().isEmpty()
                    && level.getBlockState(candidate).isAir()
                    && level.getBlockState(candidate.above()).isAir()
                    && !level.canSeeSky(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * Tops {@code player} up towards {@code cap} with up to {@code perPass} <b>cave-floor</b> mobs of
     * {@code type}, placing only at loaded underground spots at or below {@code maxY}. The third and
     * last shared placement helper; same bounds as {@link #topUp} (server-authoritative, loaded
     * chunks only, deterministic sampling, never force-loads).
     */
    public static <T extends Mob> void topUpUnderground(ServerLevel level, ServerChunkCache cache,
                                                        ServerPlayer player, EntityType<T> type,
                                                        Class<T> typeClass, int radius, int cap,
                                                        int perPass, int maxY, int tick, SpawnGate gate) {
        if (cap <= 0 || perPass <= 0) {
            return;
        }
        BlockPos origin = player.blockPosition();
        AABB box = player.getBoundingBox().inflate(radius);
        int present = level.getEntitiesOfClass(typeClass, box, mob -> mob.isAlive()).size();
        int budget = Math.min(perPass, cap - present);
        if (budget <= 0) {
            return;
        }
        RandomSource random = RandomSource.create(
                player.getUUID().getMostSignificantBits() ^ tick * 0x9E3779B97F4A7C15L);
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
            BlockPos spot = undergroundSpot(level, x, z, maxY);
            if (spot == null) {
                continue;
            }
            boolean dark = Monster.isDarkEnoughToSpawn(level, spot, random);
            if (!gate.allows(level, spot, dark, tainted)) {
                continue;
            }
            T mob = type.spawn(level, spot, EntitySpawnReason.EVENT);
            if (mob == null) {
                continue;
            }
            mob.setPersistenceRequired();
            spawned++;
        }
    }

    /**
     * Tops {@code player} up towards {@code cap} with up to {@code perPass} <b>flying</b> mobs of
     * {@code type}, placed in air above the terrain. The shared loop cannot be reused directly
     * because it only places mobs on the ground, so this is the one extra shared placement helper
     * (no second scan pattern is duplicated per flier). Same contract as {@link #topUp}:
     * server-authoritative, loaded chunks only, deterministic sampling, and it never force-loads.
     * {@code minY} keeps a flier above the deep dark; {@code airGate} receives whether the spot is
     * dark (the only context an air spot has).
     */
    public static <T extends Mob> void topUpAir(ServerLevel level, ServerChunkCache cache, ServerPlayer player,
                                                EntityType<T> type, Class<T> typeClass,
                                                int radius, int cap, int perPass, int minY, int tick,
                                                AirGate airGate) {
        if (cap <= 0 || perPass <= 0) {
            return;
        }
        BlockPos origin = player.blockPosition();
        AABB box = player.getBoundingBox().inflate(radius);
        int present = level.getEntitiesOfClass(typeClass, box, mob -> mob.isAlive()).size();
        int budget = Math.min(perPass, cap - present);
        if (budget <= 0) {
            return;
        }
        RandomSource random = RandomSource.create(
                player.getUUID().getMostSignificantBits() ^ tick * 0x9E3779B97F4A7C15L);
        int spawned = 0;
        int attempts = budget * PROBE_FACTOR;
        for (int i = 0; i < attempts && spawned < budget; i++) {
            int x = origin.getX() + random.nextInt(radius * 2 + 1) - radius;
            int z = origin.getZ() + random.nextInt(radius * 2 + 1) - radius;
            if (cache.getChunkNow(x >> 4, z >> 4) == null) {
                continue; // never load/generate a chunk just to spawn
            }
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z) + AIR_CLEARANCE;
            BlockPos spot = new BlockPos(x, y, z);
            if (y < minY || !level.isLoaded(spot)
                    || !level.getBlockState(spot).isAir()
                    || !level.getBlockState(spot.above()).isAir()) {
                continue;
            }
            if (!airGate.allows(level, spot, Monster.isDarkEnoughToSpawn(level, spot, random))) {
                continue;
            }
            T mob = type.spawn(level, spot, EntitySpawnReason.EVENT);
            if (mob == null) {
                continue;
            }
            mob.setPersistenceRequired();
            spawned++;
        }
    }

    /**
     * Whether any water sits within a few blocks of {@code spot} (all three axes). Used as a spawn
     * gate so a coastal mob appears at the tide line and not inland. Loaded chunks only.
     */
    public static boolean nearWater(ServerLevel level, BlockPos spot) {
        BlockPos.MutableBlockPos probe = new BlockPos.MutableBlockPos();
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                for (int dy = -2; dy <= 0; dy++) {
                    probe.set(spot.getX() + dx, spot.getY() + dy, spot.getZ() + dz);
                    if (level.isLoaded(probe) && level.getFluidState(probe).is(FluidTags.WATER)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * The single dread-aura pass for one player: counts up to each aura's own {@code auraMaxStack}
     * among loaded mobs within their {@code auraRadius} (bounded by {@code queryRadius}) and writes
     * the summed rate through the public {@code SanityAPI}/{@code CorruptionAPI} facade.
     */
    public static <T extends Mob & DreadAura> void tickAura(ServerPlayer player, Class<T> type,
                                                            double queryRadius) {
        if (queryRadius <= 0.0) {
            return;
        }
        AABB box = player.getBoundingBox().inflate(queryRadius);
        List<T> auras = player.level().getEntitiesOfClass(type, box,
                aura -> aura.isAlive() && aura.isAuraActive()
                        && aura.distanceToSqr(player) <= aura.auraRadius() * aura.auraRadius()
                        && (!aura.auraRequiresLineOfSight() || aura.hasLineOfSight(player)));
        if (auras.isEmpty()) {
            return;
        }
        int max = Math.max(auras.get(0).auraMaxStack(), 0);
        int count = Math.min(auras.size(), max);
        if (count <= 0) {
            return;
        }
        double total = 0.0;
        for (int i = 0; i < count; i++) {
            total += auras.get(i).auraRateFor(player);
        }
        if (total == 0.0) {
            return;
        }
        switch (auras.get(0).auraAxis()) {
            case SANITY -> SanityAPI.add(player, total);
            case CORRUPTION -> CorruptionAPI.add(player, total);
        }
    }

    /**
     * Per-entity taint-conversion cooldown, stored in the entity's persistent data. Kept as a string
     * (not a field) so the mundane animals need no per-class save/load boilerplate.
     */
    public static final String TAINT_CONVERT_AT = "eldritch_horror:taint_convert_at";

    /**
     * Whether {@code entity} stands in a loaded chunk whose taint is at or above the spread
     * threshold. Loaded chunks only; never force-loads.
     */
    public static boolean tainted(net.minecraft.world.entity.Entity entity) {
        if (!(entity.level() instanceof ServerLevel level)) {
            return false;
        }
        BlockPos pos = entity.blockPosition();
        LevelChunk chunk = level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
        return chunk != null && CorruptionSystem.getTaint(chunk) >= ModConfig.TAINT_SPREAD_THRESHOLD.get();
    }

    /**
     * The single bounded <b>"turns tainted"</b> conversion shared by the taintable passives
     * ({@code mire_sow}, {@code hill_hound}, {@code bog_bear}). When the animal stands in a loaded
     * chunk whose taint is at or above the spread threshold and its per-entity cooldown has elapsed,
     * it is replaced in place by exactly one {@code taintedType} with the configured chance. Uses
     * {@link RandomSource} off the animal (never {@code Math.random}), touches loaded chunks only,
     * and is cooldown-limited so a herd cannot flip wholesale in one pass.
     *
     * @param animal       the mundane animal to convert (server-side)
     * @param taintedType  the replacement type, e.g. {@code ModEntities.TAINTED_FAUNA}
     * @param enabled      config master switch for conversions
     * @param chance       per-check chance in {@code [0,1]}
     * @param cooldownTicks ticks before this same animal may be checked again
     * @param tick         current game tick (from the server tick event)
     * @return whether the animal was converted
     */
    public static <T extends Mob> boolean maybeTaintConvert(Animal animal, EntityType<T> taintedType,
                                                            boolean enabled, double chance,
                                                            int cooldownTicks, int tick) {
        if (!enabled || animal.level().isClientSide() || !animal.isAlive()) {
            return false;
        }
        if (!(animal.level() instanceof ServerLevel level)) {
            return false;
        }
        BlockPos pos = animal.blockPosition();
        LevelChunk chunk = level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
        if (chunk == null) {
            return false; // never force-load a chunk just to convert
        }
        if (CorruptionSystem.getTaint(chunk) < ModConfig.TAINT_SPREAD_THRESHOLD.get()) {
            return false;
        }
        CompoundTag data = animal.getPersistentData();
        long next = data.getLongOr(TAINT_CONVERT_AT, 0L);
        if (tick < next) {
            return false;
        }
        data.putLong(TAINT_CONVERT_AT, (long) tick + Math.max(0, cooldownTicks));
        if (animal.getRandom().nextDouble() >= chance) {
            return false;
        }
        T converted = animal.convertTo(taintedType, ConversionParams.single(animal, false, false),
                mob -> mob.setPersistenceRequired());
        return converted != null;
    }
}
