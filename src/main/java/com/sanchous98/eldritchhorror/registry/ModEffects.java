package com.sanchous98.eldritchhorror.registry;

import com.sanchous98.eldritchhorror.EldritchHorror;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Mob effect registry. Expected effects: {@code Madness} (sanity floor penalties),
 * {@code Marked} (the horror's attention), and {@code Corrupted}.
 */
public final class ModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, EldritchHorror.MODID);

    // public static final DeferredHolder<MobEffect, MadnessEffect> MADNESS =
    //         MOB_EFFECTS.register("madness", MadnessEffect::new);

    private ModEffects() {
    }
}
