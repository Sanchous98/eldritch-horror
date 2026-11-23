package com.sanchous98.eldritchhorror.client.model.kit;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * A column/orb of living flame for Cthugha and Tulzscha: stacked narrowing boxes plus licking
 * tongues. The returned {@code flame} part is scaled by the model so the column breathes.
 */
public final class FlameBody {

    private FlameBody() {
    }

    public static PartDefinition build(PartDefinition parent, float baseY, float width, float depth,
                                       float height, int levels, int tongues, int u, int v) {
        CubeListBuilder cubes = CubeListBuilder.create();
        for (int i = 0; i < levels; i++) {
            float t = i / (float) (levels - 1);
            float w = width * (1.0F - t * 0.62F);
            float d = depth * (1.0F - t * 0.62F);
            float y = -t * height;
            cubes.texOffs(u, v).addBox(-w / 2.0F, y - height / levels, -d / 2.0F,
                    w, height / levels * 1.05F, d);
        }
        PartDefinition flame = parent.addOrReplaceChild("flame", cubes, PartPose.offset(0.0F, baseY, 0.0F));
        for (int i = 0; i < tongues; i++) {
            float angle = (float) (Math.PI * 2.0 * i / tongues);
            float x = (float) Math.cos(angle) * width * 0.42F;
            float z = (float) Math.sin(angle) * depth * 0.42F;
            BossParts.spike(flame, "tongue_" + i, x, -height * (0.55F + 0.25F * Math.abs((float) Math.cos(i))),
                    z, height * 0.55F, width * 0.06F,
                    (float) (Math.PI * 0.85F), -angle, 0.0F, u, v);
        }
        return flame;
    }
}
