package com.sanchous98.eldritchhorror.client.model.kit;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * The drowned shell of Glaaki: a squat dome with rings of pale spines, an under-lip of tendrils and
 * a scatter of eyes. The returned part is the shell itself, so a model can pulse or bob it.
 */
public final class SpinedShell {

    private SpinedShell() {
    }

    public static PartDefinition build(PartDefinition parent, String name, float y,
                                       float width, float height, float depth,
                                       int spineRings, int spinesPerRing, int u, int v) {
        CubeListBuilder cubes = CubeListBuilder.create()
                .texOffs(u, v).addBox(-width / 2.0F, -height / 2.0F, -depth / 2.0F, width, height, depth)
                .texOffs(u, v).addBox(-width * 0.40F, -height * 0.72F, -depth * 0.40F,
                        width * 0.80F, height * 0.34F, depth * 0.80F)
                .texOffs(u, v).addBox(-width * 0.30F, -height * 0.86F, -depth * 0.30F,
                        width * 0.60F, height * 0.22F, depth * 0.60F)
                .texOffs(u, v).addBox(-width * 0.46F, height * 0.36F, -depth * 0.46F,
                        width * 0.92F, height * 0.16F, depth * 0.92F);
        PartDefinition shell = parent.addOrReplaceChild(name, cubes, PartPose.offset(0.0F, y, 0.0F));

        for (int ring = 0; ring < spineRings; ring++) {
            float ringY = -height * 0.34F + ring * height * 0.26F;
            float radius = width * (0.40F + ring * 0.07F);
            float length = height * (0.42F + ring * 0.06F);
            BossParts.spikeRing(shell, "spine_" + ring + "_", spinesPerRing, radius, ringY, length,
                    Math.max(1.0F, width * 0.035F), u, v);
        }

        // A few mismatched eyes embedded in the dome.
        BossParts.eye(shell, "eye_0", -width * 0.18F, -height * 0.10F, -depth * 0.48F, width * 0.13F);
        BossParts.eye(shell, "eye_1", width * 0.16F, -height * 0.20F, -depth * 0.50F, width * 0.09F);
        BossParts.eye(shell, "eye_2", width * 0.02F, -height * 0.02F, -depth * 0.52F, width * 0.06F);
        return shell;
    }
}
