package com.sanchous98.eldritchhorror.advancement;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.cult.CultRank;
import com.sanchous98.eldritchhorror.entity.AncientOne;
import com.sanchous98.eldritchhorror.world.city.City;
import com.sanchous98.eldritchhorror.world.city.Cities;
import com.sanchous98.eldritchhorror.world.city.CityState;
import java.util.Map;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jspecify.annotations.Nullable;

/**
 * Programmatic awarding for the vanilla Advancements tab that guides the mod's content
 * ({@code docs/ADVANCEMENTS.md}).
 *
 * <p>Every advancement under {@code data/eldritch_horror/advancement/} uses one criterion:
 * {@code minecraft:impossible}. The game never grants it by itself; the systems that actually own a
 * piece of progress call {@link #award} at the moment it happens. That keeps the journal
 * ({@code quest/QuestJournal}) the authority on quest progress and makes the advancement screen a
 * read-only guide: a missing id is simply skipped, and awarding twice is harmless because
 * {@link PlayerAdvancements#award} is idempotent.
 *
 * <p>The root is granted on login (and refreshed periodically) so the tab is always visible as the
 * guide, even on a world that predates it.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class Advancements {

    /** The tab root, {@code data/eldritch_horror/advancement/root.json}. */
    public static final String ROOT = "root";

    /** Ticks between root refreshes (one minute). */
    private static final int ROOT_REFRESH_TICKS = 1200;

    /**
     * Quests whose advancement lives outside the default {@code quests/<id>} path. Everything else
     * falls back to that path, so a new quest needs only its JSON file.
     */
    private static final Map<String, String> QUEST_PATHS = Map.ofEntries(
            Map.entry("first_steps", "prologue/first_steps"),
            Map.entry("first_rite", "prologue/first_rite"),
            Map.entry("lorekeeper", "investigation/lorekeeper"),
            Map.entry("lorekeeper_ii", "investigation/lorekeeper_ii"),
            Map.entry("sworn_ally", "cults/sworn_ally"),
            Map.entry("open_the_way", "cults/open_the_way"),
            Map.entry("awaken_the_horror", "cults/awaken_the_horror"),
            Map.entry("the_decision", "cults/the_decision"),
            Map.entry("the_seal", "cults/the_seal"),
            Map.entry("reclaim_the_city", "threats/reclaim_the_city"));

    private Advancements() {
    }

    /**
     * Grants {@code path} to {@code player}. The path may be absolute ({@code namespace:path}) or
     * relative to this mod. Unknown advancements and players without a server are ignored, which is
     * what keeps this safe to call from any gameplay system.
     *
     * @return whether this call completed the advancement (false if unknown, already done, or absent)
     */
    public static boolean award(ServerPlayer player, String path) {
        Identifier id = path.indexOf(':') >= 0 ? Identifier.tryParse(path) : EldritchHorror.id(path);
        if (id == null) {
            return false;
        }
        AdvancementHolder holder = holder(player, id);
        if (holder == null) {
            return false;
        }
        return player.getAdvancements().award(holder, "impossible");
    }

    /** @return whether {@code player} has already completed {@code path}. */
    public static boolean earned(ServerPlayer player, String path) {
        Identifier id = path.indexOf(':') >= 0 ? Identifier.tryParse(path) : EldritchHorror.id(path);
        AdvancementHolder holder = id == null ? null : holder(player, id);
        if (holder == null) {
            return false;
        }
        AdvancementProgress progress = player.getAdvancements().getOrStartProgress(holder);
        return progress.isDone();
    }

    /** The {@code @Nullable} lookup shared by {@link #award} and {@link #earned}. */
    private static @Nullable AdvancementHolder holder(ServerPlayer player, Identifier id) {
        MinecraftServer server = player.level().getServer();
        return server == null ? null : server.getAdvancements().get(id);
    }

    /**
     * Awards the advancement for a completed journal quest. Starter tasks live under
     * {@code investigation/}, the prologue and endgame under their chapters, and anything else at
     * the default {@code quests/<questId>}; a quest without a JSON file is skipped.
     */
    public static boolean awardQuest(ServerPlayer player, String questId) {
        String path = QUEST_PATHS.get(questId);
        if (path == null) {
            path = questId.startsWith("starter_")
                    ? "investigation/" + questId
                    : "quests/" + questId;
        }
        return award(player, path);
    }

    /**
     * Awards a curated city's contract advancement ({@code cities/<city_key>}) and, once every
     * curated city has been completed, the {@code cities/all_contracts} capstone.
     */
    public static boolean awardCityContract(ServerPlayer player, City city) {
        boolean earnedNow = award(player, cityAdvancement(city));
        awardAllContractsIfComplete(player);
        return earnedNow;
    }

    /** @return the advancement path for {@code city}: {@code cities/<city_key>}. */
    public static String cityAdvancement(City city) {
        return "cities/" + cityKey(city.id());
    }

    /**
     * The stable short key of a curated city. The world id carries its coordinates
     * ({@code tokyo_50881_-12993}); the advancement path only needs the name part.
     */
    public static String cityKey(String cityId) {
        return cityId.replaceFirst("_[-0-9]+_[-0-9]+$", "");
    }

    /** Grants the capstone when every curated city's contract is already completed. */
    private static void awardAllContractsIfComplete(ServerPlayer player) {
        if (Cities.all().isEmpty()) {
            return;
        }
        for (City city : Cities.all()) {
            if (!earned(player, cityAdvancement(city))) {
                return;
            }
        }
        award(player, "cities/all_contracts");
    }

    /**
     * Awards the per-cult ladder milestones for {@code cultId} up to {@code rank}. Called from
     * {@code CultSystem} on every reputation change, so a player who jumps straight to a high rank
     * gets the earlier rungs too and the tree never shows a gap.
     */
    public static void awardCultMilestones(ServerPlayer player, String cultId, CultRank rank) {
        if (rank.atLeast(CultRank.INITIATE)) {
            award(player, "cults/" + cultId + "/initiate");
        }
        if (rank.atLeast(CultRank.MEMBER)) {
            award(player, "cults/" + cultId + "/member");
        }
        if (rank.atLeast(CultRank.DEVOTED)) {
            award(player, "cults/" + cultId + "/devoted");
        }
        if (rank.atLeast(CultRank.INNER_CIRCLE)) {
            award(player, "cults/" + cultId + "/inner_circle");
        }
    }

    /** The guide tab is always available: grant the root when the player joins. */
    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            award(player, ROOT);
        }
    }

    /** Refresh the root once a minute, so datapack reloads and pre-existing worlds get the tab too. */
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer().getTickCount() % ROOT_REFRESH_TICKS != 0) {
            return;
        }
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            if (!earned(player, ROOT)) {
                award(player, ROOT);
            }
        }
    }

    /**
     * The first Ancient One felled by a player is a challenge. The kill is attributed exactly like
     * the quest/cult kill triggers: only direct player damage counts.
     */
    @SubscribeEvent
    public static void onKill(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer player
                && event.getEntity() instanceof AncientOne) {
            award(player, "threats/first_ancient_one");
        }
    }

    /**
     * A player standing in a city when it reads {@code FALLEN} has seen a city fall. Called from
     * {@code CityStateDiscovery}, which already samples the city a player is inside every 5 s.
     */
    public static void onCityState(ServerPlayer player, City city, CityState state) {
        if (state == CityState.FALLEN && award(player, "threats/fallen_city")) {
            EldritchHorror.LOGGER.info("advancement: {} witnessed the fall of {}",
                    player.getGameProfile().name(), city.name());
        }
    }
}
