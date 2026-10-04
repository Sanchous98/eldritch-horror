package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.entity.AshFowl;
import com.sanchous98.eldritchhorror.entity.BogBear;
import com.sanchous98.eldritchhorror.entity.Burrowling;
import com.sanchous98.eldritchhorror.entity.Deer;
import com.sanchous98.eldritchhorror.entity.GreyFox;
import com.sanchous98.eldritchhorror.entity.HearthCat;
import com.sanchous98.eldritchhorror.entity.HillHound;
import com.sanchous98.eldritchhorror.entity.MireSow;
import com.sanchous98.eldritchhorror.entity.PackBeast;
import com.sanchous98.eldritchhorror.entity.SporeBee;
import com.sanchous98.eldritchhorror.entity.StoneSentinel;
import com.sanchous98.eldritchhorror.entity.TideGrazer;
import com.sanchous98.eldritchhorror.entity.WoolBeast;
import com.sanchous98.eldritchhorror.entity.WoolHare;
import net.minecraft.client.model.animal.bee.AdultBeeModel;
import net.minecraft.client.model.animal.chicken.AdultChickenModel;
import net.minecraft.client.model.animal.cow.CowModel;
import net.minecraft.client.model.animal.feline.AdultCatModel;
import net.minecraft.client.model.animal.fox.AdultFoxModel;
import net.minecraft.client.model.animal.golem.IronGolemModel;
import net.minecraft.client.model.animal.equine.HorseModel;
import net.minecraft.client.model.animal.llama.LlamaModel;
import net.minecraft.client.model.animal.panda.PandaModel;
import net.minecraft.client.model.animal.pig.PigModel;
import net.minecraft.client.model.animal.rabbit.AdultRabbitModel;
import net.minecraft.client.model.animal.sheep.SheepModel;
import net.minecraft.client.model.animal.turtle.AdultTurtleModel;
import net.minecraft.client.model.animal.wolf.AdultWolfModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.BeeRenderState;
import net.minecraft.client.renderer.entity.state.CatRenderState;
import net.minecraft.client.renderer.entity.state.ChickenRenderState;
import net.minecraft.client.renderer.entity.state.EquineRenderState;
import net.minecraft.client.renderer.entity.state.FoxRenderState;
import net.minecraft.client.renderer.entity.state.IronGolemRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.LlamaRenderState;
import net.minecraft.client.renderer.entity.state.PandaRenderState;
import net.minecraft.client.renderer.entity.state.RabbitRenderState;
import net.minecraft.client.renderer.entity.state.SheepRenderState;
import net.minecraft.client.renderer.entity.state.TurtleRenderState;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraft.resources.Identifier;

/**
 * Placeholder renderers for the mundane passive roster. Art is the user's job, so each mob reuses an
 * existing baked vanilla model layer and a flat placeholder texture; roles that share a body shape
 * share a model (e.g. the grazers). All are plain {@link MobRenderer}s over the vanilla models —
 * only {@code createRenderState} and {@code getTextureLocation} are implemented, so no vanilla
 * render-state fields beyond their defaults are needed.
 *
 * <p>26.3 verified generics: {@code CowModel}/{@code PigModel} state is
 * {@link LivingEntityRenderState}; {@code SheepModel} likewise; the rest use their own state
 * ({@link ChickenRenderState}, {@link RabbitRenderState}, {@link EquineRenderState},
 * {@link FoxRenderState}, {@link WolfRenderState}, {@link CatRenderState}, {@link LlamaRenderState},
 * {@link PandaRenderState}, {@link TurtleRenderState}, {@link BeeRenderState},
 * {@link IronGolemRenderState}).
 */
public final class PassiveRenderers {

    private static final String NS = "eldritch_horror";

    private PassiveRenderers() {
    }

    /** A simple renderer over a fixed vanilla model, distinguished only by texture. */
    private static Identifier tex(String name) {
        return Identifier.fromNamespaceAndPath(NS, "textures/entity/" + name + ".png");
    }

    /** Deer: cow quadruped, deer texture. */
    public static final class DeerRenderer extends MobRenderer<Deer, LivingEntityRenderState, CowModel> {
        public DeerRenderer(EntityRendererProvider.Context context) {
            super(context, new CowModel(context.bakeLayer(ModelLayers.COW)), 0.7F);
        }

        @Override
        public LivingEntityRenderState createRenderState() {
            return new LivingEntityRenderState();
        }

        @Override
        public Identifier getTextureLocation(LivingEntityRenderState state) {
            return tex("deer");
        }
    }

    /** Wool hare: sheep quadruped, hare texture. */
    public static final class WoolHareRenderer extends MobRenderer<WoolHare, SheepRenderState, SheepModel> {
        public WoolHareRenderer(EntityRendererProvider.Context context) {
            super(context, new SheepModel(context.bakeLayer(ModelLayers.SHEEP)), 0.6F);
        }

        @Override
        public SheepRenderState createRenderState() {
            return new SheepRenderState();
        }

        @Override
        public Identifier getTextureLocation(SheepRenderState state) {
            return tex("wool_hare");
        }
    }

    /** Mire sow: pig quadruped, sow texture. */
    public static final class MireSowRenderer extends MobRenderer<MireSow, LivingEntityRenderState, PigModel> {
        public MireSowRenderer(EntityRendererProvider.Context context) {
            super(context, new PigModel(context.bakeLayer(ModelLayers.PIG)), 0.7F);
        }

        @Override
        public LivingEntityRenderState createRenderState() {
            return new LivingEntityRenderState();
        }

        @Override
        public Identifier getTextureLocation(LivingEntityRenderState state) {
            return tex("mire_sow");
        }
    }

    /** Ash fowl: chicken model, fowl texture. */
    public static final class AshFowlRenderer extends MobRenderer<AshFowl, ChickenRenderState, AdultChickenModel> {
        public AshFowlRenderer(EntityRendererProvider.Context context) {
            super(context, new AdultChickenModel(context.bakeLayer(ModelLayers.CHICKEN)), 0.3F);
        }

        @Override
        public ChickenRenderState createRenderState() {
            return new ChickenRenderState();
        }

        @Override
        public Identifier getTextureLocation(ChickenRenderState state) {
            return tex("ash_fowl");
        }
    }

    /** Burrowling: rabbit model, burrowling texture. */
    public static final class BurrowlingRenderer extends MobRenderer<Burrowling, RabbitRenderState, AdultRabbitModel> {
        public BurrowlingRenderer(EntityRendererProvider.Context context) {
            super(context, new AdultRabbitModel(context.bakeLayer(ModelLayers.RABBIT)), 0.3F);
        }

        @Override
        public RabbitRenderState createRenderState() {
            return new RabbitRenderState();
        }

        @Override
        public Identifier getTextureLocation(RabbitRenderState state) {
            return tex("burrowling");
        }
    }

    /** Pack beast: horse model, beast texture. */
    public static final class PackBeastRenderer extends MobRenderer<PackBeast, EquineRenderState, HorseModel> {
        public PackBeastRenderer(EntityRendererProvider.Context context) {
            super(context, new HorseModel(context.bakeLayer(ModelLayers.HORSE)), 0.8F);
        }

        @Override
        public EquineRenderState createRenderState() {
            return new EquineRenderState();
        }

        @Override
        public Identifier getTextureLocation(EquineRenderState state) {
            return tex("pack_beast");
        }
    }

    /** Grey fox: fox model, fox texture. */
    public static final class GreyFoxRenderer extends MobRenderer<GreyFox, FoxRenderState, AdultFoxModel> {
        public GreyFoxRenderer(EntityRendererProvider.Context context) {
            super(context, new AdultFoxModel(context.bakeLayer(ModelLayers.FOX)), 0.4F);
        }

        @Override
        public FoxRenderState createRenderState() {
            return new FoxRenderState();
        }

        @Override
        public Identifier getTextureLocation(FoxRenderState state) {
            return tex("grey_fox");
        }
    }

    /** Hill hound: wolf model, hound texture. */
    public static final class HillHoundRenderer extends MobRenderer<HillHound, WolfRenderState, AdultWolfModel> {
        public HillHoundRenderer(EntityRendererProvider.Context context) {
            super(context, new AdultWolfModel(context.bakeLayer(ModelLayers.WOLF)), 0.5F);
        }

        @Override
        public WolfRenderState createRenderState() {
            return new WolfRenderState();
        }

        @Override
        public Identifier getTextureLocation(WolfRenderState state) {
            return tex("hill_hound");
        }
    }

    /** Hearth cat: cat model, cat texture. */
    public static final class HearthCatRenderer extends MobRenderer<HearthCat, CatRenderState, AdultCatModel> {
        public HearthCatRenderer(EntityRendererProvider.Context context) {
            super(context, new AdultCatModel(context.bakeLayer(ModelLayers.CAT)), 0.4F);
        }

        @Override
        public CatRenderState createRenderState() {
            return new CatRenderState();
        }

        @Override
        public Identifier getTextureLocation(CatRenderState state) {
            return tex("hearth_cat");
        }
    }

    /** Wool beast: llama model, beast texture. */
    public static final class WoolBeastRenderer extends MobRenderer<WoolBeast, LlamaRenderState, LlamaModel> {
        public WoolBeastRenderer(EntityRendererProvider.Context context) {
            super(context, new LlamaModel(context.bakeLayer(ModelLayers.LLAMA)), 0.7F);
        }

        @Override
        public LlamaRenderState createRenderState() {
            return new LlamaRenderState();
        }

        @Override
        public Identifier getTextureLocation(LlamaRenderState state) {
            return tex("wool_beast");
        }
    }

    /** Bog bear: panda model, bear texture. */
    public static final class BogBearRenderer extends MobRenderer<BogBear, PandaRenderState, PandaModel> {
        public BogBearRenderer(EntityRendererProvider.Context context) {
            super(context, new PandaModel(context.bakeLayer(ModelLayers.PANDA)), 0.9F);
        }

        @Override
        public PandaRenderState createRenderState() {
            return new PandaRenderState();
        }

        @Override
        public Identifier getTextureLocation(PandaRenderState state) {
            return tex("bog_bear");
        }
    }

    /** Tide grazer: turtle model, grazer texture. */
    public static final class TideGrazerRenderer extends MobRenderer<TideGrazer, TurtleRenderState, AdultTurtleModel> {
        public TideGrazerRenderer(EntityRendererProvider.Context context) {
            super(context, new AdultTurtleModel(context.bakeLayer(ModelLayers.TURTLE)), 0.7F);
        }

        @Override
        public TurtleRenderState createRenderState() {
            return new TurtleRenderState();
        }

        @Override
        public Identifier getTextureLocation(TurtleRenderState state) {
            return tex("tide_grazer");
        }
    }

    /** Spore bee: bee model, bee texture. */
    public static final class SporeBeeRenderer extends MobRenderer<SporeBee, BeeRenderState, AdultBeeModel> {
        public SporeBeeRenderer(EntityRendererProvider.Context context) {
            super(context, new AdultBeeModel(context.bakeLayer(ModelLayers.BEE)), 0.4F);
        }

        @Override
        public BeeRenderState createRenderState() {
            return new BeeRenderState();
        }

        @Override
        public Identifier getTextureLocation(BeeRenderState state) {
            return tex("spore_bee");
        }
    }

    /** Stone sentinel: iron golem model, sentinel texture. */
    public static final class StoneSentinelRenderer extends MobRenderer<StoneSentinel, IronGolemRenderState, IronGolemModel> {
        public StoneSentinelRenderer(EntityRendererProvider.Context context) {
            super(context, new IronGolemModel(context.bakeLayer(ModelLayers.IRON_GOLEM)), 1.0F);
        }

        @Override
        public IronGolemRenderState createRenderState() {
            return new IronGolemRenderState();
        }

        @Override
        public Identifier getTextureLocation(IronGolemRenderState state) {
            return tex("stone_sentinel");
        }
    }
}
