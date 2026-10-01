package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Burrowling — the rabbit role: scatters and burrows near taint. Family: mundane. It is skittish
 * around the wrong and, if it finds itself standing in a tainted chunk, bolts for clean ground
 * (bounded server nudge, loaded chunks only).
 */
public final class Burrowling extends MundaneAnimal {

    public Burrowling(EntityType<? extends Burrowling> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return animalAttributes()
                .add(Attributes.MAX_HEALTH, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.32);
    }

    @Override
    protected void serverBehavior(ServerLevel level) {
        if (this.tickCount % 20 != 0 || !BestiarySupport.tainted(this)) {
            return;
        }
        @Nullable Vec3 away = DefaultRandomPos.getPosAway(this, 10, 5, this.position());
        if (away != null) {
            this.getNavigation().moveTo(away.x, away.y, away.z, 1.5);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.BURROWLING_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.BURROWLING_AMBIENT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.BURROWLING_AMBIENT.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.SHEEP_STEP, 0.08F, 1.3F);
    }
}
