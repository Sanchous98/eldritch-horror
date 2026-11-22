package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.registry.ModEntities;
import com.sanchous98.eldritchhorror.registry.items.Firearms;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/**
 * Thrown Dynamite ({@code design/16-items.md}): the board game's one-shot "each Monster on your
 * space loses 3 Health" as a real explosion.
 *
 * <p>{@link Level.ExplosionInteraction#NONE} keeps the blast entity-only: it hurts what stands in
 * the radius but never edits the world, so a thrown stick cannot carve a crater through a city or
 * a site. That matches the card's intent and keeps the world honest for the server's structure
 * generation.
 */
public final class ThrownDynamite extends ThrowableItemProjectile {

    /** Blast radius in blocks; entity damage falls off with distance like a vanilla explosion. */
    private static final float POWER = 2.0F;

    public ThrownDynamite(EntityType<? extends ThrownDynamite> type, Level level) {
        super(type, level);
    }

    public ThrownDynamite(Level level, LivingEntity owner, ItemStack stack) {
        super(ModEntities.THROWN_DYNAMITE.get(), owner, level, stack);
    }

    @Override
    protected Item getDefaultItem() {
        return Firearms.DYNAMITE.get();
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (this.level() instanceof ServerLevel server) {
            server.explode(this, this.getX(), this.getY(), this.getZ(), POWER,
                    Level.ExplosionInteraction.NONE);
            this.discard();
        }
    }
}
