package com.sanchous98.eldritchhorror.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Base renderer for the Ancient Ones: a {@link MobRenderer} bound to {@link BossRenderState}, with
 * one texture and one world scale per concrete renderer.
 *
 * <p>The models are built at a readable "design" height around 3-5 blocks and then scaled here to
 * the canonical presence size (6-9 blocks for the major presences, 4-6 for the roaming nine,
 * 3-4 for the lesser ones). Scaling in the renderer keeps the geometry and the texture UVs intact,
 * and the hitbox in {@code ModEntities} is matched to the same numbers.
 *
 * <p>The rig puts every model's feet at y = 24 units (1.5 blocks), which is exactly the offset that
 * {@link net.minecraft.client.renderer.entity.LivingEntityRenderer} applies as its fixed -1.501
 * translation. Because that translation is post-multiplied (applied to the model point before the
 * scale), the feet stay on the ground at any scale, so no extra compensation is needed.
 */
@OnlyIn(Dist.CLIENT)
public abstract class BossRenderer<T extends Mob, M extends EntityModel<BossRenderState>>
        extends MobRenderer<T, BossRenderState, M> {

    private final Identifier texture;
    private final float modelScale;

    protected BossRenderer(EntityRendererProvider.Context context, M model, float shadow,
                           Identifier texture, float modelScale) {
        super(context, model, shadow);
        this.texture = texture;
        this.modelScale = modelScale;
    }

    @Override
    protected void scale(BossRenderState state, PoseStack poseStack) {
        super.scale(state, poseStack);
        if (this.modelScale != 1.0F) {
            poseStack.scale(this.modelScale, this.modelScale, this.modelScale);
        }
    }

    @Override
    public BossRenderState createRenderState() {
        return new BossRenderState();
    }

    @Override
    public void extractRenderState(T entity, BossRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.attacking = entity.isSwinging();
        state.attackProgress = state.attacking ? entity.getSwingAnimation(partialTicks) : 0.0F;
        // Triangle over the swing: 0 at start/end, 1 at the midpoint, so the lunge reads as a
        // single forward thrust rather than a snap.
        state.lunge = state.attacking
                ? (float) Math.sin(state.attackProgress * Math.PI)
                : 0.0F;
    }

    @Override
    public Identifier getTextureLocation(BossRenderState state) {
        return this.texture;
    }
}
