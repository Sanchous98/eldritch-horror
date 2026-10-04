package com.sanchous98.eldritchhorror.classes;

import com.sanchous98.eldritchhorror.EldritchHorror;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * The one data table for class archetypes: {@link ClassId} → starting kit and starting rites, from
 * {@code design/14-classes.md} and {@code design/29-prologue.md}. The prologue, commands and any
 * future class-gated content read from here, so the archetypes are defined exactly once.
 *
 * <p>The mod items are resolved from {@link BuiltInRegistries#ITEM} by id (the same shape
 * {@code rite/RiteEngine} uses), because the item registry only exposes them as deferred holders
 * without per-item constants. This class is touched at gameplay time, never during registration.
 */
public final class Classes {

    private Classes() {
    }

    /** The three archetypes in canonical presentation order. */
    public static List<ClassId> all() {
        return List.of(ClassId.values());
    }

    /**
     * The archetype's starting kit, as fresh stacks. An unresolvable item yields an empty stack
     * rather than throwing, so a bad id can never crash a pedestal interaction.
     */
    public static List<ItemStack> starterKit(ClassId id) {
        return switch (id) {
            case INVESTIGATOR -> List.of(
                    stack("watchers_diary", 1),
                    stack("wardstone_pendant", 1),
                    new ItemStack(Items.TORCH, 16),
                    new ItemStack(Items.BREAD, 8));
            case OCCULTIST -> List.of(
                    stack("star_codex", 1),
                    stack("void_reagent", 4),
                    stack("chalk_reagent", 8));
            case CULTIST -> List.of(
                    stack("bone_ledger", 1),
                    stack("blood_offering", 4),
                    stack("bone_reagent", 8));
        };
    }

    /** The rite ids (known to {@code rite/Rites}) the archetype starts knowing. */
    public static List<String> startingRites(ClassId id) {
        return switch (id) {
            case INVESTIGATOR -> List.of("ward_of_the_eye");
            case OCCULTIST -> List.of("drowned_blessing");
            case CULTIST -> List.of("call_the_lesser");
        };
    }

    /** A one-line summary of the archetype's playstyle (design/14-classes.md). */
    public static Component description(ClassId id) {
        return Component.translatableWithFallback(
                "class.eldritch_horror." + id.id() + ".desc",
                switch (id) {
                    case INVESTIGATOR -> "Sanity-resilient: beats the occult with tools and wards, not corruption.";
                    case OCCULTIST -> "The scholar: learns rites faster and reads tomes with less backlash.";
                    case CULTIST -> "Embraces corruption: strongest rituals and fastest reputation, fragile mind.";
                });
    }

    private static ItemStack stack(String path, int count) {
        Item item = BuiltInRegistries.ITEM.getValue(EldritchHorror.id(path));
        return item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item, count);
    }
}
