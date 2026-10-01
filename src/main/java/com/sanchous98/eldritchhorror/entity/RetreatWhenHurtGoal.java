package com.sanchous98.eldritchhorror.entity;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Flee when wounded. A squishy mob (or any mob configured so) bolts for distance the moment it is
 * hurt and its health fraction has fallen below the threshold, instead of standing its ground.
 * Reusable: the destination is {@link DefaultRandomPos} away from the last attacker, so it is
 * bounded and never a world scan. Server-side only; takes only the MOVE flag so it composes under a
 * higher-priority attack/panic goal.
 *
 * <p>Every entity read is null-guarded: with no living attacker, or on an unpathable spot, the goal
 * simply does not start.
 */
public final class RetreatWhenHurtGoal extends Goal {

    private final PathfinderMob mob;
    private final double speedModifier;
    private final float healthFraction;
    private final int distance;
    private @Nullable Vec3 pos;

    public RetreatWhenHurtGoal(PathfinderMob mob, double speedModifier, float healthFraction) {
        this(mob, speedModifier, healthFraction, 10);
    }

    public RetreatWhenHurtGoal(PathfinderMob mob, double speedModifier, float healthFraction, int distance) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.healthFraction = healthFraction;
        this.distance = distance;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        LivingEntity attacker = this.mob.getLastHurtByMob();
        if (attacker == null || !attacker.isAlive()) {
            return false;
        }
        if (this.mob.getHealth() > this.mob.getMaxHealth() * this.healthFraction) {
            return false;
        }
        Vec3 away = DefaultRandomPos.getPosAway(this.mob, this.distance, 5, attacker.position());
        if (away == null) {
            return false;
        }
        this.pos = away;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.pos != null
                && !this.mob.getNavigation().isDone()
                && this.mob.getHealth() <= this.mob.getMaxHealth() * this.healthFraction;
    }

    @Override
    public void start() {
        if (this.pos != null) {
            this.mob.getNavigation().moveTo(this.pos.x, this.pos.y, this.pos.z, this.speedModifier);
        }
    }

    @Override
    public void stop() {
        this.pos = null;
    }
}
