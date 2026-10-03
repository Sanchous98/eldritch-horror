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
 * Tulzscha — the <b>green flame at the centre</b> of the Veil, proxied by the <b>rift_scar</b>
 * ({@code design/28-ancient-ones.md}). Looking into it costs what you remember, so its aura is
 * <b>sanity</b>, and the non-combat solve ({@code quench_tulzscha}, resolve {@link Solve#SOOTHED})
 * closes the flame's eye and lets memory return.
 *
 * <p>Spawn attachment is the shared, bounded {@code SitePopulationSpawner} {@code rift_scar} case;
 * no per-boss ticker. Placeholder model: the vanilla blaze.
 */
public final class Tulzscha extends AncientOne {

    public Tulzscha(EntityType<? extends Tulzscha> type, Level level) {
        super(type, level, BossEvent.BossBarColor.GREEN, Component.translatable("entity.eldritch_horror.tulzscha"));
        this.xpReward = 50;
        if (!ModConfig.TULZSCHA_KILLABLE.get()) {
            this.setPermanentlyInvulnerable(true);
        }
    }

    /** 130 HP, 7 attack, 0.30 speed (a dancing flame), 40 follow range, low armour. */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 130.0)
                .add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.MOVEMENT_SPEED, 0.30)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.ARMOR, 3.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.3);
    }

    // --- Aura: SANITY (looking costs what you remember) -----------------------------------------

    @Override
    public Axis auraAxis() {
        return Axis.SANITY;
    }

    @Override
    public boolean isAuraActive() {
        // Quenched: the eye closes and memory stops being taken.
        return !isSolved() && masterAuraEnabled() && ModConfig.ENABLE_SANITY_TULZSCHA.get();
    }

    @Override
    public double auraRate() {
        // It flares when threatened: the enraged band burns memory faster.
        double base = ModConfig.SANITY_TULZSCHA_RATE.get();
        return this.phase() == Phase.ENRAGED ? base * 1.5 : base;
    }

    @Override
    public double auraRadius() {
        return ModConfig.SANITY_TULZSCHA_RADIUS.get();
    }

    /** Quenched: the green flame closes its eye; memory returns. */
    @Override
    protected void onSolved(Solve kind) {
        if (kind == Solve.SOOTHED) {
            this.playSound(ModSounds.TULZSCHA_DEATH.get(), 1.0F, 0.4F);
        }
    }

    // --- Presence -------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.TULZSCHA_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.TULZSCHA_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.TULZSCHA_DEATH.get();
    }

    /** A flame at the centre of the Veil: it carries far. */
    @Override
    protected float getSoundVolume() {
        return 2.2F;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 145;
    }

    @Override
    public Phase phaseFor(ServerLevel level, float healthFraction) {
        return healthFraction <= ModConfig.BOSS_ENRAGE_HEALTH.get() ? Phase.ENRAGED : Phase.DORMANT;
    }
}
