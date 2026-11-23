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
 * Azathoth: the blind, bubbling nucleus at the centre of everything. A bloated core of poisonous
 * light, a ring of lesser bubbles orbiting it, flutes of bone jutting out of the mass and a skirt of
 * tendrils. No eyes, no limbs, no face.
 */
public final class AzathothModel extends BossModelBase {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "azathoth"), "main");

    private final ModelPart nucleus;
    private final ModelPart orbit;

    public AzathothModel(ModelPart root) {
        super(root);
        this.nucleus = root.getChild("nucleus");
        this.orbit = root.getChild("orbit");

        this.pulse(this.nucleus, 0.0F, 0.035F, 0.07F);
        this.pulse(this.orbit, 0.8F, 0.02F, 0.05F);
        for (int i = 0; i < 5; i++) {
            this.sway(this.orbit.getChild("bubble" + i), i * 0.8F, 0.05F, 0.08F, 0.03F);
        }
        for (int i = 0; i < 9; i++) {
            this.sway(root.getChild("tendril" + i), i * 0.6F, 0.10F, 0.16F, 0.05F);
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition nucleus = BossParts.blob(root, "nucleus", 0.0F, -4.0F, 0.0F, 32.0F, 32.0F, 30.0F, 32, 32);
        BossParts.blob(nucleus, "bubble_top", 0.0F, -16.0F, 0.0F, 18.0F, 14.0F, 16.0F, 64, 32);
        BossParts.blob(nucleus, "bubble_side", 15.0F, 2.0F, -2.0F, 14.0F, 14.0F, 14.0F, 96, 32);
        BossParts.blob(nucleus, "bubble_under", -12.0F, 12.0F, 4.0F, 16.0F, 12.0F, 14.0F, 32, 64);
        nucleus.addOrReplaceChild("maw",
                CubeListBuilder.create().texOffs(BossParts.PUPIL_U, BossParts.PUPIL_V)
                        .addBox(-7.0F, -5.0F, -15.0F, 14.0F, 10.0F, 5.0F),
                PartPose.offset(0.0F, 2.0F, -14.0F));

        // Flute-pipes of bone sticking out of the nucleus at odd angles.
        for (int i = 0; i < 6; i++) {
            float angle = (float) (Math.PI * 2.0 * i / 6.0);
            BossParts.spike(nucleus, "pipe" + i,
                    (float) Math.cos(angle) * 13.0F, -2.0F, (float) Math.sin(angle) * 13.0F,
                    16.0F, 1.2F, (float) (Math.PI / 2.0), -angle, 0.0F, 96, 96);
        }

        PartDefinition orbit = root.addOrReplaceChild("orbit", CubeListBuilder.create(), PartPose.offset(0.0F, -4.0F, 0.0F));
        for (int i = 0; i < 5; i++) {
            float angle = (float) (Math.PI * 2.0 * i / 5.0);
            float x = (float) Math.cos(angle) * 23.0F;
            float z = (float) Math.sin(angle) * 20.0F;
            float y = -6.0F + (float) Math.sin(i * 1.7F) * 7.0F;
            float size = 9.0F + (float) Math.sin(i * 2.3F) * 2.0F;
            BossParts.blob(orbit, "bubble" + i, x, y, z, size, size, size, 64, 64);
        }

        Tentacles.cluster(root, "tendril", 9, 14.0F, 6.0F, 12.0F, 12.0F, 17.0F, 1.2F, 3, 32, 96);
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    protected void applyIdle(BossRenderState state, float time, float breath) {
        this.orbit.yRot = time * 0.012F;
        this.orbit.xRot = Mth.sin(time * 0.02F) * 0.10F;
        this.nucleus.yRot += Mth.sin(time * 0.015F) * 0.05F;
        this.nucleus.getChild("bubble_top").y += Mth.sin(time * 0.06F) * 0.8F;
    }

    @Override
    protected void applyLunge(BossRenderState state) {
        super.applyLunge(state);
        float lunge = state.lunge;
        this.nucleus.z += -3.5F * lunge;
        this.nucleus.yScale = 1.0F + 0.08F * lunge;
    }
}
