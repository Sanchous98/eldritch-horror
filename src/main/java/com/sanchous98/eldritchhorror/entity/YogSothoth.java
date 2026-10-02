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
 * Yog-Sothoth — the gate at the <b>order_vault</b> on the observatory plateau
 * ({@code design/20-map.md} places the vault on `observatory_plateau`), replacing the Ender Dragon
 * ({@code design/28-ancient-ones.md}: "the gate <i>is</i> the boss"). It is deliberately
 * <b>stationary-ish</b>: the shared {@link AncientOne} goals give it a very slow walk and no
 * aggressive chase, and its hook is a <b>large</b> sanity aura (the gate takes a step toward you for
 * every step you take toward it). The non-combat solve is sealing the gate
 * ({@code seal_the_gate}, resolve {@link Solve#SOOTHED} — the gate falls shut and stands inert rather
 * than dying).
 *
 * <p>Spawn attachment is the shared, bounded {@link SitePopulationSpawner} {@code order_vault}
 * branch: the vault is the existing site that stands on the observatory plateau (design/20), so the
 * gate attaches there; no per-boss ticker. Placeholder model: the vanilla wither model (a floating,
 * many-part silhouette) — see {@code YogSothothRenderer}.
 */
public final class YogSothoth extends AncientOne {

    public YogSothoth(EntityType<? extends YogSothoth> type, Level level) {
        super(type, level, BossEvent.BossBarColor.WHITE, Component.translatable("entity.eldritch_horror.yog_sothoth"));
        this.xpReward = 70;
        if (!ModConfig.YOG_SOTHOTH_KILLABLE.get()) {
            this.setPermanentlyInvulnerable(true);
        }
    }

    /** 300 HP, 8 attack, 0.06 speed (the gate barely moves), 64 follow range, immovable. */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 300.0)
                .add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.MOVEMENT_SPEED, 0.06)
                .add(Attributes.FOLLOW_RANGE, 64.0)
                .add(Attributes.ARMOR, 8.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    // --- Aura: SANITY (a wide, patient drain) ---------------------------------------------------

    @Override
    public Axis auraAxis() {
        return Axis.SANITY;
    }

    @Override
    public boolean isAuraActive() {
        // Sealed: the gate is shut and no longer draws the eye.
        return !isSolved() && ModConfig.ENABLE_SANITY.get() && ModConfig.ENABLE_SANITY_YOG_SOTHOTH.get();
    }

    @Override
    public double auraRate() {
        return ModConfig.SANITY_YOG_SOTHOTH_RATE.get();
    }

    @Override
    public double auraRadius() {
        return ModConfig.SANITY_YOG_SOTHOTH_RADIUS.get();
    }

    /** Sealed: the gate falls shut with a sound like a held breath released. */
    @Override
    protected void onSolved(Solve kind) {
        if (kind == Solve.SOOTHED) {
            this.playSound(ModSounds.YOG_SOTHOTH_DEATH.get(), 1.5F, 0.4F);
        }
    }

    // --- Presence -------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.YOG_SOTHOTH_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.YOG_SOTHOTH_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.YOG_SOTHOTH_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 3.5F;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 180;
    }

    /** The gate endures; it does not gain a world-driven phase. */
    @Override
    public Phase phaseFor(ServerLevel level, float healthFraction) {
        return Phase.DORMANT;
    }
}
