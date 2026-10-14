package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.core.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Ithaqual Shade — "wind-that-walks near the cold edge and deep places"
 * ({@code design/25-bestiary-and-entities.md}, the warden row). It is a <b>named</b> presence and a
 * boss gate, but a modest one: its stats sit between the lesser horrors and the big twenty, and it
 * is a roaming presence, not a site boss. Spawn attachment is the bounded
 * {@link IthaqualShadeSpawner} dual gate — the polar band inside {@code BoundaryTravel.morokEdgeZ}
 * and/or deep loaded caves — never a site trigger.
 *
 * <p>Its hook is a cold-themed sanity drain: {@link #auraRateFor} deepens the drain while the
 * player stands in a biome cold enough to snow, so the deep and the far north cost more than the
 * settled world. The non-combat solve is {@code still_the_shade} (a SOOTHE rite taught by the
 * Order), mirroring the other Ancient One solves.
 *
 * <p>Placeholder model: the vanilla warden silhouette, with our own frost skin; art is the user's
 * job. Reused vanilla warden vocalisations.
 */
public final class IthaqualShade extends AncientOne {

    public IthaqualShade(EntityType<? extends IthaqualShade> type, Level level) {
        super(type, level, BossEvent.BossBarColor.BLUE,
                Component.translatable("entity.eldritch_horror.ithaqual_shade"));
        this.xpReward = 30;
        if (!ModConfig.ITHAQUAL_SHADE_KILLABLE.get()) {
            this.setPermanentlyInvulnerable(true);
        }
    }

    /** Modest boss stats: 70 HP, 6 attack, 0.32 speed (wind-like), 40 follow range, light armour. */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 70.0)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.ARMOR, 3.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.4);
    }

    // --- Aura: SANITY (cold-themed) --------------------------------------------------------------

    @Override
    public Axis auraAxis() {
        return Axis.SANITY;
    }

    @Override
    public boolean isAuraActive() {
        // Stilled: the wind cannot find the seam to blow through.
        return !isSolved() && masterAuraEnabled() && ModConfig.ENABLE_SANITY_ITHAQUAL_SHADE.get();
    }

    @Override
    public double auraRate() {
        return ModConfig.SANITY_ITHAQUAL_SHADE_RATE.get();
    }

    /**
     * The cold carries it: standing where the world is cold enough to snow (the polar edge, high
     * ground) deepens the drain; elsewhere the presence only whispers.
     */
    @Override
    public double auraRateFor(ServerPlayer player) {
        BlockPos pos = player.blockPosition();
        boolean cold = player.level().getBiome(pos).value().coldEnoughToSnow(pos, player.level().getSeaLevel());
        return cold ? auraRate() * 1.5 : auraRate();
    }

    @Override
    public double auraRadius() {
        return ModConfig.SANITY_ITHAQUAL_SHADE_RADIUS.get();
    }

    /** Stilled: the gust is turned aside for a while. */
    @Override
    protected void onSolved(Solve kind) {
        if (kind == Solve.SOOTHED) {
            this.playSound(SoundEvents.WARDEN_DEATH, 1.0F, 1.4F);
        }
    }

    // --- Presence --------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WARDEN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.WARDEN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WARDEN_DEATH;
    }

    /** Heard before felt: a howl carried on the walking wind. */
    @Override
    protected float getSoundVolume() {
        return 2.5F;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 90;
    }

    @Override
    public Phase phaseFor(ServerLevel level, float healthFraction) {
        return healthFraction <= ModConfig.BOSS_ENRAGE_HEALTH.get() ? Phase.ENRAGED : Phase.DORMANT;
    }

    @Override
    protected void onPhaseChanged(ServerLevel level, Phase previous, Phase now) {
        if (now == Phase.ENRAGED) {
            this.playSound(SoundEvents.WARDEN_ROAR, 1.5F, 0.7F);
        }
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
        this.playSound(SoundEvents.WARDEN_STEP, 0.3F, 1.2F);
    }
}
