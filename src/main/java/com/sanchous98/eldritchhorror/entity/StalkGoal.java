package com.sanchous98.eldritchhorror.entity;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

/**
 * Stalk / hide. Approaches its current target while <b>unobserved</b>, and stops or withdraws to a
 * minimum distance while the target is <b>looking at it</b>. This is the Watcher's "hides when
 * directly watched" behaviour, and it is reusable for any ambusher that wants to close only behind
 * the player's back.
 *
 * <p>Server-authoritative and bounded: it only reads the target's view vector and distance, never
 * scans the world. Both {@code getTarget()} and the navigation are null-guarded, so the goal is inert
 * without a live target. Takes MOVE and LOOK.
 */
public final class StalkGoal extends Goal {

    private final Mob mob;
    private final double speedModifier;
    private final float minDistance;
    private final float maxDistance;
    private final double gazeDot;

    public StalkGoal(Mob mob, double speedModifier, float minDistance, float maxDistance) {
        this(mob, speedModifier, minDistance, maxDistance, 0.5);
    }

    public StalkGoal(Mob mob, double speedModifier, float minDistance, float maxDistance, double gazeDot) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
        this.gazeDot = gazeDot;
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

    /** Whether {@code target} can currently see this mob: its gaze points near the mob's direction. */
    private boolean observedBy(LivingEntity target) {
        Vec3 toMob = this.mob.position().subtract(target.getEyePosition());
        if (toMob.lengthSqr() < 1.0E-7) {
            return true;
        }
        Vec3 gaze = target.getViewVector(1.0F);
        return gaze.normalize().dot(toMob.normalize()) > this.gazeDot;
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target == null) {
            return;
        }
        double distanceSqr = this.mob.distanceToSqr(target);
        if (this.observedBy(target)) {
            // Seen: never close. If too close, slip straight away; otherwise hold still and wait.
            this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
            if (distanceSqr < (double) this.minDistance * this.minDistance) {
                Vec3 away = this.mob.position().subtract(target.position()).normalize();
                this.mob.getNavigation().moveTo(
                        this.mob.getX() + away.x, this.mob.getY() + away.y, this.mob.getZ() + away.z,
                        this.speedModifier);
            } else {
                this.mob.getNavigation().stop();
            }
        } else if (distanceSqr > (double) this.maxDistance * this.maxDistance) {
            // Unobserved and too far: close the distance quietly.
            this.mob.getNavigation().moveTo(target, this.speedModifier);
        } else {
            this.mob.getNavigation().stop();
        }
    }

    @Override
    public void stop() {
        this.mob.getNavigation().stop();
    }
}
