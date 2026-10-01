package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.registry.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

/**
 * Frost wisp — the snow-golem ambient role: cold motes near the polar edge
 * ({@code design/25-bestiary-and-entities.md}, "Ambient role"). Family: ambient. A drifting spark
 * of cold air; no aura, no fight.
 */
public final class FrostWisp extends FlyingAmbientDrifter {

    public FrostWisp(EntityType<? extends FrostWisp> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return drifterAttributes()
                .add(Attributes.MAX_HEALTH, 2.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.FROST_WISP_AMBIENT.get();
    }
}
