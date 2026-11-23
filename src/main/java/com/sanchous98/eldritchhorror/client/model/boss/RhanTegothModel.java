package com.sanchous98.eldritchhorror.client.model.boss;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.client.model.BossModelBase;
import com.sanchous98.eldritchhorror.client.model.BossRenderState;
import com.sanchous98.eldritchhorror.client.model.kit.BossParts;
import com.sanchous98.eldritchhorror.client.model.kit.CrownOfEyes;
import com.sanchous98.eldritchhorror.client.model.kit.WebLimbs;
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
 * Rhan-Tegoth: the crab-idol on the polar shore. A domed carapace crowded with eyes, a golden crown
 * of spikes, eight jointed crab legs and two massive pincers held out in front. It scuttles rather
 * than walks.
 */
@OnlyIn(Dist.CLIENT)
public final class RhanTegothModel extends BossModelBase {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "rhan_tegoth"), "main");

    private final ModelPart body;
    private final ModelPart clawRight;
    private final ModelPart clawLeft;

    public RhanTegothModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.clawRight = root.getChild("claw_right");
        this.clawLeft = root.getChild("claw_left");

        this.breatheWith(this.body, 0.025F);
        this.sway(this.body, 0.3F, 0.02F, 0.03F, 0.02F);
        this.sway(this.clawRight, 0.0F, 0.02F, 0.04F, 0.02F);
        this.sway(this.clawLeft, 1.5F, 0.02F, 0.04F, 0.02F);
        for (int i = 0; i < 8; i++) {
            this.leg(root.getChild("leg" + i), i * 0.8F, 0.30F);
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition body = BossParts.blob(root, "body", 0.0F, 0.0F, 0.0F, 24.0F, 26.0F, 22.0F, 32, 32);
        BossParts.blob(body, "carapace", 0.0F, -14.0F, -2.0F, 22.0F, 14.0F, 20.0F, 64, 32);
        PartDefinition head = BossParts.blob(body, "head", 0.0F, -20.0F, -12.0F, 14.0F, 12.0F, 12.0F, 96, 32);
        CrownOfEyes.build(head, "eye_", 5, 5.0F, -2.0F, -5.0F, 1.8F);
        BossParts.spike(head, "mandible_left", -3.0F, 2.0F, -6.0F, 6.0F, 0.8F, 0.4F, 0.0F, -0.2F, 32, 96);
        BossParts.spike(head, "mandible_right", 3.0F, 2.0F, -6.0F, 6.0F, 0.8F, 0.4F, 0.0F, 0.2F, 32, 96);
        BossParts.spikeRing(head, "crown_", 7, 6.0F, -12.0F, 6.0F, 1.2F, 96, 96);

        WebLimbs.build(root, 4.0F, 22.0F, 20.0F, 2.3F, 64, 64);

        PartDefinition clawRight = root.addOrReplaceChild("claw_right",
                CubeListBuilder.create().texOffs(32, 64).addBox(0.0F, -4.0F, -4.0F, 12.0F, 7.0F, 8.0F)
                        .texOffs(32, 64).addBox(10.0F, -8.0F, -5.0F, 6.0F, 15.0F, 10.0F),
                PartPose.offsetAndRotation(10.0F, -2.0F, -9.0F, 0.0F, -0.55F, 0.0F));
        BossParts.spike(clawRight, "pincer_upper", 15.0F, -4.0F, -4.0F, 11.0F, 1.6F, 0.0F, 0.0F, -1.1F, 96, 64);
        BossParts.spike(clawRight, "pincer_lower", 15.0F, 4.0F, -4.0F, 11.0F, 1.6F, 0.0F, 0.0F, 1.6F, 96, 64);

        PartDefinition clawLeft = root.addOrReplaceChild("claw_left",
                CubeListBuilder.create().texOffs(32, 64).mirror().addBox(-12.0F, -4.0F, -4.0F, 12.0F, 7.0F, 8.0F)
                        .texOffs(32, 64).mirror().addBox(-16.0F, -8.0F, -5.0F, 6.0F, 15.0F, 10.0F),
                PartPose.offsetAndRotation(-10.0F, -2.0F, -9.0F, 0.0F, 0.55F, 0.0F));
        BossParts.spike(clawLeft, "pincer_upper", -15.0F, -4.0F, -4.0F, 11.0F, 1.6F, 0.0F, 0.0F, 1.1F, 96, 64);
        BossParts.spike(clawLeft, "pincer_lower", -15.0F, 4.0F, -4.0F, 11.0F, 1.6F, 0.0F, 0.0F, -1.6F, 96, 64);
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    protected void applyIdle(BossRenderState state, float time, float breath) {
        this.body.yRot += Mth.sin(time * 0.03F) * 0.04F;
        this.clawRight.yRot = -0.55F + Mth.sin(time * 0.06F) * 0.08F;
        this.clawLeft.yRot = 0.55F - Mth.sin(time * 0.06F + 0.7F) * 0.08F;
        for (int i = 0; i < 8; i++) {
            ModelPart leg = this.root.getChild("leg" + i);
            leg.zRot += Mth.sin(time * 0.14F + i) * 0.05F;
        }
    }

    @Override
    protected void applyLunge(BossRenderState state) {
        super.applyLunge(state);
        float lunge = state.lunge;
        this.body.z += -2.0F * lunge;
        this.clawRight.yRot = -0.9F * lunge;
        this.clawLeft.yRot = 0.9F * lunge;
        this.clawRight.xRot += -0.2F * lunge;
        this.clawLeft.xRot += -0.2F * lunge;
    }
}
