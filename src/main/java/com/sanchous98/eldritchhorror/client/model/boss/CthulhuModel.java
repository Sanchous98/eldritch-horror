package com.sanchous98.eldritchhorror.client.model.boss;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.client.model.BossModelBase;
import com.sanchous98.eldritchhorror.client.model.BossRenderState;
import com.sanchous98.eldritchhorror.client.model.kit.BossParts;
import com.sanchous98.eldritchhorror.client.model.kit.CrownOfEyes;
import com.sanchous98.eldritchhorror.client.model.kit.HumanoidRig;
import com.sanchous98.eldritchhorror.client.model.kit.Tentacles;
import com.sanchous98.eldritchhorror.client.model.kit.Wings;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * Cthulhu: vast winged humanoid, tentacled face, clawed hands and a crown of horns. The silhouette
 * is a hunched giant with wings folded behind the shoulders; it wakes with a lunge that spreads
 * them.
 */
public final class CthulhuModel extends BossModelBase {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "cthulhu"), "main");

    private static final float HIP_Y = 8.0F;
    private static final float TORSO_HEIGHT = 26.0F;

    private final ModelPart torso;
    private final ModelPart head;
    private final ModelPart wingRight;
    private final ModelPart wingLeft;

    public CthulhuModel(ModelPart root) {
        super(root);
        this.torso = root.getChild("torso");
        this.head = this.torso.getChild("head");
        this.wingRight = this.torso.getChild("wing_right");
        this.wingLeft = this.torso.getChild("wing_left");

        this.breatheWith(this.torso, 0.025F);
        this.sway(this.head, 0.6F, 0.04F, 0.07F, 0.02F);
        for (int i = 0; i < 7; i++) {
            this.sway(this.head.getChild("beard" + i), i * 0.55F, 0.12F, 0.16F, 0.06F);
        }
        this.flap(this.wingRight, 0.0F, 0.22F, 0.10F);
        this.flap(this.wingLeft, (float) Math.PI, -0.22F, 0.10F);
        this.arm(this.torso.getChild("right_arm"), (float) Math.PI, 0.55F);
        this.arm(this.torso.getChild("left_arm"), 0.0F, 0.55F);
        this.leg(root.getChild("right_leg"), 0.0F, 0.85F);
        this.leg(root.getChild("left_leg"), (float) Math.PI, 0.85F);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        HumanoidRig.build(root, HIP_Y, 26.0F, TORSO_HEIGHT, 16.0F,
                20.0F, 16.0F, 20.0F,
                7.0F, 30.0F, 7.0F,
                8.0F, 16.0F, 9.0F, 32, 32);

        PartDefinition torso = root.getChild("torso");
        BossParts.blob(torso, "chest", 0.0F, -TORSO_HEIGHT * 0.55F, 0.0F, 30.0F, 22.0F, 20.0F, 64, 32);
        BossParts.blob(torso, "abdomen", 0.0F, -TORSO_HEIGHT * 0.18F, 0.0F, 24.0F, 16.0F, 16.0F, 96, 32);

        PartDefinition head = torso.getChild("head");
        BossParts.blob(head, "skull", 0.0F, -9.0F, 1.0F, 20.0F, 12.0F, 20.0F, 32, 64);
        Tentacles.beard(head, "beard", 7, 7.5F, -1.0F, -7.0F, 17.0F, 1.5F, 4, 32, 96);
        CrownOfEyes.build(head, "eye_", 3, 6.5F, -7.0F, -7.5F, 2.6F);
        BossParts.spike(head, "horn_right", 8.5F, -12.0F, 0.0F, 15.0F, 2.2F, 2.30F, 0.0F, 0.55F, 96, 96);
        BossParts.spike(head, "horn_left", -8.5F, -12.0F, 0.0F, 15.0F, 2.2F, 2.30F, 0.0F, -0.55F, 96, 96);

        Wings.build(torso, -TORSO_HEIGHT + 5.0F, 8.0F, 34.0F, 44.0F, 64, 64);

        for (PartDefinition arm : new PartDefinition[]{
                torso.getChild("right_arm"), torso.getChild("left_arm")}) {
            BossParts.blob(arm, "hand", 0.0F, 30.0F, 0.0F, 8.0F, 8.0F, 8.0F, 32, 112);
        }
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    protected void applyIdle(BossRenderState state, float time, float breath) {
        this.torso.yRot += Mth.sin(time * 0.02F) * 0.03F;
        this.head.xRot += Mth.sin(time * 0.05F + 0.6F) * 0.04F;
        this.wingRight.zRot += Mth.sin(time * 0.025F) * 0.06F;
        this.wingLeft.zRot -= Mth.sin(time * 0.025F) * 0.06F;
    }

    @Override
    protected void applyLunge(BossRenderState state) {
        super.applyLunge(state);
        float lunge = state.lunge;
        this.torso.xRot += -0.18F * lunge;
        this.head.xRot += -0.30F * lunge;
        this.wingRight.zRot += -0.55F * lunge;
        this.wingLeft.zRot += 0.55F * lunge;
        this.torso.getChild("right_arm").xRot += -0.9F * lunge;
        this.torso.getChild("left_arm").xRot += -0.9F * lunge;
    }
}
