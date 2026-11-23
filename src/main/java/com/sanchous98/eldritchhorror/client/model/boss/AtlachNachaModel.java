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
 * Atlach-Nacha: the spider that weaves the bridge between the worlds. A low, wide cephalothorax
 * and abdomen on eight long jointed legs, a cluster of eyes and a crown of pale web-struts spun
 * above the back. Low to the ground but very wide, like a web-anchor.
 */
@OnlyIn(Dist.CLIENT)
public final class AtlachNachaModel extends BossModelBase {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "atlach_nacha"), "main");

    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart webCrown;

    public AtlachNachaModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.webCrown = root.getChild("web_crown");

        this.breatheWith(this.body, 0.025F);
        this.sway(this.body, 0.2F, 0.02F, 0.04F, 0.02F);
        this.sway(this.head, 1.2F, 0.04F, 0.07F, 0.03F);
        for (int i = 0; i < 8; i++) {
            this.leg(root.getChild("leg" + i), i * 0.8F, 0.35F);
        }
        for (int i = 0; i < 10; i++) {
            this.sway(this.webCrown.getChild("strut" + i), i * 0.6F, 0.0F, 0.12F, 0.10F);
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition body = BossParts.blob(root, "body", 0.0F, 8.0F, 2.0F, 22.0F, 15.0F, 26.0F, 32, 32);
        BossParts.blob(body, "abdomen", 0.0F, 4.0F, 12.0F, 18.0F, 14.0F, 16.0F, 64, 32);
        PartDefinition head = BossParts.blob(body, "head", 0.0F, 0.0F, -14.0F, 15.0F, 11.0F, 12.0F, 96, 32);
        CrownOfEyes.build(head, "eye_", 6, 5.5F, -2.0F, -5.0F, 1.7F);
        BossParts.spike(head, "fang_left", -2.5F, 3.0F, -6.0F, 6.0F, 0.9F, 0.35F, 0.0F, -0.25F, 32, 96);
        BossParts.spike(head, "fang_right", 2.5F, 3.0F, -6.0F, 6.0F, 0.9F, 0.35F, 0.0F, 0.25F, 32, 96);

        WebLimbs.build(root, 4.0F, 20.0F, 24.0F, 2.2F, 64, 64);

        PartDefinition web = root.addOrReplaceChild("web_crown", CubeListBuilder.create(),
                PartPose.offset(0.0F, -2.0F, 6.0F));
        for (int i = 0; i < 10; i++) {
            float angle = (float) (Math.PI * 2.0 * i / 10.0);
            BossParts.bar(web, "strut" + i,
                    (float) Math.cos(angle) * 8.0F, 0.0F, (float) Math.sin(angle) * 6.0F,
                    1.2F, 16.0F, 1.2F, 1.0F, -angle, 0.0F, 96, 96);
        }
        BossParts.bar(web, "web_ring", 0.0F, -10.0F, 0.0F, 18.0F, 1.0F, 14.0F, 0.0F, 0.0F, 0.0F, 32, 96);
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    protected void applyIdle(BossRenderState state, float time, float breath) {
        this.webCrown.yRot = time * 0.01F;
        this.body.yRot += Mth.sin(time * 0.03F) * 0.03F;
        for (int i = 0; i < 8; i++) {
            ModelPart leg = this.root.getChild("leg" + i);
            leg.zRot += Mth.sin(time * 0.12F + i * 0.8F) * 0.06F;
            leg.xRot = Mth.cos(time * 0.15F + i * 0.8F) * 0.10F;
        }
    }

    @Override
    protected void applyLunge(BossRenderState state) {
        super.applyLunge(state);
        float lunge = state.lunge;
        this.body.z += -2.5F * lunge;
        this.head.xRot += -0.25F * lunge;
    }
}
