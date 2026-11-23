package com.sanchous98.eldritchhorror.client.model.boss;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.client.model.BossModelBase;
import com.sanchous98.eldritchhorror.client.model.BossRenderState;
import com.sanchous98.eldritchhorror.client.model.kit.BossParts;
import com.sanchous98.eldritchhorror.client.model.kit.HumanoidRig;
import com.sanchous98.eldritchhorror.client.model.kit.Tentacles;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * Hastur, the Yellow King: a tall robed figure under a hood, its face a pale mask, a jagged crown
 * above and a hem of ragged yellow strips. The sleeves swallow the arms; only the mask is pale.
 */
public final class HasturModel extends BossModelBase {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "hastur"), "main");

    private static final float HIP_Y = 18.0F;

    private final ModelPart torso;
    private final ModelPart head;
    private final ModelPart robe;

    public HasturModel(ModelPart root) {
        super(root);
        this.torso = root.getChild("torso");
        this.head = this.torso.getChild("head");
        this.robe = root.getChild("robe");

        this.breatheWith(this.torso, 0.018F);
        this.sway(this.robe, 0.3F, 0.03F, 0.05F, 0.02F);
        this.sway(this.head, 1.1F, 0.02F, 0.05F, 0.02F);
        for (int i = 0; i < 8; i++) {
            this.sway(root.getChild("tatter" + i), i * 0.6F, 0.10F, 0.14F, 0.05F);
        }
        this.arm(this.torso.getChild("right_arm"), 0.0F, 0.35F);
        this.arm(this.torso.getChild("left_arm"), (float) Math.PI, 0.35F);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        HumanoidRig.build(root, HIP_Y, 20.0F, 20.0F, 12.0F,
                12.0F, 12.0F, 12.0F,
                6.0F, 22.0F, 6.0F,
                6.0F, 6.0F, 7.0F, 32, 32);

        PartDefinition robe = BossParts.torso(root, "robe", 0.0F, 24.0F, 0.0F, 26.0F, 16.0F, 18.0F, 64, 32);
        BossParts.box(robe, "hem", 0.0F, 0.0F, 0.0F, 30.0F, 4.0F, 22.0F, 96, 32);
        Tentacles.crown(root, "tatter", 8, 12.0F, 12.0F, 10.0F, 1.0F, 2, 32, 96);

        PartDefinition torso = root.getChild("torso");
        BossParts.blob(torso, "shoulders", 0.0F, -18.0F, 0.0F, 24.0F, 10.0F, 14.0F, 64, 64);
        PartDefinition head = torso.getChild("head");
        BossParts.box(head, "hood", 0.0F, -6.0F, 1.5F, 18.0F, 16.0F, 16.0F, 96, 64);
        BossParts.box(head, "mask", 0.0F, -6.0F, -6.0F, 10.0F, 12.0F, 2.0F,
                BossParts.EYE_U, BossParts.EYE_V);
        BossParts.eye(head, "eye_left", -2.8F, -7.0F, -7.3F, 2.4F);
        BossParts.eye(head, "eye_right", 2.8F, -7.0F, -7.3F, 2.4F);
        for (int i = 0; i < 5; i++) {
            float x = -6.0F + i * 3.0F;
            BossParts.spike(head, "crown" + i, x, -13.0F, 0.0F, 8.0F + i % 2 * 2.0F, 1.1F,
                    (float) Math.PI, 0.0F, 0.0F, 96, 96);
        }

        for (PartDefinition arm : new PartDefinition[]{
                torso.getChild("right_arm"), torso.getChild("left_arm")}) {
            BossParts.box(arm, "sleeve", 0.0F, 9.0F, 0.0F, 10.0F, 18.0F, 10.0F, 64, 96);
        }
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    protected void applyIdle(BossRenderState state, float time, float breath) {
        this.robe.zRot += Mth.sin(time * 0.03F) * 0.02F;
        this.head.yRot += Mth.sin(time * 0.025F + 0.8F) * 0.10F;
        this.head.xRot += Mth.sin(time * 0.04F) * 0.03F;
    }

    @Override
    protected void applyLunge(BossRenderState state) {
        super.applyLunge(state);
        float lunge = state.lunge;
        this.torso.xRot += -0.16F * lunge;
        this.head.xRot += -0.25F * lunge;
        this.torso.getChild("right_arm").zRot += -0.5F * lunge;
        this.torso.getChild("left_arm").zRot += 0.5F * lunge;
    }
}
