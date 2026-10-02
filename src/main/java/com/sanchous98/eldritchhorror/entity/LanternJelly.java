package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.registry.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

/**
 * Lantern jelly — the glow squid role: a faint light in the deep that is "a false comfort"
 * ({@code design/25-bestiary-and-entities.md}, "Ambient role"). Family: ambient. Its glow is
 * rendered by the client ({@code LanternJellyRenderer}), but standing in its light is quietly
 * unsettling: a small, config-gated {@link DreadAura} sanity drain. It is still non-combat.
 */
public final class LanternJelly extends WaterAmbientDrifter implements DreadAura {

    public LanternJelly(EntityType<? extends LanternJelly> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return drifterAttributes()
                .add(Attributes.MAX_HEALTH, 4.0)
                .add(Attributes.MOVEMENT_SPEED, 0.18);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.LANTERN_JELLY_AMBIENT.get();
    }

    // --- False comfort (DreadAura) --------------------------------------------------------------

    @Override
    public Axis auraAxis() {
        return Axis.SANITY;
    }

    @Override
    public boolean isAuraActive() {
        return masterAuraEnabled() && ModConfig.ENABLE_SANITY_LANTERN_JELLY.get();
    }

    @Override
    public double auraRate() {
        return ModConfig.SANITY_LANTERN_JELLY_RATE.get();
    }

    @Override
    public double auraRadius() {
        return ModConfig.SANITY_LANTERN_JELLY_RADIUS.get();
    }

    @Override
    public int auraMaxStack() {
        return ModConfig.SANITY_LANTERN_JELLY_MAX.get();
    }
}
