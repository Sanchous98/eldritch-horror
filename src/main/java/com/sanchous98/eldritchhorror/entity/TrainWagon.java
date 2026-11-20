package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.world.rail.RailSample;
import com.sanchous98.eldritchhorror.world.rail.TrainTimetable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * A passenger wagon coupled behind a {@link TrainLocomotive} ({@code design/30-transport.md},
 * stage 2). It owns no path and no ticket: every tick it samples the locomotive's recent trail
 * {@code carIndex × spacing} blocks behind, so the consist bends through curves instead of clipping
 * through them. It has the same four seats and fare interaction as the locomotive.
 *
 * <p>Placeholder art: a scaled vanilla minecart model via
 * {@code client/TrainCarRenderer}.
 */
public class TrainWagon extends TrainCar {

    public TrainWagon(EntityType<? extends TrainWagon> type, Level level) {
        super(type, level);
    }

    /** Places the wagon on its sampled trail slot immediately after spawning. */
    public void positionFromTrail() {
        TrainLocomotive locomotive = this.locomotive();
        if (locomotive == null) {
            return;
        }
        RailSample sample = locomotive.trailBehind(
                TrainTimetable.SPACING * this.carIndex());
        if (sample != null) {
            this.setPos(sample.x(), sample.y() + 0.1, sample.z());
            this.setYRot(sample.yaw());
        }
    }

    /**
     * Breaks the coupling and removes this wagon. Called by the locomotive's removal path; it does
     * not touch the locomotive, so the teardown cannot recurse.
     */
    public void detachAndDiscard() {
        this.detach();
        this.discard();
    }

    @Override
    public void tick() {
        if (!(this.level() instanceof ServerLevel)) {
            super.tick();
            return;
        }
        TrainLocomotive locomotive = this.locomotive();
        if (locomotive == null || locomotive.isRemoved()) {
            this.discard();
            return;
        }
        super.tick();
        RailSample sample = locomotive.trailBehind(
                TrainTimetable.SPACING * this.carIndex());
        if (sample != null) {
            this.setPos(sample.x(), sample.y() + 0.1, sample.z());
            this.setYRot(sample.yaw());
        }
        this.setDeltaMovement(Vec3.ZERO);
    }
}
