package com.sanchous98.eldritchhorror.client.model.kit;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Chaugnar Faugn's elephantine idol head: a broad skull, a hanging segmented trunk, ivory tusks,
 * fan ears and a heavy brow. The returned part is the head; the trunk is its child {@code trunk}
 * so a model can raise it.
 */
public final class TrunkedIdol {

    private TrunkedIdol() {
    }

    public static PartDefinition build(PartDefinition parent, float y,
                                       float headWidth, float headHeight, float headDepth,
                                       float trunkLength, float trunkThickness, int u, int v) {
        CubeListBuilder skull = CubeListBuilder.create()
                .texOffs(u, v).addBox(-headWidth / 2.0F, -headHeight, -headDepth / 2.0F,
                        headWidth, headHeight, headDepth)
                .texOffs(u, v).addBox(-headWidth * 0.42F, -headHeight * 1.10F, -headDepth * 0.38F,
                        headWidth * 0.84F, headHeight * 0.22F, headDepth * 0.76F)
                .texOffs(u, v).addBox(-headWidth * 0.30F, -headHeight * 0.70F, -headDepth * 0.60F,
                        headWidth * 0.60F, headHeight * 0.24F, headDepth * 0.26F);
        PartDefinition head = parent.addOrReplaceChild("head", skull, PartPose.offset(0.0F, y, 0.0F));

        // Ears: wide flat slabs fanned out to either side.
        BossParts.bar(head, "ear_left", -headWidth * 0.66F, -headHeight * 0.62F, 0.0F,
                headWidth * 0.42F, headHeight * 0.72F, headDepth * 0.16F, 0.0F, 0.0F, 0.28F, u, v);
        BossParts.bar(head, "ear_right", headWidth * 0.66F, -headHeight * 0.62F, 0.0F,
                headWidth * 0.42F, headHeight * 0.72F, headDepth * 0.16F, 0.0F, 0.0F, -0.28F, u, v);

        // Trunk: a chain of three boxes hanging from the front of the skull.
        float segLength = trunkLength / 3.0F;
        PartDefinition trunk = head.addOrReplaceChild("trunk",
                CubeListBuilder.create().texOffs(u, v).addBox(-trunkThickness, 0.0F, -trunkThickness,
                        trunkThickness * 2.0F, segLength, trunkThickness * 2.0F),
                PartPose.offsetAndRotation(0.0F, -headHeight * 0.24F, -headDepth * 0.46F, 0.24F, 0.0F, 0.0F));
        PartDefinition current = trunk;
        for (int s = 1; s < 3; s++) {
            float taper = trunkThickness * (1.0F - s * 0.16F);
            current = current.addOrReplaceChild("s" + s,
                    CubeListBuilder.create().texOffs(u, v).addBox(-taper, 0.0F, -taper,
                            taper * 2.0F, segLength, taper * 2.0F),
                    PartPose.offsetAndRotation(0.0F, segLength, 0.0F, 0.20F, 0.0F, 0.0F));
        }

        // Tusks: curved spikes angling forward and up from the jaw.
        BossParts.spike(head, "tusk_left", -headWidth * 0.26F, -headHeight * 0.28F, -headDepth * 0.46F,
                headHeight * 0.62F, headWidth * 0.045F, -0.9F, 0.0F, -0.35F, u, v);
        BossParts.spike(head, "tusk_right", headWidth * 0.26F, -headHeight * 0.28F, -headDepth * 0.46F,
                headHeight * 0.62F, headWidth * 0.045F, -0.9F, 0.0F, 0.35F, u, v);

        // Deep-set mismatched eyes under the brow.
        BossParts.eye(head, "eye_left", -headWidth * 0.22F, -headHeight * 0.72F, -headDepth * 0.52F,
                headWidth * 0.10F);
        BossParts.eye(head, "eye_right", headWidth * 0.24F, -headHeight * 0.66F, -headDepth * 0.53F,
                headWidth * 0.07F);
        return head;
    }
}
