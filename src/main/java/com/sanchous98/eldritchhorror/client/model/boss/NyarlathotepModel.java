package com.sanchous98.eldritchhorror.client.model.boss;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.client.model.BossModelBase;
import com.sanchous98.eldritchhorror.client.model.BossRenderState;
import com.sanchous98.eldritchhorror.client.model.kit.BossParts;
import com.sanchous98.eldritchhorror.client.model.kit.CrownOfEyes;
import com.sanchous98.eldritchhorror.client.model.kit.HumanoidRig;
import com.sanchous98.eldritchhorror.client.model.kit.Tentacles;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Nyarlathotep, the Crawling Chaos: a slim black-robed figure whose face is a pale, eyeless mask
 * with a third eye, a ragged cloak of tendrils, and a second mask floating beside it because it is
 * never only one thing.
 */
@OnlyIn(Dist.CLIENT)
public final class NyarlathotepModel extends BossModelBase {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "nyarlathotep"), "main");

    private static final float HIP_Y = 12.0F;

    private final ModelPart torso;
    private final ModelPart head;
    private final ModelPart cloak;
    private final ModelPart secondMask;

    public NyarlathotepModel(ModelPart root) {
        super(root);
        this.torso = root.getChild("torso");
        this.head = this.torso.getChild("head");
        this.cloak = root.getChild("cloak");
        this.secondMask = this.torso.getChild("second_mask");

        this.breatheWith(this.torso, 0.015F);
        this.sway(this.cloak, 0.2F, 0.04F, 0.06F, 0.02F);
        this.sway(this.head, 1.4F, 0.03F, 0.06F, 0.02F);
        this.sway(this.secondMask, 2.1F, 0.10F, 0.18F, 0.08F);
        for (int i = 0; i < 5; i++) {
            this.sway(this.cloak.getChild("rag" + i), i * 0.9F, 0.12F, 0.16F, 0.06F);
        }
        this.arm(this.torso.getChild("right_arm"), (float) Math.PI, 0.4F);
        this.arm(this.torso.getChild("left_arm"), 0.0F, 0.4F);
        this.leg(root.getChild("right_leg"), 0.0F, 0.7F);
        this.leg(root.getChild("left_leg"), (float) Math.PI, 0.7F);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        HumanoidRig.build(root, HIP_Y, 10.0F, 13.0F, 6.0F,
                8.0F, 7.0F, 7.0F,
                4.0F, 16.0F, 4.0F,
                4.0F, 12.0F, 5.0F, 32, 32);

        PartDefinition cloak = BossParts.torso(root, "cloak", 0.0F, 18.0F, 3.0F, 18.0F, 20.0F, 3.0F, 64, 32);
        Tentacles.cluster(cloak, "rag", 5, 8.0F, 3.0F, 1.0F, 0.0F, 12.0F, 0.8F, 3, 32, 96);

        PartDefinition torso = root.getChild("torso");
        PartDefinition head = torso.getChild("head");
        BossParts.box(head, "mask", 0.0F, -4.0F, -4.0F, 9.0F, 9.0F, 2.0F,
                BossParts.EYE_U, BossParts.EYE_V);
        BossParts.eye(head, "eye_left", -2.0F, -4.5F, -4.2F, 1.8F);
        BossParts.eye(head, "eye_right", 2.0F, -4.5F, -4.2F, 1.8F);
        BossParts.eye(head, "third_eye", 0.0F, -7.5F, -4.2F, 1.4F);
        BossParts.spike(head, "horn_right", 4.0F, -7.0F, 0.0F, 7.0F, 1.0F, -0.6F, 0.0F, 0.5F, 96, 96);
        BossParts.spike(head, "horn_left", -4.0F, -7.0F, 0.0F, 7.0F, 1.0F, -0.6F, 0.0F, -0.5F, 96, 96);

        // The second mask hovers just off the shoulder, turning the other way.
        PartDefinition second = BossParts.box(torso, "second_mask", 12.0F, -10.0F, -1.0F, 7.0F, 8.0F, 2.0F,
                BossParts.EYE_U, BossParts.EYE_V);
        CrownOfEyes.build(second, "mask_eye_", 2, 2.0F, -2.0F, -1.0F, 1.4F);

        BossParts.spike(torso, "collar_right", 7.0F, -12.0F, 0.0F, 6.0F, 0.8F, (float) Math.PI, 0.0F, 0.2F, 96, 96);
        BossParts.spike(torso, "collar_left", -7.0F, -12.0F, 0.0F, 6.0F, 0.8F, (float) Math.PI, 0.0F, -0.2F, 96, 96);
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    protected void applyIdle(BossRenderState state, float time, float breath) {
        this.head.yRot += Mth.sin(time * 0.035F) * 0.12F;
        this.secondMask.yRot = Mth.sin(time * 0.05F + 1.0F) * 0.5F;
        this.secondMask.y += Mth.sin(time * 0.07F) * 0.6F;
        this.cloak.zRot += Mth.sin(time * 0.025F) * 0.02F;
    }

    @Override
    protected void applyLunge(BossRenderState state) {
        super.applyLunge(state);
        float lunge = state.lunge;
        this.torso.xRot += -0.20F * lunge;
        this.head.xRot += -0.28F * lunge;
        this.secondMask.xRot += -0.5F * lunge;
        this.torso.getChild("right_arm").xRot += -1.0F * lunge;
        this.torso.getChild("left_arm").xRot += -1.0F * lunge;
    }
}
