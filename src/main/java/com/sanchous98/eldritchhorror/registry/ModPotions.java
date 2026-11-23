package com.sanchous98.eldritchhorror.registry;

import com.sanchous98.eldritchhorror.EldritchHorror;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** The mod's potions; currently only the Waking splash used by Smelling Salts. */
public final class ModPotions {

    public static final DeferredRegister<Potion> POTIONS =
            DeferredRegister.create(Registries.POTION, EldritchHorror.MODID);

    /** Waking: an instant effect that cures Stupor and restores a little sanity. */
    public static final DeferredHolder<Potion, Potion> WAKING =
            POTIONS.register("waking", () -> new Potion("waking",
                    new MobEffectInstance(ModEffects.WAKING, 1, 0)));

    private ModPotions() {
    }
}
