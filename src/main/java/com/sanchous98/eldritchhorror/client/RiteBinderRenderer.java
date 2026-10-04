package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.entity.RiteBinder;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.illager.IllagerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import net.minecraft.resources.Identifier;

/**
 * Placeholder renderer for the rite binder: the vanilla illager body in the caster's spellcasting
 * pose, with our own flat placeholder texture. The binder holds that pose whenever it has a target,
 * so the raised arms telegraph the rite before the dead rise. Art is the user's job.
 *
 * <p>26.3 verified: {@code IllagerRenderState.armPose} is an {@code AbstractIllager.IllagerArmPose},
 * which drives {@code IllagerModel}'s arms; {@code IllagerModel} is {@code EntityModel}-based, so
 * this is a {@link MobRenderer}.
 */
public final class RiteBinderRenderer extends MobRenderer<RiteBinder, IllagerRenderState, IllagerModel<IllagerRenderState>> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("eldritch_horror", "textures/entity/rite_binder.png");

    public RiteBinderRenderer(EntityRendererProvider.Context context) {
        super(context, new IllagerModel<>(context.bakeLayer(ModelLayers.EVOKER)), 0.5F);
    }

    @Override
    public IllagerRenderState createRenderState() {
        return new IllagerRenderState();
    }

    @Override
    public void extractRenderState(RiteBinder entity, IllagerRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.isAggressive = entity.isAggressive();
        state.armPose = entity.isAggressive()
                ? net.minecraft.world.entity.monster.illager.AbstractIllager.IllagerArmPose.SPELLCASTING
                : net.minecraft.world.entity.monster.illager.AbstractIllager.IllagerArmPose.NEUTRAL;
    }

    @Override
    public Identifier getTextureLocation(IllagerRenderState state) {
        return TEXTURE;
    }
}
