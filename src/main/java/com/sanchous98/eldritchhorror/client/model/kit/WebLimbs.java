package com.sanchous98.eldritchhorror.client.model.kit;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Eight long segmented legs for Atlach-Nacha and the spider-like things. Leg roots are named
 * {@code leg0}..{@code leg7}; the model animates them by Z rotation so the web-walker creeps.
 */
public final class WebLimbs {

    private WebLimbs() {
    }

    public static void build(PartDefinition parent, float bodyY, float bodyWidth,
                             float legLength, float legThickness, int u, int v) {
        for (int i = 0; i < 8; i++) {
            boolean right = i < 4;
            int pair = i % 4;
            float z = -bodyWidth * 0.34F + pair * bodyWidth * 0.23F;
            float x = right ? bodyWidth * 0.34F : -bodyWidth * 0.34F;
            float out = right ? 1.0F : -1.0F;
            float yRot = out * (0.55F + pair * 0.22F);
            float zRot = out * (0.55F + pair * 0.15F);
            PartDefinition root = parent.addOrReplaceChild("leg" + i,
                    CubeListBuilder.create().texOffs(u, v).addBox(0.0F, -legThickness * 0.5F, -legThickness * 0.5F,
                            legLength * 0.62F, legThickness, legThickness),
                    PartPose.offsetAndRotation(x, bodyY, z, 0.0F, yRot, zRot));
            PartDefinition knee = root.addOrReplaceChild("knee",
                    CubeListBuilder.create().texOffs(u, v).addBox(0.0F, -legThickness * 0.45F, -legThickness * 0.45F,
                            legLength * 0.62F, legThickness * 0.9F, legThickness * 0.9F),
                    PartPose.offsetAndRotation(legLength * 0.62F, 0.0F, 0.0F, 0.0F, out * 0.75F, out * 0.45F));
            BossParts.spike(knee, "claw", legLength * 0.55F, 0.0F, 0.0F, legLength * 0.22F,
                    legThickness * 0.55F, 0.0F, 0.0F, out * 0.9F, u, v);
        }
    }
}
