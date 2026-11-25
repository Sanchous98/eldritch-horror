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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * Glaaki — "green servitors drag you under; the dream is always the same lake" in the
 * <b>drowned_marsh</b> ({@code design/28-ancient-ones.md}). It is the lake-dream made present: a
 * slow, half-drowned shape whose <b>sanity</b> aura is the servitors' call, drawing the player down.
 * The non-combat solve is a rite ({@code still_glaaki}, resolve {@link Solve#SOOTHED} — the lake-dream
 * is stilled and the servitors lose their pull).
 *
 * <p>Spawn attachment is the shared, bounded {@link AncientOneSpawner} {@code marsh} pass (water on
 * loaded ground); no per-boss ticker. Placeholder model: the vanilla slime (a drowning green mass).
 */
public final class Glaaki extends AncientOne {

    public Glaaki(EntityType<? extends Glaaki> type, Level level) {
        super(type, level, BossEvent.BossBarColor.GREEN, Component.translatable("entity.eldritch_horror.glaaki"));
        this.xpReward = 40;
        if (!ModConfig.GLAAKI_KILLABLE.get()) {
            this.setPermanentlyInvulnerable(true);
        }
    }

    /** 130 HP, 10 attack, 0.22 speed (it wades), 40 follow range, heavy. */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 130.0)
                .add(Attributes.ATTACK_DAMAGE, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.ARMOR, 6.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6);
    }

    // --- Aura: SANITY (the servitors' call) -----------------------------------------------------

    @Override
    public Axis auraAxis() {
        return Axis.SANITY;
    }

    @Override
    public boolean isAuraActive() {
        return !isSolved() && masterAuraEnabled() && ModConfig.ENABLE_SANITY_GLAAKI.get();
    }

    @Override
    public double auraRate() {
        return ModConfig.SANITY_GLAAKI_RATE.get();
    }

    @Override
    public double auraRadius() {
        return ModConfig.SANITY_GLAAKI_RADIUS.get();
    }

    /** Stilled: the same lake stops repeating, and the servitors lose their pull. */
    @Override
    protected void onSolved(Solve kind) {
        if (kind == Solve.SOOTHED) {
            this.playSound(ModSounds.GLAAKI_DEATH.get(), 1.0F, 0.5F);
        }
    }

    /**
     * The drowned grip: a hit drags the body down (slowness) while the lake dream floods the mind
     * (nausea). The servitors' call is the real weapon, so a player who trades blows with Glaaki
     * keeps losing the ability to disengage.
     */
    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean hurt = super.doHurtTarget(level, target);
        if (hurt && target instanceof net.minecraft.world.entity.LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 140, 1));
            living.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 180, 0));
        }
        return hurt;
    }

    // --- Abilities (BossCombat) -----------------------------------------------------------------

    @Override
    protected List<BossAbility> abilities() {
        return List.of(
                SimpleBossAbility.builder("drowned_grip")
                        .cooldown(160)
                        .telegraph(28)
                        .range(16.0)
                        .warning("Glaaki's servitors reach for you — move!")
                        .sound(SoundEvents.ELDER_GUARDIAN_CURSE)
                        .onTelegraph((boss, level, target, ticksLeft) -> {
                            BossAbilities.telegraphAt(boss, level, target, ParticleTypes.BUBBLE, 3, 0.9);
                            BossAbilities.telegraphBurst(level, boss, ParticleTypes.SQUID_INK, 2);
                        })
                        .onExecute((boss, level, target) -> {
                            BossAbilities.pull(boss, target, 0.9, 0.15);
                            BossAbilities.brine(target, 100);
                            BossAbilities.hit(boss, level, target, 6.0F);
                        })
                        .build(),
                SimpleBossAbility.builder("servitors_call")
                        .cooldown(220)
                        .telegraph(30)
                        .range(24.0)
                        .minPhase(Phase.STIRRING)
                        .warning("The lake stirs — servitors are rising!")
                        .sound(SoundEvents.ELDER_GUARDIAN_CURSE)
                        .onTelegraph((boss, level, target, ticksLeft) ->
                                BossAbilities.telegraphBurst(level, boss, ParticleTypes.BUBBLE, 5))
                        .onExecute((boss, level, target) ->
                                boss.bossCombat().summon(level, ModEntities.DROWNED_THRALL.get(), 2, target))
                        .build(),
                SimpleBossAbility.builder("lake_dream")
                        .cooldown(240)
                        .telegraph(36)
                        .range(20.0)
                        .minPhase(Phase.ENRAGED)
                        .warning("The dream closes over your eyes — break away!")
                        .sound(SoundEvents.ELDER_GUARDIAN_CURSE)
                        .onTelegraph((boss, level, target, ticksLeft) -> {
                            BossAbilities.telegraphAt(boss, level, target, ParticleTypes.WITCH, 2, 1.2);
                            BossAbilities.ring(boss, level, target.position(), 3.0, ParticleTypes.SQUID_INK, 10);
                        })
                        .onExecute((boss, level, target) -> {
                            BossAbilities.damageAround(boss, level, target.position(), 4.0, 5.0F);
                            BossAbilities.effectAround(boss, level, target.position(), 4.0, MobEffects.BLINDNESS, 100, 0);
                            BossAbilities.effectAround(boss, level, target.position(), 4.0, MobEffects.DARKNESS, 80, 0);
                            BossAbilities.effectAround(boss, level, target.position(), 4.0, MobEffects.NAUSEA, 160, 0);
                        })
                        .build()
        );
    }

    // --- Presence -------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.GLAAKI_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.GLAAKI_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.GLAAKI_DEATH.get();
    }

    /** The lapping of a lake that is always the same lake. */
    @Override
    protected float getSoundVolume() {
        return 2.5F;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 120;
    }
}
