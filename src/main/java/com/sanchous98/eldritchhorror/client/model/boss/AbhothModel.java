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

/**
 * Abhoth: the source of all filth. A broad grey-pink pool of protoplasm, half-formed spawn budding
 * on its surface, mouths opening in the scum and a handful of thick arms reaching up. Sits low and
 * wide like something that should be scooped out and burned.
 */
public final class AbhothModel extends BossModelBase {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "abhoth"), "main");

    private static final int ARMS = 6;

    private final ModelPart pool;

    public AbhothModel(ModelPart root) {
        super(root);
        this.pool = root.getChild("pool");
        this.pulse(this.pool, 0.0F, 0.02F, 0.04F);
        for (int i = 0; i < ARMS; i++) {
            this.sway(root.getChild("arm" + i), i * 0.7F, 0.10F, 0.18F, 0.07F);
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition pool = BossParts.blob(root, "pool", 0.0F, 18.0F, 0.0F, 30.0F, 10.0F, 28.0F, 32, 32);
        BossParts.box(pool, "rim", 0.0F, 5.0F, 0.0F, 34.0F, 5.0F, 32.0F, 64, 32);
        BossParts.blob(pool, "spawn0", -8.0F, -6.0F, -6.0F, 8.0F, 7.0F, 8.0F, 96, 32);
        BossParts.blob(pool, "spawn1", 9.0F, -4.0F, 5.0F, 7.0F, 6.0F, 7.0F, 96, 64);
        BossParts.blob(pool, "spawn2", 0.0F, -7.0F, 9.0F, 6.0F, 5.0F, 6.0F, 96, 96);
        pool.addOrReplaceChild("mouth",
                CubeListBuilder.create().texOffs(BossParts.PUPIL_U, BossParts.PUPIL_V)
                        .addBox(-6.0F, -2.0F, -12.0F, 12.0F, 5.0F, 4.0F),
                PartPose.offset(0.0F, 2.0F, 0.0F));
        CrownOfEyes.build(pool, "pool_eye_", 6, 12.0F, -4.0F, -10.0F, 1.9F);

        for (int i = 0; i < ARMS; i++) {
            float angle = (float) (Math.PI * 2.0 * i / ARMS);
            float x = (float) Math.cos(angle) * 10.0F;
            float z = (float) Math.sin(angle) * 9.0F;
            float length = 20.0F + i % 3 * 4.0F;
            Tentacles.tentacle(root, "arm" + i, x, 17.0F, z,
                    (float) Math.PI - 0.35F, angle, length, 2.2F, 3, 64, 64);
        }
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    protected void applyIdle(BossRenderState state, float time, float breath) {
        this.pool.yScale = 1.0F + Mth.sin(time * 0.05F) * 0.025F;
        this.pool.yRot += Mth.sin(time * 0.018F) * 0.02F;
        this.pool.getChild("spawn0").y += Mth.sin(time * 0.08F) * 0.7F;
    }

    @Override
    protected void applyLunge(BossRenderState state) {
        super.applyLunge(state);
        float lunge = state.lunge;
        for (int i = 0; i < ARMS; i++) {
            this.root.getChild("arm" + i).xRot += 0.20F * lunge;
        }
        this.pool.z += -1.5F * lunge;
    }
}
