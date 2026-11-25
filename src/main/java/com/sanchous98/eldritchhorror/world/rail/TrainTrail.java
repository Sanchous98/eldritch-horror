package com.sanchous98.eldritchhorror.world.rail;

import com.sanchous98.eldritchhorror.world.EarthMap;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * The locomotive's recent trail, sampled by the wagons so the consist bends around curves.
 *
 * <p>The locomotive appends its position (with the distance travelled) as it moves; a wagon asks
 * for the point a fixed spacing behind, and gets a linearly interpolated position and a shortest-arc
 * yaw. Because samples are keyed by distance rather than by tick, a stopped train keeps its spacing
 * and a fast train cannot overtake its own trail. Pure and unit-tested.
 */
public final class TrainTrail {
    /** Enough history for the longest consist at the slowest configured speed. */
    private static final int CAPACITY = 512;
    /** The legacy Earth cylinder's circumference in blocks. */
    private static final int EARTH_WRAP = 2 * EarthMap.HALF_WIDTH;

    /** One recorded position. */
    public record TrailPoint(double x, double y, double z, float yaw, double distance) {
    }

    private final List<TrailPoint> points = new ArrayList<>();
    private int wrapWidth = EARTH_WRAP;

    /** Forgets all history (a fresh service). */
    public void clear() {
        this.points.clear();
    }

    /**
     * Sets the horizontal geometry of the line this trail belongs to.
     *
     * @param width cylinder circumference in blocks, or {@code 0} for a 1:1 map plane
     */
    public void setWrapWidth(int width) {
        this.wrapWidth = Math.max(0, width);
    }

    /**
     * Records a position. Distances must be non-decreasing; a duplicate is ignored.
     *
     * @param x        world X
     * @param y        world Y
     * @param z        world Z
     * @param yaw      entity yaw in degrees
     * @param distance distance travelled when the position was recorded
     */
    public void append(double x, double y, double z, float yaw, double distance) {
        if (!this.points.isEmpty()) {
            double last = this.points.get(this.points.size() - 1).distance();
            if (distance <= last + 1.0E-6) {
                return;
            }
        }
        this.points.add(new TrailPoint(x, y, z, yaw, distance));
        while (this.points.size() > CAPACITY) {
            this.points.remove(0);
        }
    }

    /** @return how many positions are retained (diagnostics/tests). */
    public int size() {
        return this.points.size();
    }

    /**
     * Samples the point {@code behind} blocks behind the newest recorded position.
     *
     * @param behind distance behind the locomotive, in blocks (non-negative)
     * @return the interpolated sample, or {@code null} when the trail is empty
     */
    public @Nullable RailSample sampleBehind(double behind) {
        int size = this.points.size();
        if (size == 0) {
            return null;
        }
        TrailPoint newest = this.points.get(size - 1);
        double target = newest.distance() - Math.max(0.0, behind);
        if (target <= this.points.get(0).distance()) {
            TrailPoint oldest = this.points.get(0);
            return new RailSample(oldest.x(), oldest.y(), oldest.z(), oldest.yaw());
        }
        for (int i = size - 1; i > 0; i--) {
            TrailPoint later = this.points.get(i);
            TrailPoint earlier = this.points.get(i - 1);
            if (target >= earlier.distance() && target <= later.distance()) {
                double span = later.distance() - earlier.distance();
                double t = span <= 0.0 ? 0.0 : (target - earlier.distance()) / span;
                double dx = deltaX(earlier.x(), later.x());
                return new RailSample(
                        this.wrapWidth == 0 ? earlier.x() + dx * t
                                : EarthMap.wrapBlockX(earlier.x() + dx * t),
                        earlier.y() + (later.y() - earlier.y()) * t,
                        earlier.z() + (later.z() - earlier.z()) * t,
                        lerpYaw(earlier.yaw(), later.yaw(), t));
            }
        }
        TrailPoint oldest = this.points.get(0);
        return new RailSample(oldest.x(), oldest.y(), oldest.z(), oldest.yaw());
    }

    private double deltaX(double from, double to) {
        if (this.wrapWidth == 0) {
            return to - from;
        }
        double width = this.wrapWidth;
        double delta = to - from;
        return delta - width * Math.floor(delta / width + 0.5);
    }

    private static float lerpYaw(float from, float to, double t) {        float delta = (to - from) % 360.0F;
        if (delta > 180.0F) {
            delta -= 360.0F;
        } else if (delta < -180.0F) {
            delta += 360.0F;
        }
        return from + delta * (float) t;
    }
}
