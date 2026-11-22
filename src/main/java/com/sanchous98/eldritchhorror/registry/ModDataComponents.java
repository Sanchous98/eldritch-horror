package com.sanchous98.eldritchhorror.registry;

import com.mojang.serialization.Codec;
import com.sanchous98.eldritchhorror.EldritchHorror;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The mod's own data components. One so far: the per-stack weapon quality
 * ({@code design/16-items.md}), an {@code int} in {@code 0..5} that survives save/load and is
 * synced to clients for the tooltip.
 */
public final class ModDataComponents {

    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, EldritchHorror.MODID);

    /** Weapon quality level ({@code 0..5}); absent means {@code 0}. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> WEAPON_QUALITY =
            DATA_COMPONENTS.register("weapon_quality", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());

    private ModDataComponents() {
    }
}
