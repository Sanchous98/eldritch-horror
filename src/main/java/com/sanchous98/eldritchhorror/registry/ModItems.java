package com.sanchous98.eldritchhorror.registry;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.registry.items.Artifacts;
import com.sanchous98.eldritchhorror.registry.items.Conditions;
import com.sanchous98.eldritchhorror.registry.items.ConsumableItem;
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
 * <p>Most items are <b>content stubs</b> (exist, stack, appear in the creative tab); consumables
 * and tomes are wired to the sanity/corruption systems via the {@link #add(String, int, double,
 * double)} overload. See {@code design/16-items.md} and {@code design/27-systems-framework.md}.
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

    /**
     * Registers a consumed {@link ConsumableItem} (fixed sanity/corruption deltas on use) and
     * records it in {@link #ALL}.
     *
     * <p>This overload is deliberately also named {@code add}: {@code tools/gen_item_assets.py}
     * discovers item ids with the regex {@code ModItems\.add\("([a-z_]+)"}, which matches this call
     * shape unchanged, so these items keep getting models/textures/item definitions with no change
     * to the (frozen) generator.
     *
     * <p>Consumables apply their deltas <b>server-side only</b> via {@code SanityAPI}/
     * {@code CorruptionAPI} and shrink the stack by one per use (creative excepted).
     *
     * @param id         the item path (snake_case), namespaced under the mod id
     * @param maxStack   the max stack size
     * @param sanity     the sanity delta applied on use (may be negative)
     * @param corruption the corruption delta applied on use (may be negative)
     */
    public static DeferredItem<ConsumableItem> add(String id, int maxStack,
                                                   double sanity, double corruption) {
        return add(id, maxStack, sanity, corruption, true);
    }

    /**
     * Registers a {@link ConsumableItem} with explicit consumption: {@code consumed=false} is for
     * knowledge items (tomes) that pay a cost but persist. Also named {@code add} so the asset
     * generator's regex still discovers the id.
     *
     * @param consumed whether using the item removes one from the stack
     */
    public static DeferredItem<ConsumableItem> add(String id, int maxStack, double sanity,
                                                   double corruption, boolean consumed) {
        DeferredItem<ConsumableItem> item = ITEMS.registerItem(id,
                p -> new ConsumableItem(p.stacksTo(maxStack), sanity, corruption, consumed));
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
