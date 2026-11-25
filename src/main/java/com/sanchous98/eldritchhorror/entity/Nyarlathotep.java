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
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * Nyarlathotep — "the god with a thousand masks", wearing a face you trust in the
 * <b>cult_stronghold</b> ({@code design/28-ancient-ones.md}). Its hook is the design's "the drain is
 * betrayal, not damage": it is the only presence here that <i>rewards</i> keeping it in sight. While
 * you look at it (line of sight) it wears the trusted face and drains you at the base rate; the
 * moment you look away, the betrayal lands and the drain <b>deepens</b> ({@link #auraRateFor}).
 *
 * <p>The non-combat solve is refusing the face ({@code deny_nyarlathotep}, resolve {@link
 * Solve#SOOTHED} — the mask is named and set aside, not killed). Spawn attachment is the shared,
 * bounded {@link SitePopulationSpawner} {@code cult_stronghold} branch (one loaded-only presence per
 * player); there is no per-boss ticker. Placeholder model: the vanilla villager (the trusted face).
 */
public final class Nyarlathotep extends AncientOne {

    public Nyarlathotep(EntityType<? extends Nyarlathotep> type, Level level) {
        super(type, level, BossEvent.BossBarColor.PURPLE, Component.translatable("entity.eldritch_horror.nyarlathotep"));
        this.xpReward = 55;
        if (!ModConfig.NYARLATHOTEP_KILLABLE.get()) {
            this.setPermanentlyInvulnerable(true);
        }
    }

    /** 150 HP, 6 attack, 0.26 speed, 40 follow range, moderate armour. */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 150.0)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.26)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.4);
    }

    /** A major presence: the thousand masks are a central encounter. */
    @Override
    public boolean majorPresence() {
        return true;
    }

    // --- Aura: SANITY (the betrayal lands when you look away) -----------------------------------

    @Override
    public Axis auraAxis() {
        return Axis.SANITY;
    }

    @Override
    public boolean isAuraActive() {
        return !isSolved() && masterAuraEnabled()
                && ModConfig.ENABLE_SANITY_NYARLATHOTEP.get();
    }

    @Override
    public double auraRate() {
        return ModConfig.SANITY_NYARLATHOTEP_RATE.get();
    }

    @Override
    public double auraRadius() {
        return ModConfig.SANITY_NYARLATHOTEP_RADIUS.get();
    }

    /** In sight the mask is trusted; out of sight the betrayal drains half again as fast. */
    @Override
    public double auraRateFor(ServerPlayer player) {
        return player.hasLineOfSight(this) ? auraRate() : auraRate() * 1.5;
    }

    /** Denied: the mask is named and set aside. */
    @Override
    protected void onSolved(Solve kind) {
        if (kind == Solve.SOOTHED) {
            this.playSound(ModSounds.NYARLATHOTEP_DEATH.get(), 1.0F, 0.5F);
        }
    }

    // --- Abilities (BossCombat) -----------------------------------------------------------------

    @Override
    protected List<BossAbility> abilities() {
        return List.of(
                SimpleBossAbility.builder("false_face")
                        .cooldown(170)
                        .telegraph(26)
                        .range(18.0)
                        .warning("A trusted face turns — watch your back!")
                        .sound(SoundEvents.ILLUSIONER_CAST_SPELL)
                        .onTelegraph((boss, level, target, ticksLeft) -> {
                            BossAbilities.telegraphAt(boss, level, target, ParticleTypes.PORTAL, 4, 1.0);
                            BossAbilities.telegraphBurst(level, boss, ParticleTypes.WITCH, 3);
                        })
                        .onExecute((boss, level, target) -> {
                            BossAbilities.teleportBehind(boss, level, target);
                            BossAbilities.effect(target, MobEffects.NAUSEA, 100, 0);
                            BossAbilities.hit(boss, level, target, 8.0F);
                        })
                        .build(),
                SimpleBossAbility.builder("thousand_masks")
                        .cooldown(200)
                        .telegraph(30)
                        .range(24.0)
                        .minPhase(Phase.STIRRING)
                        .warning("A thousand masks — do not trust them!")
                        .sound(SoundEvents.ILLUSIONER_CAST_SPELL)
                        .onTelegraph((boss, level, target, ticksLeft) ->
                                BossAbilities.telegraphBurst(level, boss, ParticleTypes.PORTAL, 6))
                        .onExecute((boss, level, target) -> {
                            boss.bossCombat().summon(level, ModEntities.NIGHT_HAG.get(), 2, target);
                            BossAbilities.effectAround(boss, level, target.position(), 5.0, MobEffects.BLINDNESS, 100, 0);
                        })
                        .build(),
                SimpleBossAbility.builder("betrayal")
                        .cooldown(240)
                        .telegraph(38)
                        .range(14.0)
                        .minPhase(Phase.ENRAGED)
                        .warning("The betrayal lands — get clear!")
                        .sound(SoundEvents.ILLUSIONER_CAST_SPELL)
                        .onTelegraph((boss, level, target, ticksLeft) ->
                                BossAbilities.telegraphBurst(level, boss, ParticleTypes.ENCHANT, 6))
                        .onExecute((boss, level, target) -> BossAbilities.cone(level, boss, 14.0, 70.0, player -> {
                            BossAbilities.hit(boss, level, player, 9.0F);
                            BossAbilities.effect(player, MobEffects.GLOWING, 200, 0);
                            BossAbilities.effect(player, MobEffects.WEAKNESS, 100, 0);
                        }))
                        .build()
        );
    }

    // --- Presence -------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.NYARLATHOTEP_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.NYARLATHOTEP_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.NYARLATHOTEP_DEATH.get();
    }

    /** A pleasant voice you cannot quite place: loud and frequent, like a friend calling. */
    @Override
    protected float getSoundVolume() {
        return 2.0F;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 80;
    }
}
