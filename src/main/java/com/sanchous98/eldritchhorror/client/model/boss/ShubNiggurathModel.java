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
 * Shub-Niggurath, the Black Goat of the Woods with a Thousand Young: a tree-thick torso of matted
 * black mass, four stunted goat legs, three long-necked goat heads, a crown of horns and a curtain
 * of tentacles. The heads browse independently, which is what makes the silhouette read as a
 * many-headed thing rather than a bull.
 */
@OnlyIn(Dist.CLIENT)
public final class ShubNiggurathModel extends BossModelBase {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "shub_niggurath"), "main");

    private static final int LEGS = 4;

    private final ModelPart mass;
    private final ModelPart neck0;
    private final ModelPart neck1;
    private final ModelPart neck2;

    public ShubNiggurathModel(ModelPart root) {
        super(root);
        this.mass = root.getChild("mass");
        this.neck0 = root.getChild("neck0");
        this.neck1 = root.getChild("neck1");
        this.neck2 = root.getChild("neck2");

        this.breatheWith(this.mass, 0.03F);
        this.sway(this.neck0, 0.0F, 0.07F, 0.10F, 0.04F);
        this.sway(this.neck1, 1.1F, 0.07F, 0.10F, 0.04F);
        this.sway(this.neck2, 2.2F, 0.07F, 0.10F, 0.04F);
        for (int i = 0; i < 12; i++) {
            this.sway(root.getChild("tuft" + i), i * 0.65F, 0.11F, 0.16F, 0.06F);
        }
        for (int i = 0; i < LEGS; i++) {
            this.leg(root.getChild("leg" + i), i * 1.6F, 0.8F);
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition mass = BossParts.blob(root, "mass", 0.0F, -4.0F, 0.0F, 32.0F, 36.0F, 28.0F, 32, 32);
        BossParts.blob(mass, "shoulders", 0.0F, -20.0F, 0.0F, 26.0F, 18.0F, 22.0F, 64, 32);
        BossParts.blob(mass, "haunch", 0.0F, 15.0F, 4.0F, 28.0F, 14.0F, 22.0F, 96, 32);
        CrownOfEyes.build(mass, "mass_eye_", 4, 11.0F, -10.0F, -11.0F, 2.0F);

        for (int i = 0; i < LEGS; i++) {
            float x = (i % 2 == 0) ? 9.0F : -9.0F;
            float z = (i < 2) ? -7.0F : 8.0F;
            PartDefinition leg = BossParts.limb(root, "leg" + i, x, 16.0F, z, 6.0F, 8.0F, 8.0F, 64, 96);
            BossParts.box(leg, "hoof" + i, 0.0F, 8.0F, -1.0F, 7.0F, 3.0F, 10.0F, 96, 96);
        }

        goatNeck(root, 0, -8.0F, -6.0F, 0.18F, -0.32F, 32, 64);
        goatNeck(root, 1, 9.0F, -3.0F, 0.25F, 0.38F, 64, 64);
        goatNeck(root, 2, 0.0F, 7.0F, 0.05F, 0.0F, 96, 64);

        Tentacles.cluster(root, "tuft", 12, 15.0F, 6.0F, 12.0F, 8.0F, 18.0F, 1.2F, 3, 32, 96);
        return LayerDefinition.create(mesh, 128, 128);
    }

    private static void goatNeck(PartDefinition root, int index, float x, float z,
                                 float tilt, float yRot, int u, int v) {
        float segLength = 5.0F;
        float thickness = 2.4F;
        PartDefinition neck = root.addOrReplaceChild("neck" + index,
                CubeListBuilder.create().texOffs(u, v).addBox(-thickness, 0.0F, -thickness,
                        thickness * 2.0F, segLength, thickness * 2.0F),
                PartPose.offsetAndRotation(x, -26.0F, z,
                        (float) Math.PI - tilt, yRot, 0.0F));
        PartDefinition current = neck;
        for (int s = 1; s < 3; s++) {
            float taper = thickness * (1.0F - s * 0.15F);
            current = current.addOrReplaceChild("s" + s,
                    CubeListBuilder.create().texOffs(u, v).addBox(-taper, 0.0F, -taper,
                            taper * 2.0F, segLength, taper * 2.0F),
                    PartPose.offsetAndRotation(0.0F, segLength, 0.0F, 0.16F, 0.0F, 0.0F));
        }

        PartDefinition head = current.addOrReplaceChild("goat_head",
                CubeListBuilder.create()
                        .texOffs(u, v).addBox(-4.0F, -5.0F, -9.0F, 8.0F, 8.0F, 12.0F)
                        .texOffs(u, v).addBox(-2.5F, -1.0F, -13.0F, 5.0F, 4.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, segLength, 0.0F, -0.18F, 0.0F, 0.0F));
        BossParts.eye(head, "eye_left", -2.6F, -1.0F, -9.3F, 1.7F);
        BossParts.eye(head, "eye_right", 2.6F, -1.0F, -9.3F, 1.7F);
        BossParts.spike(head, "horn_left", -3.0F, -5.0F, -2.0F, 8.0F, 1.4F, -2.55F, 0.0F, -0.45F, u, v);
        BossParts.spike(head, "horn_right", 3.0F, -5.0F, -2.0F, 8.0F, 1.4F, -2.55F, 0.0F, 0.45F, u, v);
    }

    @Override
    protected void applyIdle(BossRenderState state, float time, float breath) {
        this.mass.yRot += Mth.sin(time * 0.02F) * 0.04F;
        float[] phases = {0.0F, 1.7F, 3.1F};
        ModelPart[] necks = {this.neck0, this.neck1, this.neck2};
        for (int i = 0; i < necks.length; i++) {
            ModelPart head = necks[i].getChild("s1").getChild("s2").getChild("goat_head");
            head.yRot = Mth.sin(time * 0.045F + phases[i]) * 0.30F;
            head.xRot += Mth.sin(time * 0.06F + phases[i]) * 0.06F;
        }
    }

    @Override
    protected void applyLunge(BossRenderState state) {
        super.applyLunge(state);
        float lunge = state.lunge;
        this.mass.xRot += -0.12F * lunge;
        this.neck0.xRot += -0.25F * lunge;
        this.neck1.xRot += -0.25F * lunge;
        this.neck2.xRot += -0.25F * lunge;
    }
}
