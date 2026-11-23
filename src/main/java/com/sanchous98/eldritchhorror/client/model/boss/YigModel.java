package com.sanchous98.eldritchhorror.client.model.boss;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.client.model.BossModelBase;
import com.sanchous98.eldritchhorror.client.model.BossRenderState;
import com.sanchous98.eldritchhorror.client.model.kit.BossParts;
import com.sanchous98.eldritchhorror.client.model.kit.SerpentCoils;
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
 * Yig, Father of Serpents: a great coiled body rising from the ground into an upright S-neck, a
 * wedge head with a flared hood, fangs and scaled ridges. Nothing bipedal anywhere.
 */
@OnlyIn(Dist.CLIENT)
public final class YigModel extends BossModelBase {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "yig"), "main");

    private final ModelPart coils;
    private final ModelPart neck;

    public YigModel(ModelPart root) {
        super(root);
        this.coils = root.getChild("coils");
        this.neck = root.getChild("neck");

        this.sway(this.neck, 0.4F, 0.06F, 0.15F, 0.05F);
        this.sway(this.coils, 1.3F, 0.0F, 0.02F, 0.0F);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        SerpentCoils.build(root, 20.0F, 8.0F, 14.0F, 8, 22.0F, 2.8F, 2.75F, 32, 32);

        PartDefinition head = root.getChild("neck").getChild("s1").getChild("s2").getChild("head");
        BossParts.bar(head, "hood_left", -5.5F, 2.0F, 0.0F, 6.0F, 9.0F, 1.0F, 0.0F, 0.0F, 0.35F, 64, 64);
        BossParts.bar(head, "hood_right", 5.5F, 2.0F, 0.0F, 6.0F, 9.0F, 1.0F, 0.0F, 0.0F, -0.35F, 64, 64);
        BossParts.spike(head, "fang_left", -2.2F, 2.0F, -5.0F, 6.0F, 0.8F, 0.55F, 0.0F, -0.15F, 32, 96);
        BossParts.spike(head, "fang_right", 2.2F, 2.0F, -5.0F, 6.0F, 0.8F, 0.55F, 0.0F, 0.15F, 32, 96);
        BossParts.spike(head, "ridge1", 0.0F, -1.0F, 2.0F, 5.0F, 1.0F, (float) Math.PI, 0.0F, 0.0F, 96, 96);
        BossParts.spike(head, "ridge2", 0.0F, -1.0F, 5.0F, 4.0F, 0.9F, (float) Math.PI, 0.0F, 0.0F, 96, 96);
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    protected void applyIdle(BossRenderState state, float time, float breath) {
        this.neck.xRot += Mth.sin(time * 0.045F) * 0.05F;
        this.neck.yRot += Mth.sin(time * 0.03F + 0.5F) * 0.20F;
        ModelPart head = this.neck.getChild("s1").getChild("s2").getChild("head");
        head.yRot = Mth.sin(time * 0.05F + 1.4F) * 0.18F;
    }

    @Override
    protected void applyLunge(BossRenderState state) {
        super.applyLunge(state);
        float lunge = state.lunge;
        this.neck.xRot += -0.45F * lunge;
        this.neck.yRot += Mth.sin(state.ageInTicks * 0.2F) * 0.1F * lunge;
    }
}
