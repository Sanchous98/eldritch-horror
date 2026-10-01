package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.core.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Shared skeleton for the ambient (non-combat) bestiary roster
 * ({@code design/25-bestiary-and-entities.md}, "Ambient role"): harmless drifters that exist for
 * mood, not for a fight. One base class so the six entries share their non-combat goals, inert
 * hostility and water-breathing instead of copying them. It carries <b>no</b> target-selection
 * goal and a low/no {@code xpReward}, so it can never pick a player fight.
 *
 * <p>Spawned only through the shared {@code BestiarySupport.topUp}/{@code topUpAir} pass carried by
 * {@link AmbientSpawner}; the flyers extend {@link FlyingAmbientDrifter}.
 */
public abstract class AmbientDrifter extends PathfinderMob {

    protected AmbientDrifter(EntityType<? extends AmbientDrifter> type, Level level) {
        super(type, level);
        this.xpReward = 0;
    }

    /** Drift, watch, never chase: no target goals. Flee if badly wounded (config-gated). */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        if (ModConfig.BESTIARY_RETREAT_ENABLED.get()) {
            this.goalSelector.addGoal(1, new RetreatWhenHurtGoal(this, 1.3, 0.5F));
        }
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    /** Base drifter attributes: tiny health, slow, short follow range. */
    public static AttributeSupplier.Builder drifterAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 4.0)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    /** Dead-water and deep drifters do not drown at the surface. */
    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return getAmbientSound();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return getAmbientSound();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        // Drifters do not stomp.
    }
}
