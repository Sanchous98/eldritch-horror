package com.sanchous98.eldritchhorror.quest;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.network.QuestSyncPayload;
import java.util.List;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Ships the loaded quest definitions (see {@link Quests}) to players so the client journal screen
 * has them. NeoForge fires {@link OnDatapackSyncEvent} once per joining player and once for every
 * player on a datapack reload, which is exactly the two moments the client's {@code ClientQuests}
 * mirror can go stale — one hook covers both.
 *
 * <p>Definitions only: per-player progress is already synced on the {@code QUESTS} attachment.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class QuestSync {

    private QuestSync() {
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        List<Quest> quests = Quests.all();
        QuestSyncPayload payload = new QuestSyncPayload(quests);
        event.getRelevantPlayers().forEach(player ->
                PacketDistributor.sendToPlayer(player, payload));
    }
}
