package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.registry.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
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

    /** Fewer bands: it betrays at low health only. */
    @Override
    public Phase phaseFor(ServerLevel level, float healthFraction) {
        return healthFraction <= ModConfig.BOSS_ENRAGE_HEALTH.get() ? Phase.ENRAGED : Phase.DORMANT;
    }
}
