package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.registry.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * Hastur — the <b>name you must not say</b> in the <b>yellow_court</b>
 * ({@code design/28-ancient-ones.md}). It is a presence of the spoken word: hearing or reading the
 * name makes the listener want to say it, so its aura is a <b>sanity</b> drain, and the non-combat
 * solve ({@code silence_hastur}, resolve {@link Solve#STILLED}) is to leave the name unspoken and
 * let the court still for a time.
 *
 * <p>Spawn attachment is the shared, bounded {@code SitePopulationSpawner} {@code yellow_court}
 * case; no per-boss ticker. Placeholder model: the vanilla wither.
 */
public final class Hastur extends AncientOne {

    public Hastur(EntityType<? extends Hastur> type, Level level) {
        super(type, level, BossEvent.BossBarColor.YELLOW, Component.translatable("entity.eldritch_horror.hastur"));
        this.xpReward = 50;
        if (!ModConfig.HASTUR_KILLABLE.get()) {
            this.setPermanentlyInvulnerable(true);
        }
    }

    /** 140 HP, 7 attack, 0.22 speed, 40 follow range, moderate armour. */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 140.0)
                .add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.ARMOR, 5.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5);
    }

    // --- Aura: SANITY (the name wants to be spoken) ---------------------------------------------

    @Override
    public Axis auraAxis() {
        return Axis.SANITY;
    }

    @Override
    public boolean isAuraActive() {
        // The name is unspoken: the court stills and the pull to say it lapses.
        return !isSolved() && masterAuraEnabled() && ModConfig.ENABLE_SANITY_HASTUR.get();
    }

    @Override
    public double auraRate() {
        // The more you hear it, the more it insists: the enraged band drains faster.
        double base = ModConfig.SANITY_HASTUR_RATE.get();
        return this.phase() == Phase.ENRAGED ? base * 1.5 : base;
    }

    @Override
    public double auraRadius() {
        return ModConfig.SANITY_HASTUR_RADIUS.get();
    }

    /** Unspoken: the name goes quiet and the court stills for a time. */
    @Override
    protected void onSolved(Solve kind) {
        if (kind == Solve.STILLED) {
            this.playSound(ModSounds.HASTUR_DEATH.get(), 1.0F, 0.5F);
        }
    }

    // --- Presence -------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.HASTUR_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.HASTUR_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.HASTUR_DEATH.get();
    }

    /** Heard before seen: the name carries across the court. */
    @Override
    protected float getSoundVolume() {
        return 2.5F;
    }

    /** A rare, vast presence, not a chatterbox. */
    @Override
    public int getAmbientSoundInterval() {
        return 170;
    }

    /** The court stills to a two-band ladder: present, then the worst of it. */
    @Override
    public Phase phaseFor(ServerLevel level, float healthFraction) {
        return healthFraction <= ModConfig.BOSS_ENRAGE_HEALTH.get() ? Phase.ENRAGED : Phase.DORMANT;
    }
}
