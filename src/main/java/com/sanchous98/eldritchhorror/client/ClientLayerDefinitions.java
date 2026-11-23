package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.client.model.TrainCarModel;
import com.sanchous98.eldritchhorror.client.model.boss.ChaugnarFaugnModel;
import com.sanchous98.eldritchhorror.client.model.boss.DunwichHorrorModel;
import com.sanchous98.eldritchhorror.client.model.boss.GlaakiModel;
import com.sanchous98.eldritchhorror.client.model.boss.AzathothModel;
import com.sanchous98.eldritchhorror.client.model.boss.CthulhuModel;
import com.sanchous98.eldritchhorror.client.model.boss.AtlachNachaModel;
import com.sanchous98.eldritchhorror.client.model.boss.AbhothModel;
import com.sanchous98.eldritchhorror.client.model.boss.ChoirLeviathanModel;
import com.sanchous98.eldritchhorror.client.model.boss.IthaqualShadeModel;
import com.sanchous98.eldritchhorror.client.model.boss.NephrenKaModel;
import com.sanchous98.eldritchhorror.client.model.boss.TulzschaModel;
import com.sanchous98.eldritchhorror.client.model.boss.ZstylzhemghiModel;
import com.sanchous98.eldritchhorror.client.model.boss.CthughaModel;
import com.sanchous98.eldritchhorror.client.model.boss.HydraModel;
import com.sanchous98.eldritchhorror.client.model.boss.IthaquaModel;
import com.sanchous98.eldritchhorror.client.model.boss.NyogthaModel;
import com.sanchous98.eldritchhorror.client.model.boss.RhanTegothModel;
import com.sanchous98.eldritchhorror.client.model.boss.YigModel;
import com.sanchous98.eldritchhorror.client.model.boss.HasturModel;
import com.sanchous98.eldritchhorror.client.model.boss.NyarlathotepModel;
import com.sanchous98.eldritchhorror.client.model.boss.ShubNiggurathModel;
import com.sanchous98.eldritchhorror.client.model.boss.TheHorrorModel;
import com.sanchous98.eldritchhorror.client.model.boss.YogSothothModel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * Bakes the custom Ancient One model layers. Kept separate from
 * {@link ClientEntityRenderers} so model geometry (which touches client-only
 * {@code ModelPart} classes) is registered in the dedicated layer-definition phase, before any
 * renderer asks {@code context.bakeLayer(...)}.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID, value = Dist.CLIENT)
public final class ClientLayerDefinitions {

    private ClientLayerDefinitions() {
    }

    @SubscribeEvent
    public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(DunwichHorrorModel.LAYER, DunwichHorrorModel::createBodyLayer);
        event.registerLayerDefinition(GlaakiModel.LAYER, GlaakiModel::createBodyLayer);
        event.registerLayerDefinition(ChaugnarFaugnModel.LAYER, ChaugnarFaugnModel::createBodyLayer);
        event.registerLayerDefinition(CthulhuModel.LAYER, CthulhuModel::createBodyLayer);
        event.registerLayerDefinition(AzathothModel.LAYER, AzathothModel::createBodyLayer);
        event.registerLayerDefinition(YogSothothModel.LAYER, YogSothothModel::createBodyLayer);
        event.registerLayerDefinition(HasturModel.LAYER, HasturModel::createBodyLayer);
        event.registerLayerDefinition(NyarlathotepModel.LAYER, NyarlathotepModel::createBodyLayer);
        event.registerLayerDefinition(ShubNiggurathModel.LAYER, ShubNiggurathModel::createBodyLayer);
        event.registerLayerDefinition(TheHorrorModel.LAYER, TheHorrorModel::createBodyLayer);
        event.registerLayerDefinition(AtlachNachaModel.LAYER, AtlachNachaModel::createBodyLayer);
        event.registerLayerDefinition(IthaquaModel.LAYER, IthaquaModel::createBodyLayer);
        event.registerLayerDefinition(YigModel.LAYER, YigModel::createBodyLayer);
        event.registerLayerDefinition(CthughaModel.LAYER, CthughaModel::createBodyLayer);
        event.registerLayerDefinition(HydraModel.LAYER, HydraModel::createBodyLayer);
        event.registerLayerDefinition(NyogthaModel.LAYER, NyogthaModel::createBodyLayer);
        event.registerLayerDefinition(RhanTegothModel.LAYER, RhanTegothModel::createBodyLayer);
        event.registerLayerDefinition(TulzschaModel.LAYER, TulzschaModel::createBodyLayer);
        event.registerLayerDefinition(ZstylzhemghiModel.LAYER, ZstylzhemghiModel::createBodyLayer);
        event.registerLayerDefinition(AbhothModel.LAYER, AbhothModel::createBodyLayer);
        event.registerLayerDefinition(NephrenKaModel.LAYER, NephrenKaModel::createBodyLayer);
        event.registerLayerDefinition(ChoirLeviathanModel.LAYER, ChoirLeviathanModel::createBodyLayer);
        event.registerLayerDefinition(IthaqualShadeModel.LAYER, IthaqualShadeModel::createBodyLayer);
        event.registerLayerDefinition(TrainCarModel.WAGON_LAYER, TrainCarModel::createWagonLayer);
        event.registerLayerDefinition(TrainCarModel.LOCOMOTIVE_LAYER,
                TrainCarModel::createLocomotiveLayer);
    }
}
