package com.sanchous98.eldritchhorror.core;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.corruption.CorruptionTicker;
import com.sanchous98.eldritchhorror.cult.ReputationDecayTicker;
import com.sanchous98.eldritchhorror.entity.ChoirLeviathanTrigger;
import com.sanchous98.eldritchhorror.entity.CthulhuSiteTrigger;
import com.sanchous98.eldritchhorror.event.EventTicker;
import com.sanchous98.eldritchhorror.registry.blocks.CityGateBlock;
import com.sanchous98.eldritchhorror.registry.blocks.InvestigatorPedestalBlock;
import com.sanchous98.eldritchhorror.registry.items.RiteEchoes;
import com.sanchous98.eldritchhorror.registry.items.VeilLensItem;
import com.sanchous98.eldritchhorror.registry.items.Waypoints;
import com.sanchous98.eldritchhorror.rite.RiteEngine;
import com.sanchous98.eldritchhorror.sanity.SanityTicker;
import com.sanchous98.eldritchhorror.world.cult.CultRituals;
import com.sanchous98.eldritchhorror.world.PregenService;
import com.sanchous98.eldritchhorror.world.loc.CitySigns;
import com.sanchous98.eldritchhorror.world.rail.TrainService;
import com.sanchous98.eldritchhorror.world.rift.RiftGates;
import com.sanchous98.eldritchhorror.world.transport.ShipService;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/**
 * Clears the mod's process-wide caches when a world is unloaded or a new one is started.
 *
 * <p>Most per-world bookkeeping in this mod lives in {@code static} maps (rite cooldowns, ship
 * reservations, one-shot spawn flags, ...). Without a reset they leak across worlds in the same
 * JVM: a fresh world starts with a low {@code gameTime}, so stale {@code now < ready} timestamps
 * lock rites, altars and gates out, and stale "already spawned" flags stop the great presences
 * from ever appearing. Each owner exposes a {@code clear()} hook; this class is the single place
 * that calls them.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class ServerStateReset {

    private ServerStateReset() {}

    /**
     * Install the map package before any world is loaded, so the very first chunk generated (or
     * the first pregen batch) already sees the package city list.
     */
    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        net.minecraft.world.level.storage.LevelResource root =
                net.minecraft.world.level.storage.LevelResource.ROOT;
        com.sanchous98.eldritchhorror.world.city.MapPackage.load(
                net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get(),
                event.getServer().getWorldPath(root));
    }

    /** A new world starts with empty caches; anything left over is from the previous one. */
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        clearAll();
    }

    /** The world is gone, so every reference to it (and every tick stamp) must go too. */
    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        clearAll();
        com.sanchous98.eldritchhorror.world.city.MapPackage.clear();
    }

    private static void clearAll() {
        RiteEngine.clear();
        ReputationDecayTicker.clear();
        CultRituals.clear();
        ShipService.clear();
        CthulhuSiteTrigger.clear();
        ChoirLeviathanTrigger.clear();
        VeilLensItem.clear();
        RiteEchoes.clear();
        Waypoints.clear();
        CityGateBlock.clear();
        InvestigatorPedestalBlock.clear();
        EventTicker.clear();
        SanityTicker.clear();
        CorruptionTicker.clear();
        TrainService.clear();
        com.sanchous98.eldritchhorror.world.rail.RailLines.clear();
        com.sanchous98.eldritchhorror.world.city.quest.CityQuests.clear();
        RiftGates.clear();
        CitySigns.clear();
        PregenService.stop();
    }
}
