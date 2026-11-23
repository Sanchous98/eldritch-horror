package com.sanchous98.eldritchhorror.sanity;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.codex.CodexEntry;
import com.sanchous98.eldritchhorror.codex.CodexRegistry;
import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.world.loc.city.CityLocation;
import com.sanchous98.eldritchhorror.world.loc.Location;
import com.sanchous98.eldritchhorror.world.loc.Locations;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jspecify.annotations.Nullable;

/**
 * The <b>Sixth Sense</b>: at zero sanity the whispers stop being only a threat. The buff gives
 * night vision and periodically slips something into the player's pack — usually junk, sometimes
 * something useful, and occasionally a note with the coordinates of the nearest site where an
 * artefact can be found. That is the ambiguity: madness costs Stupor and phantoms but also offers
 * information and supplies.
 *
 * <p>The first gift fires when the effect is applied, then every
 * {@code sixthSenseGiftTicks} (jittered). Gifts go to the inventory; a full inventory drops them at
 * the player's feet. All randomness is the player's own {@code RandomSource}.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class SixthSense {

    private static final Map<UUID, Long> NEXT = new ConcurrentHashMap<>();

    private static final Item[] JUNK = {
            Items.ROTTEN_FLESH, Items.BONE, Items.GRAVEL, Items.DIRT, Items.STICK, Items.COAL,
            Items.STRING, Items.FLINT, Items.CLAY_BALL, Items.POISONOUS_POTATO, Items.INK_SAC
    };

    private SixthSense() {
    }

    /** One tick of the effect: deliver the next gift when due. */
    public static void tick(ServerPlayer player) {
        if (!ModConfig.ENABLE_SIXTH_SENSE.get()) {
            return;
        }
        long now = player.level().getGameTime();
        Long next = NEXT.get(player.getUUID());
        if (next == null) {
            NEXT.put(player.getUUID(), now + interval(player));
            return;
        }
        if (now < next) {
            return;
        }
        NEXT.put(player.getUUID(), now + interval(player));
        grant(player);
    }

    /** Delivers one whisper gift. Called on effect start and periodically. */
    public static void grant(ServerPlayer player) {
        if (!ModConfig.ENABLE_SIXTH_SENSE.get()) {
            return;
        }
        double roll = player.getRandom().nextDouble();
        double clue = ModConfig.SIXTH_SENSE_CLUE_CHANCE.get();
        double useful = ModConfig.SIXTH_SENSE_USEFUL_CHANCE.get();
        if (roll < clue) {
            giftClue(player);
        } else if (roll < clue + useful) {
            giftUseful(player);
        } else {
            giftJunk(player);
        }
    }

    private static void giftJunk(ServerPlayer player) {
        Item item = JUNK[player.getRandom().nextInt(JUNK.length)];
        ItemStack stack = new ItemStack(item, 1 + player.getRandom().nextInt(3));
        give(player, stack, "sixth_sense.gift.junk");
    }

    private static void giftUseful(ServerPlayer player) {
        ItemStack stack;
        int pick = player.getRandom().nextInt(5);
        switch (pick) {
            case 0 -> stack = new ItemStack(Items.TORCH, 4 + player.getRandom().nextInt(5));
            case 1 -> stack = new ItemStack(Items.ARROW, 8 + player.getRandom().nextInt(9));
            case 2 -> stack = new ItemStack(Items.BREAD, 2 + player.getRandom().nextInt(3));
            case 3 -> stack = new ItemStack(Items.IRON_INGOT, 1 + player.getRandom().nextInt(2));
            default -> {
                Item obol = net.minecraft.core.registries.BuiltInRegistries.ITEM
                        .getValue(EldritchHorror.id("black_obol"));
                stack = new ItemStack(obol == null ? Items.GOLD_NUGGET : obol,
                        1 + player.getRandom().nextInt(3));
            }
        }
        give(player, stack, "sixth_sense.gift.useful");
    }

    private static void giftClue(ServerPlayer player) {
        Location site = nearestSite(player);
        if (site == null) {
            giftUseful(player);
            return;
        }
        int x = Locations.xOf(site);
        int z = Locations.zOf(site);
        ItemStack note = new ItemStack(Items.PAPER);
        note.set(DataComponents.CUSTOM_NAME, Component.translatable("sixth_sense.note.name"));
        note.set(DataComponents.LORE, new ItemLore(List.of(
                Component.translatable("sixth_sense.note.site", siteName(site)),
                Component.literal(String.format(Locale.ROOT, "x %d   z %d", x, z)),
                Component.translatable("sixth_sense.note.hint"))));
        give(player, note, "sixth_sense.gift.clue");
    }

    private static void give(ServerPlayer player, ItemStack stack, String messageKey) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false, Prediction.SERVER_ONLY);
        }
        player.sendSystemMessage(Component.translatable(messageKey, stack.getHoverName()));
    }

    private static long interval(ServerPlayer player) {
        long base = Math.max(20L, ModConfig.SIXTH_SENSE_GIFT_TICKS.get());
        return base / 2L + player.getRandom().nextInt((int) base);
    }

    /** @return the nearest non-city site within the clue radius, or a random one, or {@code null}. */
    private static @Nullable Location nearestSite(ServerPlayer player) {
        double radius = ModConfig.SIXTH_SENSE_CLUE_RADIUS.get();
        Location best = null;
        double bestDistance = Double.MAX_VALUE;
        for (Location site : Locations.all()) {
            if (site instanceof CityLocation) {
                continue;
            }
            double dx = player.getX() - Locations.xOf(site);
            double dz = player.getZ() - Locations.zOf(site);
            double distance = dx * dx + dz * dz;
            if (distance < bestDistance && distance <= radius * radius) {
                bestDistance = distance;
                best = site;
            }
        }
        if (best != null) {
            return best;
        }
        List<Location> sites = Locations.all().stream()
                .filter(site -> !(site instanceof CityLocation))
                .toList();
        return sites.isEmpty() ? null : sites.get(player.getRandom().nextInt(sites.size()));
    }

    private static Component siteName(Location site) {
        CodexEntry entry = CodexRegistry.byId(site.id());
        return entry != null ? entry.name() : Component.literal(site.id());
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        NEXT.remove(event.getEntity().getUUID());
    }
}
