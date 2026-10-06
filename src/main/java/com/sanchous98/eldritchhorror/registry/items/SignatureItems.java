package com.sanchous98.eldritchhorror.registry.items;

import com.sanchous98.eldritchhorror.investigator.Investigator;
import com.sanchous98.eldritchhorror.registry.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * The twelve per-investigator signature items (design/14-classes.md). Each is a
 * {@link SignatureItem} bound to its owner, so the active is dispatched by the item's own identity
 * rather than by a shared id looked up against the holder. Registered through
 * {@link ModItems#addItem} with a literal id, so {@code tools/gen_item_assets.py} (which reads the
 * {@code ModItems.addItem("…")} call shape) generates their models/textures.
 *
 * <p>All are stack-size 1 and given in the matching investigator's starting kit
 * ({@code Investigators.starterKit}). The registration calls below are the single source of truth;
 * {@link #signatureId(Investigator)} resolves the id back from the registered item, so an id can
 * never drift from its owner.
 */
public final class SignatureItems {

    private SignatureItems() {
    }

    /** Registers the twelve signature items, each bound to its investigator for active dispatch. */
    public static void init() {
        ModItems.addItem("seers_lens", p -> new SignatureItem(p.stacksTo(1), Investigator.ELEANOR_VANCE), 1);
        ModItems.addItem("investigators_badge", p -> new SignatureItem(p.stacksTo(1), Investigator.JACK_CORRIGAN), 1);
        ModItems.addItem("dog_tags", p -> new SignatureItem(p.stacksTo(1), Investigator.TOM_MALLORY), 1);
        ModItems.addItem("smugglers_token", p -> new SignatureItem(p.stacksTo(1), Investigator.CORMAC_BLACKWOOD), 1);
        ModItems.addItem("rosary", p -> new SignatureItem(p.stacksTo(1), Investigator.SISTER_AGATHA), 1);
        ModItems.addItem("planchette", p -> new SignatureItem(p.stacksTo(1), Investigator.MARION_DELACROIX), 1);
        ModItems.addItem("blood_chalice", p -> new SignatureItem(p.stacksTo(1), Investigator.VERA_NIGHTINGALE), 1);
        ModItems.addItem("forbidden_grimoire", p -> new SignatureItem(p.stacksTo(1), Investigator.NIKOLAI_VOLKOV), 1);
        ModItems.addItem("sedative_vial", p -> new SignatureItem(p.stacksTo(1), Investigator.DR_AMOS_HARTLEY), 1);
        ModItems.addItem("family_signet", p -> new SignatureItem(p.stacksTo(1), Investigator.EVELYN_ASHCOMBE), 1);
        ModItems.addItem("antiquarians_compass", p -> new SignatureItem(p.stacksTo(1), Investigator.ALDOUS_PEMBERTON), 1);
        ModItems.addItem("press_pass", p -> new SignatureItem(p.stacksTo(1), Investigator.HAZEL_QUINN), 1);
    }

    /** @return the signature item id for {@code id} (used by its starting kit). */
    public static String signatureId(Investigator id) {
        for (DeferredItem<?> holder : ModItems.ALL) {
            if (holder.get() instanceof SignatureItem signature && signature.investigator() == id) {
                Identifier key = BuiltInRegistries.ITEM.getKey(signature);
                if (key != null) {
                    return key.getPath();
                }
            }
        }
        throw new IllegalStateException("No signature item registered for " + id);
    }
}
