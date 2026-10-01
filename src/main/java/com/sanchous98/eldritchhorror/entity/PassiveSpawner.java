package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.registry.ModEntities;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Bounded ambient spawner for the mundane passive roster (and the two odd ones: {@code spore_bee},
 * {@code stone_sentinel}). One shared pass reuses {@link BestiarySupport#topUp}; each mob supplies
 * only its gate and config, so there is no per-mob scan loop. Gates are biome/terrain-appropriate:
 * clean ground for the mundane herd, near-water for the shore/marsh grazers, tainted ground only
 * for the spore bee. {@code stone_sentinel} is off by default (Order-built; vaults/sites populate it
 * later, and the egg/command work now). Loaded chunks only, deterministic, per-player capped.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class PassiveSpawner {

    private PassiveSpawner() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        int interval = ModConfig.PASSIVE_SPAWN_INTERVAL_TICKS.get();
        if (interval <= 0 || event.getServer().getTickCount() % interval != 0) {
            return;
        }
        ServerLevel level = event.getServer().getLevel(Level.OVERWORLD);
        if (level == null) {
            return;
        }
        ServerChunkCache cache = level.getChunkSource();
        int tick = event.getServer().getTickCount();
        for (ServerPlayer player : level.players()) {
            if (ModConfig.ENABLE_DEER_SPAWNS.get()) {
                clean(level, cache, player, ModEntities.DEER.get(), Deer.class, ModConfig.DEER_SPAWN_CAP.get(), tick);
            }
            if (ModConfig.ENABLE_WOOL_HARE_SPAWNS.get()) {
                clean(level, cache, player, ModEntities.WOOL_HARE.get(), WoolHare.class,
                        ModConfig.WOOL_HARE_SPAWN_CAP.get(), tick);
            }
            if (ModConfig.ENABLE_MIRE_SOW_SPAWNS.get()) {
                clean(level, cache, player, ModEntities.MIRE_SOW.get(), MireSow.class,
                        ModConfig.MIRE_SOW_SPAWN_CAP.get(), tick);
            }
            if (ModConfig.ENABLE_ASH_FOWL_SPAWNS.get()) {
                clean(level, cache, player, ModEntities.ASH_FOWL.get(), AshFowl.class,
                        ModConfig.ASH_FOWL_SPAWN_CAP.get(), tick);
            }
            if (ModConfig.ENABLE_BURROWLING_SPAWNS.get()) {
                clean(level, cache, player, ModEntities.BURROWLING.get(), Burrowling.class,
                        ModConfig.BURROWLING_SPAWN_CAP.get(), tick);
            }
            if (ModConfig.ENABLE_PACK_BEAST_SPAWNS.get()) {
                clean(level, cache, player, ModEntities.PACK_BEAST.get(), PackBeast.class,
                        ModConfig.PACK_BEAST_SPAWN_CAP.get(), tick);
            }
            if (ModConfig.ENABLE_GREY_FOX_SPAWNS.get()) {
                clean(level, cache, player, ModEntities.GREY_FOX.get(), GreyFox.class,
                        ModConfig.GREY_FOX_SPAWN_CAP.get(), tick);
            }
            if (ModConfig.ENABLE_HILL_HOUND_SPAWNS.get()) {
                clean(level, cache, player, ModEntities.HILL_HOUND.get(), HillHound.class,
                        ModConfig.HILL_HOUND_SPAWN_CAP.get(), tick);
            }
            if (ModConfig.ENABLE_HEARTH_CAT_SPAWNS.get()) {
                clean(level, cache, player, ModEntities.HEARTH_CAT.get(), HearthCat.class,
                        ModConfig.HEARTH_CAT_SPAWN_CAP.get(), tick);
            }
            if (ModConfig.ENABLE_WOOL_BEAST_SPAWNS.get()) {
                clean(level, cache, player, ModEntities.WOOL_BEAST.get(), WoolBeast.class,
                        ModConfig.WOOL_BEAST_SPAWN_CAP.get(), tick);
            }
            if (ModConfig.ENABLE_BOG_BEAR_SPAWNS.get()) {
                coastal(level, cache, player, ModEntities.BOG_BEAR.get(), BogBear.class,
                        ModConfig.BOG_BEAR_SPAWN_CAP.get(), tick);
            }
            if (ModConfig.ENABLE_TIDE_GRAZER_SPAWNS.get()) {
                coastal(level, cache, player, ModEntities.TIDE_GRAZER.get(), TideGrazer.class,
                        ModConfig.TIDE_GRAZER_SPAWN_CAP.get(), tick);
            }
            if (ModConfig.ENABLE_SPORE_BEE_SPAWNS.get()) {
                tainted(level, cache, player, ModEntities.SPORE_BEE.get(), SporeBee.class,
                        ModConfig.SPORE_BEE_SPAWN_CAP.get(), tick);
            }
            if (ModConfig.ENABLE_STONE_SENTINEL_SPAWNS.get()) {
                clean(level, cache, player, ModEntities.STONE_SENTINEL.get(), StoneSentinel.class,
                        ModConfig.STONE_SENTINEL_SPAWN_CAP.get(), tick);
            }
        }
    }

    private static <T extends Mob> void clean(ServerLevel level, ServerChunkCache cache, ServerPlayer player,
                                              EntityType<T> type, Class<T> typeClass, int cap, int tick) {
        shared(level, cache, player, type, typeClass, cap, tick,
                (lvl, spot, dark, tainted) -> !tainted);
    }

    private static <T extends Mob> void coastal(ServerLevel level, ServerChunkCache cache, ServerPlayer player,
                                                EntityType<T> type, Class<T> typeClass, int cap, int tick) {
        shared(level, cache, player, type, typeClass, cap, tick,
                (lvl, spot, dark, tainted) -> !tainted && BestiarySupport.nearWater(lvl, spot));
    }

    private static <T extends Mob> void tainted(ServerLevel level, ServerChunkCache cache, ServerPlayer player,
                                                EntityType<T> type, Class<T> typeClass, int cap, int tick) {
        shared(level, cache, player, type, typeClass, cap, tick,
                (lvl, spot, dark, tainted) -> tainted);
    }

    private static <T extends Mob> void shared(ServerLevel level, ServerChunkCache cache, ServerPlayer player,
                                               EntityType<T> type, Class<T> typeClass, int cap, int tick,
                                               BestiarySupport.SpawnGate gate) {
        BestiarySupport.topUp(level, cache, player, type, typeClass,
                ModConfig.PASSIVE_SPAWN_RADIUS.get(), cap,
                ModConfig.PASSIVE_SPAWN_COUNT.get(), tick, gate);
    }
}
