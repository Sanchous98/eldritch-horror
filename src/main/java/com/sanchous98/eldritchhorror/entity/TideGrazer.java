package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Tide grazer — the turtle role: a coastal grazer near Choir sites. Family: mundane (Choir
 * contrast). Deliberately slow and unthreatening; it is the living shore the drowned temples
 * stand against.
 */
public final class TideGrazer extends MundaneAnimal {

    public TideGrazer(EntityType<? extends TideGrazer> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return animalAttributes()
                .add(Attributes.MAX_HEALTH, 16.0)
                .add(Attributes.MOVEMENT_SPEED, 0.16);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.TIDE_GRAZER_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.TIDE_GRAZER_AMBIENT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.TIDE_GRAZER_AMBIENT.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.SHEEP_STEP, 0.12F, 0.85F);
    }
}
