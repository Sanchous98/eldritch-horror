package com.sanchous98.eldritchhorror.client.model.boss;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.client.model.BossModelBase;
import com.sanchous98.eldritchhorror.client.model.BossRenderState;
import com.sanchous98.eldritchhorror.client.model.kit.BossParts;
import com.sanchous98.eldritchhorror.client.model.kit.CrownOfEyes;
import com.sanchous98.eldritchhorror.client.model.kit.FlameBody;
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
 * Cthugha: the living flame that watches. A tall licking column of fire with a ring of embers
 * orbiting it, three eyes floating inside the flame and burning tendrils rooted to the ground.
 * No legs and no humanoid torso.
 */
@OnlyIn(Dist.CLIENT)
public final class CthughaModel extends BossModelBase {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "cthugha"), "main");

    private final ModelPart flame;
    private final ModelPart orbit;

    public CthughaModel(ModelPart root) {
        super(root);
        this.flame = root.getChild("flame");
        this.orbit = root.getChild("orbit");

        this.pulse(this.flame, 0.0F, 0.04F, 0.12F);
        this.pulse(this.orbit, 0.7F, 0.02F, 0.08F);
        for (int i = 0; i < 6; i++) {
            this.sway(this.orbit.getChild("ember" + i), i * 0.8F, 0.05F, 0.10F, 0.04F);
        }
        for (int i = 0; i < 9; i++) {
            this.sway(root.getChild("root" + i), i * 0.6F, 0.12F, 0.18F, 0.07F);
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        FlameBody.build(root, 18.0F, 22.0F, 20.0F, 30.0F, 5, 8, 32, 32);
        CrownOfEyes.build(root, "flame_eye_", 3, 7.0F, -6.0F, -8.0F, 2.2F);

        PartDefinition orbit = root.addOrReplaceChild("orbit", CubeListBuilder.create(), PartPose.offset(0.0F, -2.0F, 0.0F));
        for (int i = 0; i < 6; i++) {
            float angle = (float) (Math.PI * 2.0 * i / 6.0);
            BossParts.blob(orbit, "ember" + i,
                    (float) Math.cos(angle) * 15.0F, Mth.sin(i * 1.7F) * 9.0F,
                    (float) Math.sin(angle) * 13.0F, 4.0F, 4.0F, 4.0F, 96, 64);
        }

        Tentacles.cluster(root, "root", 9, 10.0F, 2.0F, 8.0F, 16.0F, 12.0F, 1.0F, 3, 64, 96);
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    protected void applyIdle(BossRenderState state, float time, float breath) {
        this.orbit.yRot = time * 0.06F;
        this.orbit.xRot = Mth.sin(time * 0.09F) * 0.10F;
        this.flame.yRot += Mth.sin(time * 0.08F) * 0.06F;
    }

    @Override
    protected void applyLunge(BossRenderState state) {
        super.applyLunge(state);
        float lunge = state.lunge;
        this.flame.z += -2.0F * lunge;
        this.flame.yScale = 1.0F + 0.18F * lunge;
        this.orbit.xScale = 1.0F + 0.12F * lunge;
        this.orbit.zScale = 1.0F + 0.12F * lunge;
    }
}
