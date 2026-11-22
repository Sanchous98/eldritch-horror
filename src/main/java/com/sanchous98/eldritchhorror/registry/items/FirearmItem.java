package com.sanchous98.eldritchhorror.registry.items;

import com.sanchous98.eldritchhorror.entity.Bullet;
import java.util.function.Supplier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * A field firearm ({@code design/16-items.md}): right-click fires a {@link Bullet} and spends a
 * brass cartridge.
 *
 * <p>Server-authoritative, like every other usable item here: the client half only returns success
 * so the click feels immediate. One class covers the whole ladder - revolver, automatic, shotgun
 * and carbine - because the differences are {@link FirearmSpec} numbers, not code paths. Durability
 * is spent per trigger pull, so a long expedition still needs a melee fallback; that is deliberate
 * after the night-5 playthrough showed a stone-tier melee alone cannot carry a boss farm.
 *
 * <p>Ammo is read from the inventory, not a magazine: no reload screen exists in 26.3 for a
 * non-vanilla gun, and a magazine would add a UI for no gameplay gain. The ammo item is supplied
 * lazily ({@link Supplier}) because the item holder is not bound while the weapon itself is being
 * registered.
 */
public final class FirearmItem extends Item {

    private final FirearmSpec spec;
    private final SoundEvent shot;
    private final Supplier<Item> ammo;

    public FirearmItem(Properties properties, FirearmSpec spec, SoundEvent shot, Supplier<Item> ammo) {
        super(properties);
        this.spec = spec;
        this.shot = shot;
        this.ammo = ammo;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        Item cartridge = this.ammo.get();
        if (!consumeAmmo(player, cartridge, this.spec.ammoPerShot())) {
            return InteractionResult.FAIL;
        }
        ServerLevel server = (ServerLevel) level;
        for (int i = 0; i < this.spec.pellets(); i++) {
            Bullet bullet = new Bullet(level, player, new ItemStack(cartridge));
            bullet.setDamage(this.spec.damage());
            bullet.setPierce(this.spec.pierce());
            bullet.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F,
                    this.spec.velocity(), this.spec.spread());
            server.addFreshEntity(bullet);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), this.shot,
                SoundSource.PLAYERS, 0.9F, 0.95F + level.getRandom().nextFloat() * 0.1F);
        player.getCooldowns().addCooldown(stack, this.spec.cooldownTicks());
        stack.hurtAndBreak(1, server, player, broken -> {
        });
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResult.SUCCESS;
    }

    /** Spends {@code count} cartridges from the inventory; creative keeps an endless supply. */
    private static boolean consumeAmmo(Player player, Item cartridge, int count) {
        if (player.getAbilities().instabuild) {
            return true;
        }
        Inventory inventory = player.getInventory();
        int available = 0;
        for (int slot = 0; slot < inventory.getContainerSize() && available < count; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(cartridge)) {
                available += stack.getCount();
            }
        }
        if (available < count) {
            return false;
        }
        int left = count;
        for (int slot = 0; slot < inventory.getContainerSize() && left > 0; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!stack.is(cartridge)) {
                continue;
            }
            int take = Math.min(left, stack.getCount());
            stack.shrink(take);
            left -= take;
        }
        return true;
    }
}
