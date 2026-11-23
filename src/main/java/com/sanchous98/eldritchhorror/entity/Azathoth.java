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
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Azathoth — the blind, idiot god at the centre of the <b>rift_scar</b>/Veil, replacing the Wither
 * ({@code design/28-ancient-ones.md}, roster "no fight — the music at the centre unmakes the will to
 * act"). It is the purest expression of the rule that an Ancient One is a <i>presence</i>, not a
 * health bar:
 *
 * <ul>
 *   <li><b>No melee.</b> {@link #registerGoals} deliberately omits the attack and target goals the
 *       shared {@link AncientOne} base installs, so it never swings — only {@link FloatGoal} and
 *       the look goals remain.</li>
 *   <li><b>Hard sanity drain</b> over a wide radius (the music unmakes the will to act).</li>
 *   <li><b>Non-combat solve:</b> a rite that stills the music ({@code still_azathoth}, resolve
 *       {@link Solve#SOOTHED}). Once soothed it is awake but no longer drains.</li>
 * </ul>
 *
 * <p>Spawn attachment reuses the shared, bounded {@link SitePopulationSpawner} {@code rift_scar}
 * case (one loaded-only presence per player, capped); there is no per-boss ticker. Model reuse is
 * the vanilla wither model as a placeholder.
 */
public final class Azathoth extends AncientOne {

    public Azathoth(EntityType<? extends Azathoth> type, Level level) {
        super(type, level, BossEvent.BossBarColor.PURPLE, Component.translatable("entity.eldritch_horror.azathoth"));
        this.xpReward = 60;
        if (!ModConfig.AZATHOTH_KILLABLE.get()) {
            this.setPermanentlyInvulnerable(true);
        }
    }

    /** 250 HP, no real melee (attack is vestigial), barely moves, 64 follow range, very heavy. */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 250.0)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.MOVEMENT_SPEED, 0.10)
                .add(Attributes.FOLLOW_RANGE, 64.0)
                .add(Attributes.ARMOR, 12.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    /** A major presence: the blind god at the centre is an endgame-scale dread. */
    @Override
    public boolean majorPresence() {
        return true;
    }

    /** No fight: only float and look goals; never a melee or target goal. */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 32.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    // --- Abilities (BossCombat) -----------------------------------------------------------------
    // No melee goals: these are the only way Azathoth acts on the world, so they fall back to the
    // nearest player in range rather than requiring a target.

    @Override
    protected List<BossAbility> abilities() {
        return List.of(
                SimpleBossAbility.builder("blind_flare")
                        .cooldown(200)
                        .telegraph(32)
                        .range(20.0)
                        .warning("A blind flare — close your eyes and move!")
                        .sound(SoundEvents.WARDEN_SONIC_CHARGE)
                        .onTelegraph((boss, level, target, ticksLeft) -> {
                            BossAbilities.telegraphAt(level, target, ParticleTypes.END_ROD, 4, 1.2);
                            BossAbilities.ring(level, target.position(), 5.0, ParticleTypes.ELECTRIC_SPARK, 12);
                        })
                        .onExecute((boss, level, target) -> {
                            BossAbilities.damageAround(boss, level, target.position(), 6.0, 6.0F);
                            BossAbilities.effectAround(level, target.position(), 6.0, MobEffects.BLINDNESS, 100, 0);
                            BossAbilities.effectAround(level, target.position(), 6.0, MobEffects.NAUSEA, 140, 0);
                        })
                        .build(),
                SimpleBossAbility.builder("unmade_will")
                        .cooldown(210)
                        .telegraph(36)
                        .range(24.0)
                        .minPhase(Phase.STIRRING)
                        .warning("The music unmakes your will — move!")
                        .sound(SoundEvents.WARDEN_ROAR)
                        .onTelegraph((boss, level, target, ticksLeft) -> {
                            BossAbilities.telegraphBurst(level, boss, ParticleTypes.WITCH, 4);
                            BossAbilities.ring(level, boss.position(), 7.0, ParticleTypes.SCULK_SOUL, 18);
                        })
                        .onExecute((boss, level, target) -> {
                            BossAbilities.damageAround(boss, level, boss.position(), 8.0, 5.0F);
                            BossAbilities.effectAround(level, boss.position(), 8.0, MobEffects.WEAKNESS, 140, 0);
                            BossAbilities.effectAround(level, boss.position(), 8.0, MobEffects.NAUSEA, 160, 0);
                        })
                        .build(),
                SimpleBossAbility.builder("idiot_gods_wail")
                        .cooldown(260)
                        .telegraph(44)
                        .range(16.0)
                        .minPhase(Phase.ENRAGED)
                        .warning("Azathoth wails — get out of the centre!")
                        .sound(SoundEvents.WARDEN_SONIC_BOOM)
                        .onTelegraph((boss, level, target, ticksLeft) -> {
                            BossAbilities.ring(level, target.position(), 6.0, ParticleTypes.SONIC_BOOM, 1);
                            BossAbilities.telegraphAt(level, target, ParticleTypes.END_ROD, 5, 1.5);
                        })
                        .onExecute((boss, level, target) -> {
                            BossAbilities.damageAround(boss, level, target.position(), 7.0, 9.0F);
                            BossAbilities.effectAround(level, target.position(), 7.0, MobEffects.DARKNESS, 120, 0);
                            for (var player : BossAbilities.playersAround(level, target.position(), 7.0)) {
                                BossAbilities.knockAway(boss, player, 1.0, 0.4);
                            }
                        })
                        .build()
        );
    }

    // --- Aura: SANITY (the music unmakes the will to act) ---------------------------------------

    @Override
    public Axis auraAxis() {
        return Axis.SANITY;
    }

    @Override
    public boolean isAuraActive() {
        // Soothed: the music stills; the presence remains but no longer drains.
        return !isSolved() && masterAuraEnabled() && ModConfig.ENABLE_SANITY_AZATHOTH.get();
    }

    @Override
    public double auraRate() {
        return ModConfig.SANITY_AZATHOTH_RATE.get();
    }

    @Override
    public double auraRadius() {
        return ModConfig.SANITY_AZATHOTH_RADIUS.get();
    }

    /** Soothed: the music stills at the centre of the scar. */
    @Override
    protected void onSolved(Solve kind) {
        if (kind == Solve.SOOTHED) {
            this.playSound(ModSounds.AZATHOTH_DEATH.get(), 1.2F, 0.5F);
        }
    }

    // --- Presence -------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.AZATHOTH_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.AZATHOTH_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.AZATHOTH_DEATH.get();
    }

    /** A vast, slow drone heard across the whole scar. */
    @Override
    protected float getSoundVolume() {
        return 4.0F;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 200;
    }
}
