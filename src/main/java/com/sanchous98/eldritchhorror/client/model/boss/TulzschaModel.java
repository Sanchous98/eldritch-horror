package com.sanchous98.eldritchhorror.client.model.boss;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.client.model.BossModelBase;
import com.sanchous98.eldritchhorror.client.model.BossRenderState;
import com.sanchous98.eldritchhorror.client.model.kit.BossParts;
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
 * Tulzscha, the Green Flame: a narrow spiralling column of green fire inside a slowly turning ring
 * of embers, with flame tendrils licking down to the ground. Tall and thin, unlike Cthugha's
 * spreading fireball.
 */
@OnlyIn(Dist.CLIENT)
public final class TulzschaModel extends BossModelBase {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "tulzscha"), "main");

    private final ModelPart flame;
    private final ModelPart halo;

    public TulzschaModel(ModelPart root) {
        super(root);
        this.flame = root.getChild("flame");
        this.halo = root.getChild("halo");

        this.pulse(this.flame, 0.0F, 0.045F, 0.14F);
        this.pulse(this.halo, 1.1F, 0.02F, 0.07F);
        for (int i = 0; i < 5; i++) {
            this.sway(root.getChild("root" + i), i * 0.7F, 0.10F, 0.15F, 0.06F);
        }
        for (int i = 0; i < 8; i++) {
            this.sway(this.halo.getChild("ember" + i), i * 0.5F, 0.0F, 0.08F, 0.0F);
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        FlameBody.build(root, 20.0F, 16.0F, 14.0F, 34.0F, 5, 6, 32, 32);

        PartDefinition halo = root.addOrReplaceChild("halo", CubeListBuilder.create(), PartPose.offset(0.0F, 4.0F, 0.0F));
        for (int i = 0; i < 8; i++) {
            float angle = (float) (Math.PI * 2.0 * i / 8.0);
            BossParts.blob(halo, "ember" + i,
                    (float) Math.cos(angle) * 11.0F, Mth.sin(i * 1.9F) * 8.0F,
                    (float) Math.sin(angle) * 10.0F, 3.0F, 3.0F, 3.0F, 96, 64);
        }
        BossParts.bar(halo, "halo_ring", 0.0F, -4.0F, 0.0F, 22.0F, 0.8F, 20.0F, 0.0F, 0.0F, 0.0F, 96, 96);

        Tentacles.cluster(root, "root", 5, 7.0F, 2.0F, 6.0F, 16.0F, 10.0F, 1.0F, 3, 64, 96);
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    protected void applyIdle(BossRenderState state, float time, float breath) {
        this.halo.yRot = time * 0.04F;
        this.halo.xRot = Mth.sin(time * 0.03F) * 0.12F;
        this.flame.yRot += Mth.sin(time * 0.07F) * 0.10F;
        this.flame.zRot += Mth.cos(time * 0.06F) * 0.03F;
    }

    @Override
    protected void applyLunge(BossRenderState state) {
        super.applyLunge(state);
        float lunge = state.lunge;
        this.flame.yScale = 1.0F + 0.22F * lunge;
        this.flame.z += -1.5F * lunge;
        this.halo.yScale = 1.0F + 0.15F * lunge;
    }
}
