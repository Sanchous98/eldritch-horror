package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.registry.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

/**
 * Drowned minnow — the fish-shoal role: the only thing left in dead water
 * ({@code design/25-bestiary-and-entities.md}, "Ambient role"). Family: ambient. A small water
 * drifter, harmless and aimless.
 */
public final class DrownedMinnow extends WaterAmbientDrifter {

    public DrownedMinnow(EntityType<? extends DrownedMinnow> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return drifterAttributes()
                .add(Attributes.MAX_HEALTH, 2.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.DROWNED_MINNOW_AMBIENT.get();
    }
}
