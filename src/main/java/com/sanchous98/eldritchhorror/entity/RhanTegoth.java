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
 * Rhan-Tegoth — "an idol that is not an idol; worship sustains it" at the <b>polar/cold edge</b>
 * ({@code design/28-ancient-ones.md}). It is deliberately <b>stationary-ish</b> (barely walks) so it
 * reads as a thing on a plinth rather than a hunter; its hook is a large, patient <b>sanity</b> aura —
 * the pull to kneel and keep it fed. The non-combat solve is a rite ({@code topple_idol}, resolve
 * {@link Solve#SOOTHED} — the idol is toppled and the worship ends, not the thing).
 *
 * <p>Spawn attachment is the shared, bounded {@link AncientOneSpawner} {@code icy} cold-ground pass
 * (the polar edge / highlands); no per-boss ticker. Placeholder model: the vanilla iron golem (a
 * standing carved shape).
 */
public final class RhanTegoth extends AncientOne {

    public RhanTegoth(EntityType<? extends RhanTegoth> type, Level level) {
        super(type, level, BossEvent.BossBarColor.WHITE, Component.translatable("entity.eldritch_horror.rhan_tegoth"));
        this.xpReward = 55;
        if (!ModConfig.RHAN_TEGOTH_KILLABLE.get()) {
            this.setPermanentlyInvulnerable(true);
        }
    }

    /** 170 HP, 7 attack, 0.07 speed (an idol barely moves), 48 follow range, carved-stone armour. */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 170.0)
                .add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.MOVEMENT_SPEED, 0.07)
                .add(Attributes.FOLLOW_RANGE, 48.0)
                .add(Attributes.ARMOR, 9.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    // --- Aura: SANITY (the pull to worship) -----------------------------------------------------

    @Override
    public Axis auraAxis() {
        return Axis.SANITY;
    }

    @Override
    public boolean isAuraActive() {
        return !isSolved() && masterAuraEnabled() && ModConfig.ENABLE_SANITY_RHAN_TEGOTH.get();
    }

    @Override
    public double auraRate() {
        return ModConfig.SANITY_RHAN_TEGOTH_RATE.get();
    }

    @Override
    public double auraRadius() {
        return ModConfig.SANITY_RHAN_TEGOTH_RADIUS.get();
    }

    /** Toppled: the worship ends and the pull is broken. */
    @Override
    protected void onSolved(Solve kind) {
        if (kind == Solve.SOOTHED) {
            this.playSound(ModSounds.RHAN_TEGOTH_DEATH.get(), 1.0F, 0.4F);
        }
    }

    // --- Presence -------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.RHAN_TEGOTH_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.RHAN_TEGOTH_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.RHAN_TEGOTH_DEATH.get();
    }

    /** A low stone hum, as of something listening to your prayers. */
    @Override
    protected float getSoundVolume() {
        return 3.0F;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 160;
    }

    /** The idol endures; it does not gain a world-driven phase. */
    @Override
    public Phase phaseFor(ServerLevel level, float healthFraction) {
        return Phase.DORMANT;
    }
}
