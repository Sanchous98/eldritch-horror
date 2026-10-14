package com.sanchous98.eldritchhorror.registry.items;

import com.sanchous98.eldritchhorror.EldritchHorror;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * The mod's "carried" test for artefact passives.
 *
 * <p>There is no trinket slot: an artefact counts as carried when it sits anywhere in the player's
 * inventory — main slots, hotbar, offhand and worn armour (the {@code Inventory} iterator walks
 * the whole container). The tooltips say so where a passive depends on it.
 */
public final class CarriedItems {

    private CarriedItems() {
    }

    /**
     * @param player   the player to search
     * @param itemPath the item's registry path (namespaced under this mod)
     * @return whether {@code player} carries at least one such item anywhere in the inventory
     */
    public static boolean carries(Player player, String itemPath) {
        Item item = BuiltInRegistries.ITEM.getValue(EldritchHorror.id(itemPath));
        if (item == Items.AIR) {
            return false;
        }
        for (ItemStack stack : player.getInventory()) {
            if (stack.getItem() == item) {
                return true;
            }
        }
        return false;
    }
}
