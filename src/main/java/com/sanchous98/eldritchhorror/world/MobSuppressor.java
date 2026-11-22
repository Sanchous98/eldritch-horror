package com.sanchous98.eldritchhorror.world;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.entity.AncientOne;
import com.sanchous98.eldritchhorror.entity.CultRaider;
import com.sanchous98.eldritchhorror.entity.CultZealot;
import com.sanchous98.eldritchhorror.entity.PlagueCrone;
import com.sanchous98.eldritchhorror.entity.RiteBinder;
import com.sanchous98.eldritchhorror.entity.SporeBee;
import com.sanchous98.eldritchhorror.entity.TaintedFauna;
import com.sanchous98.eldritchhorror.world.ward.WardLedger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

/**
 * The single choke point that removes every <b>vanilla</b> mob from the world: no natural,
 * spawner, or structure spawns, and no spawn eggs or {@code /summon} either. Mod entities (any
 * namespace other than {@code minecraft}) and the player are always allowed, so this mod can
 * still add its own creatures.
 *
 * <p>Authority is {@link EntityJoinLevelEvent}, fired by
 * {@code PersistentEntitySectionManager#addEntity} for every entity that enters a level on either
 * logical side. We act only on the server and cancel the add for a vanilla {@link LivingEntity}
 * that is not a {@link Player}. This one predicate catches natural spawns, spawn eggs,
 * {@code /summon}, monster spawners, and structure/worldgen spawns, because they all funnel
 * through entity join. Mod entities pass untouched.
 *
 * <p>{@link FinalizeSpawnEvent} is belt-and-braces: it lets a natural/mob-spawner attempt be
 * rejected before the entity is initialised, and {@code setSpawnCancelled(true)} is also honoured
 * by NeoForge's built-in join blocker. It cannot be the sole guard (commands and eggs do not
 * finalize), but it is cheaper for the hot natural-spawn path.
 *
 * <p><b>Chunk-load re-adds:</b> entities deserialised from a chunk file also fire
 * {@link EntityJoinLevelEvent}, with {@link EntityJoinLevelEvent#loadedFromDisk()} true; we cancel
 * those too, so an existing world is purged as its chunks load. The cancel happens before the
 * entity is registered (no level callback, no UUID, never {@code onAddedToLevel}), so the object
 * is simply dropped and GC'd — we deliberately do <b>not</b> call {@code discard()}, as there is
 * nothing to untrack and the call would be redundant. The stale record remains in the chunk's NBT
 * and is re-cancelled on every load; that is harmless and the simplest correct behaviour (a true
 * purge would mean rewriting chunk data, which this class intentionally avoids).
 *
 * <p><b>Wards (design/26):</b> a <em>freshly spawned</em> lesser or tainted horror — every hostile
 * bestiary {@link Monster} except the {@link AncientOne} bosses and the human cultists, plus the
 * tainted fauna and spore bee — inside an unexpired ward is cancelled here too. Chunk-load re-adds
 * are exempt: they are existing creatures, not spawns, and walking into a haven is not the same as
 * one opening inside it.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class MobSuppressor {

    /** The namespace of vanilla content; everything else is treated as a mod entity. */
    private static final String VANILLA_NAMESPACE = "minecraft";

    private MobSuppressor() {
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        if (!event.loadedFromDisk() && isWardedLesserSpawn(event.getEntity())) {
            event.setCanceled(true);
            return;
        }
        if (!ModConfig.SUPPRESS_VANILLA_MOBS.get()) {
            return;
        }
        if (isVanillaMob(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        if (isWardedLesserSpawn(event.getEntity())) {
            event.setSpawnCancelled(true);
            return;
        }
        if (!ModConfig.SUPPRESS_VANILLA_MOBS.get()) {
            return;
        }
        if (isVanillaMob(event.getEntity())) {
            event.setSpawnCancelled(true);
        }
    }

    /**
     * Whether {@code entity} is a lesser/tainted horror spawning inside an unexpired ward. The
     * bestiary registry is the authority: every hostile {@link Monster} is covered except the
     * {@link AncientOne} bosses and the human cultists (who are a faction, not horrors), and the
     * two tainted non-monsters ({@link TaintedFauna}, {@link SporeBee}) are included explicitly.
     */
    private static boolean isWardedLesserSpawn(Entity entity) {
        if (!ModConfig.ENABLE_WARDS.get() || !isLesserHorror(entity)) {
            return false;
        }
        if (!(entity.level() instanceof ServerLevel level)) {
            return false;
        }
        return WardLedger.of(level).wardAt(level, entity.blockPosition()) != null;
    }

    /** The shared lesser/tainted-horror classification (mirrors {@code StoneSentinel.isWardable}). */
    private static boolean isLesserHorror(Entity entity) {
        if (entity instanceof TaintedFauna || entity instanceof SporeBee) {
            return true;
        }
        if (!(entity instanceof Monster) || entity instanceof AncientOne) {
            return false;
        }
        // Only this mod's bestiary: a haven must not silently repel vanilla monsters.
        if (!BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getNamespace()
                .equals(EldritchHorror.MODID)) {
            return false;
        }
        return !(entity instanceof CultRaider) && !(entity instanceof CultZealot)
                && !(entity instanceof RiteBinder) && !(entity instanceof PlagueCrone);
    }

    /**
     * Predicate for suppression: a vanilla-namespaced {@link LivingEntity} that is not the player
     * and is not on the configured allow-list. Reads config live so edits apply without a restart.
     */
    private static boolean isVanillaMob(Entity entity) {
        if (!(entity instanceof LivingEntity) || entity instanceof Player) {
            return false;
        }
        Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (id == null || !VANILLA_NAMESPACE.equals(id.getNamespace())) {
            return false;
        }
        return !ModConfig.SUPPRESSED_MOB_ALLOWLIST.get().contains(id.toString());
    }
}
