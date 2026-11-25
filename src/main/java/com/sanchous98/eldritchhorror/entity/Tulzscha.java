package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.entity.combat.BossAbility;
import com.sanchous98.eldritchhorror.entity.combat.BossAbilities;
import com.sanchous98.eldritchhorror.entity.combat.SimpleBossAbility;
import com.sanchous98.eldritchhorror.registry.ModSounds;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
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

/**
 * Tulzscha — the <b>green flame at the centre</b> of the rift_scar
 * ({@code design/28-ancient-ones.md}). Looking into it costs what you remember, so its aura is
 * <b>sanity</b>, and the non-combat solve ({@code quench_tulzscha}, resolve {@link Solve#SOOTHED})
 * closes the flame's eye and lets memory return.
 *
 * <p>Spawn attachment is the shared, bounded {@code SitePopulationSpawner} {@code rift_scar} case;
 * no per-boss ticker. Placeholder model: the vanilla blaze.
 */
public final class Tulzscha extends AncientOne {

    public Tulzscha(EntityType<? extends Tulzscha> type, Level level) {
        super(type, level, BossEvent.BossBarColor.GREEN, Component.translatable("entity.eldritch_horror.tulzscha"));
        this.xpReward = 50;
        if (!ModConfig.TULZSCHA_KILLABLE.get()) {
            this.setPermanentlyInvulnerable(true);
        }
    }

    /** 130 HP, 7 attack, 0.30 speed (a dancing flame), 40 follow range, low armour. */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 130.0)
                .add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.MOVEMENT_SPEED, 0.30)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.ARMOR, 3.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.3);
    }

    // --- Aura: SANITY (looking costs what you remember) -----------------------------------------

    @Override
    public Axis auraAxis() {
        return Axis.SANITY;
    }

    @Override
    public boolean isAuraActive() {
        // Quenched: the eye closes and memory stops being taken.
        return !isSolved() && masterAuraEnabled() && ModConfig.ENABLE_SANITY_TULZSCHA.get();
    }

    @Override
    public double auraRate() {
        // It flares when threatened: the enraged band burns memory faster.
        double base = ModConfig.SANITY_TULZSCHA_RATE.get();
        return this.phase() == Phase.ENRAGED ? base * 1.5 : base;
    }

    @Override
    public double auraRadius() {
        return ModConfig.SANITY_TULZSCHA_RADIUS.get();
    }

    /** Quenched: the green flame closes its eye; memory returns. */
    @Override
    protected void onSolved(Solve kind) {
        if (kind == Solve.SOOTHED) {
            this.playSound(ModSounds.TULZSCHA_DEATH.get(), 1.0F, 0.4F);
        }
    }

    // --- Abilities (BossCombat) -----------------------------------------------------------------

    @Override
    protected List<BossAbility> abilities() {
        return List.of(
                SimpleBossAbility.builder("green_flare")
                        .cooldown(130)
                        .telegraph(20)
                        .range(14.0)
                        .warning("The green flame flares — step out!")
                        .sound(SoundEvents.BLAZE_SHOOT)
                        .onTelegraph((boss, level, target, ticksLeft) -> {
                            BossAbilities.telegraphBurst(level, boss, ParticleTypes.SOUL_FIRE_FLAME, 4);
                            BossAbilities.ring(boss, level, target.position(), 4.5, ParticleTypes.SMALL_FLAME, 12);
                        })
                        .onExecute((boss, level, target) -> {
                            BossAbilities.damageAround(boss, level, target.position(), 5.0, 6.0F);
                            for (var player : BossAbilities.playersAround(boss, level, target.position(), 5.0)) {
                                BossAbilities.burn(player, 4.0F);
                            }
                        })
                        .build(),
                SimpleBossAbility.builder("memory_burn")
                        .cooldown(150)
                        .telegraph(22)
                        .range(12.0)
                        .minPhase(Phase.STIRRING)
                        .warning("Memory burns — look away!")
                        .sound(SoundEvents.BLAZE_SHOOT)
                        .onTelegraph((boss, level, target, ticksLeft) ->
                                BossAbilities.telegraphAt(boss, level, target, ParticleTypes.SOUL_FIRE_FLAME, 4, 1.0))
                        .onExecute((boss, level, target) -> {
                            BossAbilities.effectAround(boss, level, target.position(), 5.0, MobEffects.BLINDNESS, 100, 0);
                            BossAbilities.effectAround(boss, level, target.position(), 5.0, MobEffects.NAUSEA, 120, 0);
                            BossAbilities.damageAround(boss, level, target.position(), 5.0, 4.0F);
                        })
                        .build()
        );
    }

    // --- Presence -------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.TULZSCHA_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.TULZSCHA_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.TULZSCHA_DEATH.get();
    }

    /** A flame at the centre of the rift_scar: it carries far. */
    @Override
    protected float getSoundVolume() {
        return 2.2F;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 145;
    }
}
