package com.sanchous98.eldritchhorror.world.city.quest;

import com.sanchous98.eldritchhorror.world.city.City;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.level.Level;
import net.minecraft.util.Prediction;

/**
 * The single compass that carries a player's city contract.
 *
 * <p>It is an ordinary {@link Items#COMPASS} with a {@link DataComponents#LODESTONE_TRACKER} whose
 * target is the current objective, so the vanilla needle keeps pointing the right way while the
 * tooltip says where to go in words. The item is identified by the {@code eh_contract_compass=1}
 * marker in its {@link DataComponents#CUSTOM_DATA} — never by its display name, which changes with
 * every step — so a contract update replaces the old compass instead of filling the inventory.
 *
 * <p>The tracker is deliberately created with {@code tracked = false}: vanilla's
 * {@code CompassItem.inventoryTick} only re-validates {@code tracked} lenses, and the objective is
 * a place in the world, not a lodestone block. With {@code tracked = true} the server would clear
 * the target on the next tick because no lodestone POI exists there, and the needle would silently
 * stop working. The client still reads {@code target} for a non-tracked lens.
 */
public final class ContractCompass {

    /** Marker key that identifies our compass in {@code CUSTOM_DATA}. */
    private static final String MARKER = "eh_contract_compass";

    /** The city whose contract the compass currently tracks (for re-use across cities). */
    private static final String CITY = "eh_contract_city";

    private ContractCompass() {
    }

    /** Gives the player the contract compass for {@code step}, or retargets the one they carry. */
    public static void update(ServerPlayer player, City city, CityQuestChain.Step step,
                              ServerLevel level) {
        ItemStack stack = build(player, city, step, level);
        int slot = find(player);
        if (slot >= 0) {
            player.getInventory().setItem(slot, stack);
            return;
        }
        insert(player, stack);
    }

    /**
     * Ensures a tracked player still has the compass (re-join, death, a fresh contract) without
     * rewriting a compass that is already there.
     */
    public static void ensure(ServerPlayer player, City city, CityQuestChain.Step step,
                              ServerLevel level) {
        if (find(player) < 0) {
            insert(player, build(player, city, step, level));
        }
    }

    /** Removes the contract compass from the player's inventory on completion. */
    public static void remove(ServerPlayer player) {
        int slot = find(player);
        if (slot >= 0) {
            player.getInventory().setItem(slot, ItemStack.EMPTY);
        }
    }

    /** Whether {@code stack} is our contract compass (marker in {@code CUSTOM_DATA}). */
    public static boolean isContractCompass(ItemStack stack) {
        if (!stack.has(DataComponents.CUSTOM_DATA)) {
            return false;
        }
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                .copyTag();
        return tag.getIntOr(MARKER, 0) == 1;
    }

    /** The compass stack: name, lore, plain look, lodestone-style needle and the marker. */
    private static ItemStack build(ServerPlayer player, City city, CityQuestChain.Step step,
                                   ServerLevel level) {
        ItemStack stack = new ItemStack(Items.COMPASS);
        stack.set(DataComponents.CUSTOM_NAME, Component.translatable(
                "cityquest.eldritch_horror.compass.name", CityQuests.place(step))
                .withStyle(ChatFormatting.GOLD));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
                CityQuests.objective(city, step),
                CityQuests.bearing(player, step))));
        // A lodestone compass is always foiled in this version; the contract one stays plain.
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, false);
        BlockPos target = new BlockPos(step.x(), CityQuests.objectiveY(level, step), step.z());
        stack.set(DataComponents.LODESTONE_TRACKER, new LodestoneTracker(
                Optional.of(GlobalPos.of(Level.OVERWORLD, target)), false));
        CompoundTag tag = new CompoundTag();
        tag.putInt(MARKER, 1);
        tag.putString(CITY, city.id());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }

    /** The inventory slot of the marked compass, or {@code -1}. */
    private static int find(ServerPlayer player) {
        var inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (isContractCompass(inventory.getItem(i))) {
                return i;
            }
        }
        return -1;
    }

    /** First free hotbar slot, else the first free slot, else the offhand, else drop it. */
    private static void insert(ServerPlayer player, ItemStack stack) {
        var inventory = player.getInventory();
        for (int i = 0; i < 9; i++) {
            if (inventory.getItem(i).isEmpty()) {
                inventory.setItem(i, stack);
                return;
            }
        }
        int free = inventory.getFreeSlot();
        if (free >= 0) {
            inventory.setItem(free, stack);
            return;
        }
        if (inventory.getItem(40).isEmpty()) {
            inventory.setItem(40, stack);
            return;
        }
        // A full inventory must not eat the compass: drop it at the player's feet instead.
        player.drop(stack, false, Prediction.SERVER_ONLY);
    }
}
