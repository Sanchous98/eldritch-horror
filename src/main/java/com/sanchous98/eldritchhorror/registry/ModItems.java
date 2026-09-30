package com.sanchous98.eldritchhorror.registry;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.registry.items.Artifacts;
import com.sanchous98.eldritchhorror.registry.items.Conditions;
import com.sanchous98.eldritchhorror.registry.items.Consumables;
import com.sanchous98.eldritchhorror.registry.items.Currency;
import com.sanchous98.eldritchhorror.registry.items.Factions;
import com.sanchous98.eldritchhorror.registry.items.Keys;
import com.sanchous98.eldritchhorror.registry.items.Navigation;
import com.sanchous98.eldritchhorror.registry.items.Reagents;
import com.sanchous98.eldritchhorror.registry.items.Tomes;
import com.sanchous98.eldritchhorror.registry.items.Utility;
import com.sanchous98.eldritchhorror.registry.items.Weapons;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

/**
 * Item registry — the single shared surface every item category registers through.
 *
 * <p>Categories live one file per group under {@code registry/items/} and each item calls
 * {@link #add(String, int)}. This keeps the registration pattern in one place (so many category
 * files can be authored in parallel) and collects everything into {@link #ALL} for the creative
 * tab.
 *
 * <p>These items are <b>content stubs</b>: they exist, stack and appear in the creative tab, and
 * their intended sanity/corruption effects are recorded in {@code design/16-items.md} and the
 * item's own javadoc — but no behaviour is wired yet (see {@code design/27-systems-framework.md}).
 */
public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(EldritchHorror.MODID);
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, EldritchHorror.MODID);

    /** Every registered item, in category-init order; drives the creative tab. */
    public static final List<DeferredItem<?>> ALL = new ArrayList<>();

    /**
     * Registers one plain item and records it in {@link #ALL}. The canonical helper every category
     * file uses, so the pattern is uniform and parallel-safe.
     *
     * @param id       the item path (snake_case), namespaced under the mod id
     * @param maxStack the max stack size (1 for tomes/artefacts/gear, 16 for reagents, 64 for bulk currency)
     */
    public static DeferredItem<Item> add(String id, int maxStack) {
        DeferredItem<Item> item = ITEMS.registerSimpleItem(id, p -> p.stacksTo(maxStack));
        ALL.add(item);
        return item;
    }

    /** Forces every category class to initialise and register its items. Called at mod construction. */
    public static void registerCategories() {
        Reagents.init();
        Tomes.init();
        Artifacts.init();
        Weapons.init();
        Consumables.init();
        Currency.init();
        Conditions.init();
        Navigation.init();
        Factions.init();
        Keys.init();
        Utility.init();
    }

    /** The mod's creative tab, listing every item in {@link #ALL}. */
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB =
            TABS.register("main", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.eldritch_horror"))
                    .icon(() -> ALL.isEmpty() ? ItemStack.EMPTY : new ItemStack(ALL.get(0).get()))
                    .displayItems((params, output) -> {
                        for (DeferredItem<?> item : ALL) {
                            output.accept(item.get());
                        }
                    })
                    .build());

    private ModItems() {
    }
}
