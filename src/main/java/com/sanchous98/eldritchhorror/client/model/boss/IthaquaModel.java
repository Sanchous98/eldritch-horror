package com.sanchous98.eldritchhorror.client.model.boss;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.client.model.BossModelBase;
import com.sanchous98.eldritchhorror.client.model.BossRenderState;
import com.sanchous98.eldritchhorror.client.model.kit.BossParts;
import com.sanchous98.eldritchhorror.client.model.kit.HumanoidRig;
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
 * Ithaqua, the Wind-Walker: an unnaturally tall, starved humanoid with too-long arms, claws of ice,
 * a ragged cloak of frozen strips and a ring of wind that never stops turning around its legs.
 */
@OnlyIn(Dist.CLIENT)
public final class IthaquaModel extends BossModelBase {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "ithaqua"), "main");

    private static final float HIP_Y = 10.0F;

    private final ModelPart torso;
    private final ModelPart head;
    private final ModelPart wind;

    public IthaquaModel(ModelPart root) {
        super(root);
        this.torso = root.getChild("torso");
        this.head = this.torso.getChild("head");
        this.wind = root.getChild("wind");

        this.breatheWith(this.torso, 0.02F);
        this.sway(this.head, 0.7F, 0.03F, 0.06F, 0.02F);
        for (int i = 0; i < 6; i++) {
            this.sway(this.torso.getChild("tatter" + i), i * 0.8F, 0.14F, 0.18F, 0.07F);
        }
        this.arm(this.torso.getChild("right_arm"), (float) Math.PI, 0.5F);
        this.arm(this.torso.getChild("left_arm"), 0.0F, 0.5F);
        this.leg(root.getChild("right_leg"), 0.0F, 0.8F);
        this.leg(root.getChild("left_leg"), (float) Math.PI, 0.8F);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        HumanoidRig.build(root, HIP_Y, 14.0F, 20.0F, 9.0F,
                10.0F, 8.0F, 10.0F,
                5.0F, 28.0F, 5.0F,
                5.0F, 14.0F, 6.0F, 32, 32);

        PartDefinition torso = root.getChild("torso");
        BossParts.blob(torso, "chest", 0.0F, -12.0F, 0.0F, 18.0F, 14.0F, 12.0F, 64, 32);
        PartDefinition head = torso.getChild("head");
        BossParts.box(head, "face", 0.0F, -4.0F, -5.0F, 9.0F, 8.0F, 2.0F, 96, 32);
        BossParts.eye(head, "eye_left", -2.2F, -4.0F, -5.2F, 1.8F);
        BossParts.eye(head, "eye_right", 2.2F, -4.0F, -5.2F, 1.8F);
        BossParts.spike(head, "antler_left", -4.0F, -7.0F, 0.0F, 9.0F, 1.0F, -2.4F, 0.0F, -0.5F, 96, 96);
        BossParts.spike(head, "antler_right", 4.0F, -7.0F, 0.0F, 9.0F, 1.0F, -2.4F, 0.0F, 0.5F, 96, 96);

        Tentacles.cluster(torso, "tatter", 6, 8.0F, 2.0F, 6.0F, 0.0F, 22.0F, 0.9F, 4, 64, 96);
        BossParts.spike(torso, "shoulder_ice_left", -8.0F, -18.0F, 0.0F, 8.0F, 1.6F, -2.2F, 0.0F, -0.4F, 96, 96);
        BossParts.spike(torso, "shoulder_ice_right", 8.0F, -18.0F, 0.0F, 8.0F, 1.6F, -2.2F, 0.0F, 0.4F, 96, 96);

        for (PartDefinition arm : new PartDefinition[]{
                torso.getChild("right_arm"), torso.getChild("left_arm")}) {
            BossParts.spike(arm, "claw1", 0.0F, 28.0F, -2.0F, 6.0F, 0.8F, 0.3F, 0.0F, 0.2F, 32, 96);
            BossParts.spike(arm, "claw2", 0.0F, 28.0F, 2.0F, 6.0F, 0.8F, 0.3F, 0.0F, -0.2F, 32, 96);
        }

        PartDefinition wind = root.addOrReplaceChild("wind", CubeListBuilder.create(), PartPose.offset(0.0F, 8.0F, 0.0F));
        for (int i = 0; i < 6; i++) {
            float angle = (float) (Math.PI * 2.0 * i / 6.0);
            BossParts.bar(wind, "gust" + i, (float) Math.cos(angle) * 13.0F, i * 3.0F - 6.0F,
                    (float) Math.sin(angle) * 13.0F, 10.0F, 1.0F, 1.0F, 0.0F, -angle, 0.15F, 96, 64);
        }
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    protected void applyIdle(BossRenderState state, float time, float breath) {
        this.wind.yRot = time * 0.05F;
        this.wind.y += Mth.sin(time * 0.1F) * 0.4F;
        this.torso.yRot += Mth.sin(time * 0.025F) * 0.03F;
        this.head.xRot += Mth.sin(time * 0.05F + 1.0F) * 0.04F;
    }

    @Override
    protected void applyLunge(BossRenderState state) {
        super.applyLunge(state);
        float lunge = state.lunge;
        this.torso.xRot += -0.22F * lunge;
        this.head.xRot += -0.3F * lunge;
        this.torso.getChild("right_arm").xRot += -1.2F * lunge;
        this.torso.getChild("left_arm").xRot += -1.2F * lunge;
        this.wind.yRot += 1.2F * lunge;
    }
}
