package com.sanchous98.eldritchhorror.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

/**
 * Pack alert. A gregarious mob that spots a player (within {@code searchRadius}, in line of sight)
 * sets that player as its own target and, once, shares the target with up to {@code maxAlerted}
 * neighbours of the same class inside the small {@code alertRadius}. This is how a swarm turns as
 * one: a single mite sees you and the pack answers.
 *
 * <p>Bounded and cheap: proximity uses {@code getNearestPlayer} (loaded entities only), the pack
 * lookup is one small {@code getEntitiesOfClass} box capped by {@code maxAlerted}, and the whole
 * check runs on a random gate of roughly once per ten ticks. It is a one-shot goal (no movement
 * flags), so it never fights the melee goals. All entity reads are null-guarded.
 */
public final class PackAlertGoal<T extends Mob> extends Goal {

    private final T mob;
    private final Class<T> packClass;
    private final double searchRadius;
    private final float alertRadius;
    private final int maxAlerted;
    private @Nullable Player spotter;

    public PackAlertGoal(T mob, Class<T> packClass, double searchRadius, float alertRadius, int maxAlerted) {
        this.mob = mob;
        this.packClass = packClass;
        this.searchRadius = searchRadius;
        this.alertRadius = alertRadius;
        this.maxAlerted = Math.max(0, maxAlerted);
    }

    @Override
    public boolean canUse() {
        if (this.mob.getTarget() != null) {
            return false;
        }
        if (!(this.mob.level() instanceof ServerLevel level)) {
            return false;
        }
        // Random gate: roughly one real check per ten ticks, so a horde is not a per-tick cost.
        if (this.mob.getRandom().nextInt(reducedTickDelay(20)) != 0) {
            return false;
        }
        Player nearest = level.getNearestPlayer(this.mob, this.searchRadius);
        if (nearest == null || nearest.isCreative() || nearest.isSpectator()) {
            return false;
        }
        if (!this.mob.hasLineOfSight(nearest)) {
            return false;
        }
        this.spotter = nearest;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return false; // one-shot: spread the alarm, then let the melee/target goals take over
    }

    @Override
    public void start() {
        Player target = this.spotter;
        this.spotter = null;
        if (target == null) {
            return;
        }
        this.mob.setTarget(target);
        AABB box = this.mob.getBoundingBox().inflate(this.alertRadius);
        int alerted = 0;
        for (T other : this.mob.level().getEntitiesOfClass(this.packClass, box,
                candidate -> candidate != this.mob && candidate.isAlive() && candidate.getTarget() == null)) {
            if (alerted >= this.maxAlerted) {
                break;
            }
            other.setTarget(target);
            alerted++;
        }
    }
}
