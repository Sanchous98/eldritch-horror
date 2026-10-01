package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.entity.DunwichHorror;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.ravager.RavagerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.RavagerRenderState;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Placeholder renderer for the Dunwich Horror: the vanilla ravager quadruped model and a flat
 * placeholder texture (128x128 ravager sheet). Art is the user's job.
 *
 * <p>26.3 verified as for {@link CthulhuRenderer}: {@code RavagerModel} +
 * {@code RavagerRenderState}, only {@code createRenderState()} and {@code getTextureLocation} to
 * implement.
 */
@OnlyIn(Dist.CLIENT)
public final class DunwichHorrorRenderer
        extends MobRenderer<DunwichHorror, RavagerRenderState, RavagerModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("eldritch_horror", "textures/entity/dunwich_horror.png");

    public DunwichHorrorRenderer(EntityRendererProvider.Context context) {
        super(context, new RavagerModel(context.bakeLayer(ModelLayers.RAVAGER)), 1.0F);
    }

    @Override
    public RavagerRenderState createRenderState() {
        return new RavagerRenderState();
    }

    @Override
    public Identifier getTextureLocation(RavagerRenderState state) {
        return TEXTURE;
    }
}
