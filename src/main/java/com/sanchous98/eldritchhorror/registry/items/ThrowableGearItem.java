package com.sanchous98.eldritchhorror.registry.items;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * A thrown piece of field gear ({@code design/16-items.md}): Dynamite and Tear Gas.
 *
 * <p>The item is only the launcher: the projectile class owns the effect (blast, gas cloud), so
 * both share this one implementation and the difference lives in the {@link Factory}. The pattern
 * mirrors vanilla's {@code SnowballItem} - play the throw sound, spawn from rotation on the server,
 * spend one item - with the projectile factory injected instead of hard-coded.
 */
public final class ThrowableGearItem extends Item {

    /** Builds the projectile for this gear; called server-side only. */
    @FunctionalInterface
    public interface Factory {
        Projectile create(Level level, LivingEntity owner, ItemStack stack);
    }

    private final Factory factory;
    private final float power;
    private final float uncertainty;
    private final SoundEvent throwSound;

    public ThrowableGearItem(Properties properties, Factory factory, float power,
                             float uncertainty, SoundEvent throwSound) {
        super(properties);
        this.factory = factory;
        this.power = power;
        this.uncertainty = uncertainty;
        this.throwSound = throwSound;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), this.throwSound,
                SoundSource.NEUTRAL, 0.6F, 0.9F + level.getRandom().nextFloat() * 0.2F);
        if (level instanceof ServerLevel server) {
            Projectile projectile = this.factory.create(level, player, stack);
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F,
                    this.power, this.uncertainty);
            server.addFreshEntity(projectile);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        stack.consume(1, player);
        return InteractionResult.SUCCESS;
    }
}
