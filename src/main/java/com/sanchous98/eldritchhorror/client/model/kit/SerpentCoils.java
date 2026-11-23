package com.sanchous98.eldritchhorror.client.model.kit;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Serpent geometry for Yig and the hydra: a stacked coil base plus an upright S-neck ending in a
 * wedge head. The coil part is {@code coils}, the neck is {@code neck} (a direct child of the
 * parent, so the model can sway it).
 */
public final class SerpentCoils {

    private SerpentCoils() {
    }

    public static void build(PartDefinition parent, float baseY, float radius, float coilHeight,
                             int coils, float neckLength, float neckThickness, float neckTilt,
                             int u, int v) {
        CubeListBuilder coilCubes = CubeListBuilder.create();
        for (int i = 0; i < coils; i++) {
            float angle = (float) (Math.PI * 2.0 * i / coils);
            float x = (float) Math.cos(angle) * radius;
            float z = (float) Math.sin(angle) * radius;
            float y = -i * (coilHeight / coils);
            coilCubes.texOffs(u, v).addBox(x - radius * 0.42F, y, z - radius * 0.42F,
                    radius * 0.84F, coilHeight * 0.55F, radius * 0.84F);
        }
        parent.addOrReplaceChild("coils", coilCubes, PartPose.offset(0.0F, baseY, 0.0F));

        float segLength = neckLength / 3.0F;
        PartDefinition neck = parent.addOrReplaceChild("neck",
                CubeListBuilder.create().texOffs(u, v).addBox(-neckThickness, 0.0F, -neckThickness,
                        neckThickness * 2.0F, segLength, neckThickness * 2.0F),
                PartPose.offsetAndRotation(0.0F, baseY - coilHeight * 0.55F, -radius * 0.45F,
                        neckTilt, 0.0F, 0.0F));
        PartDefinition current = neck;
        for (int s = 1; s < 3; s++) {
            current = current.addOrReplaceChild("s" + s,
                    CubeListBuilder.create().texOffs(u, v).addBox(-neckThickness * 0.86F, 0.0F, -neckThickness * 0.86F,
                            neckThickness * 1.72F, segLength, neckThickness * 1.72F),
                    PartPose.offsetAndRotation(0.0F, segLength, 0.0F, -neckTilt * 0.35F, 0.0F, 0.0F));
        }

        PartDefinition head = current.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(u, v).addBox(-neckThickness * 1.3F, 0.0F, -neckThickness * 1.9F,
                                neckThickness * 2.6F, neckThickness * 1.5F, neckThickness * 3.4F)
                        .texOffs(u, v).addBox(-neckThickness * 0.9F, -neckThickness * 0.8F, -neckThickness * 1.2F,
                                neckThickness * 1.8F, neckThickness * 0.8F, neckThickness * 2.2F),
                PartPose.offsetAndRotation(0.0F, segLength, 0.0F, -neckTilt, 0.0F, 0.0F));
        BossParts.eye(head, "eye_left", -neckThickness * 0.9F, neckThickness * 0.45F,
                -neckThickness * 1.6F, neckThickness * 0.75F);
        BossParts.eye(head, "eye_right", neckThickness * 0.9F, neckThickness * 0.45F,
                -neckThickness * 1.6F, neckThickness * 0.75F);
    }
}
