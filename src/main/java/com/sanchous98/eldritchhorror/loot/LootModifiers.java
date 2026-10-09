package com.sanchous98.eldritchhorror.loot;

import com.mojang.serialization.MapCodec;
import com.sanchous98.eldritchhorror.EldritchHorror;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Global loot modifier serializers for the mod. Registered on the mod event bus in
 * {@link EldritchHorror}; the modifier itself is enabled by a datapack entry under
 * {@code data/eldritch_horror/loot_modifiers/}.
 */
public final class LootModifiers {

    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS,
                    EldritchHorror.MODID);

    /** {@code eldritch_horror:antiquarian} — Aldous's better-loot modifier. */
    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>,
            MapCodec<AntiquarianLootModifier>> ANTIQUARIAN =
            SERIALIZERS.register("antiquarian", () -> AntiquarianLootModifier.CODEC);

    private LootModifiers() {
    }
}
