package com.sanchous98.eldritchhorror.registry;

import com.sanchous98.eldritchhorror.EldritchHorror;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Attribute registry: the player's {@code eldritch_horror:max_sanity} ceiling.
 *
 * <p>Verified against the 26.3 patched sources: vanilla attributes are created with the public
 * {@code RangedAttribute(String descriptionId, double defaultValue, double min, double max)}
 * constructor and registered through a {@code DeferredRegister<Attribute>} on
 * {@link Registries#ATTRIBUTE}; the entity's base value is attached by the mod-bus
 * {@link EntityAttributeModificationEvent} (fired after registration) via
 * {@code add(EntityType, Holder<Attribute>, double)}. Because the event implements
 * {@code IModBusEvent}, this {@code @EventBusSubscriber} class is wired to the mod bus.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class ModAttributes {

    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(Registries.ATTRIBUTE, EldritchHorror.MODID);

    /**
     * The sanity meter ceiling. Default {@code 100.0} (matching
     * {@code SanitySystem.DEFAULT_MAX}); the bound allows class bonuses to exceed the base (the
     * Investigator's {@code +20%} needs headroom, and {@code RangedAttribute} sanitises anything
     * above its max back down). Synced so the HUD sees it.
     */
    public static final DeferredHolder<Attribute, Attribute> MAX_SANITY =
            ATTRIBUTES.register("max_sanity",
                    () -> new RangedAttribute("attribute.name.eldritch_horror.max_sanity", 100.0, 0.0, 1000.0)
                            .setSyncable(true));

    private ModAttributes() {
    }

    /** Gives every player the {@code max_sanity} attribute at its base value (100). */
    @SubscribeEvent
    public static void onAttributeModification(EntityAttributeModificationEvent event) {
        event.add(EntityTypes.PLAYER, MAX_SANITY, MAX_SANITY.value().getDefaultValue());
    }
}
