package com.sanchous98.eldritchhorror.client.model.boss;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.client.model.BossModelBase;
import com.sanchous98.eldritchhorror.client.model.BossRenderState;
import com.sanchous98.eldritchhorror.client.model.kit.BossParts;
import com.sanchous98.eldritchhorror.client.model.kit.CrownOfEyes;
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
 * The Dunwich Horror: a bloated, half-invisible mass with a crown of tentacles, too many mismatched
 * eyes and a set of insect-thin legs. Roughly 2.9 blocks tall; deliberately nothing like a bull.
 *
 * <p>The body is a lumpy sphere (two overlapping blobs), the head is sunk into the shoulders, and
 * eight tentacles hang from the crown. Four spindly two-segment legs carry it, with a phase offset
 * per leg so the gait reads as skittering rather than trotting.
 */
public final class DunwichHorrorModel extends BossModelBase {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "dunwich_horror"), "main");

    private static final int TENTACLES = 8;
    private static final int LEGS = 4;

    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart maw;

    public DunwichHorrorModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.maw = this.head.getChild("maw");

        this.breatheWith(this.body, 0.035F);
        this.sway(this.body, 0.4F, 0.015F, 0.02F, 0.01F);
        this.sway(this.head, 1.7F, 0.05F, 0.08F, 0.03F);

        for (int i = 0; i < TENTACLES; i++) {
            this.sway(this.head.getChild("t" + i), i * 0.8F, 0.10F, 0.14F, 0.06F);
        }
        for (int i = 0; i < LEGS; i++) {
            this.leg(root.getChild("leg" + i), i == 0 || i == 3 ? 0.0F : (float) Math.PI, 1.15F);
        }
        this.arm(this.body.getChild("arm_right"), (float) Math.PI, 0.5F);
        this.arm(this.body.getChild("arm_left"), 0.0F, 0.5F);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition body = BossParts.blob(root, "body", 0.0F, 4.0F, 0.0F, 26.0F, 26.0F, 22.0F, 32, 32);
        BossParts.blob(body, "belly", 0.0F, 7.0F, 3.0F, 30.0F, 20.0F, 24.0F, 64, 32);
        BossParts.blob(body, "shoulder_lump", 0.0F, -9.0F, 2.0F, 20.0F, 12.0F, 18.0F, 96, 32);

        PartDefinition head = BossParts.blob(body, "head", 0.0F, -18.0F, -5.0F, 18.0F, 16.0F, 16.0F, 32, 64);
        // The maw is drawn from the reserved dark pupil UV so it reads as a hole in the mass.
        head.addOrReplaceChild("maw",
                CubeListBuilder.create().texOffs(BossParts.PUPIL_U, BossParts.PUPIL_V)
                        .addBox(-5.0F, -2.0F, -3.0F, 10.0F, 4.0F, 4.0F),
                PartPose.offset(0.0F, 3.5F, -8.0F));

        Tentacles.crown(head, "t", TENTACLES, 8.0F, -2.0F, 14.0F, 1.5F, 4, 32, 96);
        CrownOfEyes.build(head, "head_eye_", 5, 7.0F, -6.0F, -7.0F, 2.4F);
        CrownOfEyes.build(body, "body_eye_", 3, 11.0F, -2.0F, -10.0F, 2.0F);

        BossParts.limb(body, "arm_right", 13.0F, -2.0F, -1.0F, 3.0F, 16.0F, 3.0F, 32, 112);
        BossParts.limb(body, "arm_left", -13.0F, -2.0F, -1.0F, 3.0F, 16.0F, 3.0F, 32, 112);
        BossParts.spike(body, "claw_right", 13.0F, 14.0F, -1.0F, 5.0F, 1.1F, 0.0F, 0.0F, 0.2F, 96, 112);
        BossParts.spike(body, "claw_left", -13.0F, 14.0F, -1.0F, 5.0F, 1.1F, 0.0F, 0.0F, -0.2F, 96, 112);

        for (int i = 0; i < LEGS; i++) {
            float x = (i == 0 || i == 3) ? 8.0F : -8.0F;
            float z = (i < 2) ? -5.0F : 6.0F;
            PartDefinition upper = BossParts.limb(root, "leg" + i, x, 8.0F, z,
                    3.0F, 9.0F, 3.0F, 64, 112);
            upper.addOrReplaceChild("shin",
                    CubeListBuilder.create().texOffs(64, 112).addBox(-1.2F, 0.0F, -1.2F, 2.4F, 7.0F, 2.4F),
                    PartPose.offsetAndRotation(0.0F, 9.0F, 0.0F, x > 0 ? -0.12F : 0.12F, 0.0F, 0.0F));
        }
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    protected void applyIdle(BossRenderState state, float time, float breath) {
        this.body.yRot += Mth.sin(time * 0.03F) * 0.06F;
        this.head.xRot += Mth.sin(time * 0.07F + 1.0F) * 0.05F;
        this.maw.yScale = 1.0F + Mth.sin(time * 0.11F) * 0.25F;
    }

    @Override
    protected void applyLunge(BossRenderState state) {
        super.applyLunge(state);
        float lunge = state.lunge;
        this.body.xRot += -0.20F * lunge;
        this.head.xRot += -0.35F * lunge;
        this.maw.yScale = 1.0F + 1.2F * lunge;
    }
}
