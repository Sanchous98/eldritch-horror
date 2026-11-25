package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.corruption.CorruptionAPI;
import com.sanchous98.eldritchhorror.entity.combat.BossAbility;
import com.sanchous98.eldritchhorror.entity.combat.BossAbilities;
import com.sanchous98.eldritchhorror.entity.combat.SimpleBossAbility;
import com.sanchous98.eldritchhorror.registry.ModEntities;
import com.sanchous98.eldritchhorror.registry.ModSounds;
import com.sanchous98.eldritchhorror.sanity.SanityAPI;
import com.sanchous98.eldritchhorror.world.ward.WardLedger;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Shub-Niggurath — a pure biome presence, replacing "none" ({@code design/28-ancient-ones.md},
 * roster ★3). Slow, massive and low-mobility by design: it is not a fight so much as a place the
 * world has become wrong. Unlike the other Ancient Ones it does <b>not</b> use the shared
 * {@link BestiarySupport#tickAura} single-axis pass; because its two axes depend on what the
 * <i>player</i> is doing it runs its own once-per-second pulse in {@link #customServerAiStep}:
 * <ul>
 *   <li><b>corruption</b> over a wide radius — running (sprinting) within it costs more;</li>
 *   <li><b>sanity</b> drains only while the player is standing still.</li>
 * </ul>
 * That is the design's "standing still costs sanity, running costs corruption" in one place. Model
 * reuse: the vanilla {@code RavagerModel}.
 */
public final class ShubNiggurath extends AncientOne {

    public ShubNiggurath(EntityType<? extends ShubNiggurath> type, Level level) {
        super(type, level, BossEvent.BossBarColor.GREEN, Component.translatable("entity.eldritch_horror.shub_niggurath"));
        this.xpReward = 40;
        if (!ModConfig.SHUB_NIGGURATH_KILLABLE.get()) {
            this.setPermanentlyInvulnerable(true);
        }
    }

    /** 200 HP, 5 attack, 0.12 speed (barely walks), 48 follow range, very heavy. */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 200.0)
                .add(Attributes.ATTACK_DAMAGE, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.12)
                .add(Attributes.FOLLOW_RANGE, 48.0)
                .add(Attributes.ARMOR, 10.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    /** A major presence: the Black Goat is a biome-scale encounter. */
    @Override
    public boolean majorPresence() {
        return true;
    }

    // --- Aura (both axes; see class doc) --------------------------------------------------------

    @Override
    public Axis auraAxis() {
        return Axis.CORRUPTION;
    }

    @Override
    public boolean isAuraActive() {
        return !isSolved() && masterAuraEnabled()
                && ModConfig.ENABLE_CORRUPTION_SHUB_NIGGURATH.get();
    }

    @Override
    public double auraRate() {
        return ModConfig.CORRUPTION_SHUB_NIGGURATH_RATE.get();
    }

    @Override
    public double auraRadius() {
        return ModConfig.CORRUPTION_SHUB_NIGGURATH_RADIUS.get();
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        pulseAura(level);
    }

    /** Stilled: the woods' breathing fades for a time. */
    @Override
    protected void onSolved(Solve kind) {
        if (kind == Solve.STILLED) {
            this.playSound(SoundEvents.RAVAGER_ROAR, 1.0F, 0.3F);
        }
    }

    /** Once per second: the wide corruption aura (sprint-aware) plus the standing-still sanity drain. */
    private void pulseAura(ServerLevel level) {
        if (!this.isAlive() || level.getGameTime() % 20L != 0L) {
            return;
        }
        // A stilled presence holds its breath for a while (design/28 non-combat solve).
        if (isSolved()) {
            return;
        }
        double radiusSqr = this.auraRadius() * this.auraRadius();
        boolean corruptionOn = this.isAuraActive();
        boolean sanityOn = ModConfig.ENABLE_SANITY.get();
        for (ServerPlayer player : level.players()) {
            if (player.isCreative() || player.isSpectator()) {
                continue;
            }
            if (player.distanceToSqr(this) > radiusSqr) {
                continue;
            }
            boolean moving = player.getDeltaMovement().horizontalDistanceSqr() >= 1.0E-4;
            if (corruptionOn) {
                double rate = this.auraRate()
                        + (player.isSprinting() ? ModConfig.CORRUPTION_SHUB_NIGGURATH_SPRINT_RATE.get() : 0.0);
                CorruptionAPI.add(player, rate);
            }
            if (sanityOn && !moving && !WardLedger.shelters(player)) {
                SanityAPI.add(player, ModConfig.SANITY_SHUB_NIGGURATH_STILL_RATE.get());
            }
        }
    }

    // --- Abilities (BossCombat) -----------------------------------------------------------------

    @Override
    protected List<BossAbility> abilities() {
        return List.of(
                SimpleBossAbility.builder("thousand_young")
                        .cooldown(200)
                        .telegraph(30)
                        .range(24.0)
                        .warning("The Black Goat's young are born — clear them out!")
                        .sound(SoundEvents.RAVAGER_ROAR)
                        .onTelegraph((boss, level, target, ticksLeft) -> {
                            BossAbilities.telegraphBurst(level, boss, ParticleTypes.SPORE_BLOSSOM_AIR, 5);
                            BossAbilities.telegraphAt(boss, level, target, ParticleTypes.CRIMSON_SPORE, 3, 1.1);
                        })
                        .onExecute((boss, level, target) ->
                                boss.bossCombat().summon(level, ModEntities.LESSER_SWARM.get(), 3, target))
                        .build(),
                SimpleBossAbility.builder("entangling_roots")
                        .cooldown(190)
                        .telegraph(32)
                        .range(16.0)
                        .minPhase(Phase.STIRRING)
                        .warning("Roots take your feet — break free!")
                        .sound(SoundEvents.RAVAGER_ROAR)
                        .onTelegraph((boss, level, target, ticksLeft) -> {
                            BossAbilities.ring(boss, level, target.position(), 4.5, ParticleTypes.SPORE_BLOSSOM_AIR, 14);
                            BossAbilities.telegraphAt(boss, level, target, ParticleTypes.WARPED_SPORE, 3, 0.9);
                        })
                        .onExecute((boss, level, target) -> {
                            BossAbilities.effectAround(boss, level, target.position(), 5.0, MobEffects.SLOWNESS, 120, 3);
                            BossAbilities.damageAround(boss, level, target.position(), 5.0, 6.0F);
                        })
                        .build(),
                SimpleBossAbility.builder("the_woods_breathe")
                        .cooldown(240)
                        .telegraph(38)
                        .range(18.0)
                        .minPhase(Phase.ENRAGED)
                        .warning("The woods breathe in — get clear!")
                        .sound(SoundEvents.RAVAGER_ROAR)
                        .onTelegraph((boss, level, target, ticksLeft) -> {
                            BossAbilities.ring(boss, level, target.position(), 6.5, ParticleTypes.SPORE_BLOSSOM_AIR, 18);
                            BossAbilities.telegraphAt(boss, level, target, ParticleTypes.ASH, 4, 1.4);
                        })
                        .onExecute((boss, level, target) -> {
                            BossAbilities.damageAround(boss, level, target.position(), 7.0, 8.0F);
                            BossAbilities.effectAround(boss, level, target.position(), 7.0, MobEffects.POISON, 120, 0);
                            BossAbilities.effectAround(boss, level, target.position(), 7.0, MobEffects.NAUSEA, 140, 0);
                        })
                        .build()
        );
    }

    // --- Presence -------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.SHUB_NIGGURATH_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.SHUB_NIGGURATH_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.SHUB_NIGGURATH_DEATH.get();
    }

    /** The woods breathing: slow, vast, hard to place. */
    @Override
    protected float getSoundVolume() {
        return 4.0F;
    }

    /** Rarer than the default: a biome presence, not a resident that chatters. */
    @Override
    public int getAmbientSoundInterval() {
        return 200;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
        this.playSound(SoundEvents.RAVAGER_STEP, 0.5F, 0.4F);
    }
}
