package com.sanchous98.eldritchhorror.client.model.boss;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.client.model.BossModelBase;
import com.sanchous98.eldritchhorror.client.model.BossRenderState;
import com.sanchous98.eldritchhorror.client.model.kit.BossParts;
import com.sanchous98.eldritchhorror.client.model.kit.HumanoidRig;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * Nephren-Ka, the Black Pharaoh who remembers: a bandaged king in a tall double crown, one eye
 * burning through the wrappings, a false beard and a staff of office held like a weapon.
 */
public final class NephrenKaModel extends BossModelBase {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "nephren_ka"), "main");

    private static final float HIP_Y = 14.0F;

    private final ModelPart torso;
    private final ModelPart head;

    public NephrenKaModel(ModelPart root) {
        super(root);
        this.torso = root.getChild("torso");
        this.head = this.torso.getChild("head");

        this.breatheWith(this.torso, 0.015F);
        this.sway(this.head, 0.7F, 0.02F, 0.04F, 0.015F);
        this.arm(this.torso.getChild("right_arm"), (float) Math.PI, 0.4F);
        this.leg(root.getChild("right_leg"), 0.0F, 0.7F);
        this.leg(root.getChild("left_leg"), (float) Math.PI, 0.7F);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        HumanoidRig.build(root, HIP_Y, 12.0F, 16.0F, 8.0F,
                9.0F, 7.0F, 9.0F,
                5.0F, 18.0F, 5.0F,
                5.0F, 10.0F, 6.0F, 32, 32);

        PartDefinition torso = root.getChild("torso");
        for (int i = 0; i < 5; i++) {
            float y = -2.0F - i * 3.0F;
            BossParts.box(torso, "wrap" + i, 0.0F, y, 0.0F, 13.0F, 1.6F, 9.0F, 64, 32);
        }
        BossParts.box(torso, "collar", 0.0F, -14.0F, 0.0F, 16.0F, 3.0F, 11.0F, 96, 32);

        PartDefinition head = torso.getChild("head");
        BossParts.box(head, "face", 0.0F, -4.0F, -4.7F, 9.0F, 8.0F, 2.0F,
                BossParts.EYE_U, BossParts.EYE_V);
        BossParts.eye(head, "eye", 0.0F, -5.0F, -5.0F, 2.2F);
        BossParts.box(head, "beard", 0.0F, 2.5F, -4.5F, 3.0F, 6.0F, 2.0F, 64, 64);
        BossParts.bar(head, "crown_left", -8.0F, -9.0F, 0.0F, 10.0F, 3.0F, 2.0F, 0.0F, 0.0F, 0.35F, 96, 64);
        BossParts.bar(head, "crown_right", 8.0F, -9.0F, 0.0F, 10.0F, 3.0F, 2.0F, 0.0F, 0.0F, -0.35F, 96, 64);
        BossParts.bar(head, "crown_top", 0.0F, -14.0F, 0.0F, 4.0F, 10.0F, 4.0F, 0.0F, 0.0F, 0.0F, 96, 96);

        BossParts.bar(torso.getChild("left_arm"), "staff", 0.0F, 18.0F, -2.0F,
                2.0F, 26.0F, 2.0F, 0.0F, 0.0F, 0.0F, 32, 96);
        BossParts.spike(torso.getChild("left_arm"), "staff_head", 0.0F, 18.0F, -2.0F,
                7.0F, 1.2F, (float) Math.PI, 0.0F, 0.0F, 96, 96);
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    protected void applyIdle(BossRenderState state, float time, float breath) {
        this.head.yRot += Mth.sin(time * 0.02F) * 0.05F;
        this.torso.getChild("left_arm").xRot += Mth.sin(time * 0.03F) * 0.02F;
    }

    @Override
    protected void applyLunge(BossRenderState state) {
        super.applyLunge(state);
        float lunge = state.lunge;
        this.torso.xRot += -0.15F * lunge;
        this.head.xRot += -0.2F * lunge;
        this.torso.getChild("left_arm").xRot += -1.0F * lunge;
    }
}
