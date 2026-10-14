package com.sanchous98.eldritchhorror.bot;

import com.mojang.authlib.GameProfile;
import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.world.city.Cities;
import com.sanchous98.eldritchhorror.world.city.City;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jspecify.annotations.Nullable;

/**
 * Registry and ticker for {@link BotPlayer}s: headless agents the mod's per-player systems see as
 * ordinary online players.
 *
 * <p>A bot is given an offline {@link UUID} (the same scheme the server uses without
 * authentication), registered in both the level's entity list and
 * {@link PlayerList#getPlayers()} (via {@link PlayerList#getPlayersByUUID()} and the private
 * {@code players} list), and advanced from {@link ServerTickEvent.Post}. It is intentionally
 * <b>not</b> registered with the server's connection listener, so real clients are unaffected; the
 * tick handler drives it instead.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class Bots {

    private static final Map<UUID, BotPlayer> BOTS = new ConcurrentHashMap<>();

    private Bots() {
    }

    /**
     * Spawns a bot at the level's respawn point. Returns the existing bot when one with that name is
     * already tracked, so a repeated {@code /eh bot spawn} is idempotent.
     */
    public static BotPlayer spawn(ServerLevel level, String name) {
        UUID uuid = uuidFor(name);
        BotPlayer existing = BOTS.get(uuid);
        if (existing != null) {
            return existing;
        }
        MinecraftServer server = level.getServer();
        GameProfile profile = new GameProfile(uuid, name);
        BotPlayer bot = new BotPlayer(server, level, profile);
        // Spawn on a curated city's street ring (the world spawn is open ocean, where a bot would
        // drown); fall back to the respawn point if no city is registered.
        City city = Cities.all().stream().findFirst().orElse(null);
        BlockPos spawn = city == null
                ? level.getRespawnData().globalPos().pos()
                : new BlockPos(city.x() + 48, 0, city.z());
        // Force the spawn chunk so the heightmap reflects generated terrain (an unloaded chunk
        // reports min build height and would drop the bot into the bedrock).
        level.getChunk(spawn.getX() >> 4, spawn.getZ() >> 4);
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawn.getX(), spawn.getZ());
        bot.setPos(spawn.getX() + 0.5, y, spawn.getZ() + 0.5);

        PlayerList playerList = server.getPlayerList();
        playerList.getPlayersByUUID().put(uuid, bot);
        addToPlayersList(playerList, bot);
        level.addNewPlayer(bot);
        giveStarterKit(bot);
        BOTS.put(uuid, bot);
        EldritchHorror.LOGGER.info("bot: spawned {} ({}) at {}, {}, {} in {}",
                name, uuid, bot.getX(), bot.getY(), bot.getZ(), level.dimension().identifier());
        return bot;
    }

    /** Removes a bot by name. Returns {@code true} when one was tracked. */
    public static boolean remove(MinecraftServer server, String name) {
        BotPlayer bot = BOTS.remove(uuidFor(name));
        if (bot == null) {
            return false;
        }
        // Fires PlayerLoggedOutEvent and removes the bot from the list, uuid map and level.
        server.getPlayerList().remove(bot);
        EldritchHorror.LOGGER.info("bot: removed {}", name);
        return true;
    }

    /** The tracked bot with this name, or {@code null}. */
    public static @Nullable BotPlayer get(String name) {
        return BOTS.get(uuidFor(name));
    }

    /** Snapshot of every tracked bot, for iteration without holding a live view. */
    public static List<BotPlayer> all() {
        return List.copyOf(BOTS.values());
    }

    /** Advances every bot like a real player connection would, once per server tick. */
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (BOTS.isEmpty()) {
            return;
        }
        for (BotPlayer bot : BOTS.values()) {
            bot.tick();
            bot.doTick();
        }
    }

    /** Bots do not survive a world unload, so drop the registry when the server stops. */
    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        BOTS.clear();
    }

    /** A simple kit so a bot survives long enough to exercise the world (the mod is very hostile). */
    private static void giveStarterKit(BotPlayer bot) {
        bot.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        bot.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
        bot.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.IRON_LEGGINGS));
        bot.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS));
        bot.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        bot.setHealth(bot.getMaxHealth());
        // Ritual kit: enough reagents to build an altar and run the quest rites without grinding
        // rare hostile mobs for bone. (Altar rune stones need chalk; the offerings need salt.)
        reagent(bot, "chalk_reagent", 48);
        reagent(bot, "salt_reagent", 32);
        reagent(bot, "tome_of_the_eye", 1);      // ward_of_the_eye
        reagent(bot, "codex_of_wards", 1);       // close_rift
        reagent(bot, "atlas_of_the_veil", 1);    // open_rift
        reagent(bot, "tome_of_tides", 1);        // soothe_cthulhu
        reagent(bot, "void_reagent", 8);         // open_rift offering
        reagent(bot, "blood_offering", 6);       // open_rift offering
        reagent(bot, "offering_bowl", 4);        // altar components: a rebuild never stalls on a bowl
        // A little stone so the altar can be built even when the spawn area has none to mine.
        bot.getInventory().add(new ItemStack(Items.STONE, 48));
    }

    /** Adds {@code count} of the named mod reagent to a bot's inventory. */
    private static void reagent(BotPlayer bot, String id, int count) {
        Item item = BuiltInRegistries.ITEM.getValue(EldritchHorror.id(id));
        if (item != null && item != Items.AIR) {
            bot.getInventory().add(new ItemStack(item, count));
        }
    }

    private static UUID uuidFor(String name) {        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Appends the bot to the player list. {@link PlayerList#getPlayers()} hands back an unmodifiable
     * view of the private {@code players} list, which is the only collection without a mutator, so
     * it is reached reflectively. The uuid map is public and is updated by the caller.
     */
    @SuppressWarnings("unchecked")
    private static void addToPlayersList(PlayerList playerList, ServerPlayer bot) {
        try {
            Field players = PlayerList.class.getDeclaredField("players");
            players.setAccessible(true);
            ((List<ServerPlayer>) players.get(playerList)).add(bot);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to register bot in the player list", e);
        }
    }
}
