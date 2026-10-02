package com.sanchous98.eldritchhorror.event;

import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.corruption.CorruptionAPI;
import com.sanchous98.eldritchhorror.corruption.TaintAPI;
import com.sanchous98.eldritchhorror.entity.BestiarySupport;
import com.sanchous98.eldritchhorror.entity.LesserSwarm;
import com.sanchous98.eldritchhorror.entity.RiftMite;
import com.sanchous98.eldritchhorror.registry.ModEffects;
import com.sanchous98.eldritchhorror.registry.ModEntities;
import com.sanchous98.eldritchhorror.sanity.SanityAPI;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ChunkPos;

/**
 * The first wave of {@link EldritchEvent} definitions and their bounded, server-side effects
 * (design/19-events.md). Each factory is data: a trigger predicate plus the per-tick effect used
 * by {@link EventTicker}. Everything is overworld-only, loaded-chunks-only, deterministic
 * ({@link RandomSource}, never {@code Math.random}) and rate-bounded — the only world mutation is
 * taint through the public {@link TaintAPI} (rift_bloom raises the spread rate, it does not edit
 * blocks). Spawn surges go through the shared {@link BestiarySupport#topUp} path.
 */
final class EventEffects {

    private EventEffects() {
    }

    /** Whisper sound table: a small, curated set of "someone is there" cues. */
    private static final SoundEvent[] WHISPERS = {
            SoundEvents.AMBIENT_CAVE.value(),
            SoundEvents.ENDERMAN_STARE,
            SoundEvents.WARDEN_NEARBY_CLOSER,
            SoundEvents.ZOMBIE_AMBIENT
    };

    /** Hallucination sound table: fake mob cues only — no entity is ever spawned. */
    private static final SoundEvent[] HALLUCINATIONS = {
            SoundEvents.ZOMBIE_AMBIENT,
            SoundEvents.SKELETON_AMBIENT,
            SoundEvents.CREEPER_PRIMED,
            SoundEvents.WARDEN_HEARTBEAT
    };

    // --- whisper ---------------------------------------------------------------------------

    static EldritchEvent whisper() {
        return new EldritchEvent("whisper", EventTrigger.LOW_SANITY_ANYWHERE,
                ModConfig.EVENT_WHISPER_DURATION.get(), 10, ModConfig.EVENT_WHISPER_COOLDOWN.get(),
                ctx -> ctx.sanity() < ModConfig.EVENT_WHISPER_SANITY_THRESHOLD.get(),
                EventEffects::whisperEffect);
    }

    private static void whisperEffect(ServerPlayer player, EventContext ctx, int elapsed, int duration, int tick) {
        // The effect first runs on the tick after start, so the "warning" is the first tick (<2).
        if (elapsed < 2 && !player.hasEffect(ModEffects.MADNESS)) {
            player.addEffect(new MobEffectInstance(ModEffects.MADNESS, 100, 0, true, false, false));
        }
        if (elapsed % 40 != 0) {
            return;
        }
        RandomSource random = seed(player, ctx, elapsed);
        SoundEvent sound = WHISPERS[random.nextInt(WHISPERS.length)];
        double angle = random.nextDouble() * Math.PI * 2.0;
        double dist = 4.0 + random.nextDouble() * 3.0;
        sendSound(player, sound, player.getX() + Math.cos(angle) * dist, player.getY(),
                player.getZ() + Math.sin(angle) * dist, SoundSource.AMBIENT,
                0.45F, 0.7F + random.nextFloat() * 0.2F, random.nextLong());
    }

    // --- darkness_pulse --------------------------------------------------------------------

    static EldritchEvent darknessPulse() {
        return new EldritchEvent("darkness_pulse", EventTrigger.NIGHT_NEAR_RIFT,
                ModConfig.EVENT_DARKNESS_DURATION.get(), 20, ModConfig.EVENT_DARKNESS_COOLDOWN.get(),
                ctx -> ctx.night() && !ctx.rifts().isEmpty(),
                EventEffects::darknessPulseEffect);
    }

    private static void darknessPulseEffect(ServerPlayer player, EventContext ctx, int elapsed, int duration, int tick) {
        if (elapsed % 20 != 0) {
            return;
        }
        // A short per-player darkness held open for the whole event; the world light is untouched.
        player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 40, 0, true, false, false));
        SanityAPI.add(player, ModConfig.EVENT_DARKNESS_SANITY_RATE.get());
    }

    // --- rift_bloom ------------------------------------------------------------------------

    static EldritchEvent riftBloom() {
        return new EldritchEvent("rift_bloom", EventTrigger.RIFT_AGING,
                ModConfig.EVENT_RIFT_BLOOM_DURATION.get(), 15, ModConfig.EVENT_RIFT_BLOOM_COOLDOWN.get(),
                ctx -> !ctx.rifts().isEmpty()
                        && ctx.taint() >= ModConfig.EVENT_RIFT_BLOOM_TAINT_THRESHOLD.get(),
                EventEffects::riftBloomEffect);
    }

    private static void riftBloomEffect(ServerPlayer player, EventContext ctx, int elapsed, int duration, int tick) {
        if (ctx.rifts().isEmpty()) {
            return; // the rift closed mid-event; the ticker ends this event on the same pass
        }
        BlockPos rift = ctx.rifts().get(0);
        // Rate-only: raise the local taint field in the loaded 3x3 chunk patch around the tear.
        if (elapsed % 20 == 0) {
            ChunkPos centre = ChunkPos.containing(rift);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    TaintAPI.add(ctx.level(), new ChunkPos(centre.x() + dx, centre.z() + dz),
                            ModConfig.EVENT_RIFT_BLOOM_TAINT_RATE.get());
                }
            }
        }
        if (elapsed % 10 == 0) {
            ctx.level().sendParticles(ParticleTypes.PORTAL, rift.getX() + 0.5, rift.getY() + 1.5,
                    rift.getZ() + 0.5, 6, 0.4, 0.6, 0.4, 0.02);
        }
        if (elapsed % 40 == 0) {
            sendSound(player, SoundEvents.PORTAL_TRAVEL, rift.getX() + 0.5, rift.getY() + 1.5,
                    rift.getZ() + 0.5, SoundSource.AMBIENT, 0.6F, 0.6F, tick);
        }
    }

    // --- veil_thin -------------------------------------------------------------------------

    static EldritchEvent veilThin() {
        return new EldritchEvent("veil_thin", EventTrigger.STORM_MANY_RIFTS,
                ModConfig.EVENT_VEIL_THIN_DURATION.get(), 25, ModConfig.EVENT_VEIL_THIN_COOLDOWN.get(),
                ctx -> ctx.thundering()
                        && (ctx.rifts().size() >= ModConfig.EVENT_VEIL_THIN_MIN_RIFTS.get()
                            || ctx.taint() >= ModConfig.EVENT_VEIL_THIN_TAINT_THRESHOLD.get()),
                EventEffects::veilThinEffect);
    }

    private static void veilThinEffect(ServerPlayer player, EventContext ctx, int elapsed, int duration, int tick) {
        int interval = Math.max(1, ModConfig.EVENT_VEIL_THIN_SPAWN_INTERVAL.get());
        if (elapsed % interval == 0) {
            int cap = ModConfig.EVENT_VEIL_THIN_SURGE_CAP.get();
            surge(player, ctx, ModEntities.LESSER_SWARM.get(), LesserSwarm.class, cap, tick);
            surge(player, ctx, ModEntities.RIFT_MITE.get(), RiftMite.class, cap, tick);
        }
        if (elapsed % 40 == 0) {
            sendSound(player, SoundEvents.WARDEN_NEARBY_CLOSER, player.getX(), player.getY(),
                    player.getZ(), SoundSource.AMBIENT, 0.5F, 0.5F, tick);
        }
    }

    /** One bounded spawn surge for {@code type} through the shared top-up path. */
    private static <T extends Mob> void surge(ServerPlayer player, EventContext ctx,
                                              EntityType<T> type, Class<T> typeClass, int cap, int tick) {
        BestiarySupport.topUp(ctx.level(), ctx.level().getChunkSource(), player, type, typeClass,
                ModConfig.EVENT_VEIL_THIN_SPAWN_RADIUS.get(), cap,
                ModConfig.EVENT_VEIL_THIN_SURGE_PER_PASS.get(), tick,
                (level, spot, dark, tainted) -> dark || tainted);
    }

    // --- hallucination_wave ----------------------------------------------------------------

    static EldritchEvent hallucinationWave() {
        return new EldritchEvent("hallucination_wave", EventTrigger.SANITY_BELOW_20,
                ModConfig.EVENT_HALLUCINATION_DURATION.get(), 30,
                ModConfig.EVENT_HALLUCINATION_COOLDOWN.get(),
                ctx -> ctx.sanity() < ModConfig.EVENT_HALLUCINATION_SANITY_THRESHOLD.get(),
                EventEffects::hallucinationEffect);
    }

    private static void hallucinationEffect(ServerPlayer player, EventContext ctx, int elapsed, int duration, int tick) {
        if (elapsed % 30 != 0) {
            return;
        }
        RandomSource random = seed(player, ctx, elapsed);
        double angle = random.nextDouble() * Math.PI * 2.0;
        double dist = 5.0 + random.nextDouble() * 4.0;
        double x = player.getX() + Math.cos(angle) * dist;
        double z = player.getZ() + Math.sin(angle) * dist;
        SoundEvent sound = HALLUCINATIONS[random.nextInt(HALLUCINATIONS.length)];
        sendSound(player, sound, x, player.getY(), z, SoundSource.HOSTILE,
                0.7F, 0.9F + random.nextFloat() * 0.2F, random.nextLong());
        ctx.level().sendParticles(ParticleTypes.SMOKE, x, player.getY() + 1.0, z,
                4, 0.3, 0.4, 0.3, 0.01);
    }

    // --- cleansing_dawn --------------------------------------------------------------------

    static EldritchEvent cleansingDawn() {
        return new EldritchEvent("cleansing_dawn", EventTrigger.RIFT_CLOSED,
                ModConfig.EVENT_CLEANSING_DAWN_DURATION.get(), 40,
                ModConfig.EVENT_CLEANSING_DAWN_COOLDOWN.get(),
                // Never auto-starts: a close_rift is a one-shot; /eh event cleansing_dawn drives it.
                ctx -> false,
                EventEffects::cleansingDawnEffect);
    }

    private static void cleansingDawnEffect(ServerPlayer player, EventContext ctx, int elapsed, int duration, int tick) {
        if (elapsed % 20 != 0) {
            return;
        }
        SanityAPI.add(player, ModConfig.EVENT_CLEANSING_SANITY_RATE.get());
        CorruptionAPI.add(player, ModConfig.EVENT_CLEANSING_CORRUPTION_RATE.get());
        ChunkPos centre = player.chunkPosition();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                TaintAPI.add(ctx.level(), new ChunkPos(centre.x() + dx, centre.z() + dz),
                        ModConfig.EVENT_CLEANSING_TAINT_RATE.get());
            }
        }
    }

    /** A deterministic per-player, per-tick RNG (same idiom as the bestiary), never {@code Math.random}. */
    private static RandomSource seed(ServerPlayer player, EventContext ctx, int elapsed) {
        return RandomSource.create(player.getUUID().getMostSignificantBits()
                ^ (ctx.ticks() + elapsed) * 0x9E3779B97F4A7C15L);
    }

    /** Sends a positional sound to exactly one player, so no global broadcast is needed. */
    private static void sendSound(ServerPlayer player, SoundEvent sound, double x, double y, double z,
                                  SoundSource source, float volume, float pitch, long seed) {
        player.connection.send(new ClientboundSoundPacket(
                BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound), source, x, y, z, volume, pitch, seed));
    }
}
