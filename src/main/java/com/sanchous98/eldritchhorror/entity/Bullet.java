package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.registry.ModEntities;
import com.sanchous98.eldritchhorror.registry.items.Firearms;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * The fired round of the field firearms ({@code design/16-items.md}).
 *
 * <p>One entity for every gun: the weapon hands it its damage, pierce count and velocity before it
 * enters the level, so a revolver round and a carbine round differ only in the numbers a
 * {@link com.sanchous98.eldritchhorror.registry.items.FirearmSpec} supplies. It is not an arrow -
 * guns do not consume vanilla arrows, and the round carries the cartridge icon for the vanilla
 * thrown-item renderer.
 *
 * <p>No gravity: a bullet is a fast, flat shot, and 26.3's throwable base would otherwise drop it
 * visibly over the ranges the guns are balanced for. Piercing rounds pass through their first
 * target and stop on the next body or on a block.
 */
public final class Bullet extends ThrowableItemProjectile {

    private float damage = 5.0F;
    private int pierce;

    public Bullet(EntityType<? extends Bullet> type, Level level) {
        super(type, level);
    }

    public Bullet(Level level, LivingEntity owner, ItemStack stack) {
        super(ModEntities.BULLET.get(), owner, level, stack);
        this.setNoGravity(true);
    }

    /** Damage applied to the first body hit (after armour, as every melee hit is). */
    public void setDamage(float damage) {
        this.damage = damage;
    }

    /** Extra bodies the round passes through before it stops. */
    public void setPierce(int pierce) {
        this.pierce = pierce;
    }


    @Override
    protected Item getDefaultItem() {
        return Firearms.CARTRIDGE.get();
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return target != this.getOwner() && super.canHitEntity(target);
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        hit.getEntity().hurt(this.damageSources().thrown(this, this.getOwner()), this.damage);
        if (this.pierce > 0) {
            this.pierce--;
        } else {
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (!this.level().isClientSide() && hit.getType() == HitResult.Type.BLOCK) {
            this.discard();
        }
    }
}
