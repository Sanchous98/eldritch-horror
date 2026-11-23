package com.sanchous98.eldritchhorror.client.model.kit;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Low-level part helpers for the Ancient One model kit.
 *
 * <p>Coordinates are Minecraft model units (1 unit = 1/16 block). The model root spans
 * y = -24 (three blocks above the feet) to y = 24 (the feet plane, because
 * {@code LivingEntityRenderer} translates the baked model by -1.501 blocks). All boxes are added
 * relative to their part pivot so the pivots stay animatable.
 *
 * <p>The texture sheet reserves the rectangle (0,0)-(32,16) for eyes: {@link #EYE_U}/{@link #EYE_V}
 * for the sclera and {@link #PUPIL_U}/{@link #PUPIL_V} for the pupil. Every generated texture
 * paints those two regions, so a model can put a readable eye anywhere on a flat mottled body.
 */
public final class BossParts {

    /** UV origin of the pale eye region reserved by the texture generator. */
    public static final int EYE_U = 0;
    public static final int EYE_V = 0;

    /** UV origin of the dark pupil region reserved by the texture generator. */
    public static final int PUPIL_U = 16;
    public static final int PUPIL_V = 0;

    private BossParts() {
    }

    /** A box centred on the part pivot. */
    public static PartDefinition box(PartDefinition parent, String name,
                                     float x, float y, float z, float w, float h, float d,
                                     int u, int v) {
        return parent.addOrReplaceChild(name,
                CubeListBuilder.create().texOffs(u, v).addBox(-w / 2.0F, -h / 2.0F, -d / 2.0F, w, h, d),
                PartPose.offset(x, y, z));
    }

    /** A box whose top face sits at the pivot (a torso / head growing upward). */
    public static PartDefinition torso(PartDefinition parent, String name,
                                       float x, float y, float z, float w, float h, float d,
                                       int u, int v) {
        return parent.addOrReplaceChild(name,
                CubeListBuilder.create().texOffs(u, v).addBox(-w / 2.0F, -h, -d / 2.0F, w, h, d),
                PartPose.offset(x, y, z));
    }

    /** A limb whose pivot is its top joint, hanging downward. */
    public static PartDefinition limb(PartDefinition parent, String name,
                                      float x, float y, float z, float w, float h, float d,
                                      int u, int v) {
        return parent.addOrReplaceChild(name,
                CubeListBuilder.create().texOffs(u, v).addBox(-w / 2.0F, 0.0F, -d / 2.0F, w, h, d),
                PartPose.offset(x, y, z));
    }

    /** A lumpy rounded mass: a main box plus two offset caps so the silhouette is not a cube. */
    public static PartDefinition blob(PartDefinition parent, String name,
                                      float x, float y, float z, float w, float h, float d,
                                      int u, int v) {
        CubeListBuilder cubes = CubeListBuilder.create()
                .texOffs(u, v).addBox(-w / 2.0F, -h / 2.0F, -d / 2.0F, w, h, d)
                .texOffs(u, v).addBox(-w * 0.40F, -h * 0.62F, -d * 0.40F, w * 0.80F, h * 0.28F, d * 0.80F)
                .texOffs(u, v).addBox(-w * 0.34F, h * 0.34F, -d * 0.34F, w * 0.68F, h * 0.24F, d * 0.68F)
                .texOffs(u, v).addBox(-w * 0.58F, -h * 0.28F, -d * 0.30F, w * 0.16F, h * 0.56F, d * 0.60F)
                .texOffs(u, v).addBox(w * 0.42F, -h * 0.28F, -d * 0.30F, w * 0.16F, h * 0.56F, d * 0.60F);
        return parent.addOrReplaceChild(name, cubes, PartPose.offset(x, y, z));
    }

    /**
     * A tapered spike pointing along +y from its pivot. Rotate the part to aim it.
     */
    public static PartDefinition spike(PartDefinition parent, String name,
                                       float x, float y, float z, float length, float thickness,
                                       float xRot, float yRot, float zRot, int u, int v) {
        CubeListBuilder cubes = CubeListBuilder.create()
                .texOffs(u, v).addBox(-thickness, 0.0F, -thickness, thickness * 2.0F, length * 0.72F, thickness * 2.0F)
                .texOffs(u, v).addBox(-thickness * 0.55F, length * 0.68F, -thickness * 0.55F,
                        thickness * 1.10F, length * 0.38F, thickness * 1.10F);
        return parent.addOrReplaceChild(name, cubes, PartPose.offsetAndRotation(x, y, z, xRot, yRot, zRot));
    }

    /** A ring of radiating spikes around the Y axis, e.g. a spined shell or a crown. */
    public static void spikeRing(PartDefinition parent, String prefix, int count, float radius,
                                 float y, float length, float thickness, int u, int v) {
        for (int i = 0; i < count; i++) {
            float angle = (float) (Math.PI * 2.0 * i / count);
            float x = (float) Math.cos(angle) * radius;
            float z = (float) Math.sin(angle) * radius;
            spike(parent, prefix + i, x, y, z, length, thickness,
                    (float) (Math.PI / 2.0), -angle, 0.0F, u, v);
        }
    }

    /** A single eye: pale sclera cube with a dark pupil cube just in front (-z). */
    public static PartDefinition eye(PartDefinition parent, String name,
                                     float x, float y, float z, float size) {
        CubeListBuilder cubes = CubeListBuilder.create()
                .texOffs(EYE_U, EYE_V).addBox(-size / 2.0F, -size / 2.0F, -size * 0.30F, size, size, size * 0.60F)
                .texOffs(PUPIL_U, PUPIL_V).addBox(-size * 0.22F, -size * 0.22F, -size * 0.42F,
                        size * 0.44F, size * 0.44F, size * 0.30F);
        return parent.addOrReplaceChild(name, cubes, PartPose.offset(x, y, z));
    }

    /** Horizontal cross beam, used for gate rings and web crowns. */
    public static PartDefinition bar(PartDefinition parent, String name,
                                     float x, float y, float z, float w, float h, float d,
                                     float xRot, float yRot, float zRot, int u, int v) {
        return parent.addOrReplaceChild(name,
                CubeListBuilder.create().texOffs(u, v).addBox(-w / 2.0F, -h / 2.0F, -d / 2.0F, w, h, d),
                PartPose.offsetAndRotation(x, y, z, xRot, yRot, zRot));
    }
}
