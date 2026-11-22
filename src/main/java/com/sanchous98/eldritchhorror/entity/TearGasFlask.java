package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.registry.ModEntities;
import com.sanchous98.eldritchhorror.registry.items.Firearms;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;

/**
 * Thrown Tear Gas ({@code design/16-items.md}): the board game's "reduce the Monster's damage and
 * reroll dice" as a non-damaging cloud.
 *
 * <p>Weakness and Blindness are the closest vanilla reading of a gas that ruins an attacker's
 * aim; Nausea keeps the "cannot act cleanly" flavour for players caught in their own cloud. The
 * cloud is instant (no lingering entity): the thrown flask bursts, everything inside the radius
 * is affected, and the particles mark the space for a moment.
 */
public final class TearGasFlask extends ThrowableItemProjectile {

    /** Effect radius in blocks. */
    private static final double RADIUS = 3.5;

    public TearGasFlask(EntityType<? extends TearGasFlask> type, Level level) {
        super(type, level);
    }

    public TearGasFlask(Level level, LivingEntity owner, ItemStack stack) {
        super(ModEntities.TEAR_GAS.get(), owner, level, stack);
    }

    @Override
    protected Item getDefaultItem() {
        return Firearms.TEAR_GAS.get();
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        AABB cloud = this.getBoundingBox().inflate(RADIUS);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, cloud,
                e -> e != this.getOwner() && e.isAlive())) {
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 1), this.getOwner());
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 120, 0), this.getOwner());
            target.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 160, 0), this.getOwner());
        }
        level.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY(), this.getZ(),
                40, RADIUS / 2.0, 1.0, RADIUS / 2.0, 0.02);
        this.discard();
    }
}
