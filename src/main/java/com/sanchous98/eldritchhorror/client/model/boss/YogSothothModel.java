package com.sanchous98.eldritchhorror.client.model.boss;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.client.model.BossModelBase;
import com.sanchous98.eldritchhorror.client.model.BossRenderState;
import com.sanchous98.eldritchhorror.client.model.kit.BossParts;
import com.sanchous98.eldritchhorror.client.model.kit.Tentacles;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * Yog-Sothoth: the gate and the key. A core of orange-gold light inside two great rotating rings,
 * surrounded by lesser spheres, with tendrils dripping into our space. Nothing humanoid.
 */
public final class YogSothothModel extends BossModelBase {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "yog_sothoth"), "main");

    private final ModelPart nucleus;
    private final ModelPart gate;
    private final ModelPart gateTilted;
    private final ModelPart orbit;

    public YogSothothModel(ModelPart root) {
        super(root);
        this.nucleus = root.getChild("nucleus");
        this.gate = root.getChild("gate");
        this.gateTilted = root.getChild("gate_tilted");
        this.orbit = root.getChild("orbit");

        this.pulse(this.nucleus, 0.0F, 0.03F, 0.06F);
        this.pulse(this.orbit, 0.4F, 0.02F, 0.04F);
        for (int i = 0; i < 6; i++) {
            this.sway(this.orbit.getChild("sphere" + i), i * 0.7F, 0.04F, 0.06F, 0.02F);
        }
        for (int i = 0; i < 8; i++) {
            this.sway(root.getChild("drip" + i), i * 0.5F, 0.09F, 0.14F, 0.05F);
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        BossParts.blob(root, "nucleus", 0.0F, -26.0F, 0.0F, 30.0F, 30.0F, 28.0F, 32, 32);
        BossParts.blob(root, "crown_sphere", 0.0F, -58.0F, 0.0F, 12.0F, 12.0F, 12.0F, 64, 32);
        BossParts.bar(root, "cord", 0.0F, -43.0F, 0.0F, 3.0F, 18.0F, 3.0F, 0.0F, 0.0F, 0.0F, 96, 96);

        // Two gate-rings: one upright, one tilted through it.
        PartDefinition gate = root.addOrReplaceChild("gate", CubeListBuilder.create(), PartPose.offset(0.0F, -26.0F, 0.0F));
        ring(gate, "g1_", 14, 31.0F, 96, 32);
        PartDefinition tilted = root.addOrReplaceChild("gate_tilted", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, -26.0F, 0.0F, 0.0F, 0.0F, 0.62F));
        ring(tilted, "g2_", 12, 25.0F, 96, 64);

        PartDefinition orbit = root.addOrReplaceChild("orbit", CubeListBuilder.create(), PartPose.offset(0.0F, -26.0F, 0.0F));
        for (int i = 0; i < 6; i++) {
            float angle = (float) (Math.PI * 2.0 * i / 6.0);
            float x = (float) Math.cos(angle) * 24.0F;
            float z = (float) Math.sin(angle) * 24.0F;
            float y = Mth.sin(i * 1.9F) * 14.0F;
            BossParts.blob(orbit, "sphere" + i, x, y, z, 10.0F, 10.0F, 10.0F, 64, 64);
        }

        Tentacles.cluster(root, "drip", 8, 18.0F, 6.0F, 16.0F, 8.0F, 18.0F, 1.1F, 3, 32, 96);
        return LayerDefinition.create(mesh, 128, 128);
    }

    private static void ring(PartDefinition parent, String prefix, int segments, float radius,
                             int u, int v) {
        float chord = 2.0F * radius * Mth.sin((float) Math.PI / segments);
        for (int i = 0; i < segments; i++) {
            float angle = (float) (Math.PI * 2.0 * i / segments);
            float x = (float) Math.cos(angle) * radius;
            float y = (float) Math.sin(angle) * radius;
            BossParts.bar(parent, prefix + i, x, y, 0.0F, 3.0F, chord * 1.08F, 4.0F,
                    0.0F, 0.0F, angle, u, v);
        }
    }

    @Override
    protected void applyIdle(BossRenderState state, float time, float breath) {
        this.gate.yRot = time * 0.008F;
        this.gateTilted.yRot = -time * 0.011F;
        this.orbit.yRot = time * 0.014F;
        this.orbit.xRot = Mth.sin(time * 0.017F) * 0.15F;
        this.nucleus.yRot += Mth.sin(time * 0.02F) * 0.08F;
    }

    @Override
    protected void applyLunge(BossRenderState state) {
        super.applyLunge(state);
        float lunge = state.lunge;
        this.gate.yScale = 1.0F + 0.10F * lunge;
        this.gateTilted.xScale = 1.0F + 0.10F * lunge;
        this.nucleus.z += -3.0F * lunge;
    }
}
