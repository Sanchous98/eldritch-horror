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
 * Yog-Sothoth — the gate at the <b>order_vault</b> on the observatory plateau
 * ({@code design/20-map.md} places the vault on `observatory_plateau`), replacing the Ender Dragon
 * ({@code design/28-ancient-ones.md}: "the gate <i>is</i> the boss"). It is deliberately
 * <b>stationary-ish</b>: the shared {@link AncientOne} goals give it a very slow walk and no
 * aggressive chase, and its hook is a <b>large</b> sanity aura (the gate takes a step toward you for
 * every step you take toward it). The non-combat solve is sealing the gate
 * ({@code seal_the_gate}, resolve {@link Solve#SOOTHED} — the gate falls shut and stands inert rather
 * than dying).
 *
 * <p>Spawn attachment is the shared, bounded {@link SitePopulationSpawner} {@code order_vault}
 * branch: the vault is the existing site that stands on the observatory plateau (design/20), so the
 * gate attaches there; no per-boss ticker. Placeholder model: the vanilla wither model (a floating,
 * many-part silhouette) — see {@code YogSothothRenderer}.
 */
public final class YogSothoth extends AncientOne {

    public YogSothoth(EntityType<? extends YogSothoth> type, Level level) {
        super(type, level, BossEvent.BossBarColor.WHITE, Component.translatable("entity.eldritch_horror.yog_sothoth"));
        this.xpReward = 70;
        if (!ModConfig.YOG_SOTHOTH_KILLABLE.get()) {
            this.setPermanentlyInvulnerable(true);
        }
    }

    /** 300 HP, 8 attack, 0.06 speed (the gate barely moves), 64 follow range, immovable. */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 300.0)
                .add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.MOVEMENT_SPEED, 0.06)
                .add(Attributes.FOLLOW_RANGE, 64.0)
                .add(Attributes.ARMOR, 8.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    /** A major presence: the gate is an endgame-scale encounter. */
    @Override
    public boolean majorPresence() {
        return true;
    }

    // --- Aura: SANITY (a wide, patient drain) ---------------------------------------------------

    @Override
    public Axis auraAxis() {
        return Axis.SANITY;
    }

    @Override
    public boolean isAuraActive() {
        // Sealed: the gate is shut and no longer draws the eye.
        return !isSolved() && masterAuraEnabled() && ModConfig.ENABLE_SANITY_YOG_SOTHOTH.get();
    }

    @Override
    public double auraRate() {
        return ModConfig.SANITY_YOG_SOTHOTH_RATE.get();
    }

    @Override
    public double auraRadius() {
        return ModConfig.SANITY_YOG_SOTHOTH_RADIUS.get();
    }

    /** Sealed: the gate falls shut with a sound like a held breath released. */
    @Override
    protected void onSolved(Solve kind) {
        if (kind == Solve.SOOTHED) {
            this.playSound(ModSounds.YOG_SOTHOTH_DEATH.get(), 1.5F, 0.4F);
        }
    }

    // --- Abilities (BossCombat) -----------------------------------------------------------------

    @Override
    protected List<BossAbility> abilities() {
        return List.of(
                SimpleBossAbility.builder("gate_step")
                        .cooldown(190)
                        .telegraph(30)
                        .range(20.0)
                        .warning("The gate opens beside you — move!")
                        .sound(SoundEvents.ENDERMAN_SCREAM)
                        .onTelegraph((boss, level, target, ticksLeft) -> {
                            BossAbilities.telegraphAt(boss, level, target, ParticleTypes.PORTAL, 5, 1.2);
                            BossAbilities.telegraphBurst(level, boss, ParticleTypes.REVERSE_PORTAL, 4);
                        })
                        .onExecute((boss, level, target) -> {
                            BossAbilities.teleportBeside(boss, level, target);
                            BossAbilities.hit(boss, level, target, 7.0F);
                            BossAbilities.effect(target, MobEffects.SLOWNESS, 80, 1);
                        })
                        .build(),
                SimpleBossAbility.builder("key_and_gate")
                        .cooldown(200)
                        .telegraph(34)
                        .range(18.0)
                        .minPhase(Phase.STIRRING)
                        .warning("The gate draws you in — break away!")
                        .sound(SoundEvents.ENDERMAN_SCREAM)
                        .onTelegraph((boss, level, target, ticksLeft) -> {
                            BossAbilities.telegraphAt(boss, level, target, ParticleTypes.END_ROD, 4, 1.2);
                            BossAbilities.ring(boss, level, target.position(), 3.0, ParticleTypes.PORTAL, 12);
                        })
                        .onExecute((boss, level, target) -> {
                            BossAbilities.pull(boss, target, 0.9, 0.2);
                            BossAbilities.effect(target, MobEffects.LEVITATION, 60, 0);
                            BossAbilities.hit(boss, level, target, 6.0F);
                        })
                        .build(),
                SimpleBossAbility.builder("the_gate_opens")
                        .cooldown(260)
                        .telegraph(44)
                        .range(16.0)
                        .minPhase(Phase.ENRAGED)
                        .warning("Yog-Sothoth opens — get clear!")
                        .sound(SoundEvents.WARDEN_SONIC_BOOM)
                        .onTelegraph((boss, level, target, ticksLeft) -> {
                            BossAbilities.ring(boss, level, target.position(), 7.0, ParticleTypes.REVERSE_PORTAL, 20);
                            BossAbilities.telegraphBurst(level, boss, ParticleTypes.END_ROD, 6);
                        })
                        .onExecute((boss, level, target) -> {
                            BossAbilities.damageAround(boss, level, target.position(), 7.0, 10.0F);
                            BossAbilities.effectAround(boss, level, target.position(), 7.0, MobEffects.WITHER, 120, 0);
                            BossAbilities.effectAround(boss, level, target.position(), 7.0, MobEffects.BLINDNESS, 100, 0);
                        })
                        .build()
        );
    }

    // --- Presence -------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.YOG_SOTHOTH_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.YOG_SOTHOTH_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.YOG_SOTHOTH_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 3.5F;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 180;
    }
}
