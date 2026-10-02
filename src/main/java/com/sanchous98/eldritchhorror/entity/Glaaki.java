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

    /** 130 HP, 5 attack, 0.18 speed (it wades), 40 follow range, heavy. */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 130.0)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.MOVEMENT_SPEED, 0.18)
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

    @Override
    public Phase phaseFor(ServerLevel level, float healthFraction) {
        return healthFraction <= ModConfig.BOSS_ENRAGE_HEALTH.get() ? Phase.ENRAGED : Phase.DORMANT;
    }
}
