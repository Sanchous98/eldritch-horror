package com.sanchous98.eldritchhorror.rite;

import com.sanchous98.eldritchhorror.EldritchHorror;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * The offerings a rite consumes, and the code that checks and removes them.
 *
 * <p>From {@code design/08-rituals.md}'s Offerings column for the eight seed rites; the Ancient One
 * solve rites (design/28) share a single modest default, because their gate is the rite-knowledge
 * tome rather than a new material. The design's {@code fish_offering} and {@code prayer_bead} are
 * not shipped items, so their slots use the nearest shipped reagent (the pearl; salt) — we invent no
 * new item for a single rite. All-or-nothing, per {@code design/05-ritual-engine.md}: the engine
 * checks every line is present before the rite starts and removes them only on success.
 *
 * <p>Keys are the rite ids themselves (no change to the frozen {@link RiteDefinition}), so a new
 * rite only needs a row here or it falls to {@link #DEFAULT}.
 */
public final class RiteReagents {

    /** One offering line: {@code count} of an item, by registry path. */
    public record Reagent(String itemPath, int count) {

        /** @return the resolved item (AIR when unregistered — this registry is defaulted, not null). */
        public Item item() {
            return BuiltInRegistries.ITEM.getValue(EldritchHorror.id(this.itemPath));
        }

        /** @return a stack of the resolved item (empty when unregistered), for display and matching. */
        public ItemStack stack() {
            return new ItemStack(item(), this.count);
        }
    }

    /**
     * Offerings per rite id, from the design tables. Missing ids fall back to {@link #DEFAULT}.
     * The solve rites are intentionally absent: they all take the default.
     */
    private static final Map<String, List<Reagent>> OFFERINGS = Map.of(
            "ward_of_the_eye", List.of(new Reagent("salt_reagent", 4), new Reagent("chalk_reagent", 1)),
            "drowned_blessing", List.of(new Reagent("pearl_reagent", 2)),
            "call_the_lesser", List.of(new Reagent("bone_reagent", 4), new Reagent("blood_offering", 1)),
            "summon_star_spawn", List.of(new Reagent("star_reagent", 8), new Reagent("blood_offering", 2)),
            "open_rift", List.of(new Reagent("void_reagent", 6), new Reagent("blood_offering", 4)),
            "close_rift", List.of(new Reagent("salt_reagent", 8), new Reagent("chalk_reagent", 2)),
            "rite_of_cleansing", List.of(new Reagent("silver_reagent", 4)),
            "respec", List.of(new Reagent("mirror_shard", 4), new Reagent("silver_reagent", 4)));

    /** The default for every rite without an explicit row: the Ancient One solves. */
    private static final List<Reagent> DEFAULT = List.of(new Reagent("salt_reagent", 2));

    private RiteReagents() {
    }

    /** @return the offerings {@code rite} consumes (never empty; falls back to {@link #DEFAULT}). */
    public static List<Reagent> offerings(RiteDefinition rite) {
        return OFFERINGS.getOrDefault(rite.id(), DEFAULT);
    }

    /** @return a human-readable "4x Consecrated Salt, 1x Ritual Chalk" list for the altar prompt. */
    public static Component describe(List<Reagent> reagents) {
        MutableComponent out = Component.empty();
        for (int i = 0; i < reagents.size(); i++) {
            Reagent reagent = reagents.get(i);
            if (i > 0) {
                out.append(Component.literal(", "));
            }
            out.append(Component.literal(reagent.count() + "x ").append(reagent.stack().getHoverName()));
        }
        return out;
    }

    /** @return whether {@code player} carries every line in {@code reagents}. */
    public static boolean has(ServerPlayer player, List<Reagent> reagents) {
        for (Reagent reagent : reagents) {
            ItemStack prototype = reagent.stack();
            if (prototype.isEmpty()) {
                return false; // unregistered item id: treat as missing rather than silently free
            }
            int carried = player.getInventory().clearOrCountMatchingItems(
                    stack -> ItemStack.isSameItemSameComponents(stack, prototype), true, 0,
                    player.inventoryMenu.getCraftSlots());
            if (carried < reagent.count()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Removes every line in {@code reagents}, all-or-nothing. Call only after {@link #has} returned
     * {@code true} (the engine does); this still re-checks per line so a concurrent change cannot
     * leave the player short.
     *
     * @return whether the full set was removed
     */
    public static boolean consume(ServerPlayer player, List<Reagent> reagents) {
        if (!has(player, reagents)) {
            return false;
        }
        for (Reagent reagent : reagents) {
            ItemStack prototype = reagent.stack();
            player.getInventory().clearOrCountMatchingItems(
                    stack -> ItemStack.isSameItemSameComponents(stack, prototype), false, reagent.count(),
                    player.inventoryMenu.getCraftSlots());
        }
        player.containerMenu.broadcastChanges();
        player.inventoryMenu.slotsChanged(player.getInventory());
        return true;
    }
}
