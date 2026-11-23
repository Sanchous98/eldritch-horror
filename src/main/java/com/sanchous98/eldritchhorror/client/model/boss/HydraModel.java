package com.sanchous98.eldritchhorror.client.model.boss;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.client.model.BossModelBase;
import com.sanchous98.eldritchhorror.client.model.BossRenderState;
import com.sanchous98.eldritchhorror.client.model.kit.BossParts;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * The Hydra of the drowned marsh: a low, bloated body with four stumpy legs and six long necks
 * fanning out, each ending in a horned head. The necks move out of phase so it always looks like
 * more than one creature is deciding what to do.
 */
public final class HydraModel extends BossModelBase {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "hydra"), "main");

    private static final int NECKS = 6;
    private static final int LEGS = 4;

    private final ModelPart body;
    private final ModelPart[] necks = new ModelPart[NECKS];

    public HydraModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        for (int i = 0; i < NECKS; i++) {
            this.necks[i] = root.getChild("neck" + i);
            this.sway(this.necks[i], i * 0.9F, 0.09F, 0.14F, 0.05F);
        }
        for (int i = 0; i < LEGS; i++) {
            this.leg(root.getChild("leg" + i), i * 1.6F, 0.8F);
        }
        this.breatheWith(this.body, 0.03F);
        this.sway(this.body, 0.4F, 0.02F, 0.03F, 0.02F);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        BossParts.blob(root, "body", 0.0F, 8.0F, 2.0F, 24.0F, 16.0F, 22.0F, 32, 32);
        BossParts.blob(root, "haunch", 0.0F, 15.0F, 6.0F, 20.0F, 10.0F, 16.0F, 64, 32);

        for (int i = 0; i < LEGS; i++) {
            float x = (i % 2 == 0) ? 8.0F : -8.0F;
            float z = (i < 2) ? -4.0F : 8.0F;
            PartDefinition leg = BossParts.limb(root, "leg" + i, x, 14.0F, z, 5.0F, 10.0F, 5.0F, 96, 32);
            BossParts.box(leg, "foot" + i, 0.0F, 10.0F, 0.0F, 6.0F, 3.0F, 8.0F, 64, 96);
        }

        for (int i = 0; i < NECKS; i++) {
            float angle = (float) (Math.PI * 2.0 * i / NECKS);
            float x = (float) Math.cos(angle) * 6.0F;
            float z = (float) Math.sin(angle) * 5.0F - 1.0F;
            neck(root, i, x, z, 0.20F + i % 2 * 0.10F, angle * 0.35F, 32, 64);
        }
        return LayerDefinition.create(mesh, 128, 128);
    }

    private static void neck(PartDefinition root, int index, float x, float z,
                             float tilt, float yRot, int u, int v) {
        float segLength = 5.0F;
        float thickness = 1.9F;
        PartDefinition neck = root.addOrReplaceChild("neck" + index,
                CubeListBuilder.create().texOffs(u, v).addBox(-thickness, 0.0F, -thickness,
                        thickness * 2.0F, segLength, thickness * 2.0F),
                PartPose.offsetAndRotation(x, 2.0F, z, (float) Math.PI - tilt, yRot, 0.0F));
        PartDefinition current = neck;
        for (int s = 1; s < 3; s++) {
            float taper = thickness * (1.0F - s * 0.14F);
            current = current.addOrReplaceChild("s" + s,
                    CubeListBuilder.create().texOffs(u, v).addBox(-taper, 0.0F, -taper,
                            taper * 2.0F, segLength, taper * 2.0F),
                    PartPose.offsetAndRotation(0.0F, segLength, 0.0F, 0.14F, 0.0F, 0.0F));
        }
        PartDefinition head = current.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(u, v).addBox(-3.5F, -4.0F, -7.0F, 7.0F, 7.0F, 10.0F)
                        .texOffs(u, v).addBox(-2.0F, -1.0F, -10.0F, 4.0F, 3.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, segLength, 0.0F, -0.16F, 0.0F, 0.0F));
        BossParts.eye(head, "eye_left", -2.2F, -1.0F, -7.3F, 1.5F);
        BossParts.eye(head, "eye_right", 2.2F, -1.0F, -7.3F, 1.5F);
        BossParts.spike(head, "horn_left", -2.5F, -4.0F, -1.0F, 7.0F, 1.1F, -2.5F, 0.0F, -0.4F, u, v);
        BossParts.spike(head, "horn_right", 2.5F, -4.0F, -1.0F, 7.0F, 1.1F, -2.5F, 0.0F, 0.4F, u, v);
    }

    @Override
    protected void applyIdle(BossRenderState state, float time, float breath) {
        for (int i = 0; i < NECKS; i++) {
            ModelPart head = this.necks[i].getChild("s1").getChild("s2").getChild("head");
            head.yRot = Mth.sin(time * 0.05F + i * 1.3F) * 0.25F;
            head.xRot += Mth.sin(time * 0.07F + i * 0.9F) * 0.05F;
        }
        this.body.yRot += Mth.sin(time * 0.025F) * 0.03F;
    }

    @Override
    protected void applyLunge(BossRenderState state) {
        super.applyLunge(state);
        float lunge = state.lunge;
        this.body.z += -2.0F * lunge;
        for (ModelPart neck : this.necks) {
            neck.xRot += -0.30F * lunge;
        }
    }
}
