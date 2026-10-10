package com.sanchous98.eldritchhorror.quest;

import com.sanchous98.eldritchhorror.EldritchHorror;
import java.util.Map;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

/**
 * Loads the quest journal from {@code data/<namespace>/quest/*.json} into {@link Quests} on every
 * server data load — startup and {@code /reload}. Built on the vanilla JSON resource loader, so it
 * inherits the data-pack folder scanning, the conditional-file support and per-file error logging: a
 * malformed quest is reported and skipped, never fatal.
 *
 * <p>The listener is registered on the game bus via {@link AddServerReloadListenersEvent}; nothing
 * else about quests lives here.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class QuestReload extends SimpleJsonResourceReloadListener<Quest> {

    /** The data directory scanned for quest files. */
    private static final FileToIdConverter LISTER = FileToIdConverter.json("quest");

    private QuestReload() {
        super(Quest.CODEC, LISTER);
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(EldritchHorror.id("quests"), new QuestReload());
    }

    @Override
    protected void apply(Map<Identifier, Quest> entries, ResourceManager manager, ProfilerFiller profiler) {
        Quests.replaceAll(entries.values());
        EldritchHorror.LOGGER.info("quests: loaded {} quest(s) from data", entries.size());
    }
}
