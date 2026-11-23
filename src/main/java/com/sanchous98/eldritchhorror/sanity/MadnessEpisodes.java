package com.sanchous98.eldritchhorror.sanity;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.entity.BestiarySupport;
import com.sanchous98.eldritchhorror.registry.ModEntities;
import com.sanchous98.eldritchhorror.registry.ModSounds;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Prediction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jspecify.annotations.Nullable;

/**
 * Madness <b>episodes</b>: what a worsening sanity band actually does, beyond the stat effects.
 *
 * <ul>
 *   <li><b>Hallucination</b> — one or two phantom mobs appear around the player. The type is drawn
 *       from this biome's own monster spawn list, so it looks exactly like something that could
 *       really be there. They hit <b>hard</b> (damage multiplier) so a player cannot safely "test"
 *       one by letting it strike them, and they have real health, so destroying one costs arrows
 *       and durability; they give no drops and no XP, and only the afflicted player can see them.
 *       Killing one earns a small "it was never there" relief.</li>
 *   <li><b>Fugue</b> — blindness and nausea, and a short involuntary displacement (8–16 blocks).</li>
 *   <li><b>Compulsion</b> — the main-hand stack is dropped; weakness/mining fatigue follow.</li>
 * </ul>
 *
 * <p>Phantoms are ordinary entities tagged in persistent data, tracked here for expiry, caps and
 * cleanup. They are visible to the owner only: the tracker's spawn packet is followed by a remove
 * packet for every other player (the tracking event is not cancellable in NeoForge 26.3), re-sent
 * whenever someone starts tracking one.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class MadnessEpisodes {

    private static final String ILLUSION_TAG = "eh_illusion";
    private static final int TICK_INTERVAL = 20;
    private static final int MAX_ILLUSIONS_PER_PLAYER = 2;
    private static final int HIT_COOLDOWN_TICKS = 40;

    private record Illusion(UUID owner, long until) {
    }

    private static final Map<UUID, Long> LAST_EPISODE = new ConcurrentHashMap<>();
    private static final Map<UUID, Illusion> ILLUSIONS = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> LAST_HIT = new ConcurrentHashMap<>();
    private static final Set<UUID> HIDE = ConcurrentHashMap.newKeySet();

    private MadnessEpisodes() {
    }

    /** Called by {@link SanityTicker} when the player's band just got worse. */
    public static void onBandWorsened(ServerPlayer player, SanityState state) {
        if (!ModConfig.ENABLE_MADNESS_EPISODES.get()) {
            return;
        }
        long now = player.level().getGameTime();
        Long last = LAST_EPISODE.get(player.getUUID());
        if (last != null && now - last < ModConfig.MADNESS_EPISODE_COOLDOWN.get()) {
            return;
        }
        LAST_EPISODE.put(player.getUUID(), now);
        switch (state) {
            case MARKED -> hallucinate(player, 2);
            case BREAKING -> {
                if (player.getRandom().nextBoolean()) {
                    hallucinate(player, 1);
                } else {
                    compulsion(player);
                }
            }
            case FRAYING -> {
                if (player.getRandom().nextBoolean()) {
                    compulsion(player);
                } else {
                    fugue(player);
                }
            }
            default -> {
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        processHides(server);
        if (server.getTickCount() % TICK_INTERVAL != 0) {
            return;
        }
        long now = server.overworld().getGameTime();
        expire(server, now);
        if (!ModConfig.ENABLE_MADNESS_EPISODES.get()) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!player.level().dimension().equals(Level.OVERWORLD)
                    || SanitySystem.state(player) != SanityState.MARKED) {
                continue;
            }
            Long last = LAST_EPISODE.get(player.getUUID());
            if (last == null || now - last >= ModConfig.MADNESS_MARKED_INTERVAL.get()) {
                LAST_EPISODE.put(player.getUUID(), now);
                hallucinate(player, 1);
            }
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID id = event.getEntity().getUUID();
        LAST_EPISODE.remove(id);
    }

    /**
     * Phantoms never deal damage. Their attack is cancelled but staged as a hit (hurt sound and
     * animation to nearby clients), so finding out whether a mob is real by letting it strike you is
     * a trade-off rather than a free test.
     */
    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        if (isPhantom(victim)) {
            if (!(event.getSource().getEntity() instanceof ServerPlayer)) {
                event.setCanceled(true);
            }
            return;
        }
        Entity attacker = event.getSource().getEntity();
        if (attacker != null && isPhantom(attacker) && victim instanceof ServerPlayer player) {
            event.setCanceled(true);
            fakeHit(player, attacker);
        }
    }

    /** Plays the hurt sound/animation without touching health. */
    private static void fakeHit(ServerPlayer victim, Entity illusion) {
        long now = victim.level().getGameTime();
        Long last = LAST_HIT.get(illusion.getUUID());
        if (last != null && now - last < HIT_COOLDOWN_TICKS) {
            return;
        }
        LAST_HIT.put(illusion.getUUID(), now);
        if (!(victim.level() instanceof ServerLevel level)) {
            return;
        }
        level.playSound(null, victim.getX(), victim.getY(), victim.getZ(),
                SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 0.8F, 0.9F);
        var packet = new ClientboundHurtAnimationPacket(victim.getId(), 0.0F);
        for (ServerPlayer nearby : level.players()) {
            if (nearby.distanceToSqr(victim) <= 16.0 * 16.0) {
                nearby.connection.send(packet);
            }
        }
    }

    /** A late tracker must not see someone else's phantom. */
    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof LivingEntity target && isPhantom(target)
                && event.getEntity() instanceof ServerPlayer player) {
            Illusion illusion = ILLUSIONS.get(target.getUUID());
            if (illusion != null && !illusion.owner().equals(player.getUUID())) {
                HIDE.add(target.getUUID());
            }
        }
    }

    /** A phantom "dies" like a real mob, but leaves nothing behind. */
    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!isPhantom(event.getEntity())) {
            return;
        }
        ServerPlayer killer = event.getSource().getEntity() instanceof ServerPlayer player
                ? player
                : null;
        pop(event.getEntity(), killer);
    }

    /** A phantom that explodes (blight pod, or a vanilla creeper if one is present) is staged. */
    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Start event) {
        Entity source = event.getExplosion().getDirectSourceEntity();
        if (source == null || !isPhantom(source)) {
            return;
        }
        event.setCanceled(true);
        if (source.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, source.getX(), source.getY() + 0.5,
                    source.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
            level.playSound(null, source.getX(), source.getY(), source.getZ(),
                    SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        pop(source, null);
    }

    @SubscribeEvent
    public static void onDrops(LivingDropsEvent event) {
        if (isPhantom(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onExperience(LivingExperienceDropEvent event) {
        if (isPhantom(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    /** Spawns phantoms around the player. */
    private static void hallucinate(ServerPlayer player, int count) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        long now = level.getGameTime();
        long mine = ILLUSIONS.values().stream()
                .filter(illusion -> illusion.owner().equals(player.getUUID()))
                .count();
        int budget = (int) Math.max(0, MAX_ILLUSIONS_PER_PLAYER - mine);
        if (budget <= 0) {
            return;
        }
        int spawned = 0;
        for (int i = 0; i < count && spawned < budget; i++) {
            if (spawnPhantom(level, player, now)) {
                spawned++;
            }
        }
        if (spawned > 0) {
            player.sendSystemMessage(Component.translatable("madness.eldritch_horror.hallucination"));
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.AMBIENT_WHISPER.get(), SoundSource.AMBIENT, 0.7F, 1.0F);
        }
    }

    private static boolean spawnPhantom(ServerLevel level, ServerPlayer player, long now) {
        for (int attempt = 0; attempt < 16; attempt++) {
            double angle = level.getRandom().nextDouble() * Math.PI * 2.0;
            double distance = 6.0 + level.getRandom().nextDouble() * 6.0;
            int x = player.getBlockX() + (int) Math.round(Math.cos(angle) * distance);
            int z = player.getBlockZ() + (int) Math.round(Math.sin(angle) * distance);
            BlockPos spot = BestiarySupport.surfaceSpot(level, x, z, false);
            if (spot == null) {
                continue;
            }
            EntityType<?> type = phantomType(level, spot);
            Entity entity = type.spawn(level, spot, EntitySpawnReason.EVENT);
            if (entity == null) {
                continue;
            }
            entity.getPersistentData().putBoolean(ILLUSION_TAG, true);
            if (entity instanceof LivingEntity living) {
                double health = ModConfig.MADNESS_ILLUSION_HEALTH.get();
                var maxHealth = living.getAttribute(Attributes.MAX_HEALTH);
                if (maxHealth != null) {
                    maxHealth.setBaseValue(health);
                    living.setHealth((float) health);
                }
            }
            if (entity instanceof Mob mob) {
                mob.setTarget(player);
            }
            ILLUSIONS.put(entity.getUUID(),
                    new Illusion(player.getUUID(), now + ModConfig.MADNESS_ILLUSION_LIFETIME.get()));
            HIDE.add(entity.getUUID());
            return true;
        }
        return false;
    }

    /** A hostile mob from this mod that plausibly lives here (no vanilla-role creeper exists). */
    private static EntityType<?> phantomType(ServerLevel level, BlockPos pos) {
        var biome = level.getBiome(pos);
        if (biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_RIVER)
                || biome.is(BiomeTags.IS_BEACH)) {
            return ModEntities.DROWNED_THRALL.get();
        }
        EntityType<?>[] land = {
                ModEntities.RISEN_HUSK.get(),
                ModEntities.WATCHER.get(),
                ModEntities.BLIGHT_POD.get(),
                ModEntities.LESSER_SWARM.get(),
                ModEntities.VEIL_STALKER.get()
        };
        return land[level.getRandom().nextInt(land.length)];
    }

    /** Blindness, nausea and a short involuntary step to a loaded surface spot. */
    private static void fugue(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
        player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 100, 0));
        if (player.level() instanceof ServerLevel level) {
            double angle = level.getRandom().nextDouble() * Math.PI * 2.0;
            int distance = 8 + level.getRandom().nextInt(9);
            int x = player.getBlockX() + (int) Math.round(Math.cos(angle) * distance);
            int z = player.getBlockZ() + (int) Math.round(Math.sin(angle) * distance);
            if (level.hasChunkAt(x, z)) {
                int y = level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
                player.teleportTo(x + 0.5, y, z + 0.5);
            }
        }
        player.sendSystemMessage(Component.translatable("madness.eldritch_horror.fugue"));
    }

    /** Drop the held stack and shake for a moment. */
    private static void compulsion(ServerPlayer player) {
        ItemStack held = player.getMainHandItem();
        if (!held.isEmpty()) {
            player.drop(held.copy(), false, Prediction.SERVER_ONLY);
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
        player.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 100, 0));
        player.sendSystemMessage(Component.translatable("madness.eldritch_horror.compulsion"));
    }

    /** Sends a remove packet to everyone but the owner, one tick after a spawn/tracking event. */
    private static void processHides(MinecraftServer server) {
        if (HIDE.isEmpty()) {
            return;
        }
        Iterator<UUID> iterator = HIDE.iterator();
        while (iterator.hasNext()) {
            UUID id = iterator.next();
            Entity entity = server.overworld().getEntity(id);
            Illusion illusion = ILLUSIONS.get(id);
            if (entity == null || illusion == null) {
                iterator.remove();
                continue;
            }
            var packet = new ClientboundRemoveEntitiesPacket(entity.getId());
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (!player.getUUID().equals(illusion.owner())
                        && player.level() == entity.level()) {
                    player.connection.send(packet);
                }
            }
            iterator.remove();
        }
    }

    private static void expire(MinecraftServer server, long now) {
        Iterator<Map.Entry<UUID, Illusion>> iterator = ILLUSIONS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Illusion> entry = iterator.next();
            Entity entity = server.overworld().getEntity(entry.getKey());
            if (entity == null || now >= entry.getValue().until()) {
                if (entity != null) {
                    entity.discard();
                }
                LAST_HIT.remove(entry.getKey());
                iterator.remove();
            }
        }
    }

    /** Removes a phantom with a puff; a player kill earns a small relief. */
    private static void pop(Entity illusion, @Nullable ServerPlayer striker) {
        illusion.discard();
        ILLUSIONS.remove(illusion.getUUID());
        LAST_HIT.remove(illusion.getUUID());
        if (illusion.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.SMOKE, illusion.getX(), illusion.getY() + 1.0,
                    illusion.getZ(), 12, 0.4, 0.6, 0.4, 0.01);
            level.playSound(null, illusion.getX(), illusion.getY(), illusion.getZ(),
                    ModSounds.AMBIENT_WHISPER.get(), SoundSource.AMBIENT, 0.6F, 0.9F);
        }
        if (striker != null) {
            SanityAPI.add(striker, ModConfig.MADNESS_ILLUSION_REWARD.get());
            striker.sendSystemMessage(Component.translatable("madness.eldritch_horror.never_there"));
        }
    }

    /** @return whether {@code entity} is a madness phantom (also used by the aura ticker). */
    public static boolean isPhantom(Entity entity) {
        return entity.getPersistentData().getBooleanOr(ILLUSION_TAG, false);
    }
}
