package com.sanchous98.eldritchhorror.client.model.kit;

import net.minecraft.client.model.geom.builders.PartDefinition;

/** A scattering of mismatched eyes around a head or body. */
public final class CrownOfEyes {

    private CrownOfEyes() {
    }

    /**
     * Adds {@code count} eyes at a given height/radius. Sizes vary deterministically so the gaze
     * looks wrong in the intended way.
     */
    public static void build(PartDefinition parent, String prefix, int count, float radius,
                             float y, float zOffset, float baseSize) {
        for (int i = 0; i < count; i++) {
            float angle = (float) (Math.PI * 2.0 * i / count);
            float x = (float) Math.cos(angle) * radius;
            float z = (float) Math.sin(angle) * radius * 0.6F + zOffset;
            float size = baseSize * (0.7F + 0.55F * Math.abs((float) Math.sin(i * 1.7F)));
            BossParts.eye(parent, prefix + i, x, y + (float) Math.sin(i * 2.1F) * baseSize * 0.40F, z, size);
        }
    }
}
