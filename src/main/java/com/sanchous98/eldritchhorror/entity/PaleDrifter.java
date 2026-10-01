package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.registry.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

/**
 * Pale drifter — the squid role: a dead-water flier near drowned sites
 * ({@code design/25-bestiary-and-entities.md}, "Ambient role"). Family: ambient. A drifting
 * presence of still water; no aura, no fight.
 */
public final class PaleDrifter extends WaterAmbientDrifter {

    public PaleDrifter(EntityType<? extends PaleDrifter> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return drifterAttributes()
                .add(Attributes.MAX_HEALTH, 4.0)
                .add(Attributes.MOVEMENT_SPEED, 0.2);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.PALE_DRIFTER_AMBIENT.get();
    }
}
