package com.sanchous98.eldritchhorror.client.model.kit;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Bat/dragon wings for the winged presences (Cthulhu, night-gaunt shapes). Two parts,
 * {@code wing_left} and {@code wing_right}, pivoted at the shoulder blades so a model can flap them
 * by rotating Z.
 */
public final class Wings {

    private Wings() {
    }

    public static void build(PartDefinition parent, float y, float z,
                             float span, float height, int u, int v) {
        parent.addOrReplaceChild("wing_right",
                wing(span, height, true, u, v),
                PartPose.offsetAndRotation(5.0F, y, z, 0.20F, 0.0F, -0.35F));
        parent.addOrReplaceChild("wing_left",
                wing(span, height, false, u, v),
                PartPose.offsetAndRotation(-5.0F, y, z, 0.20F, 0.0F, 0.35F));
    }

    private static CubeListBuilder wing(float span, float height, boolean right, int u, int v) {
        float x0 = right ? 0.0F : -span;
        CubeListBuilder cubes = CubeListBuilder.create()
                .texOffs(u, v).addBox(x0, -height * 0.50F, -1.0F, span, height, 2.0F)
                .texOffs(u, v).addBox(x0 + (right ? span * 0.18F : span * 0.34F),
                        -height * 0.18F, 0.8F, span * 0.52F, height * 0.72F, 1.6F)
                .texOffs(u, v).addBox(x0 + (right ? span * 0.10F : span * 0.48F),
                        -height * 0.66F, -0.4F, span * 0.38F, height * 0.30F, 1.4F);
        return cubes;
    }
}
