package com.sanchous98.eldritchhorror.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.level.Level;

/**
 * Water-oriented variant of {@link AmbientDrifter}: the dead-water drifters ({@code pale_drifter},
 * {@code lantern_jelly}, {@code drowned_minnow}). They use the shared ground placement (so they
 * appear at the tide line) and then a {@link RandomSwimmingGoal} draws them into the water; a
 * {@link FloatGoal} keeps them at the surface and the drifter's water-breathing means they are
 * never harmed on the way. It carries no target goals — it cannot fight.
 */
public abstract class WaterAmbientDrifter extends AmbientDrifter {

    protected WaterAmbientDrifter(EntityType<? extends WaterAmbientDrifter> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(4, new RandomSwimmingGoal(this, 0.8, 40));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }
}
