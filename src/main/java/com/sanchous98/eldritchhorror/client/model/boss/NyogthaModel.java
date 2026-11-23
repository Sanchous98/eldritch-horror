package com.sanchous98.eldritchhorror.client.model.boss;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.client.model.BossModelBase;
import com.sanchous98.eldritchhorror.client.model.BossRenderState;
import com.sanchous98.eldritchhorror.client.model.kit.BossParts;
import com.sanchous98.eldritchhorror.client.model.kit.CrownOfEyes;
import com.sanchous98.eldritchhorror.client.model.kit.Tentacles;
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
 * Nyogtha: the thing under the floor. A wide tar-black pool of protoplasm with bubbles and eyes,
 * out of which a dozen long tendrils rear up like kelp. It has no body above the pool.
 */
@OnlyIn(Dist.CLIENT)
public final class NyogthaModel extends BossModelBase {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "nyogtha"), "main");

    private static final int ARMS = 12;

    private final ModelPart pool;

    public NyogthaModel(ModelPart root) {
        super(root);
        this.pool = root.getChild("pool");
        this.pulse(this.pool, 0.0F, 0.02F, 0.05F);
        for (int i = 0; i < ARMS; i++) {
            this.sway(root.getChild("arm" + i), i * 0.55F, 0.10F, 0.20F, 0.08F);
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition pool = BossParts.blob(root, "pool", 0.0F, 18.0F, 0.0F, 26.0F, 10.0F, 24.0F, 32, 32);
        BossParts.blob(pool, "rim", 0.0F, 4.0F, 0.0F, 30.0F, 5.0F, 28.0F, 64, 32);
        BossParts.blob(pool, "bubble0", -7.0F, -4.0F, -5.0F, 7.0F, 5.0F, 7.0F, 96, 32);
        BossParts.blob(pool, "bubble1", 8.0F, -2.0F, 4.0F, 6.0F, 4.0F, 6.0F, 96, 64);
        CrownOfEyes.build(pool, "pool_eye_", 5, 10.0F, -3.0F, -9.0F, 1.8F);

        for (int i = 0; i < ARMS; i++) {
            float angle = (float) (Math.PI * 2.0 * i / ARMS);
            float x = (float) Math.cos(angle) * 9.0F;
            float z = (float) Math.sin(angle) * 8.0F;
            float tilt = (float) Math.PI - 0.25F - i % 3 * 0.12F;
            float length = 30.0F + i % 4 * 4.0F;
            Tentacles.tentacle(root, "arm" + i, x, 17.0F, z, tilt, angle, length, 1.3F, 3, 32, 96);
        }
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    protected void applyIdle(BossRenderState state, float time, float breath) {
        this.pool.yScale = 1.0F + Mth.sin(time * 0.04F) * 0.03F;
        this.pool.yRot += Mth.sin(time * 0.02F) * 0.03F;
    }

    @Override
    protected void applyLunge(BossRenderState state) {
        super.applyLunge(state);
        float lunge = state.lunge;
        for (int i = 0; i < ARMS; i++) {
            this.root.getChild("arm" + i).xRot += 0.15F * lunge;
        }
    }
}
