package com.sanchous98.eldritchhorror.registry.items;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.registry.ModDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

/**
 * The stack half of the weapon quality system ({@code design/16-items.md}): read/write the
 * {@link ModDataComponents#WEAPON_QUALITY} component, mirror it onto the vanilla {@code RARITY}
 * component (the name colour) and rescale durability while preserving the current wear.
 *
 * <p>Only graded weapons (see {@link WeaponGrade#byItem()}) that have durability accept quality; a
 * whetstone used on anything else — the throwables in particular, which are graded but stack — does
 * nothing.
 */
public final class WeaponQualityStacks {

    private WeaponQualityStacks() {
    }

    /** The stack's quality, {@code 0} when the component is absent. */
    public static int quality(ItemStack stack) {
        return WeaponQuality.clamp(stack.getOrDefault(ModDataComponents.WEAPON_QUALITY.get(), 0));
    }

    /**
     * Whether this stack is one of the mod's graded weapons (the only valid tempering target).
     * Graded gear without durability — the throwables — is graded for its tooltip only: quality
     * rescales max durability, so a non-damageable stack has nothing to gain and would only lose
     * its stackability.
     */
    public static boolean isGradedWeapon(ItemStack stack) {
        if (!stack.isDamageableItem()) {
            return false;
        }
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id != null && EldritchHorror.MODID.equals(id.getNamespace())
                && WeaponGrade.byItem().containsKey(id.getPath());
    }

    /** Stores the quality, updates the name colour and rescales durability from the item's base. */
    public static void apply(ItemStack stack, int quality) {
        int level = WeaponQuality.clamp(quality);
        stack.set(ModDataComponents.WEAPON_QUALITY.get(), level);
        stack.set(DataComponents.RARITY, rarity(level));
        rescaleDurability(stack, level);
    }

    /** The quality line's colour, matching the name colour buckets. */
    public static Rarity rarity(int quality) {
        return switch (WeaponQuality.rarityBucket(quality)) {
            case 0 -> Rarity.COMMON;
            case 1 -> Rarity.UNCOMMON;
            case 2 -> Rarity.RARE;
            default -> Rarity.EPIC;
        };
    }

    /**
     * Recomputes max durability from the item's default value times the quality bonus and moves the
     * current wear onto the new scale, so tempering never repairs or over-damages a blade.
     */
    private static void rescaleDurability(ItemStack stack, int quality) {
        int base = new ItemStack(stack.getItem()).getMaxDamage();
        if (base <= 0) {
            return; // unbreakable or no durability: nothing to scale
        }
        int currentMax = stack.getMaxDamage();
        float wear = currentMax <= 0 ? 0.0F : (float) stack.getDamageValue() / currentMax;
        int newMax = Math.max(1,
                Math.round(base * (1.0F + WeaponQuality.DURABILITY_PER_LEVEL * quality)));
        stack.set(DataComponents.MAX_DAMAGE, newMax);
        stack.setDamageValue(Math.min(newMax - 1, Math.round(wear * newMax)));
    }
}
