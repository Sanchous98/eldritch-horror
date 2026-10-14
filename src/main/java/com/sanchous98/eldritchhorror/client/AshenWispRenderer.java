package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.entity.AshenWisp;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.blaze.BlazeModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

/**
 * Placeholder renderer for the ashen wisp: a {@link MobRenderer} over the vanilla blaze model, with
 * our own molten-orange texture (64x32, the blaze sheet layout). Like the blaze, it is drawn at
 * full block light ({@link #getBlockLightLevel}) — it looks like a light source, while placing not
 * one block of light: the false comfort. Art is the user's job.
 */
public final class AshenWispRenderer extends MobRenderer<AshenWisp, LivingEntityRenderState, BlazeModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("eldritch_horror", "textures/entity/ashen_wisp.png");

    public AshenWispRenderer(EntityRendererProvider.Context context) {
        super(context, new BlazeModel(context.bakeLayer(ModelLayers.BLAZE)), 0.5F);
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    /** It glows: the watching flame is drawn lit even where the world is not. */
    @Override
    protected int getBlockLightLevel(AshenWisp entity, BlockPos blockPos) {
        return 15;
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return TEXTURE;
    }
}
