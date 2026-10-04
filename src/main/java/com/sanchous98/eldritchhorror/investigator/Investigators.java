package com.sanchous98.eldritchhorror.investigator;

import com.sanchous98.eldritchhorror.EldritchHorror;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * The one data table for the twelve investigators: {@link Investigator} → starting kit and starting
 * rites, from {@code design/14-classes.md}. The prologue, commands and any future
 * investigator-gated content read from here, so the roster is defined exactly once.
 *
 * <p>The mod items are resolved from {@link BuiltInRegistries#ITEM} by id (the same shape
 * {@code rite/RiteEngine} uses), because the item registry only exposes them as deferred holders
 * without per-item constants. This class is touched at gameplay time, never during registration. The
 * signature item {@code signature_charm} is included in every kit; its active behaviour is
 * dispatched by {@link InvestigatorAPI#get}.
 */
public final class Investigators {

    private Investigators() {
    }

    /** The twelve investigators in canonical presentation order. */
    public static List<Investigator> all() {
        return List.of(Investigator.values());
    }

    /** The investigator's occupation (flavour), from lang key {@code <id>.occupation}. */
    public static Component occupation(Investigator id) {
        return Component.translatable("investigator.eldritch_horror." + id.id() + ".occupation");
    }

    /** A one-line summary of the investigator's playstyle (lang key {@code <id>.desc}). */
    public static Component description(Investigator id) {
        return Component.translatableWithFallback(
                "investigator.eldritch_horror." + id.id() + ".desc",
                switch (id) {
                    case ELEANOR_VANCE -> "Reads the signs others miss; the mind holds longer than most.";
                    case JACK_CORRIGAN -> "Hard-eyed and hard to taint; tracks a rift like a suspect.";
                    case TOM_MALLORY -> "War already broke his nerves once; the dark can do no worse.";
                    case CORMAC_BLACKWOOD -> "Knows every dirty trick; corruption finds him easy, fear does not.";
                    case SISTER_AGATHA -> "Faith as a shield: corruption cannot easily settle on her.";
                    case MARION_DELACROIX -> "Hears what lies beyond; the dead are patient teachers.";
                    case VERA_NIGHTINGALE -> "Pays in blood and madness for power, and pays gladly.";
                    case NIKOLAI_VOLKOV -> "A fragile mind full of forbidden knowledge.";
                    case DR_AMOS_HARTLEY -> "Treats the mind as a wound; cities are his sanatorium.";
                    case EVELYN_ASHCOMBE -> "Money opens doors, and buys time when nothing else will.";
                    case ALDOUS_PEMBERTON -> "Follows ruins and old maps to the things best left buried.";
                    case HAZEL_QUINN -> "Turns the horror into a story, and stories into a shield.";
                });
    }

    /**
     * The investigator's starting kit, as fresh stacks, always including the signature item. An
     * unresolvable item yields an empty stack rather than throwing, so a bad id can never crash a
     * pedestal interaction.
     */
    public static List<ItemStack> starterKit(Investigator id) {
        return switch (id) {
            case ELEANOR_VANCE -> List.of(
                    stack("signature_charm", 1),
                    stack("watchers_diary", 1),
                    stack("wardstone_pendant", 1),
                    new ItemStack(Items.TORCH, 16),
                    new ItemStack(Items.BREAD, 8));
            case JACK_CORRIGAN -> List.of(
                    stack("signature_charm", 1),
                    stack("field_journal", 1),
                    stack("hand_lantern", 1),
                    new ItemStack(Items.TORCH, 16));
            case TOM_MALLORY -> List.of(
                    stack("signature_charm", 1),
                    stack("silver_blade", 1),
                    stack("iron_broth", 2),
                    new ItemStack(Items.BREAD, 8));
            case CORMAC_BLACKWOOD -> List.of(
                    stack("signature_charm", 1),
                    stack("sacrificial_dagger", 1),
                    stack("rope_coil", 1),
                    stack("black_obol", 4));
            case SISTER_AGATHA -> List.of(
                    stack("signature_charm", 1),
                    stack("holy_cross", 1),
                    stack("holy_water", 2),
                    stack("blessing_of_isis", 1));
            case MARION_DELACROIX -> List.of(
                    stack("signature_charm", 1),
                    stack("black_candle", 4),
                    stack("spirit_ash", 4),
                    stack("wardstone_pendant", 1));
            case VERA_NIGHTINGALE -> List.of(
                    stack("signature_charm", 1),
                    stack("star_codex", 1),
                    stack("void_reagent", 4),
                    stack("chalk_reagent", 8));
            case NIKOLAI_VOLKOV -> List.of(
                    stack("signature_charm", 1),
                    stack("tome_of_the_eye", 1),
                    stack("chalk_reagent", 8),
                    stack("star_reagent", 4));
            case DR_AMOS_HARTLEY -> List.of(
                    stack("signature_charm", 1),
                    stack("lucid_draught", 2),
                    stack("soothing_tea", 2),
                    stack("valerian_tonic", 2));
            case EVELYN_ASHCOMBE -> List.of(
                    stack("signature_charm", 1),
                    stack("relic_coin", 16),
                    stack("barter_seal", 1),
                    stack("blessed_water", 2));
            case ALDOUS_PEMBERTON -> List.of(
                    stack("signature_charm", 1),
                    stack("cartographers_lens", 1),
                    stack("city_map", 1),
                    stack("sextant", 1));
            case HAZEL_QUINN -> List.of(
                    stack("signature_charm", 1),
                    stack("field_journal", 1),
                    stack("compass_of_longing", 1),
                    new ItemStack(Items.TORCH, 16));
        };
    }

    /** The rite ids (known to {@code rite/Rites}) the investigator starts knowing. */
    public static List<String> startingRites(Investigator id) {
        return switch (id) {
            case ELEANOR_VANCE -> List.of("ward_of_the_eye");
            case JACK_CORRIGAN -> List.of("close_rift");
            case TOM_MALLORY -> List.of("ward_of_the_eye");
            case CORMAC_BLACKWOOD -> List.of("call_the_lesser");
            case SISTER_AGATHA -> List.of("rite_of_cleansing");
            case MARION_DELACROIX -> List.of("close_rift");
            // Occultist: one extra starting rite.
            case VERA_NIGHTINGALE -> List.of("drowned_blessing", "ward_of_the_eye");
            case NIKOLAI_VOLKOV -> List.of("ward_of_the_eye");
            case DR_AMOS_HARTLEY -> List.of("rite_of_cleansing");
            case EVELYN_ASHCOMBE -> List.of("ward_of_the_eye");
            case ALDOUS_PEMBERTON -> List.of("close_rift");
            case HAZEL_QUINN -> List.of("ward_of_the_eye");
        };
    }

    private static ItemStack stack(String path, int count) {
        Item item = BuiltInRegistries.ITEM.getValue(EldritchHorror.id(path));
        return item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item, count);
    }
}
