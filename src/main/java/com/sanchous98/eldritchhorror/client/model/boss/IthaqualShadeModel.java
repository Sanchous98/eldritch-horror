package com.sanchous98.eldritchhorror.client.model.boss;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.client.model.BossModelBase;
import com.sanchous98.eldritchhorror.client.model.BossRenderState;
import com.sanchous98.eldritchhorror.client.model.kit.BossParts;
import com.sanchous98.eldritchhorror.client.model.kit.Tentacles;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * The Ithaqual Shade: the cold that walks after the Wind-Walker. A gaunt, hooded grey shape whose
 * lower body has given up being legs and become a drift of frozen rags, with long arms and two pale
 * eyes. Thin enough to look like a hole in the snow.
 */
public final class IthaqualShadeModel extends BossModelBase {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "ithaqual_shade"), "main");

    private static final int TATTERS = 10;

    private final ModelPart torso;
    private final ModelPart head;

    public IthaqualShadeModel(ModelPart root) {
        super(root);
        this.torso = root.getChild("torso");
        this.head = this.torso.getChild("head");

        this.breatheWith(this.torso, 0.02F);
        this.sway(this.head, 0.5F, 0.03F, 0.05F, 0.02F);
        this.sway(this.torso, 1.0F, 0.02F, 0.04F, 0.02F);
        for (int i = 0; i < TATTERS; i++) {
            this.sway(root.getChild("tatter" + i), i * 0.55F, 0.12F, 0.18F, 0.07F);
        }
        this.arm(this.torso.getChild("right_arm"), (float) Math.PI, 0.45F);
        this.arm(this.torso.getChild("left_arm"), 0.0F, 0.45F);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition torso = BossParts.torso(root, "torso", 0.0F, 10.0F, 0.0F, 10.0F, 20.0F, 8.0F, 32, 32);
        BossParts.torso(torso, "head", 0.0F, -20.0F, 0.0F, 8.0F, 8.0F, 8.0F, 64, 32);
        PartDefinition head = torso.getChild("head");
        BossParts.box(head, "hood", 0.0F, -4.0F, 1.0F, 13.0F, 14.0F, 12.0F, 96, 32);
        BossParts.box(head, "face", 0.0F, -4.0F, -4.2F, 7.0F, 7.0F, 1.6F,
                BossParts.EYE_U, BossParts.EYE_V);
        BossParts.eye(head, "eye_left", -1.8F, -4.5F, -4.6F, 1.6F);
        BossParts.eye(head, "eye_right", 1.8F, -4.5F, -4.6F, 1.6F);
        BossParts.spike(head, "frost_left", -5.0F, -7.0F, 0.0F, 6.0F, 0.8F, -2.4F, 0.0F, -0.5F, 96, 64);
        BossParts.spike(head, "frost_right", 5.0F, -7.0F, 0.0F, 6.0F, 0.8F, -2.4F, 0.0F, 0.5F, 96, 64);

        BossParts.limb(torso, "right_arm", 6.0F, -18.0F, 0.0F, 3.0F, 26.0F, 3.0F, 32, 64);
        BossParts.limb(torso, "left_arm", -6.0F, -18.0F, 0.0F, 3.0F, 26.0F, 3.0F, 32, 64);

        Tentacles.crown(root, "tatter", TATTERS, 7.0F, 10.0F, 16.0F, 0.9F, 4, 64, 96);
        BossParts.spike(torso, "shoulder_spike_left", -6.0F, -18.0F, 0.0F, 7.0F, 1.2F, -2.2F, 0.0F, -0.4F, 96, 96);
        BossParts.spike(torso, "shoulder_spike_right", 6.0F, -18.0F, 0.0F, 7.0F, 1.2F, -2.2F, 0.0F, 0.4F, 96, 96);
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    protected void applyIdle(BossRenderState state, float time, float breath) {
        this.torso.y += Mth.sin(time * 0.03F) * 0.6F;
        this.head.yRot += Mth.sin(time * 0.035F + 0.8F) * 0.10F;
        this.torso.getChild("right_arm").zRot += Mth.sin(time * 0.04F) * 0.04F;
        this.torso.getChild("left_arm").zRot -= Mth.sin(time * 0.04F) * 0.04F;
    }

    @Override
    protected void applyLunge(BossRenderState state) {
        super.applyLunge(state);
        float lunge = state.lunge;
        this.torso.xRot += -0.18F * lunge;
        this.head.xRot += -0.22F * lunge;
        this.torso.getChild("right_arm").xRot += -1.1F * lunge;
        this.torso.getChild("left_arm").xRot += -1.1F * lunge;
    }
}
