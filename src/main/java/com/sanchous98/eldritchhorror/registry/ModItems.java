package com.sanchous98.eldritchhorror.registry;

import com.sanchous98.eldritchhorror.EldritchHorror;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Item registry. Expected first items: sanity restoratives, ritual components (reagents,
 * tomes), and the cult-signature tools.
 */
public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(EldritchHorror.MODID);

    // Placeholder reagent.
    public static final DeferredItem<Item> FORBIDDEN_REAGENT =
            ITEMS.registerSimpleItem("forbidden_reagent", new Item.Properties().stacksTo(16));

    private ModItems() {
    }
}
