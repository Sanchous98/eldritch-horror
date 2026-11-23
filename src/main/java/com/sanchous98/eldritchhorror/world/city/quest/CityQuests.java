package com.sanchous98.eldritchhorror.world.city.quest;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.advancement.Advancements;
import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.cult.CultSystem;
import com.sanchous98.eldritchhorror.registry.ModAttachments;
import com.sanchous98.eldritchhorror.world.city.Cities;
import com.sanchous98.eldritchhorror.world.city.City;
import com.sanchous98.eldritchhorror.world.loc.city.CityLocation;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jspecify.annotations.Nullable;

/**
 * Per-city local contracts ({@code design/21-settlements.md}): the 3-5 step chain every curated
 * city offers, built by {@link CityQuestChain} from the city plan's authored anchors.
 *
 * <p>The chain starts the first time a player is close enough to a city and advances from what the
 * player actually does: standing at the board/contact/hidden place, carrying the reagents, and
 * killing district threats inside the district. Progress lives in
 * {@link ModAttachments#CITY_QUESTS} ({@code step:}, {@code kills:}, {@code done:} keys) so it
 * survives a restart and a death. Completion pays relic coins and a little Order standing, once
 * per city.
 *
 * <p>Everything is server-authoritative and bounded: one pass per second over the online players,
 * one nearest-city lookup each, and no chunk loads.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class CityQuests {

    /** How often the tracker looks at the players (1 s). */
    private static final int INTERVAL_TICKS = 20;

    /** How far outside the city edge a player may stand and still be considered "there". */
    private static final int APPROACH_MARGIN = 24;

    /** Reach radius for board/contact/secret steps (blocks, horizontal). */
    private static final int REACH = 8;

    /** Kill credit radius: the district plus its outskirts. */
    private static final int HUNT_MARGIN = 32;

    private static final String STEP = "step:";
    private static final String KILLS = "kills:";
    private static final String DONE = "done:";

    /** Chains are derived from immutable plan data, so one instance per city is enough. */
    private static final Map<String, CityQuestChain> CHAINS = new ConcurrentHashMap<>();

    private CityQuests() {
    }

    /** The chain for {@code city} (cached). */
    public static CityQuestChain chain(City city) {
        return CHAINS.computeIfAbsent(city.id(), id -> CityQuestChain.of(city));
    }

    /** The player's completed step count in {@code city} (0 when the chain has not started). */
    public static int step(ServerPlayer player, City city) {
        return player.getData(ModAttachments.CITY_QUESTS).getOrDefault(STEP + city.id(), 0);
    }

    /** Whether the player has completed {@code city}'s contract. */
    public static boolean done(ServerPlayer player, City city) {
        return player.getData(ModAttachments.CITY_QUESTS).getOrDefault(DONE + city.id(), 0) == 1;
    }

    /** Starts the contract on first approach and advances it from observed behaviour. */
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % INTERVAL_TICKS != 0 || !ModConfig.ENABLE_CITY_QUESTS.get()) {
            return;
        }
        ServerLevel level = server.overworld();
        for (ServerPlayer player : level.players()) {
            City city = nearest(level, player, APPROACH_MARGIN);
            if (city == null) {
                continue;
            }
            CityQuestChain chain = chain(city);
            if (!ensureStarted(player, chain)) {
                continue;
            }
            advance(player, chain);
        }
    }

    /** Credits a kill toward the current district-threat step. */
    @SubscribeEvent
    public static void onKill(LivingDeathEvent event) {
        if (!ModConfig.ENABLE_CITY_QUESTS.get()
                || !(event.getSource().getEntity() instanceof ServerPlayer player)
                || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        City city = nearest(level, player, HUNT_MARGIN);
        if (city == null) {
            return;
        }
        CityQuestChain chain = chain(city);
        int index = step(player, city);
        if (index >= chain.steps().size()) {
            return;
        }
        CityQuestChain.Step current = chain.steps().get(index);
        if (current.kind() != CityQuestChain.Kind.HUNT) {
            return;
        }
        String type = BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntity().getType()).toString();
        if (!CityQuestChain.shortId(current.entity()).equals(CityQuestChain.shortId(type))) {
            return;
        }
        Map<String, Integer> data = new HashMap<>(player.getData(ModAttachments.CITY_QUESTS));
        int kills = data.getOrDefault(KILLS + city.id(), 0) + 1;
        data.put(KILLS + city.id(), kills);
        player.setData(ModAttachments.CITY_QUESTS, data);
        player.sendSystemMessage(Component.translatableWithFallback(
                "cityquest.eldritch_horror.progress",
                "Contract %s: %s/%s", city.name(), Math.min(kills, current.count()),
                current.count()));
    }

    /** @return whether the chain is running (starts it and announces the first step if new). */
    private static boolean ensureStarted(ServerPlayer player, CityQuestChain chain) {
        City city = chain.city();
        if (done(player, city)) {
            return false;
        }
        Map<String, Integer> data = new HashMap<>(player.getData(ModAttachments.CITY_QUESTS));
        if (data.containsKey(STEP + city.id())) {
            return true;
        }
        data.put(STEP + city.id(), 0);
        player.setData(ModAttachments.CITY_QUESTS, data);
        player.sendSystemMessage(Component.translatableWithFallback(
                "cityquest.eldritch_horror.started",
                "A local contract begins in %s: %s", city.name(),
                describe(city, chain.steps().get(0))));
        return true;
    }

    /** Checks the current step and moves the chain on when it is satisfied. */
    private static void advance(ServerPlayer player, CityQuestChain chain) {
        City city = chain.city();
        int index = step(player, city);
        if (index >= chain.steps().size()) {
            return;
        }
        CityQuestChain.Step current = chain.steps().get(index);
        Map<String, Integer> data = new HashMap<>(player.getData(ModAttachments.CITY_QUESTS));
        if (!satisfied(player, city, current, data)) {
            return;
        }
        int next = index + 1;
        data.put(STEP + city.id(), next);
        if (next >= chain.steps().size()) {
            data.put(DONE + city.id(), 1);
            player.setData(ModAttachments.CITY_QUESTS, data);
            reward(player, city);
            return;
        }
        player.setData(ModAttachments.CITY_QUESTS, data);
        player.sendSystemMessage(Component.translatableWithFallback(
                "cityquest.eldritch_horror.advanced",
                "Contract %s: next — %s", city.name(), describe(city, chain.steps().get(next))));
    }

    /** @return whether the player has met {@code step}. */
    private static boolean satisfied(ServerPlayer player, City city, CityQuestChain.Step step,
                                     Map<String, Integer> data) {
        return switch (step.kind()) {
            case BOARD, TALK, SECRET -> {
                double dx = player.getX() - (step.x() + 0.5);
                double dz = player.getZ() - (step.z() + 0.5);
                yield dx * dx + dz * dz <= (double) REACH * REACH;
            }
            case GATHER -> carried(player, step.item()) >= step.count();
            case HUNT -> data.getOrDefault(KILLS + city.id(), 0) >= step.count();
        };
    }

    /** The number of {@code registryPath} items carried (namespace and display name both match). */
    public static int carried(ServerPlayer player, String registryPath) {
        String shortId = CityQuestChain.shortId(registryPath);
        int total = 0;
        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            if (CityQuestChain.shortId(id).equals(shortId)
                    || stack.getHoverName().getString().equalsIgnoreCase(registryPath)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    /** Pays the contract: relic coins and a little Order standing. */
    private static void reward(ServerPlayer player, City city) {
        Item coin = BuiltInRegistries.ITEM.getValue(EldritchHorror.id("relic_coin"));
        if (coin != null && coin != Items.AIR) {
            player.getInventory().add(new ItemStack(coin, 5));
        }
        CultSystem.add(player, "unblinking_eye", 5);
        // The city contract is also a node on the guided advancement tree (docs/ADVANCEMENTS.md).
        Advancements.awardCityContract(player, city);
        player.sendSystemMessage(Component.translatableWithFallback(
                "cityquest.eldritch_horror.completed",
                "Contract complete: %s. The Order pays five relic coins.", city.name()));
        EldritchHorror.LOGGER.info("city quest: {} completed the contract in {}",
                player.getGameProfile().name(), city.name());
    }

    /** The nearest curated city to {@code player} whose district (plus margin) contains them. */
    private static @Nullable City nearest(ServerLevel level, ServerPlayer player, int margin) {
        double x = player.getX();
        double z = player.getZ();
        City nearest = null;
        double best = Double.MAX_VALUE;
        for (City city : Cities.all()) {
            double radius = new CityLocation(city).radius() + margin;
            double dx = x - city.x();
            double dz = z - city.z();
            double distanceSq = dx * dx + dz * dz;
            if (distanceSq <= radius * radius && distanceSq < best) {
                best = distanceSq;
                nearest = city;
            }
        }
        return nearest;
    }

    /** Human-readable objective text with the coordinates the player needs. */
    public static Component describe(City city, CityQuestChain.Step step) {
        return switch (step.kind()) {
            case BOARD -> Component.translatableWithFallback(
                    "cityquest.eldritch_horror.board", "find the notice board in the %s (%s)",
                    place(step), direction(city, step));
            case TALK -> Component.translatableWithFallback(
                    "cityquest.eldritch_horror.talk", "ask for %s (%s)",
                    place(step), direction(city, step));
            case GATHER -> Component.translatableWithFallback(
                    "cityquest.eldritch_horror.gather", "gather %sx %s",
                    step.count(), itemName(step.item()));
            case HUNT -> Component.translatableWithFallback(
                    "cityquest.eldritch_horror.hunt", "clear %sx %s from the district",
                    step.count(), entityName(step.entity()));
            case SECRET -> Component.translatableWithFallback(
                    "cityquest.eldritch_horror.secret", "look for the hidden place at the %s (%s)",
                    place(step), direction(city, step));
        };
    }

    /** The place name, or the city centre when the plan has no anchor for the step. */
    private static Component place(CityQuestChain.Step step) {
        return Component.literal(step.place().isEmpty() ? "city centre" : step.place());
    }

    /**
     * An immersive direction: the compass bearing from the city centre and a rounded distance.
     * No raw coordinates in chat; the place name and the bearing match what a player can see in
     * the streets.
     */
    public static Component direction(City city, CityQuestChain.Step step) {
        int dx = step.x() - city.x();
        int dz = step.z() - city.z();
        double distance = Math.hypot(dx, dz);
        int rounded = Math.max(10, (int) (Math.round(distance / 10.0) * 10));
        return Component.translatableWithFallback("cityquest.eldritch_horror.direction",
                "%s, about %s blocks from the centre",
                Component.translatableWithFallback("cityquest.eldritch_horror.dir."
                        + compass(dx, dz), compass(dx, dz)), rounded);
    }

    /** Eight-way compass word for a delta, screen-north being -Z. */
    private static String compass(int dx, int dz) {
        double angle = Math.toDegrees(Math.atan2(dx, -dz));
        int sector = (int) Math.floorMod(Math.round(angle / 45.0), 8L);
        return switch (sector) {
            case 0 -> "north";
            case 1 -> "north_east";
            case 2 -> "east";
            case 3 -> "south_east";
            case 4 -> "south";
            case 5 -> "south_west";
            case 6 -> "west";
            default -> "north_west";
        };
    }

    /** The display name of an item registry path, falling back to the path itself. */
    public static Component itemName(String registryPath) {
        Item item = BuiltInRegistries.ITEM.getValue(EldritchHorror.id(
                CityQuestChain.shortId(registryPath)));
        return item == null || item == Items.AIR
                ? Component.literal(registryPath)
                : new ItemStack(item).getHoverName();
    }

    /** The display name of an entity registry path, falling back to the path itself. */
    public static Component entityName(String registryPath) {
        var type = BuiltInRegistries.ENTITY_TYPE.getValue(EldritchHorror.id(
                CityQuestChain.shortId(registryPath)));
        if (type == null) {
            return Component.literal(registryPath);
        }
        // A throwaway entity is not needed: the registry key is the authored name, so title-case it.
        String path = CityQuestChain.shortId(registryPath).replace('_', ' ');
        return Component.literal(Character.toUpperCase(path.charAt(0)) + path.substring(1));
    }

    /** One-line summary for {@code /eh cityquest}. */
    public static String summary(ServerPlayer player, City city) {
        CityQuestChain chain = chain(city);
        int index = step(player, city);
        if (done(player, city)) {
            return city.name() + ": contract complete";
        }
        if (index == 0 && !player.getData(ModAttachments.CITY_QUESTS)
                .containsKey(STEP + city.id())) {
            return city.name() + ": no contract yet (come closer to start it)";
        }
        StringBuilder sb = new StringBuilder(city.name()).append(": step ")
                .append(Math.min(index + 1, chain.steps().size())).append('/')
                .append(chain.steps().size()).append(" — ")
                .append(describe(city, chain.steps().get(Math.min(index, chain.steps().size() - 1)))
                        .getString());
        if (index < chain.steps().size() && chain.steps().get(index).kind()
                == CityQuestChain.Kind.HUNT) {
            sb.append(" (").append(player.getData(ModAttachments.CITY_QUESTS)
                    .getOrDefault(KILLS + city.id(), 0)).append('/')
                    .append(chain.steps().get(index).count()).append(')');
        }
        return sb.toString();
    }

    /** The nearest city within {@code margin} of the player's position, for the command. */
    public static @Nullable City nearestCity(ServerLevel level, ServerPlayer player, int margin) {
        return nearest(level, player, margin);
    }
}
