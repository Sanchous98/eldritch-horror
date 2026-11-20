package com.sanchous98.eldritchhorror.world.rail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/**
 * Guards the consist coupling maths (design/30 stage 2): wagons keep a fixed spacing on a straight
 * run, bend around a curve, and do not drift when the locomotive stops. This is what keeps a
 * multi-car consist from clipping through itself at corners.
 */
class TrainTrailTest {

    @Test
    void wagonsKeepTheirSpacingOnAStraightRun() {
        TrainTrail trail = new TrainTrail();
        for (int i = 0; i <= 20; i++) {
            trail.append(i, 64.0, 0.0, -90.0F, i);
        }
        RailSample sample = trail.sampleBehind(4.5);
        assertNotNull(sample);
        assertEquals(15.5, sample.x(), 1.0E-9);
        assertEquals(0.0, sample.z(), 1.0E-9);
        assertEquals(-90.0F, sample.yaw(), 1.0E-4);
    }

    @Test
    void theTrailBendsAroundACorner() {
        TrainTrail trail = new TrainTrail();
        trail.append(0.0, 64.0, 0.5, -90.0F, 0.0);
        trail.append(4.0, 64.0, 0.5, -90.0F, 4.0);
        trail.append(4.5, 64.0, 1.0, 0.0F, 4.707);
        RailSample behind = trail.sampleBehind(0.5);
        assertNotNull(behind);
        assertEquals(4.146, behind.x(), 0.01);
        assertEquals(0.646, behind.z(), 0.01);
    }

    @Test
    void aStoppedLocomotiveDoesNotPullTheTrailForward() {
        TrainTrail trail = new TrainTrail();
        trail.append(0.0, 64.0, 0.5, 0.0F, 0.0);
        trail.append(5.0, 64.0, 0.5, 0.0F, 5.0);
        RailSample first = trail.sampleBehind(3.0);
        RailSample second = trail.sampleBehind(3.0);
        assertNotNull(first);
        assertNotNull(second);
        assertEquals(first.x(), second.x(), 0.0);
        assertEquals(first.z(), second.z(), 0.0);
    }

    @Test
    void askingBeyondTheOldestPointClampsToTheOldest() {
        TrainTrail trail = new TrainTrail();
        trail.append(2.0, 64.0, 2.0, 0.0F, 2.0);
        trail.append(3.0, 64.0, 2.0, 0.0F, 3.0);
        RailSample sample = trail.sampleBehind(50.0);
        assertNotNull(sample);
        assertEquals(2.0, sample.x(), 1.0E-9);
    }
}
