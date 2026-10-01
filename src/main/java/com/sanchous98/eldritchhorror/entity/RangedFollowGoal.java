package com.sanchous98.eldritchhorror.entity;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

/**
 * Presence behaviour: stay at arm's length from the current target. The mob advances when the target
 * is farther than {@code maxDistance}, backs straight away when closer than {@code minDistance} (so
 * it never closes to melee), and otherwise holds still — always turning to face the target. It
 * carries no attack at all: the horror is the attention, not the damage
 * ({@code design/25-bestiary-and-entities.md}, "Dread over damage"). Shared by the Watcher.
 */
public final class RangedFollowGoal extends Goal {
    private final Mob mob;
    private final double speedModifier;
    private final float minDistance;
    private final float maxDistance;

    public RangedFollowGoal(Mob mob, double speedModifier, float minDistance, float maxDistance) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.mob.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target == null) {
            return;
        }
        this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        double distanceSqr = this.mob.distanceToSqr(target);
        if (distanceSqr > (double) this.maxDistance * this.maxDistance) {
            this.mob.getNavigation().moveTo(target, this.speedModifier);
        } else if (distanceSqr < (double) this.minDistance * this.minDistance) {
            Vec3 away = this.mob.position().subtract(target.position()).normalize();
            this.mob.getNavigation().moveTo(
                    this.mob.getX() + away.x, this.mob.getY() + away.y, this.mob.getZ() + away.z,
                    this.speedModifier);
        } else {
            this.mob.getNavigation().stop();
        }
    }

    @Override
    public void stop() {
        this.mob.getNavigation().stop();
    }
}
