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
 * Zstylzhemghi: a name being erased, worn as a swarm. A dark chitinous core disappears inside a
 * cloud of beetle-like motes that orbits it on two axes; a few eyes and a fringe of legs are the
 * only fixed parts.
 */
public final class ZstylzhemghiModel extends BossModelBase {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "zstylzhemghi"), "main");

    private static final int MOTES = 18;

    private final ModelPart core;
    private final ModelPart swarm;

    public ZstylzhemghiModel(ModelPart root) {
        super(root);
        this.core = root.getChild("core");
        this.swarm = root.getChild("swarm");

        this.breatheWith(this.core, 0.03F);
        this.pulse(this.swarm, 0.5F, 0.025F, 0.08F);
        for (int i = 0; i < MOTES; i++) {
            this.sway(this.swarm.getChild("mote" + i), i * 0.7F, 0.08F, 0.12F, 0.06F);
        }
        for (int i = 0; i < 8; i++) {
            this.sway(root.getChild("tendril" + i), i * 0.6F, 0.10F, 0.15F, 0.05F);
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition core = BossParts.blob(root, "core", 0.0F, 0.0F, 0.0F, 18.0F, 24.0F, 18.0F, 32, 32);
        CrownOfEyes.build(core, "core_eye_", 4, 6.0F, -6.0F, -8.0F, 1.8F);
        BossParts.spike(core, "leg_left", -8.0F, 8.0F, 0.0F, 9.0F, 1.2F, 0.6F, 0.0F, -0.8F, 96, 96);
        BossParts.spike(core, "leg_right", 8.0F, 8.0F, 0.0F, 9.0F, 1.2F, 0.6F, 0.0F, 0.8F, 96, 96);

        PartDefinition swarm = root.addOrReplaceChild("swarm", CubeListBuilder.create(), PartPose.offset(0.0F, -4.0F, 0.0F));
        for (int i = 0; i < MOTES; i++) {
            float angle = (float) (Math.PI * 2.0 * i / MOTES);
            float radius = 9.0F + i % 3 * 3.0F;
            float x = (float) Math.cos(angle) * radius;
            float z = (float) Math.sin(angle) * radius * 0.8F;
            float y = Mth.sin(i * 2.1F) * 11.0F;
            BossParts.bar(swarm, "mote" + i, x, y, z, 2.0F, 5.0F, 2.0F,
                    i * 0.37F, angle, i * 0.21F, 64, 64);
        }

        Tentacles.cluster(root, "tendril", 8, 9.0F, 3.0F, 8.0F, 14.0F, 10.0F, 0.9F, 3, 64, 96);
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    protected void applyIdle(BossRenderState state, float time, float breath) {
        this.swarm.yRot = time * 0.045F;
        this.swarm.xRot = Mth.sin(time * 0.031F) * 0.35F;
        this.swarm.zRot = Mth.cos(time * 0.024F) * 0.20F;
        this.core.yRot += Mth.sin(time * 0.05F) * 0.08F;
    }

    @Override
    protected void applyLunge(BossRenderState state) {
        super.applyLunge(state);
        float lunge = state.lunge;
        this.core.z += -2.0F * lunge;
        this.swarm.z += -3.0F * lunge;
        this.swarm.xScale = 1.0F + 0.15F * lunge;
        this.swarm.zScale = 1.0F + 0.15F * lunge;
    }
}
