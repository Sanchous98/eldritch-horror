package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.registry.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

/**
 * Marsh mote — the allay/tadpole/axolotl role: drift-life of the drowned marsh
 * ({@code design/25-bestiary-and-entities.md}, "Ambient role"). Family: ambient. A tiny flier that
 * hovers over the marsh; no aura, no fight.
 */
public final class MarshMote extends FlyingAmbientDrifter {

    public MarshMote(EntityType<? extends MarshMote> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return drifterAttributes()
                .add(Attributes.MAX_HEALTH, 3.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.MARSH_MOTE_AMBIENT.get();
    }
}
