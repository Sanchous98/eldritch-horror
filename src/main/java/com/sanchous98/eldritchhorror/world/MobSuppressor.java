package com.sanchous98.eldritchhorror.world;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
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
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class MobSuppressor {

    /** The namespace of vanilla content; everything else is treated as a mod entity. */
    private static final String VANILLA_NAMESPACE = "minecraft";

    private MobSuppressor() {
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !ModConfig.SUPPRESS_VANILLA_MOBS.get()) {
            return;
        }
        if (isVanillaMob(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        if (!ModConfig.SUPPRESS_VANILLA_MOBS.get()) {
            return;
        }
        if (isVanillaMob(event.getEntity())) {
            event.setSpawnCancelled(true);
        }
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
