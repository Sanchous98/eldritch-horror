package com.sanchous98.eldritchhorror.client.model.kit;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Tentacle builders: radial crowns, beards and loose clusters. Each tentacle is a short chain of
 * tapering boxes; the first segment is the part the model animates (swaying it moves the chain).
 */
public final class Tentacles {

    private Tentacles() {
    }

    /** A ring of tentacles hanging from a head or body, fanned outward. */
    public static void crown(PartDefinition parent, String prefix, int count, float radius, float baseY,
                             float length, float thickness, int segments, int u, int v) {
        for (int i = 0; i < count; i++) {
            float angle = (float) (Math.PI * 2.0 * i / count);
            float x = (float) Math.cos(angle) * radius;
            float z = (float) Math.sin(angle) * radius;
            tentacle(parent, prefix + i, x, baseY, z, 0.32F, angle, length, thickness, segments, u, v);
        }
    }

    /** Tentacles sprouting from the lower front of a head, like Cthulhu's beard. */
    public static void beard(PartDefinition parent, String prefix, int count, float spread, float baseY,
                             float z, float length, float thickness, int segments, int u, int v) {
        for (int i = 0; i < count; i++) {
            float t = count == 1 ? 0.0F : i / (float) (count - 1) * 2.0F - 1.0F;
            float x = t * spread;
            float angle = -t * 0.5F;
            tentacle(parent, prefix + i, x, baseY, z, -0.15F, angle, length, thickness, segments, u, v);
        }
    }

    /** A loose cluster for shapeless masses: deterministic pseudo-random positions. */
    public static void cluster(PartDefinition parent, String prefix, int count, float spreadX,
                               float spreadY, float spreadZ, float baseY, float length,
                               float thickness, int segments, int u, int v) {
        for (int i = 0; i < count; i++) {
            float x = (float) Math.sin(i * 2.399F) * spreadX;
            float z = (float) Math.cos(i * 1.731F) * spreadZ;
            float y = baseY + (float) Math.sin(i * 0.977F) * spreadY;
            float tilt = 0.45F + (float) Math.sin(i * 1.13F) * 0.35F;
            tentacle(parent, prefix + i, x, y, z, tilt, i * 0.7F, length, thickness, segments, u, v);
        }
    }

    /** One tapered chain. The root part contains the first segment and the children the rest. */
    public static PartDefinition tentacle(PartDefinition parent, String name,
                                          float x, float y, float z, float tilt, float yRot,
                                          float length, float thickness, int segments, int u, int v) {
        float segLength = length / segments;
        PartDefinition root = parent.addOrReplaceChild(name,
                segmentCube(segLength, thickness, u, v),
                PartPose.offsetAndRotation(x, y, z, tilt, yRot, 0.0F));
        PartDefinition current = root;
        for (int s = 1; s < segments; s++) {
            float taper = thickness * (1.0F - 0.62F * s / segments);
            current = current.addOrReplaceChild("s" + s,
                    segmentCube(segLength, taper, u, v),
                    PartPose.offsetAndRotation(0.0F, segLength, 0.0F, 0.22F, 0.0F, 0.0F));
        }
        return root;
    }

    private static CubeListBuilder segmentCube(float length, float thickness, int u, int v) {
        return CubeListBuilder.create()
                .texOffs(u, v).addBox(-thickness, 0.0F, -thickness, thickness * 2.0F, length, thickness * 2.0F);
    }
}
