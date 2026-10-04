package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.entity.Worshipper;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.npc.VillagerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.resources.Identifier;

/**
 * Placeholder renderer for the worshipper: a {@link MobRenderer} over the vanilla villager model,
 * with our own flat placeholder texture. The passive-humanoid shape (not {@code HumanoidMobRenderer},
 * which expects a {@code HumanoidRenderState}) is the one 26.3 exposes for the villager; the cult
 * skin comes later. Art is the user's job.
 *
 * <p>26.3 verified: {@code VillagerModel extends EntityModel<VillagerRenderState>} and vanilla
 * {@code VillagerRenderer} is an {@code AgeableMobRenderer}; we take the plain {@code MobRenderer}
 * shape and need only {@code createRenderState()} + {@code getTextureLocation(state)}.
 */
public final class WorshipperRenderer extends MobRenderer<Worshipper, VillagerRenderState, VillagerModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("eldritch_horror", "textures/entity/worshipper.png");

    public WorshipperRenderer(EntityRendererProvider.Context context) {
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
