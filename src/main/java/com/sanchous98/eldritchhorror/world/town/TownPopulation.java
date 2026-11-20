package com.sanchous98.eldritchhorror.world.town;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.corruption.CorruptionSystem;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PathBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jspecify.annotations.Nullable;

/**
 * Gives the generic towns a population: vanilla villagers with vanilla trades, topped up in small
 * numbers around the plaza and houses.
 *
 * <p>Server-authoritative and overworld-only, modelled on {@code world.CityPopulation}. Every
 * {@code townPopulationIntervalTicks} (default 200 = 10 s) each town whose centre lies within
 * {@value TownPopulationMath#NEAR_PLAYER} blocks of a player is topped up towards
 * {@code townVillagerCap} (default 5), {@value TownPopulationMath#PER_PASS} per pass. Everything
 * is bounded: only loaded chunks are touched (the centre chunk is read with
 * {@code getChunkNow}, so nothing is force-loaded or generated), the per-pass budget is small,
 * and each villager is persistent.
 *
 * <p>Placement is deterministic — a hash of the town id and the pass, never a shared
 * {@code RandomSource} — and only lands on open surface ground, so villagers stay out of the
 * well, the pond and the houses. A town whose centre chunk is heavily tainted reads as abandoned:
 * no new villagers are spawned (see {@link TownPopulationMath#abandoned}).
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class TownPopulation {

    private TownPopulation() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!ModConfig.ENABLE_TOWN_POPULATION.get() || !ModConfig.ENABLE_TOWNS.get()) {
            return;
        }
        int interval = ModConfig.TOWN_POPULATION_INTERVAL_TICKS.get();
        int tick = event.getServer().getTickCount();
        if (!TownPopulationMath.due(tick, interval)) {
            return;
        }
        int cap = ModConfig.TOWN_VILLAGER_CAP.get();
        if (cap <= 0) {
            return;
        }
        ServerLevel level = event.getServer().getLevel(Level.OVERWORLD);
        if (level == null) {
            return;
        }
        double abandonTaint = ModConfig.TOWN_ABANDON_TAINT.get();
        Set<Town> done = new HashSet<>();
        for (ServerPlayer player : level.players()) {
            for (Town town : Towns.index().near(player.getBlockX(), player.getBlockZ(),
                    TownPopulationMath.NEAR_PLAYER)) {
                if (done.add(town)) {
                    populate(level, town, cap, abandonTaint, tick);
                }
            }
        }
    }

    /**
     * Tops one town up towards the cap. Returns early when the centre chunk is not loaded (never
     * force-load a town just to populate it) or when the centre is tainted enough that the town
     * has been abandoned.
     */
    private static void populate(ServerLevel level, Town town, int cap, double abandonTaint,
                                 int tick) {
        LevelChunk centre = level.getChunkSource().getChunkNow(town.x() >> 4, town.z() >> 4);
        if (centre == null) {
            return;
        }
        if (TownPopulationMath.abandoned(CorruptionSystem.getTaint(centre), abandonTaint)) {
            return;
        }
        int radius = TownPopulationMath.COUNT_RADIUS;
        AABB box = new AABB(town.x() - radius, level.getMinY(), town.z() - radius,
                town.x() + radius, level.getMaxY(), town.z() + radius);
        int present = level.getEntities(EntityTypes.VILLAGER, box, entity -> true).size();
        int budget = TownPopulationMath.budget(present, cap, TownPopulationMath.PER_PASS);
        if (budget <= 0) {
            return;
        }

        int footprint = new TownLocation(town).radius();
        int spawnSeed = town.id().hashCode() ^ tick;
        int tradeSeed = town.id().hashCode();
        int spawned = 0;
        int attempts = budget * TownPopulationMath.PROBE_FACTOR;
        for (int i = 0; i < attempts && spawned < budget; i++) {
            int[] offset = TownPopulationMath.spotOffset(spawnSeed, i, footprint);
            int x = town.x() + offset[0];
            int z = town.z() + offset[1];
            BlockPos spot = surfaceSpot(level, x, z);
            if (spot == null) {
                continue;
            }
            Villager villager = EntityTypes.VILLAGER.spawn(level, spot, EntitySpawnReason.STRUCTURE);
            if (villager == null) {
                continue;
            }
            // The trade seed is stable per town, so the first five villagers (ordinals 0..4) are
            // guaranteed one of each trade; the spawning pass itself still varies with the tick.
            style(villager, level, TownPopulationMath.profession(tradeSeed, present + spawned));
            spawned++;
        }
    }

    /**
     * Open, dry, walkable ground at {@code (x,z)}, or {@code null}. Requires a loaded column, a
     * hard block below, two air blocks and open sky, so a villager never lands inside a house or a
     * wall, on water, or on a fence.
     */
    private static @Nullable BlockPos surfaceSpot(ServerLevel level, int x, int z) {
        if (!level.hasChunkAt(x, z)) {
            return null;
        }
        int y = level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
        BlockPos pos = new BlockPos(x, y, z);
        if (!level.isLoaded(pos)) {
            return null;
        }
        BlockState below = level.getBlockState(pos.below());
        // PathBlock is 15/16 high: walkable, but not isSolidRender().
        boolean ground = below.isSolidRender() || below.getBlock() instanceof PathBlock;
        if (!ground || !below.getFluidState().isEmpty()) {
            return null;
        }
        if (!level.getBlockState(pos).isAir() || !level.getBlockState(pos.above()).isAir()) {
            return null;
        }
        if (!level.canSeeSky(pos)) {
            return null;
        }
        return pos;
    }

    /**
     * Gives the villager a town trade and a biome-appropriate type, then makes it persistent so
     * it survives chunk unloads and never despawns.
     *
     * <p>A point of XP is set deliberately: vanilla's {@code ResetProfession} behaviour fires for
     * a jobless villager with zero XP at level 1 and turns it back into {@code none} within a
     * tick. Towns have no workstation blocks, so without this every trade would be erased
     * immediately. One XP does not level the villager.
     */
    private static void style(Villager villager, ServerLevel level, TownProfession trade) {
        Holder<VillagerProfession> holder = level.registryAccess().getOrThrow(key(trade));
        Holder<VillagerType> type = level.registryAccess()
                .getOrThrow(VillagerType.byBiome(level.getBiome(villager.blockPosition())));
        villager.setVillagerData(villager.getVillagerData().withType(type).withProfession(holder));
        villager.setVillagerDataFinalized(true);
        villager.setVillagerXp(1);
        villager.setPersistenceRequired();
    }

    /** Maps the pure town trade to its vanilla profession holder. */
    private static ResourceKey<VillagerProfession> key(TownProfession trade) {
        return switch (trade) {
            case FARMER -> VillagerProfession.FARMER;
            case LIBRARIAN -> VillagerProfession.LIBRARIAN;
            case CLERIC -> VillagerProfession.CLERIC;
            case FISHERMAN -> VillagerProfession.FISHERMAN;
            case SHEPHERD -> VillagerProfession.SHEPHERD;
        };
    }
}
