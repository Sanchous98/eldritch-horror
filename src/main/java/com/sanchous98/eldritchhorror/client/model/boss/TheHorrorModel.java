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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * The Horror at the end of the roster: the final, accumulated mass. A mountain of eyes and mouths
 * surrounded by two crowns of tentacles, with faces growing out of its flanks and tendrils reaching
 * the ground. It has no legs and no one shape.
 */
@OnlyIn(Dist.CLIENT)
public final class TheHorrorModel extends BossModelBase {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "the_horror"), "main");

    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart sideFace;

    public TheHorrorModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.sideFace = this.body.getChild("side_face");

        this.breatheWith(this.body, 0.035F);
        this.pulse(this.head, 0.3F, 0.02F, 0.05F);
        this.sway(this.head, 0.9F, 0.04F, 0.07F, 0.03F);
        this.sway(this.sideFace, 1.8F, 0.06F, 0.10F, 0.04F);
        for (int i = 0; i < 10; i++) {
            this.sway(this.head.getChild("crown" + i), i * 0.55F, 0.11F, 0.16F, 0.06F);
        }
        for (int i = 0; i < 14; i++) {
            this.sway(root.getChild("tendril" + i), i * 0.5F, 0.12F, 0.18F, 0.07F);
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition body = BossParts.blob(root, "body", 0.0F, -6.0F, 0.0F, 34.0F, 46.0F, 30.0F, 32, 32);
        BossParts.blob(body, "haunch", 0.0F, 28.0F, 2.0F, 30.0F, 20.0F, 28.0F, 64, 32);
        PartDefinition head = BossParts.blob(body, "head", 0.0F, -34.0F, -2.0F, 26.0F, 20.0F, 22.0F, 96, 32);
        PartDefinition sideFace = BossParts.blob(body, "side_face", 15.0F, -16.0F, -10.0F, 15.0F, 14.0F, 14.0F, 32, 64);

        head.addOrReplaceChild("maw",
                CubeListBuilder.create().texOffs(BossParts.PUPIL_U, BossParts.PUPIL_V)
                        .addBox(-8.0F, -4.0F, -11.5F, 16.0F, 8.0F, 5.0F),
                PartPose.offset(0.0F, 2.0F, 0.0F));
        sideFace.addOrReplaceChild("side_maw",
                CubeListBuilder.create().texOffs(BossParts.PUPIL_U, BossParts.PUPIL_V)
                        .addBox(-5.0F, -3.0F, -7.5F, 10.0F, 6.0F, 4.0F),
                PartPose.offset(0.0F, 1.0F, 0.0F));

        Tentacles.crown(head, "crown", 10, 11.0F, -8.0F, 18.0F, 1.7F, 4, 32, 96);
        Tentacles.cluster(root, "tendril", 14, 17.0F, 8.0F, 13.0F, 10.0F, 20.0F, 1.4F, 4, 64, 96);
        CrownOfEyes.build(head, "head_eye_", 7, 9.0F, -5.0F, -10.0F, 2.4F);
        CrownOfEyes.build(body, "body_eye_", 5, 13.0F, -8.0F, -13.0F, 2.2F);
        CrownOfEyes.build(sideFace, "side_eye_", 2, 5.0F, -2.0F, -6.0F, 1.7F);

        BossParts.spike(head, "horn0", -8.0F, -8.0F, 0.0F, 22.0F, 2.0F, (float) Math.PI, 0.0F, -0.35F, 96, 96);
        BossParts.spike(head, "horn1", 8.0F, -8.0F, 0.0F, 22.0F, 2.0F, (float) Math.PI, 0.0F, 0.35F, 96, 96);
        BossParts.spike(head, "horn2", 0.0F, -9.0F, 4.0F, 26.0F, 2.2F, (float) Math.PI, 0.0F, 0.0F, 96, 96);
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    protected void applyIdle(BossRenderState state, float time, float breath) {
        this.body.yRot += Mth.sin(time * 0.018F) * 0.04F;
        this.head.xRot += Mth.sin(time * 0.035F) * 0.04F;
        this.head.yRot += Mth.sin(time * 0.026F + 1.2F) * 0.08F;
        this.sideFace.yRot += Mth.sin(time * 0.04F + 0.4F) * 0.12F;
    }

    @Override
    protected void applyLunge(BossRenderState state) {
        super.applyLunge(state);
        float lunge = state.lunge;
        this.body.xRot += -0.15F * lunge;
        this.head.xRot += -0.35F * lunge;
        this.sideFace.xRot += -0.3F * lunge;
    }
}
