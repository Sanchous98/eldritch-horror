package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.network.QuestSyncPayload;
import java.util.List;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;

/**
 * Client-side payload plumbing: attaches the handlers to payload types whose registration lives in
 * common code, so no client class is ever referenced from the server-reachable side (see
 * {@code network/ModPayloads}). Client-only by construction: the class is never loaded on a
 * dedicated server.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID, value = Dist.CLIENT)
public final class ClientPayloadHandlers {

    private ClientPayloadHandlers() {
    }

    @SubscribeEvent
    public static void onRegisterClientPayloadHandlers(RegisterClientPayloadHandlersEvent event) {
        // Runs on the main client thread (the default), so the mirror needs no synchronisation
        // beyond its volatile swap.
        event.register(QuestSyncPayload.TYPE,
                (payload, context) -> ClientQuests.replaceAll(payload.quests()));
    }

    /** Drops the synced quest definitions on disconnect, so they cannot leak into the next world. */
    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientQuests.replaceAll(List.of());
    }
}
