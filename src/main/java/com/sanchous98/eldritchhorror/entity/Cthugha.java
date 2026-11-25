package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.entity.combat.BossAbility;
import com.sanchous98.eldritchhorror.entity.combat.BossAbilities;
import com.sanchous98.eldritchhorror.entity.combat.SimpleBossAbility;
import com.sanchous98.eldritchhorror.registry.ModEntities;
import com.sanchous98.eldritchhorror.registry.ModSounds;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * Cthugha — "flame that watches; light no longer comforts" in the <b>ashen_waste</b>
 * ({@code design/28-ancient-ones.md}). Its hook is a corruption vector that <b>deepens in the light</b>:
 * the bright, safe ground a player retreats to is exactly where the watching flame taints most
 * ({@link #auraRateFor}). The non-combat solve is a ward ({@code quench_cthugha}, resolve
 * {@link Solve#STILLED} — the flame is quenched and the watching stops for a time).
 *
 * <p>Spawn attachment is the shared, bounded {@link AncientOneSpawner} {@code ashen} pass (tainted
 * loaded ground, the ashen waste); no per-boss ticker. Placeholder model: the vanilla blaze.
 */
public final class Cthugha extends AncientOne {

    /** Bright light no longer protects: from this light level up, the taint deepens. */
    private static final int BRIGHT_LIGHT = 12;

    public Cthugha(EntityType<? extends Cthugha> type, Level level) {
        super(type, level, BossEvent.BossBarColor.RED, Component.translatable("entity.eldritch_horror.cthugha"));
        this.xpReward = 45;
        if (!ModConfig.CTHUGHA_KILLABLE.get()) {
            this.setPermanentlyInvulnerable(true);
        }
    }

    /** 130 HP, 6 attack, 0.24 speed, 40 follow range, low armour (a fire, not a brute). */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 130.0)
                .add(Attributes.ATTACK_DAMAGE, 11.0)
                .add(Attributes.MOVEMENT_SPEED, 0.24)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.ARMOR, 2.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.3);
    }

    // --- Aura: CORRUPTION (the watching flame; worse in the light) ------------------------------

    @Override
    public Axis auraAxis() {
        return Axis.CORRUPTION;
    }

    @Override
    public boolean isAuraActive() {
        return !isSolved() && masterAuraEnabled() && ModConfig.ENABLE_CORRUPTION_CTHUGHA.get();
    }

    @Override
    public double auraRate() {
        return ModConfig.CORRUPTION_CTHUGHA_RATE.get();
    }

    @Override
    public double auraRadius() {
        return ModConfig.CORRUPTION_CTHUGHA_RADIUS.get();
    }

    /** Light no longer comforts: standing in brightness deepens the taint instead of warding it. */
    @Override
    public double auraRateFor(ServerPlayer player) {
        boolean bright = player.level().getMaxLocalRawBrightness(player.blockPosition()) >= BRIGHT_LIGHT;
        return bright ? auraRate() * 1.5 : auraRate();
    }

    /** Quenched: the watching flame dims for a time. */
    @Override
    protected void onSolved(Solve kind) {
        if (kind == Solve.STILLED) {
            this.playSound(ModSounds.CTHUGHA_DEATH.get(), 1.0F, 0.6F);
        }
    }

    // --- Abilities (BossCombat) -----------------------------------------------------------------

    @Override
    protected List<BossAbility> abilities() {
        return List.of(
                SimpleBossAbility.builder("flame_burst")
                        .cooldown(140)
                        .telegraph(20)
                        .range(12.0)
                        .warning("Cthugha flares — step out of the ring!")
                        .sound(SoundEvents.BLAZE_SHOOT)
                        .onTelegraph((boss, level, target, ticksLeft) -> {
                            BossAbilities.telegraphBurst(level, boss, ParticleTypes.FLAME, 4);
                            BossAbilities.ring(boss, level, target.position(), 4.5, ParticleTypes.SMALL_FLAME, 12);
                        })
                        .onExecute((boss, level, target) -> {
                            BossAbilities.damageAround(boss, level, target.position(), 5.0, 6.0F);
                            for (var player : BossAbilities.playersAround(boss, level, target.position(), 5.0)) {
                                BossAbilities.burn(player, 4.0F);
                            }
                        })
                        .build(),
                SimpleBossAbility.builder("living_flame")
                        .cooldown(180)
                        .telegraph(22)
                        .range(20.0)
                        .minPhase(Phase.STIRRING)
                        .warning("Living flame spills from the fire!")
                        .sound(SoundEvents.BLAZE_SHOOT)
                        .onTelegraph((boss, level, target, ticksLeft) -> {
                            BossAbilities.telegraphBurst(level, boss, ParticleTypes.FLAME, 5);
                            BossAbilities.telegraphAt(boss, level, target, ParticleTypes.SMALL_FLAME, 3, 1.0);
                        })
                        .onExecute((boss, level, target) -> {
                            boss.bossCombat().summon(level, ModEntities.ASHEN_WISP.get(), 2, target);
                            BossAbilities.burn(target, 3.0F);
                            BossAbilities.hit(boss, level, target, 4.0F);
                        })
                        .build()
        );
    }

    // --- Presence -------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.CTHUGHA_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.CTHUGHA_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.CTHUGHA_DEATH.get();
    }

    /** A fire's roar, carrying across the waste. */
    @Override
    protected float getSoundVolume() {
        return 3.0F;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 90;
    }
}
