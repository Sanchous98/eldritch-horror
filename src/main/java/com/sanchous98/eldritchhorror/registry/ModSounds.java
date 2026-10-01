package com.sanchous98.eldritchhorror.registry;

import com.sanchous98.eldritchhorror.EldritchHorror;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Sound registry. Horror relies on sound: whispers, distant chants, the altar's hum, and the
 * horror's own vocalisations. Add matching entries under {@code assets/eldritch_horror/sounds.json}.
 */
public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, EldritchHorror.MODID);

    /** The risen husk's vocalisation (ambient/hurt/death). Backed by a real ogg under sounds/. */
    public static final DeferredHolder<SoundEvent, SoundEvent> RISEN_HUSK_AMBIENT =
            register("entity.risen_husk.ambient");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(EldritchHorror.id(name)));
    }

    private ModSounds() {
    }
}
