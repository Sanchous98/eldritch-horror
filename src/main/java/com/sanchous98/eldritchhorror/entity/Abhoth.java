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
 * Abhoth — the <b>pool that spawns filth</b> in the <b>spawning_pool</b>
 * ({@code design/28-ancient-ones.md}). Kill one and two replace it, so killing is the trap; its
 * aura is <b>corruption</b> (the water is the wound). The non-combat solve ({@code seal_abhoth},
 * resolve {@link Solve#SOOTHED}) seals the pool: the filth stops being born.
 *
 * <p>Spawn attachment is the shared, bounded {@code SitePopulationSpawner} {@code spawning_pool}
 * case; no per-boss ticker. Placeholder model: the vanilla slime.
 */
public final class Abhoth extends AncientOne {

    public Abhoth(EntityType<? extends Abhoth> type, Level level) {
        super(type, level, BossEvent.BossBarColor.GREEN, Component.translatable("entity.eldritch_horror.abhoth"));
        this.xpReward = 50;
        if (!ModConfig.ABHOTH_KILLABLE.get()) {
            this.setPermanentlyInvulnerable(true);
        }
    }

    /** 200 HP, 5 attack, 0.18 speed, 40 follow range, heavy and hard to move. */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 200.0)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.MOVEMENT_SPEED, 0.18)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.ARMOR, 6.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8);
    }

    /** It is the pool: it does not drown in its own water. */
    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    // --- Aura: CORRUPTION (the pool is the wound) -----------------------------------------------

    @Override
    public Axis auraAxis() {
        return Axis.CORRUPTION;
    }

    @Override
    public boolean isAuraActive() {
        // Sealed: the pool stops spawning filth, and the water stops wounding.
        return !isSolved() && masterAuraEnabled() && ModConfig.ENABLE_CORRUPTION_ABHOTH.get();
    }

    @Override
    public double auraRate() {
        // Disturbed, it births faster: the enraged band taints faster.
        double base = ModConfig.CORRUPTION_ABHOTH_RATE.get();
        return this.phase() == Phase.ENRAGED ? base * 1.5 : base;
    }

    @Override
    public double auraRadius() {
        return ModConfig.CORRUPTION_ABHOTH_RADIUS.get();
    }

    /** Sealed: the filth is no longer born, and the pool is quiet. */
    @Override
    protected void onSolved(Solve kind) {
        if (kind == Solve.SOOTHED) {
            this.playSound(ModSounds.ABHOTH_DEATH.get(), 1.0F, 0.4F);
        }
    }

    // --- Presence -------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.ABHOTH_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.ABHOTH_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ABHOTH_DEATH.get();
    }

    /** A wet, bubbling presence, heard across the whole pool. */
    @Override
    protected float getSoundVolume() {
        return 2.5F;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 140;
    }

    @Override
    public Phase phaseFor(ServerLevel level, float healthFraction) {
        return healthFraction <= ModConfig.BOSS_ENRAGE_HEALTH.get() ? Phase.ENRAGED : Phase.DORMANT;
    }
}
