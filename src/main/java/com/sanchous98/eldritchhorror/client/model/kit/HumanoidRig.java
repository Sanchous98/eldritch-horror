package com.sanchous98.eldritchhorror.client.model.kit;

import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Standard upright rig for the human-shaped presences. Builds parts named:
 * {@code torso}, {@code head} (child of torso), {@code right_arm}, {@code left_arm} (children of
 * torso), {@code right_leg}, {@code left_leg}. Proportions are parameters so the same rig covers
 * the squat shapes, the gaunt Ithaqua and the slender masked Nyarlathotep.
 */
public final class HumanoidRig {

    private HumanoidRig() {
    }

    /**
     * @param hipY      y of the hip pivots (feet end at hipY + legH; use 24 - legH)
     * @param torsoW    torso width
     * @param torsoH    torso height (upward from hip)
     * @param torsoD    torso depth
     * @param headW/H/D head size, sitting on top of the torso
     * @param armW/H/D  arm size, hanging from the shoulders
     * @param legW/H/D  leg size, hanging from the hips
     */
    public static void build(PartDefinition root, float hipY,
                             float torsoW, float torsoH, float torsoD,
                             float headW, float headH, float headD,
                             float armW, float armH, float armD,
                             float legW, float legH, float legD,
                             int u, int v) {
        PartDefinition torso = BossParts.torso(root, "torso", 0.0F, hipY, 0.0F,
                torsoW, torsoH, torsoD, u, v);
        BossParts.torso(torso, "head", 0.0F, -torsoH, 0.0F, headW, headH, headD, u, v);
        BossParts.limb(torso, "right_arm", torsoW * 0.5F + armW * 0.3F, -torsoH + 2.0F, 0.0F,
                armW, armH, armD, u, v);
        BossParts.limb(torso, "left_arm", -torsoW * 0.5F - armW * 0.3F, -torsoH + 2.0F, 0.0F,
                armW, armH, armD, u, v);
        BossParts.limb(root, "right_leg", torsoW * 0.26F, hipY, 0.0F, legW, legH, legD, u, v);
        BossParts.limb(root, "left_leg", -torsoW * 0.26F, hipY, 0.0F, legW, legH, legD, u, v);
    }
}
