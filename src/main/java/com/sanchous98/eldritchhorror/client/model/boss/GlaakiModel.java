package com.sanchous98.eldritchhorror.client.model.boss;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.client.model.BossModelBase;
import com.sanchous98.eldritchhorror.client.model.BossRenderState;
import com.sanchous98.eldritchhorror.client.model.kit.BossParts;
import com.sanchous98.eldritchhorror.client.model.kit.CrownOfEyes;
import com.sanchous98.eldritchhorror.client.model.kit.SpinedShell;
import com.sanchous98.eldritchhorror.client.model.kit.Tentacles;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * Glaaki: a drowned, spined shell squatting in the marsh on a skirt of tendrils, ringed with eyes.
 * Green-black chitin dome above, pale spines, no slime cube anywhere.
 */
public final class GlaakiModel extends BossModelBase {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "glaaki"), "main");

    private static final int TENDRILS = 10;

    private final ModelPart shell;
    private final ModelPart base;

    public GlaakiModel(ModelPart root) {
        super(root);
        this.shell = root.getChild("shell");
        this.base = root.getChild("base");

        this.pulse(this.shell, 0.0F, 0.025F, 0.06F);
        this.sway(this.base, 0.5F, 0.02F, 0.04F, 0.02F);
        for (int i = 0; i < TENDRILS; i++) {
            this.sway(root.getChild("tendril" + i), i * 0.7F, 0.12F, 0.20F, 0.08F);
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        BossParts.blob(root, "base", 0.0F, 16.0F, 1.0F, 26.0F, 12.0F, 24.0F, 64, 32);
        SpinedShell.build(root, "shell", 4.0F, 24.0F, 20.0F, 22.0F, 3, 8, 32, 32);

        Tentacles.crown(root, "tendril", TENDRILS, 11.0F, 12.0F, 11.0F, 1.2F, 3, 32, 96);
        CrownOfEyes.build(root, "base_eye_", 4, 10.0F, 15.0F, -9.0F, 1.9F);
        BossParts.spike(root, "horn_left", -10.0F, -2.0F, -6.0F, 9.0F, 1.4F, -0.9F, 0.0F, -0.3F, 96, 96);
        BossParts.spike(root, "horn_right", 10.0F, -2.0F, -6.0F, 9.0F, 1.4F, -0.9F, 0.0F, 0.3F, 96, 96);
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    protected void applyIdle(BossRenderState state, float time, float breath) {
        this.shell.y += Mth.sin(time * 0.05F) * 0.7F;
        this.shell.yRot += Mth.sin(time * 0.02F) * 0.04F;
        this.base.yScale = 1.0F + Mth.sin(time * 0.06F) * 0.02F;
    }

    @Override
    protected void applyLunge(BossRenderState state) {
        super.applyLunge(state);
        float lunge = state.lunge;
        this.shell.z += -3.0F * lunge;
        this.shell.xRot += -0.18F * lunge;
        this.shell.yScale = 1.0F + 0.12F * lunge;
    }
}
