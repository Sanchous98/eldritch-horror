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
 * Chaugnar Faugn — the <b>hunger you can hear</b> in the <b>temple_of_the_feaster</b>
 * ({@code design/28-ancient-ones.md}). It feeds on the living and on courage, so its aura is
 * <b>sanity</b>, and the non-combat solve ({@code starve_chaugnar_faugn}, resolve
 * {@link Solve#SOOTHED}) feeds it nothing until it sleeps.
 *
 * <p>Spawn attachment is the shared, bounded {@code SitePopulationSpawner}
 * {@code temple_of_the_feaster} case; no per-boss ticker. Placeholder model: the vanilla ravager.
 */
public final class ChaugnarFaugn extends AncientOne {

    public ChaugnarFaugn(EntityType<? extends ChaugnarFaugn> type, Level level) {
        super(type, level, BossEvent.BossBarColor.RED,
                Component.translatable("entity.eldritch_horror.chaugnar_faugn"));
        this.xpReward = 50;
        if (!ModConfig.CHAUGNAR_FAUGN_KILLABLE.get()) {
            this.setPermanentlyInvulnerable(true);
        }
    }

    /** 180 HP, 9 attack, 0.20 speed, 40 follow range, heavy armour (a feaster). */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 180.0)
                .add(Attributes.ATTACK_DAMAGE, 9.0)
                .add(Attributes.MOVEMENT_SPEED, 0.20)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.ARMOR, 7.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6);
    }

    // --- Aura: SANITY (it eats courage) ---------------------------------------------------------

    @Override
    public Axis auraAxis() {
        return Axis.SANITY;
    }

    @Override
    public boolean isAuraActive() {
        // Starved: the hunger sleeps and takes no more courage.
        return !isSolved() && masterAuraEnabled() && ModConfig.ENABLE_SANITY_CHAUGNAR_FAUGN.get();
    }

    @Override
    public double auraRate() {
        // Hungrier when wounded: the enraged band drains faster.
        double base = ModConfig.SANITY_CHAUGNAR_FAUGN_RATE.get();
        return this.phase() == Phase.ENRAGED ? base * 1.5 : base;
    }

    @Override
    public double auraRadius() {
        return ModConfig.SANITY_CHAUGNAR_FAUGN_RADIUS.get();
    }

    /** Starved: fed nothing, the hunger sleeps and the temple quiets. */
    @Override
    protected void onSolved(Solve kind) {
        if (kind == Solve.SOOTHED) {
            this.playSound(ModSounds.CHAUGNAR_FAUGN_DEATH.get(), 1.0F, 0.4F);
        }
    }

    // --- Presence -------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.CHAUGNAR_FAUGN_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.CHAUGNAR_FAUGN_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.CHAUGNAR_FAUGN_DEATH.get();
    }

    /** A feeding is loud; you hear it well before you see it. */
    @Override
    protected float getSoundVolume() {
        return 2.5F;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 160;
    }

    @Override
    public Phase phaseFor(ServerLevel level, float healthFraction) {
        return healthFraction <= ModConfig.BOSS_ENRAGE_HEALTH.get() ? Phase.ENRAGED : Phase.DORMANT;
    }
}
