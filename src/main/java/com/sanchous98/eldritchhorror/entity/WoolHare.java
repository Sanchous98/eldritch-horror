package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Wool hare — the sheep role: a skittish herd animal that <b>flees corruption</b>. Family: mundane.
 * It avoids our tainted/wrong mobs on sight and, if it should find itself standing in a tainted
 * chunk, bolts for clean ground. Both are client-visible goals plus a bounded server nudge; loaded
 * chunks only.
 */
public final class WoolHare extends MundaneAnimal {

    public WoolHare(EntityType<? extends WoolHare> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, TaintedFauna.class, 12.0F, 1.4, 1.6));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, RisenHusk.class, 12.0F, 1.4, 1.6));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return animalAttributes()
                .add(Attributes.MAX_HEALTH, 8.0)
                .add(Attributes.MOVEMENT_SPEED, 0.30);
    }

    @Override
    protected void serverBehavior(ServerLevel level) {
        if (this.tickCount % 20 != 0) {
            return;
        }
        Vec3 fleeFrom = null;
        if (BestiarySupport.tainted(this)) {
            fleeFrom = this.position();
        } else {
            AABB box = this.getBoundingBox().inflate(12.0);
            LivingEntity wrong = level.getEntitiesOfClass(LivingEntity.class, box,
                    e -> e instanceof TaintedFauna || e instanceof RisenHusk).stream().findFirst().orElse(null);
            if (wrong != null) {
                fleeFrom = wrong.position();
            }
        }
        if (fleeFrom == null) {
            return;
        }
        @Nullable Vec3 away = DefaultRandomPos.getPosAway(this, 10, 5, fleeFrom);
        if (away != null) {
            this.getNavigation().moveTo(away.x, away.y, away.z, 1.5);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.WOOL_HARE_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.WOOL_HARE_AMBIENT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.WOOL_HARE_AMBIENT.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.SHEEP_STEP, 0.10F, 1.15F);
    }
}
