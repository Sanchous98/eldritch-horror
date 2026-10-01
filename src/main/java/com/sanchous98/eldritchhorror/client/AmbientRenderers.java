package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.entity.CaveDrifter;
import com.sanchous98.eldritchhorror.entity.DrownedMinnow;
import com.sanchous98.eldritchhorror.entity.FrostWisp;
import com.sanchous98.eldritchhorror.entity.LanternJelly;
import com.sanchous98.eldritchhorror.entity.MarshMote;
import com.sanchous98.eldritchhorror.entity.PaleDrifter;
import net.minecraft.client.model.ambient.BatModel;
import net.minecraft.client.model.animal.allay.AllayModel;
import net.minecraft.client.model.animal.fish.CodModel;
import net.minecraft.client.model.animal.frog.TadpoleModel;
import net.minecraft.client.model.animal.squid.SquidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.AllayRenderState;
import net.minecraft.client.renderer.entity.state.BatRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.SquidRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Placeholder renderers for the ambient (ambience) drifters. Art is the user's job, so each mob
 * reuses a verified 26.3 baked vanilla model layer and a flat placeholder texture. All are plain
 * {@link MobRenderer}s over the vanilla models — only {@code createRenderState} and
 * {@code getTextureLocation} are implemented.
 *
 * <p>Verified generics (26.3 sources): {@code BatModel} state {@link BatRenderState};
 * {@code SquidModel} state {@link SquidRenderState}; {@code AllayModel} state
 * {@link AllayRenderState}; {@code TadpoleModel}/{@code CodModel} state
 * {@link LivingEntityRenderState}.
 */
@OnlyIn(Dist.CLIENT)
public final class AmbientRenderers {

    private static final String NS = "eldritch_horror";

    private AmbientRenderers() {
    }

    private static Identifier tex(String name) {
        return Identifier.fromNamespaceAndPath(NS, "textures/entity/" + name + ".png");
    }

    /** Cave drifter: bat model, drifter texture. */
    public static final class CaveDrifterRenderer extends MobRenderer<CaveDrifter, BatRenderState, BatModel> {
        public CaveDrifterRenderer(EntityRendererProvider.Context context) {
            super(context, new BatModel(context.bakeLayer(ModelLayers.BAT)), 0.25F);
        }

        @Override
        public BatRenderState createRenderState() {
            return new BatRenderState();
        }

        @Override
        public Identifier getTextureLocation(BatRenderState state) {
            return tex("cave_drifter");
        }
    }

    /** Pale drifter: squid model, drifter texture. */
    public static final class PaleDrifterRenderer extends MobRenderer<PaleDrifter, SquidRenderState, SquidModel> {
        public PaleDrifterRenderer(EntityRendererProvider.Context context) {
            super(context, new SquidModel(context.bakeLayer(ModelLayers.SQUID)), 0.7F);
        }

        @Override
        public SquidRenderState createRenderState() {
            return new SquidRenderState();
        }

        @Override
        public Identifier getTextureLocation(SquidRenderState state) {
            return tex("pale_drifter");
        }
    }

    /** Lantern jelly: squid model, jelly texture (full-bright via {@link #getBlockLightLevel}). */
    public static final class LanternJellyRenderer extends MobRenderer<LanternJelly, SquidRenderState, SquidModel> {
        public LanternJellyRenderer(EntityRendererProvider.Context context) {
            super(context, new SquidModel(context.bakeLayer(ModelLayers.GLOW_SQUID)), 0.7F);
        }

        @Override
        public SquidRenderState createRenderState() {
            return new SquidRenderState();
        }

        @Override
        public Identifier getTextureLocation(SquidRenderState state) {
            return tex("lantern_jelly");
        }

        @Override
        protected int getBlockLightLevel(LanternJelly entity, BlockPos pos) {
            return 15;
        }
    }

    /** Marsh mote: allay model, mote texture. */
    public static final class MarshMoteRenderer extends MobRenderer<MarshMote, AllayRenderState, AllayModel> {
        public MarshMoteRenderer(EntityRendererProvider.Context context) {
            super(context, new AllayModel(context.bakeLayer(ModelLayers.ALLAY)), 0.2F);
        }

        @Override
        public AllayRenderState createRenderState() {
            return new AllayRenderState();
        }

        @Override
        public Identifier getTextureLocation(AllayRenderState state) {
            return tex("marsh_mote");
        }
    }

    /** Drowned minnow: cod model, minnow texture. */
    public static final class DrownedMinnowRenderer extends MobRenderer<DrownedMinnow, LivingEntityRenderState, CodModel> {
        public DrownedMinnowRenderer(EntityRendererProvider.Context context) {
            super(context, new CodModel(context.bakeLayer(ModelLayers.COD)), 0.2F);
        }

        @Override
        public LivingEntityRenderState createRenderState() {
            return new LivingEntityRenderState();
        }

        @Override
        public Identifier getTextureLocation(LivingEntityRenderState state) {
            return tex("drowned_minnow");
        }
    }

    /** Frost wisp: tadpole model (a tiny wriggling mote), wisp texture. */
    public static final class FrostWispRenderer extends MobRenderer<FrostWisp, LivingEntityRenderState, TadpoleModel> {
        public FrostWispRenderer(EntityRendererProvider.Context context) {
            super(context, new TadpoleModel(context.bakeLayer(ModelLayers.TADPOLE)), 0.14F);
        }

        @Override
        public LivingEntityRenderState createRenderState() {
            return new LivingEntityRenderState();
        }

        @Override
        public Identifier getTextureLocation(LivingEntityRenderState state) {
            return tex("frost_wisp");
        }
    }
}
