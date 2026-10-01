package com.sanchous98.eldritchhorror.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Airborne variant of {@link AmbientDrifter}: the byakhee's verified 26.3 flight composition
 * ({@link FlyingMoveControl} + {@link FlyingPathNavigation}, {@code noGravity}, {@link #travelFlying})
 * with no attack or target goals. Used by {@code cave_drifter} and {@code frost_wisp}.
 */
public abstract class FlyingAmbientDrifter extends AmbientDrifter {

    protected FlyingAmbientDrifter(EntityType<? extends FlyingAmbientDrifter> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl<>(this, 20, true);
        this.setNoGravity(true);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomFlyingGoal(this, 0.8));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        navigation.setRequiredPathLength(32.0F);
        return navigation;
    }

    @Override
    public void travel(Vec3 input) {
        this.travelFlying(input, 0.2F);
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        // It beats the air; no footfall.
    }
}
