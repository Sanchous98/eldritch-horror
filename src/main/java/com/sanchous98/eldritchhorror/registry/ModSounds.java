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

    /** The bone choir's rattling hymn (ambient/hurt/death). */
    public static final DeferredHolder<SoundEvent, SoundEvent> BONE_CHOIR_AMBIENT =
            register("entity.bone_choir.ambient");

    /** The drowned thrall's sodden gurgle (ambient/hurt/death). */
    public static final DeferredHolder<SoundEvent, SoundEvent> DROWNED_THRALL_AMBIENT =
            register("entity.drowned_thrall.ambient");

    /** The veil stalker's whisper from above (ambient/hurt/death). */
    public static final DeferredHolder<SoundEvent, SoundEvent> VEIL_STALKER_AMBIENT =
            register("entity.veil_stalker.ambient");

    /** The blight pod's wet spore-swell (ambient/hurt); its death is the burst. */
    public static final DeferredHolder<SoundEvent, SoundEvent> BLIGHT_POD_AMBIENT =
            register("entity.blight_pod.ambient");

    public static final DeferredHolder<SoundEvent, SoundEvent> BLIGHT_POD_DEATH =
            register("entity.blight_pod.death");

    /** The byakhee's leathery cry (ambient/hurt/death). */
    public static final DeferredHolder<SoundEvent, SoundEvent> BYAKHEE_AMBIENT =
            register("entity.byakhee.ambient");

    /** The star-spawn's wrong chime (ambient/hurt); its death is the fall of a star. */
    public static final DeferredHolder<SoundEvent, SoundEvent> STAR_SPAWN_AMBIENT =
            register("entity.star_spawn.ambient");

    public static final DeferredHolder<SoundEvent, SoundEvent> STAR_SPAWN_DEATH =
            register("entity.star_spawn.death");

    /** The shoggoth mass's bubbling (ambient/hurt); its death is a collapse. */
    public static final DeferredHolder<SoundEvent, SoundEvent> SHOGGOTH_AMBIENT =
            register("entity.shoggoth.ambient");

    public static final DeferredHolder<SoundEvent, SoundEvent> SHOGGOTH_DEATH =
            register("entity.shoggoth.death");

    // --- Ancient Ones (design/28-ancient-ones.md) ----------------------------------------------

    /** Cthulhu's dream-leak vocalisation (ambient). */
    public static final DeferredHolder<SoundEvent, SoundEvent> CTHULHU_AMBIENT =
            register("entity.cthulhu.ambient");

    public static final DeferredHolder<SoundEvent, SoundEvent> CTHULHU_HURT =
            register("entity.cthulhu.hurt");

    public static final DeferredHolder<SoundEvent, SoundEvent> CTHULHU_DEATH =
            register("entity.cthulhu.death");

    /** The Dunwich Horror's distant roar (ambient) — the tell before the sighting. */
    public static final DeferredHolder<SoundEvent, SoundEvent> DUNWICH_HORROR_AMBIENT =
            register("entity.dunwich_horror.ambient");

    public static final DeferredHolder<SoundEvent, SoundEvent> DUNWICH_HORROR_HURT =
            register("entity.dunwich_horror.hurt");

    public static final DeferredHolder<SoundEvent, SoundEvent> DUNWICH_HORROR_DEATH =
            register("entity.dunwich_horror.death");

    /** Shub-Niggurath's vast breathing (ambient) — the woods themselves. */
    public static final DeferredHolder<SoundEvent, SoundEvent> SHUB_NIGGURATH_AMBIENT =
            register("entity.shub_niggurath.ambient");

    public static final DeferredHolder<SoundEvent, SoundEvent> SHUB_NIGGURATH_HURT =
            register("entity.shub_niggurath.hurt");

    public static final DeferredHolder<SoundEvent, SoundEvent> SHUB_NIGGURATH_DEATH =
            register("entity.shub_niggurath.death");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(EldritchHorror.id(name)));
    }

    private ModSounds() {
    }
}
