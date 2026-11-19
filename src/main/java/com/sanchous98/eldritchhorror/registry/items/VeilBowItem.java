package com.sanchous98.eldritchhorror.registry.items;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;

/**
 * The Veil Bow ({@code design/16-items.md}): a bow whose arrows pierce the Veil and the flesh
 * behind it.
 *
 * <p>26.3 computes an arrow's pierce level from the firing weapon's enchantments in the arrow
 * constructor ({@code AbstractArrow}'s {@code firedFromWeapon} path), and there is no public
 * setter for it. Rather than reach for an access transformer (owned by another area), this class
 * hands the arrow a <b>copy</b> of the bow carrying vanilla's {@code piercing} at a fixed level;
 * the real bow stack is never modified, so it never glints, never shows the enchantment and cannot
 * be stripped by a grindstone. The bonus is therefore data-shaped, not a bespoke arrow entity.
 *
 * <p>Server-side only in effect: the {@code ServerLevel} guard falls through to the plain bow on a
 * client, and the copy is built only when an arrow is actually spawned.
 */
public final class VeilBowItem extends BowItem {

    /** Extra targets an arrow passes through: two, i.e. three bodies in a line. */
    private static final int PIERCE_LEVEL = 2;

    public VeilBowItem(Properties properties) {
        super(properties);
    }

    @Override
    protected Projectile createProjectile(Level level, LivingEntity shooter, ItemStack weapon,
                                          ItemStack projectile, boolean isCrit) {
        return super.createProjectile(level, shooter, pierced(level, weapon), projectile, isCrit);
    }

    /** A throw-away bow copy carrying vanilla piercing, used only to derive the arrow's pierce. */
    private static ItemStack pierced(Level level, ItemStack weapon) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return weapon;
        }
        Holder<Enchantment> piercing = serverLevel.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.PIERCING);
        ItemStack copy = weapon.copy();
        ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(
                copy.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY));
        enchantments.upgrade(piercing, PIERCE_LEVEL);
        copy.set(DataComponents.ENCHANTMENTS, enchantments.toImmutable());
        return copy;
    }
}
