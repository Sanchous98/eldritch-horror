package com.sanchous98.eldritchhorror.registry;

import com.sanchous98.eldritchhorror.EldritchHorror;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Entity registry. Placeholder for the bestiary (cultists, star-spawn, the horror itself).
 *
 * <p>Note: the first real entity needs a full {@code EntityType.Builder} with its dimensions,
 * a {@code Entity} subclass, a renderer (client), attributes, and spawn placement — none of
 * which are wired yet in this skeleton.
 */
public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, EldritchHorror.MODID);

    // public static final DeferredHolder<EntityType<?>, EntityType<Cultist>> CULTIST =
    //         ENTITY_TYPES.register("cultist", () -> EntityType.Builder.of(Cultist::new, MobCategory.MONSTER)
    //                 .sized(0.6f, 1.95f).build("cultist"));

    private ModEntities() {
    }
}
