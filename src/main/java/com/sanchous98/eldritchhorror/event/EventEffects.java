package com.sanchous98.eldritchhorror.event;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.corruption.CorruptionAPI;
import com.sanchous98.eldritchhorror.corruption.CorruptionState;
import com.sanchous98.eldritchhorror.corruption.TaintAPI;
import com.sanchous98.eldritchhorror.cult.CultSystem;
import com.sanchous98.eldritchhorror.entity.BestiarySupport;
import com.sanchous98.eldritchhorror.entity.ChoirSpite;
import com.sanchous98.eldritchhorror.entity.CultZealot;
import com.sanchous98.eldritchhorror.entity.LesserSwarm;
import com.sanchous98.eldritchhorror.entity.NightHag;
import com.sanchous98.eldritchhorror.entity.RiftMite;
import com.sanchous98.eldritchhorror.entity.Worshipper;
import com.sanchous98.eldritchhorror.registry.ModEffects;
import com.sanchous98.eldritchhorror.registry.ModEntities;
import com.sanchous98.eldritchhorror.sanity.SanityAPI;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * The {@link EldritchEvent} definitions and their bounded, server-side effects
 * (design/19-events.md). Each factory is data: a trigger predicate plus the per-tick effect used
 * by {@link EventTicker}. Everything is overworld-only, loaded-chunks-only, deterministic
 * ({@link RandomSource}, never {@code Math.random}) and rate-bounded — the only world mutation is
 * taint through the public {@link TaintAPI} (rift_bloom raises the spread rate, it does not edit
 * blocks). Spawn top-ups go through the shared {@link BestiarySupport#topUp} path. The one exception
 * is {@code star_fall}: design/19 asks for a meteorite, so it carves a tiny, config-bounded crater.
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

    /**
     * Radius (blocks) for event mob top-ups whose config has no radius key of its own
     * (blood_moon_rite, hollow_call). Bounded and loaded-chunks-only like every spawn pass.
     */
    private static final int EVENT_SPAWN_RADIUS = 32;

    /** A bounded event mob top-up on dark or tainted ground (the events' common spawn gate). */
    private static final BestiarySupport.SpawnGate DARK_OR_TAINTED =
            (level, spot, dark, tainted) -> dark || tainted;

    /** The procession's unbounded-ground gate: any valid standing spot. */
    private static final BestiarySupport.SpawnGate ANYWHERE =
            (level, spot, dark, tainted) -> true;

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
        BlockPos rift = ctx.rifts().getFirst();
        // Rate-only: raise the local taint field in the loaded 3x3 chunk patch around the tear.
        if (elapsed % 20 == 0) {
            taintPatch(ctx, ChunkPos.containing(rift), ModConfig.EVENT_RIFT_BLOOM_TAINT_RATE.get());
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
            int radius = ModConfig.EVENT_VEIL_THIN_SPAWN_RADIUS.get();
            int cap = ModConfig.EVENT_VEIL_THIN_SURGE_CAP.get();
            int perPass = ModConfig.EVENT_VEIL_THIN_SURGE_PER_PASS.get();
            eventTopUp(player, ctx, ModEntities.LESSER_SWARM.get(), LesserSwarm.class,
                    radius, cap, perPass, tick, DARK_OR_TAINTED);
            eventTopUp(player, ctx, ModEntities.RIFT_MITE.get(), RiftMite.class,
                    radius, cap, perPass, tick, DARK_OR_TAINTED);
        }
        if (elapsed % 40 == 0) {
            sendSound(player, SoundEvents.WARDEN_NEARBY_CLOSER, player.getX(), player.getY(),
                    player.getZ(), SoundSource.AMBIENT, 0.5F, 0.5F, tick);
        }
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
        taintPatch(ctx, player.chunkPosition(), ModConfig.EVENT_CLEANSING_TAINT_RATE.get());
    }

    // --- cult_procession -------------------------------------------------------------------

    static EldritchEvent cultProcession() {
        return new EldritchEvent("cult_procession", EventTrigger.CULT_SITE,
                ModConfig.EVENT_CULT_PROCESSION_DURATION.get(), 18,
                ModConfig.EVENT_CULT_PROCESSION_COOLDOWN.get(),
                EventContext::nearCultSite,
                EventEffects::cultProcessionEffect);
    }

    private static void cultProcessionEffect(ServerPlayer player, EventContext ctx, int elapsed,
                                             int duration, int tick) {
        // The effect first runs on the tick after start, so elapsed<2 is the opening beat.
        if (elapsed < 2) {
            CultSystem.add(player, "hollow_choir", ModConfig.EVENT_CULT_PROCESSION_REP.get());
        }
        int interval = Math.max(1, ModConfig.EVENT_CULT_PROCESSION_SPAWN_INTERVAL.get());
        if (elapsed % interval == 0) {
            int radius = ModConfig.EVENT_CULT_PROCESSION_SPAWN_RADIUS.get();
            eventTopUp(player, ctx, ModEntities.CULT_ZEALOT.get(), CultZealot.class,
                    radius, ModConfig.EVENT_CULT_PROCESSION_SPAWN_CAP.get(), 1, tick, ANYWHERE);
            eventTopUp(player, ctx, ModEntities.WORSHIPPER.get(), Worshipper.class,
                    radius, ModConfig.EVENT_CULT_PROCESSION_WORSHIPPER_CAP.get(), 1, tick, ANYWHERE);
        }
        if (elapsed % 60 == 0) {
            RandomSource random = seed(player, ctx, elapsed);
            SoundEvent sound = random.nextBoolean()
                    ? SoundEvents.EVOKER_AMBIENT : SoundEvents.RAVAGER_AMBIENT;
            double angle = random.nextDouble() * Math.PI * 2.0;
            double dist = 6.0 + random.nextDouble() * 4.0;
            sendSound(player, sound, player.getX() + Math.cos(angle) * dist, player.getY(),
                    player.getZ() + Math.sin(angle) * dist, SoundSource.AMBIENT,
                    0.7F, 0.6F, random.nextLong());
            SanityAPI.add(player, ModConfig.EVENT_CULT_PROCESSION_SANITY_RATE.get());
        }
    }

    // --- blood_moon_rite -------------------------------------------------------------------

    static EldritchEvent bloodMoonRite() {
        return new EldritchEvent("blood_moon_rite", EventTrigger.FULL_MOON_MARKED,
                ModConfig.EVENT_BLOOD_MOON_DURATION.get(), 25,
                ModConfig.EVENT_BLOOD_MOON_COOLDOWN.get(),
                ctx -> ctx.fullMoon()
                        && ctx.corruption().ordinal() >= CorruptionState.MARKED.ordinal(),
                EventEffects::bloodMoonRiteEffect);
    }

    private static void bloodMoonRiteEffect(ServerPlayer player, EventContext ctx, int elapsed,
                                            int duration, int tick) {
        if (elapsed % 20 == 0) {
            // Mirror rift_bloom: raise taint in the loaded 3x3 chunk patch around the player.
            taintPatch(ctx, player.chunkPosition(), ModConfig.EVENT_BLOOD_MOON_TAINT_RATE.get());
        }
        int interval = Math.max(1, ModConfig.EVENT_BLOOD_MOON_SPAWN_INTERVAL.get());
        if (elapsed % interval == 0) {
            eventTopUp(player, ctx, ModEntities.CULT_ZEALOT.get(), CultZealot.class, EVENT_SPAWN_RADIUS,
                    ModConfig.EVENT_BLOOD_MOON_CULT_CAP.get(), 1, tick, DARK_OR_TAINTED);
            // The hag rides the same cadence at half rate: only every second top-up pass.
            if (elapsed % (interval * 2) == 0) {
                eventTopUp(player, ctx, ModEntities.NIGHT_HAG.get(), NightHag.class, EVENT_SPAWN_RADIUS,
                        ModConfig.EVENT_BLOOD_MOON_HAG_CAP.get(), 1, tick, DARK_OR_TAINTED);
            }
        }
        if (elapsed % 40 == 0) {
            RandomSource random = seed(player, ctx, elapsed);
            ctx.level().sendParticles(ParticleTypes.CRIMSON_SPORE,
                    player.getX(), player.getY() + 1.0, player.getZ(), 12, 1.2, 0.8, 1.2, 0.01);
            sendSound(player, SoundEvents.WARDEN_ROAR, player.getX(), player.getY(), player.getZ(),
                    SoundSource.AMBIENT, 1.0F, 0.5F, random.nextLong());
            SanityAPI.add(player, ModConfig.EVENT_BLOOD_MOON_SANITY_RATE.get());
        }
    }

    // --- star_fall -------------------------------------------------------------------------
    // Design/19 asks for a meteorite of star_reagent, so this is deliberately the one event that
    // edits world blocks: a tiny, config-bounded crater, loaded chunks only.

    static EldritchEvent starFall() {
        return new EldritchEvent("star_fall", EventTrigger.MARKED_RANDOM,
                ModConfig.EVENT_STAR_FALL_DURATION.get(), 30,
                ModConfig.EVENT_STAR_FALL_COOLDOWN.get(),
                ctx -> ctx.corruption().ordinal() >= CorruptionState.MARKED.ordinal(),
                EventEffects::starFallEffect);
    }

    private static void starFallEffect(ServerPlayer player, EventContext ctx, int elapsed,
                                       int duration, int tick) {
        if (elapsed % 20 == 0) {
            SanityAPI.add(player, ModConfig.EVENT_STAR_FALL_SANITY_RATE.get());
        }
        // Key on elapsed==1: that is the first tick the effect runs for a naturally triggered
        // event (the ticker advances after start). A force-started event may also report elapsed==0
        // on its start tick, so keying on 1 makes the one-shot impact fire exactly once either way.
        if (elapsed != 1) {
            return;
        }
        RandomSource random = seed(player, ctx, elapsed);
        int radius = ModConfig.EVENT_STAR_FALL_RADIUS.get();
        int x = player.getBlockX() + random.nextInt(radius * 2 + 1) - radius;
        int z = player.getBlockZ() + random.nextInt(radius * 2 + 1) - radius;
        BlockPos impact = BestiarySupport.surfaceSpot(ctx.level(), x, z, false);
        if (impact == null) {
            return; // never force-load or generate a chunk just to plant a meteor
        }
        carveCrater(ctx.level(), impact, random);
        ctx.level().sendParticles(ParticleTypes.EXPLOSION_EMITTER, impact.getX() + 0.5,
                impact.getY() + 1.0, impact.getZ() + 0.5, 3, 1.5, 1.0, 1.5, 0.0);
        sendSound(player, SoundEvents.GENERIC_EXPLODE.value(), impact.getX() + 0.5, impact.getY() + 1.0,
                impact.getZ() + 0.5, SoundSource.BLOCKS, 1.5F, 0.8F, random.nextLong());
        Block.popResource(ctx.level(), impact, new ItemStack(
                BuiltInRegistries.ITEM.getValue(EldritchHorror.id("star_reagent")), 2));
    }

    /** Carves the bounded crater, never replacing more than {@code eventStarFallCraterBlocks}. */
    private static void carveCrater(ServerLevel level, BlockPos impact, RandomSource random) {
        int craterRadius = ModConfig.EVENT_STAR_FALL_CRATER_RADIUS.get();
        int maxBlocks = ModConfig.EVENT_STAR_FALL_CRATER_BLOCKS.get();
        Block[] materials = {Blocks.MAGMA_BLOCK, Blocks.CRYING_OBSIDIAN, Blocks.OBSIDIAN};
        int changed = 0;
        for (int dx = -craterRadius; dx <= craterRadius && changed < maxBlocks; dx++) {
            for (int dz = -craterRadius; dz <= craterRadius && changed < maxBlocks; dz++) {
                int distSq = dx * dx + dz * dz;
                if (distSq > craterRadius * craterRadius) {
                    continue;
                }
                int x = impact.getX() + dx;
                int z = impact.getZ() + dz;
                if (!level.hasChunkAt(x, z)) {
                    continue; // loaded chunks only
                }
                // getHeight(OCEAN_FLOOR) is the first air y, so the ground surface is one below.
                int surfaceY = level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z) - 1;
                if (surfaceY <= level.getMinY()) {
                    continue;
                }
                BlockPos surface = new BlockPos(x, surfaceY, z);
                if (!level.isLoaded(surface)) {
                    continue;
                }
                level.setBlock(surface, materials[random.nextInt(materials.length)].defaultBlockState(),
                        Block.UPDATE_ALL);
                changed++;
                // Clear the two ejecta blocks above the new surface.
                for (int i = 1; i <= 2 && changed < maxBlocks; i++) {
                    BlockPos above = surface.above(i);
                    if (level.isLoaded(above) && !level.getBlockState(above).isAir()) {
                        level.setBlock(above, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                        changed++;
                    }
                }
            }
        }
    }

    // --- hollow_call -----------------------------------------------------------------------

    static EldritchEvent hollowCall() {
        return new EldritchEvent("hollow_call", EventTrigger.CLAIMED_CORRUPTION,
                ModConfig.EVENT_HOLLOW_CALL_DURATION.get(), 40,
                ModConfig.EVENT_HOLLOW_CALL_COOLDOWN.get(),
                ctx -> ctx.corruption() == CorruptionState.CLAIMED,
                EventEffects::hollowCallEffect);
    }

    private static void hollowCallEffect(ServerPlayer player, EventContext ctx, int elapsed,
                                         int duration, int tick) {
        if (elapsed % 20 == 0) {
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0, true, false, false));
            SanityAPI.add(player, ModConfig.EVENT_HOLLOW_CALL_SANITY_RATE.get());
        }
        if (elapsed % 60 == 0) {
            RandomSource random = seed(player, ctx, elapsed);
            SoundEvent sound = random.nextBoolean()
                    ? SoundEvents.WARDEN_NEARBY_CLOSER : SoundEvents.ELDER_GUARDIAN_CURSE;
            sendSound(player, sound, player.getX(), player.getY(), player.getZ(),
                    SoundSource.AMBIENT, 0.8F, 0.5F, random.nextLong());
            ctx.level().sendParticles(ParticleTypes.SCULK_SOUL, player.getX(), player.getY() + 1.0,
                    player.getZ(), 8, 0.6, 0.8, 0.6, 0.01);
        }
        int interval = Math.max(1, ModConfig.EVENT_HOLLOW_CALL_SPAWN_INTERVAL.get());
        if (elapsed % interval == 0) {
            eventTopUp(player, ctx, ModEntities.NIGHT_HAG.get(), NightHag.class, EVENT_SPAWN_RADIUS,
                    ModConfig.EVENT_HOLLOW_CALL_HAG_CAP.get(), 1, tick, DARK_OR_TAINTED);
            eventTopUp(player, ctx, ModEntities.CHOIR_SPITE.get(), ChoirSpite.class, EVENT_SPAWN_RADIUS,
                    ModConfig.EVENT_HOLLOW_CALL_SPITE_CAP.get(), 1, tick, DARK_OR_TAINTED);
        }
    }

    // --- shared helpers --------------------------------------------------------------------

    /** One bounded spawn pass for {@code type} through the shared {@link BestiarySupport#topUp}. */
    private static <T extends Mob> void eventTopUp(ServerPlayer player, EventContext ctx,
                                                   EntityType<T> type, Class<T> typeClass, int radius,
                                                   int cap, int perPass, int tick,
                                                   BestiarySupport.SpawnGate gate) {
        BestiarySupport.topUp(ctx.level(), ctx.level().getChunkSource(), player, type, typeClass,
                radius, cap, perPass, tick, gate);
    }

    /** Raises taint in the loaded 3x3 chunk patch around {@code centre} (the rift_bloom idiom). */
    private static void taintPatch(EventContext ctx, ChunkPos centre, double rate) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                TaintAPI.add(ctx.level(), new ChunkPos(centre.x() + dx, centre.z() + dz), rate);
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
