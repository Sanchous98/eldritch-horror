package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * The single server tick that applies every {@link DreadAura} for every player. One handler rather
 * than three, so the shared trait actually shares its loop (the design's "no duplication storms").
 * Mirrors {@code RisenHuskSanityDrain}: once per second, overworld-only, and each aura is
 * radius-bounded and capped inside {@link BestiarySupport#tickAura}.
 *
 * <p>Each aura's own config flag gates it, so disabling one mob's hook costs nothing here.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class DreadAuraTicker {

    /** Evaluate once per second, matching the sanity source pass. */
    private static final int INTERVAL_TICKS = 20;

    private DreadAuraTicker() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer().getTickCount() % INTERVAL_TICKS != 0) {
            return;
        }
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            if (!player.level().dimension().equals(Level.OVERWORLD)) {
                continue;
            }
            BestiarySupport.tickAura(player, TaintedFauna.class, ModConfig.CORRUPTION_TAINTED_FAUNA_RADIUS.get());
            BestiarySupport.tickAura(player, LesserSwarm.class, ModConfig.SANITY_LESSER_SWARM_RADIUS.get());
            BestiarySupport.tickAura(player, Watcher.class, ModConfig.SANITY_WATCHER_RADIUS.get());
            BestiarySupport.tickAura(player, BoneChoir.class, ModConfig.SANITY_BONE_CHOIR_RADIUS.get());
            BestiarySupport.tickAura(player, VeilStalker.class, ModConfig.SANITY_VEIL_STALKER_RADIUS.get());
            BestiarySupport.tickAura(player, DrownedThrall.class, ModConfig.CORRUPTION_DROWNED_THRALL_RADIUS.get());
            BestiarySupport.tickAura(player, BlightPod.class, ModConfig.CORRUPTION_BLIGHT_POD_RADIUS.get());
            BestiarySupport.tickAura(player, Byakhee.class, ModConfig.SANITY_BYAKHEE_RADIUS.get());
            BestiarySupport.tickAura(player, StarSpawn.class, ModConfig.SANITY_STAR_SPAWN_RADIUS.get());
            BestiarySupport.tickAura(player, ShoggothMass.class, ModConfig.CORRUPTION_SHOGGOTH_RADIUS.get());
            BestiarySupport.tickAura(player, WeaverSpawn.class, ModConfig.SANITY_WEAVER_SPAWN_RADIUS.get());
            BestiarySupport.tickAura(player, RiftMite.class, ModConfig.CORRUPTION_RIFT_MITE_RADIUS.get());
            BestiarySupport.tickAura(player, ShamblerOoze.class, ModConfig.CORRUPTION_SHAMBLER_OOZE_RADIUS.get());
            BestiarySupport.tickAura(player, ChoirSpite.class, ModConfig.SANITY_CHOIR_SPITE_RADIUS.get());
            BestiarySupport.tickAura(player, NightHag.class, ModConfig.SANITY_NIGHT_HAG_RADIUS.get());
            BestiarySupport.tickAura(player, CultZealot.class, ModConfig.SANITY_CULT_ZEALOT_RADIUS.get());
            BestiarySupport.tickAura(player, CultRaider.class, ModConfig.SANITY_CULT_RAIDER_RADIUS.get());
            BestiarySupport.tickAura(player, RiteBinder.class, ModConfig.CORRUPTION_RITE_BINDER_RADIUS.get());
            BestiarySupport.tickAura(player, PlagueCrone.class, ModConfig.CORRUPTION_PLAGUE_CRONE_RADIUS.get());
            // Ancient Ones with a single-axis shared aura. Shub-Niggurath is deliberately absent:
            // it runs its own two-axis pulse (corruption by motion, sanity at rest) in its own tick.
            BestiarySupport.tickAura(player, Cthulhu.class, ModConfig.SANITY_CTHULHU_RADIUS.get());
            BestiarySupport.tickAura(player, DunwichHorror.class, ModConfig.CORRUPTION_DUNWICH_HORROR_RADIUS.get());
        }
    }
}
