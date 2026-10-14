package com.sanchous98.eldritchhorror.network;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.quest.Quest;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Server → client: the loaded quest definitions, so the journal screen can render names, targets
 * and prerequisites without a datapack on the client (design/13). Sent on player join and on every
 * datapack reload through {@code quest/QuestSync}.
 *
 * <p>Only definitions travel; per-player progress stays on the synced {@code QUESTS} attachment.
 * The list is encoded with {@link Quest#CODEC} over NBT, so the payload mirrors exactly the JSON
 * schema the datapack loads.
 *
 * @param quests the quest definitions, in no particular order (the client re-sorts by stage)
 */
public record QuestSyncPayload(List<Quest> quests) implements CustomPacketPayload {

    /** The payload id used by the registrar; namespaced by the mod id. */
    public static final CustomPacketPayload.Type<QuestSyncPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "quest_sync"));

    /** One quest as NBT, the same schema as a {@code quest/*.json} file. */
    private static final StreamCodec<FriendlyByteBuf, List<Quest>> QUESTS =
            ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.fromCodec(Quest.CODEC));

    public static final StreamCodec<RegistryFriendlyByteBuf, QuestSyncPayload> STREAM_CODEC =
            StreamCodec.composite(QUESTS, QuestSyncPayload::quests, QuestSyncPayload::new);

    public QuestSyncPayload {
        quests = List.copyOf(quests);
    }

    @Override
    public CustomPacketPayload.Type<QuestSyncPayload> type() {
        return TYPE;
    }
}
