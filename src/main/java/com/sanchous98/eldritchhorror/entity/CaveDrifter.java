package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.registry.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

/**
 * Cave drifter — the bat role: a harmless flier whose <b>absence</b> is the warning
 * ({@code design/25-bestiary-and-entities.md}, "Ambient role"). Family: ambient. It carries no aura
 * of its own; it drifts in the dark cave, and a cave that has gone quiet has simply lost it.
 */
public final class CaveDrifter extends FlyingAmbientDrifter {

    public CaveDrifter(EntityType<? extends CaveDrifter> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return drifterAttributes()
                .add(Attributes.MAX_HEALTH, 3.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.CAVE_DRIFTER_AMBIENT.get();
    }
}
