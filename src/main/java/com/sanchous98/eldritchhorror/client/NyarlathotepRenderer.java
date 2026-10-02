package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.entity.Nyarlathotep;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.npc.VillagerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Placeholder renderer for Nyarlathotep: the vanilla villager model (the trusted face) with our own
 * flat placeholder texture (64x64 villager sheet). Art is the user's job.
 *
 * <p>26.3 verified as for {@code WorshipperRenderer}: {@code VillagerModel} +
 * {@code VillagerRenderState}, only {@code createRenderState()} and {@code getTextureLocation} to
 * implement.
 */
@OnlyIn(Dist.CLIENT)
public final class NyarlathotepRenderer
        extends MobRenderer<Nyarlathotep, VillagerRenderState, VillagerModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("eldritch_horror", "textures/entity/nyarlathotep.png");

    public NyarlathotepRenderer(EntityRendererProvider.Context context) {
        super(context, new VillagerModel(context.bakeLayer(ModelLayers.VILLAGER)), 0.5F);
    }

    @Override
    public VillagerRenderState createRenderState() {
        return new VillagerRenderState();
    }

    @Override
    public Identifier getTextureLocation(VillagerRenderState state) {
        return TEXTURE;
    }
}
