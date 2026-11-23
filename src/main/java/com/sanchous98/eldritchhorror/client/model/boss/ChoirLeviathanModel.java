package com.sanchous98.eldritchhorror.client.model.boss;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.client.model.BossModelBase;
import com.sanchous98.eldritchhorror.client.model.BossRenderState;
import com.sanchous98.eldritchhorror.client.model.kit.BossParts;
import com.sanchous98.eldritchhorror.client.model.kit.CrownOfEyes;
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
 * The Choir Leviathan: a drowned whale that should not still be singing. A long barnacled body
 * hanging in the water, one great head crowded with eyes, rows of mouths opening along the belly,
 * and a fluked tail that never quite stops moving.
 */
@OnlyIn(Dist.CLIENT)
public final class ChoirLeviathanModel extends BossModelBase {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "choir_leviathan"), "main");

    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart tail;
    private final ModelPart flipperRight;
    private final ModelPart flipperLeft;

    public ChoirLeviathanModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.tail = this.body.getChild("tail");
        this.flipperRight = this.body.getChild("flipper_right");
        this.flipperLeft = this.body.getChild("flipper_left");

        this.breatheWith(this.body, 0.02F);
        this.sway(this.head, 0.4F, 0.03F, 0.05F, 0.02F);
        this.sway(this.tail, 1.3F, 0.20F, 0.10F, 0.05F);
        this.flap(this.flipperRight, 0.0F, 0.18F, 0.08F);
        this.flap(this.flipperLeft, (float) Math.PI, -0.18F, 0.08F);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition body = BossParts.blob(root, "body", 0.0F, -8.0F, 0.0F, 30.0F, 26.0F, 52.0F, 32, 32);
        BossParts.blob(body, "belly", 0.0F, 8.0F, 2.0F, 26.0F, 12.0F, 44.0F, 64, 32);
        PartDefinition head = BossParts.blob(body, "head", 0.0F, -2.0F, -33.0F, 26.0F, 22.0F, 22.0F, 96, 32);
        CrownOfEyes.build(head, "head_eye_", 6, 9.0F, -4.0F, -9.0F, 2.0F);

        for (int i = 0; i < 5; i++) {
            float z = -18.0F + i * 13.0F;
            body.addOrReplaceChild("mouth" + i,
                    CubeListBuilder.create().texOffs(BossParts.PUPIL_U, BossParts.PUPIL_V)
                            .addBox(-8.0F, 10.0F, -4.0F, 16.0F, 4.0F, 8.0F),
                    PartPose.offset(0.0F, 2.0F, z));
            BossParts.eye(body, "belly_eye_" + i, -11.0F + i % 2 * 22.0F, -2.0F, z, 2.0F);
        }

        BossParts.blob(body, "hump", 0.0F, -18.0F, 6.0F, 20.0F, 10.0F, 22.0F, 64, 64);
        BossParts.spikeRing(body, "barnacle_", 8, 13.0F, -16.0F, 5.0F, 1.3F, 96, 64);

        PartDefinition tail = BossParts.blob(body, "tail", 0.0F, -2.0F, 28.0F, 16.0F, 12.0F, 20.0F, 64, 96);
        BossParts.bar(tail, "fluke_left", -14.0F, -2.0F, 8.0F, 22.0F, 2.5F, 12.0F, 0.0F, 0.0F, 0.35F, 96, 96);
        BossParts.bar(tail, "fluke_right", 14.0F, -2.0F, 8.0F, 22.0F, 2.5F, 12.0F, 0.0F, 0.0F, -0.35F, 96, 96);

        PartDefinition flipperRight = BossParts.blob(body, "flipper_right", 16.0F, 2.0F, -4.0F, 16.0F, 4.0F, 12.0F, 32, 96);
        BossParts.bar(flipperRight, "flipper_right_tip", 10.0F, 0.0F, 4.0F, 16.0F, 2.5F, 8.0F, 0.0F, -0.4F, -0.2F, 96, 32);
        PartDefinition flipperLeft = BossParts.blob(body, "flipper_left", -16.0F, 2.0F, -4.0F, 16.0F, 4.0F, 12.0F, 32, 96);
        BossParts.bar(flipperLeft, "flipper_left_tip", -10.0F, 0.0F, 4.0F, 16.0F, 2.5F, 8.0F, 0.0F, 0.4F, 0.2F, 96, 32);
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    protected void applyIdle(BossRenderState state, float time, float breath) {
        this.body.y += Mth.sin(time * 0.02F) * 0.8F;
        this.body.xRot += Mth.sin(time * 0.015F) * 0.02F;
        this.tail.yRot = Mth.sin(time * 0.04F) * 0.22F;
        this.head.yRot += Mth.sin(time * 0.025F + 0.5F) * 0.08F;
    }

    @Override
    protected void applyLunge(BossRenderState state) {
        super.applyLunge(state);
        float lunge = state.lunge;
        this.body.z += -3.0F * lunge;
        this.head.xRot += -0.15F * lunge;
        this.tail.yRot += Mth.sin(state.ageInTicks * 0.3F) * 0.2F * lunge;
    }
}
