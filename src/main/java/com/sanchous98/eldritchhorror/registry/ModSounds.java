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

    /** Tainted fauna's distorted animal call (ambient). */
    public static final DeferredHolder<SoundEvent, SoundEvent> TAINTED_FAUNA_AMBIENT =
            register("entity.tainted_fauna.ambient");

    /** Tainted fauna's distorted cry when struck. */
    public static final DeferredHolder<SoundEvent, SoundEvent> TAINTED_FAUNA_HURT =
            register("entity.tainted_fauna.hurt");

    /** Tainted fauna's distorted death cry. */
    public static final DeferredHolder<SoundEvent, SoundEvent> TAINTED_FAUNA_DEATH =
            register("entity.tainted_fauna.death");

    /** The lesser swarm's skittering vocalisation (ambient/hurt/death). */
    public static final DeferredHolder<SoundEvent, SoundEvent> LESSER_SWARM_AMBIENT =
            register("entity.lesser_swarm.ambient");

    /** The Watcher's vocalisation — near-absent, heard more than seen. */
    public static final DeferredHolder<SoundEvent, SoundEvent> WATCHER_AMBIENT =
            register("entity.watcher.ambient");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(EldritchHorror.id(name)));
    }

    private ModSounds() {
    }
}
