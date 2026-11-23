package com.sanchous98.eldritchhorror.client.model.boss;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.client.model.BossModelBase;
import com.sanchous98.eldritchhorror.client.model.BossRenderState;
import com.sanchous98.eldritchhorror.client.model.kit.BossParts;
import com.sanchous98.eldritchhorror.client.model.kit.HumanoidRig;
import com.sanchous98.eldritchhorror.client.model.kit.TrunkedIdol;
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
 * Chaugnar Faugn: an elephantine idol that walks. Broad trunked head, curved tusks, heavy arms and
 * stunted legs under a bloated gut: a temple statue that decided to feed.
 */
@OnlyIn(Dist.CLIENT)
public final class ChaugnarFaugnModel extends BossModelBase {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "chaugnar_faugn"), "main");

    private static final float HIP_Y = 16.0F;
    private static final float TORSO_HEIGHT = 22.0F;

    private final ModelPart torso;
    private final ModelPart head;
    private final ModelPart trunk;

    public ChaugnarFaugnModel(ModelPart root) {
        super(root);
        this.torso = root.getChild("torso");
        this.head = this.torso.getChild("head");
        this.trunk = this.head.getChild("trunk");
        this.breatheWith(this.torso, 0.02F);
        this.sway(this.head, 0.9F, 0.03F, 0.05F, 0.02F);
        this.sway(this.trunk, 0.4F, 0.10F, 0.12F, 0.06F);
        this.arm(this.torso.getChild("right_arm"), (float) Math.PI, 0.45F);
        this.arm(this.torso.getChild("left_arm"), 0.0F, 0.45F);
        this.leg(root.getChild("right_leg"), 0.0F, 0.8F);
        this.leg(root.getChild("left_leg"), (float) Math.PI, 0.8F);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        HumanoidRig.build(root, HIP_Y, 26.0F, TORSO_HEIGHT, 16.0F,
                24.0F, 15.0F, 18.0F,
                9.0F, 24.0F, 9.0F,
                7.0F, 8.0F, 8.0F, 64, 32);

        PartDefinition torso = root.getChild("torso");
        BossParts.blob(torso, "gut", 0.0F, -7.0F, 2.0F, 28.0F, 18.0F, 20.0F, 32, 32);
        BossParts.blob(torso, "shoulder_right", 13.0F, -TORSO_HEIGHT + 6.0F, 0.0F, 10.0F, 10.0F, 10.0F, 96, 64);
        BossParts.blob(torso, "shoulder_left", -13.0F, -TORSO_HEIGHT + 6.0F, 0.0F, 10.0F, 10.0F, 10.0F, 96, 64);

        PartDefinition head = TrunkedIdol.build(torso, -TORSO_HEIGHT, 24.0F, 15.0F, 18.0F,
                13.0F, 2.6F, 32, 64);
        BossParts.spikeRing(head, "crown_", 7, 8.0F, -14.0F, 5.0F, 1.2F, 96, 96);

        BossParts.blob(torso.getChild("right_arm"), "fist_right", 0.0F, 24.0F, 0.0F, 9.0F, 8.0F, 9.0F, 32, 112);
        BossParts.blob(torso.getChild("left_arm"), "fist_left", 0.0F, 24.0F, 0.0F, 9.0F, 8.0F, 9.0F, 32, 112);

        BossParts.box(root.getChild("right_leg"), "foot_right", 0.0F, 8.0F, -1.0F, 10.0F, 4.0F, 13.0F, 64, 112);
        BossParts.box(root.getChild("left_leg"), "foot_left", 0.0F, 8.0F, -1.0F, 10.0F, 4.0F, 13.0F, 64, 112);
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    protected void applyIdle(BossRenderState state, float time, float breath) {
        this.head.xRot += Mth.sin(time * 0.04F) * 0.035F;
        this.trunk.xRot += Mth.sin(time * 0.06F + 1.3F) * 0.09F;
        this.trunk.yRot += Mth.sin(time * 0.045F) * 0.08F;
        this.torso.yRot += Mth.sin(time * 0.02F) * 0.025F;
    }

    @Override
    protected void applyLunge(BossRenderState state) {
        super.applyLunge(state);
        float lunge = state.lunge;
        this.torso.xRot += -0.22F * lunge;
        this.head.xRot += -0.18F * lunge;
        this.trunk.xRot += -0.55F * lunge;
        this.torso.getChild("right_arm").xRot += -0.8F * lunge;
        this.torso.getChild("left_arm").xRot += -0.8F * lunge;
    }
}
