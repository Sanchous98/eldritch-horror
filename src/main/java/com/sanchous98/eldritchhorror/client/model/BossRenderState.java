package com.sanchous98.eldritchhorror.client.model;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Shared render state for every Ancient One. 26.3 mobs have no generic "attack ticks" field, but
 * {@code LivingEntity#getSwingAnimation(partialTicks)} gives the 0..1 progress of the swing the
 * melee goal starts on every hit, which is exactly what an attack lunge needs.
 *
 * <p>One state class for the whole roster keeps the renderers tiny and lets
 * {@link BossModelBase} animate every boss the same way.
 */
@OnlyIn(Dist.CLIENT)
public class BossRenderState extends LivingEntityRenderState {

    /** 0..1 progress through the current attack swing; 0 when idle. */
    public float attackProgress;

    /** True while a swing is in flight (used by models that keep the lunge pose briefly). */
    public boolean attacking;

    /** Convenience 0..1 triangle that peaks in the middle of a swing (the lunge itself). */
    public float lunge;
}
