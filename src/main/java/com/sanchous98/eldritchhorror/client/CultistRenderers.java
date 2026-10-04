package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.entity.CultRaider;
import com.sanchous98.eldritchhorror.entity.CultZealot;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.illager.IllagerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import net.minecraft.resources.Identifier;

/**
 * Placeholder renderers for the two melee cult soldiers — {@link CultZealot} and {@link CultRaider}.
 * Both wear the vanilla illager silhouette (the pillager/vindicator shape the design maps them to),
 * distinguished only by texture; art is the user's job. Two small classes rather than one generic, so
 * each renderer stays as simple as the other bestiary renderers.
 *
 * <p>26.3 verified: {@code IllagerModel<S extends IllagerRenderState> extends EntityModel<S>} (it is
 * <b>not</b> a {@code HumanoidModel}, so this takes the {@link MobRenderer} shape, not
 * {@code HumanoidMobRenderer}); {@code IllagerRenderState} is a {@code LivingEntityRenderState}, and
 * {@code ModelLayers.PILLAGER} is the baked illager body layer.
 */
public final class CultistRenderers {

    private CultistRenderers() {
    }

    /** Renderer for {@link CultZealot}: illager model, cult-zealot texture. */
    public static final class Zealot extends MobRenderer<CultZealot, IllagerRenderState, IllagerModel<IllagerRenderState>> {
        private static final Identifier TEXTURE =
                Identifier.fromNamespaceAndPath("eldritch_horror", "textures/entity/cult_zealot.png");

        public Zealot(EntityRendererProvider.Context context) {
            super(context, new IllagerModel<>(context.bakeLayer(ModelLayers.PILLAGER)), 0.5F);
        }

        @Override
        public IllagerRenderState createRenderState() {
            return new IllagerRenderState();
        }

        @Override
        public void extractRenderState(CultZealot entity, IllagerRenderState state, float partialTicks) {
            super.extractRenderState(entity, state, partialTicks);
            state.isAggressive = entity.isAggressive();
            state.armPose = entity.isAggressive()
                    ? net.minecraft.world.entity.monster.illager.AbstractIllager.IllagerArmPose.ATTACKING
                    : net.minecraft.world.entity.monster.illager.AbstractIllager.IllagerArmPose.NEUTRAL;
        }

        @Override
        public Identifier getTextureLocation(IllagerRenderState state) {
            return TEXTURE;
        }
    }

    /** Renderer for {@link CultRaider}: illager model, raider texture. */
    public static final class Raider extends MobRenderer<CultRaider, IllagerRenderState, IllagerModel<IllagerRenderState>> {
        private static final Identifier TEXTURE =
                Identifier.fromNamespaceAndPath("eldritch_horror", "textures/entity/cult_raider.png");

        public Raider(EntityRendererProvider.Context context) {
            super(context, new IllagerModel<>(context.bakeLayer(ModelLayers.PILLAGER)), 0.5F);
        }

        @Override
        public IllagerRenderState createRenderState() {
            return new IllagerRenderState();
        }

        @Override
        public void extractRenderState(CultRaider entity, IllagerRenderState state, float partialTicks) {
            super.extractRenderState(entity, state, partialTicks);
            state.isAggressive = entity.isAggressive();
            state.armPose = entity.isAggressive()
                    ? net.minecraft.world.entity.monster.illager.AbstractIllager.IllagerArmPose.ATTACKING
                    : net.minecraft.world.entity.monster.illager.AbstractIllager.IllagerArmPose.NEUTRAL;
        }

        @Override
        public Identifier getTextureLocation(IllagerRenderState state) {
            return TEXTURE;
        }
    }
}
