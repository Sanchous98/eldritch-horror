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
 * Nyogtha — "it is under the floor; you can only hear it" in the <b>deep caves</b>
 * ({@code design/28-ancient-ones.md}). It is the one presence that is never really <i>seen</i>: it
 * spawns only deep underground, on loaded cave floor, and its <b>sanity</b> aura is a sound heard
 * through stone rather than a sight. The non-combat solve is a rite ({@code bar_nyogtha}, resolve
 * {@link Solve#SOOTHED} — the way below is barred and the hearing stops).
 *
 * <p>Spawn attachment is the shared, bounded {@link AncientOneSpawner} {@code deep} pass (cave-floor
 * loaded ground below the configured depth); no per-boss ticker. Placeholder model: the vanilla
 * silverfish (a segmented thing under the floor).
 */
public final class Nyogtha extends AncientOne {

    public Nyogtha(EntityType<? extends Nyogtha> type, Level level) {
        super(type, level, BossEvent.BossBarColor.WHITE, Component.translatable("entity.eldritch_horror.nyogtha"));
        this.xpReward = 45;
        if (!ModConfig.NYOGTHA_KILLABLE.get()) {
            this.setPermanentlyInvulnerable(true);
        }
    }

    /** 140 HP, 6 attack, 0.20 speed, 40 follow range, heavy armour (a thing of stone). */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 140.0)
                .add(Attributes.ATTACK_DAMAGE, 11.0)
                .add(Attributes.MOVEMENT_SPEED, 0.20)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.ARMOR, 8.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.7);
    }

    // --- Aura: SANITY (a sound through the stone) ----------------------------------------------

    @Override
    public Axis auraAxis() {
        return Axis.SANITY;
    }

    @Override
    public boolean isAuraActive() {
        return !isSolved() && masterAuraEnabled() && ModConfig.ENABLE_SANITY_NYOGTHA.get();
    }

    @Override
    public double auraRate() {
        return ModConfig.SANITY_NYOGTHA_RATE.get();
    }

    @Override
    public double auraRadius() {
        return ModConfig.SANITY_NYOGTHA_RADIUS.get();
    }

    /** Barred: the way below is shut and the hearing stops. */
    @Override
    protected void onSolved(Solve kind) {
        if (kind == Solve.SOOTHED) {
            this.playSound(ModSounds.NYOGTHA_DEATH.get(), 1.0F, 0.5F);
        }
    }

    // --- Abilities (BossCombat) -----------------------------------------------------------------

    @Override
    protected List<BossAbility> abilities() {
        return List.of(
                SimpleBossAbility.builder("tendrils_from_below")
                        .cooldown(160)
                        .telegraph(28)
                        .range(14.0)
                        .warning("Tendrils rise from below — move!")
                        .sound(SoundEvents.WARDEN_SONIC_CHARGE)
                        .onTelegraph((boss, level, target, ticksLeft) -> {
                            BossAbilities.telegraphAt(level, target, ParticleTypes.SQUID_INK, 4, 1.0);
                            BossAbilities.ring(level, target.position(), 2.5, ParticleTypes.SCULK_SOUL, 10);
                        })
                        .onExecute((boss, level, target) -> {
                            target.push(0.0, -0.8, 0.0);
                            BossAbilities.effect(target, MobEffects.DARKNESS, 100, 0);
                            BossAbilities.effect(target, MobEffects.SLOWNESS, 100, 1);
                            BossAbilities.hit(boss, level, target, 5.0F);
                        })
                        .build(),
                SimpleBossAbility.builder("black_ichor")
                        .cooldown(180)
                        .telegraph(30)
                        .range(12.0)
                        .minPhase(Phase.STIRRING)
                        .warning("Black ichor sprays — get out of the cone!")
                        .sound(SoundEvents.WARDEN_ROAR)
                        .onTelegraph((boss, level, target, ticksLeft) ->
                                BossAbilities.telegraphBurst(level, boss, ParticleTypes.SQUID_INK, 5))
                        .onExecute((boss, level, target) -> BossAbilities.cone(level, boss, 12.0, 70.0, player -> {
                            BossAbilities.venom(player, 120, 0);
                            BossAbilities.effect(player, MobEffects.WITHER, 100, 0);
                            BossAbilities.hit(boss, level, player, 5.0F);
                        }))
                        .build()
        );
    }

    // --- Presence -------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.NYOGTHA_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.NYOGTHA_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.NYOGTHA_DEATH.get();
    }

    /** Heard through the floor: quiet, low, impossible to place. */
    @Override
    protected float getSoundVolume() {
        return 2.0F;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 60;
    }
}
