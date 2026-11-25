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
 * Nephren-Ka — the <b>king who remembers you</b> in the <b>black_pyramid</b>
 * ({@code design/28-ancient-ones.md}). His gaze turns your own cult against you, so his aura is
 * <b>corruption</b> (loyalty rots to doubt), and the non-combat solve ({@code unmake_nephren_ka},
 * resolve {@link Solve#SOOTHED}) makes the king forget you and your cult doubt no more.
 *
 * <p>Spawn attachment is the shared, bounded {@code SitePopulationSpawner} {@code black_pyramid}
 * case; no per-boss ticker. Placeholder model: the vanilla husk.
 */
public final class NephrenKa extends AncientOne {

    public NephrenKa(EntityType<? extends NephrenKa> type, Level level) {
        super(type, level, BossEvent.BossBarColor.RED, Component.translatable("entity.eldritch_horror.nephren_ka"));
        this.xpReward = 50;
        if (!ModConfig.NEPHREN_KA_KILLABLE.get()) {
            this.setPermanentlyInvulnerable(true);
        }
    }

    /** 170 HP, 8 attack, 0.25 speed, 40 follow range, heavy armour (a king in his tomb). */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 170.0)
                .add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.ARMOR, 8.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.7);
    }

    // --- Aura: CORRUPTION (the gaze breeds doubt) -----------------------------------------------

    @Override
    public Axis auraAxis() {
        return Axis.CORRUPTION;
    }

    @Override
    public boolean isAuraActive() {
        // The king forgets you: the gaze lapses and the doubt recedes.
        return !isSolved() && masterAuraEnabled() && ModConfig.ENABLE_CORRUPTION_NEPHREN_KA.get();
    }

    @Override
    public double auraRate() {
        // A remembered king presses harder when cornered: the enraged band taints faster.
        double base = ModConfig.CORRUPTION_NEPHREN_KA_RATE.get();
        return this.phase() == Phase.ENRAGED ? base * 1.5 : base;
    }

    @Override
    public double auraRadius() {
        return ModConfig.CORRUPTION_NEPHREN_KA_RADIUS.get();
    }

    /** Forgotten: the king's gaze breaks, and the cult's doubt goes with it. */
    @Override
    protected void onSolved(Solve kind) {
        if (kind == Solve.SOOTHED) {
            this.playSound(ModSounds.NEPHREN_KA_DEATH.get(), 1.0F, 0.4F);
        }
    }

    // --- Abilities (BossCombat) -----------------------------------------------------------------

    @Override
    protected List<BossAbility> abilities() {
        return List.of(
                SimpleBossAbility.builder("pharaohs_curse")
                        .cooldown(160)
                        .telegraph(26)
                        .range(14.0)
                        .warning("The king remembers — break his gaze!")
                        .sound(SoundEvents.HUSK_AMBIENT)
                        .onTelegraph((boss, level, target, ticksLeft) -> {
                            BossAbilities.telegraphAt(boss, level, target, ParticleTypes.ASH, 4, 0.9);
                            BossAbilities.telegraphBurst(level, boss, ParticleTypes.SOUL, 4);
                        })
                        .onExecute((boss, level, target) -> {
                            BossAbilities.effect(target, MobEffects.WITHER, 100, 0);
                            BossAbilities.effect(target, MobEffects.WEAKNESS, 120, 0);
                            BossAbilities.hit(boss, level, target, 5.0F);
                        })
                        .build(),
                SimpleBossAbility.builder("sandstorm")
                        .cooldown(170)
                        .telegraph(28)
                        .range(12.0)
                        .minPhase(Phase.STIRRING)
                        .warning("A sandstorm rises — move!")
                        .sound(SoundEvents.HUSK_AMBIENT)
                        .onTelegraph((boss, level, target, ticksLeft) -> {
                            BossAbilities.telegraphBurst(level, boss, ParticleTypes.ASH, 6);
                            BossAbilities.ring(boss, level, boss.position(), 6.0, ParticleTypes.ASH, 14);
                        })
                        .onExecute((boss, level, target) -> BossAbilities.cone(level, boss, 12.0, 70.0, player -> {
                            BossAbilities.knockAway(boss, player, 0.9, 0.3);
                            BossAbilities.effect(player, MobEffects.BLINDNESS, 100, 0);
                            BossAbilities.hit(boss, level, player, 4.0F);
                        }))
                        .build()
        );
    }

    // --- Presence -------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.NEPHREN_KA_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.NEPHREN_KA_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.NEPHREN_KA_DEATH.get();
    }

    /** A royal pronouncement carries: the tomb makes it louder. */
    @Override
    protected float getSoundVolume() {
        return 2.2F;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 150;
    }
}
