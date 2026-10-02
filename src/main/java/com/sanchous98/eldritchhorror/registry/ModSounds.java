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

    /** The weaver spawn's skitter (ambient/hurt/death). */
    public static final DeferredHolder<SoundEvent, SoundEvent> WEAVER_SPAWN_AMBIENT =
            register("entity.weaver_spawn.ambient");

    /** The rift mite's chittering (ambient/hurt/death); small, but never alone. */
    public static final DeferredHolder<SoundEvent, SoundEvent> RIFT_MITE_AMBIENT =
            register("entity.rift_mite.ambient");

    /** The shambler ooze's wet settle (ambient/hurt); its death is the split. */
    public static final DeferredHolder<SoundEvent, SoundEvent> SHAMBLER_OOZE_AMBIENT =
            register("entity.shambler_ooze.ambient");

    public static final DeferredHolder<SoundEvent, SoundEvent> SHAMBLER_OOZE_DEATH =
            register("entity.shambler_ooze.death");

    /** The choir spite's thin, spiteful note (ambient/hurt); its death is a snuffed hum. */
    public static final DeferredHolder<SoundEvent, SoundEvent> CHOIR_SPITE_AMBIENT =
            register("entity.choir_spite.ambient");

    public static final DeferredHolder<SoundEvent, SoundEvent> CHOIR_SPITE_DEATH =
            register("entity.choir_spite.death");

    /** The night hag's ragged cry (ambient/hurt/death). */
    public static final DeferredHolder<SoundEvent, SoundEvent> NIGHT_HAG_AMBIENT =
            register("entity.night_hag.ambient");

    // --- Cult faction (design/09-cults.md, design/25 cultist family) --------------------------

    /** The worshipper's murmured prayer (ambient/hurt/death). */
    public static final DeferredHolder<SoundEvent, SoundEvent> WORSHIPPER_AMBIENT =
            register("entity.worshipper.ambient");

    /** The cult zealot's barked devotion (ambient/hurt/death). */
    public static final DeferredHolder<SoundEvent, SoundEvent> CULT_ZEALOT_AMBIENT =
            register("entity.cult_zealot.ambient");

    /** The cult raider's skirmish cry (ambient/hurt/death). */
    public static final DeferredHolder<SoundEvent, SoundEvent> CULT_RAIDER_AMBIENT =
            register("entity.cult_raider.ambient");

    /** The rite binder's intonation (ambient/hurt/death). */
    public static final DeferredHolder<SoundEvent, SoundEvent> RITE_BINDER_AMBIENT =
            register("entity.rite_binder.ambient");

    /** The plague crone's rasping (ambient/hurt/death). */
    public static final DeferredHolder<SoundEvent, SoundEvent> PLAGUE_CRONE_AMBIENT =
            register("entity.plague_crone.ambient");

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

    /** Azathoth's vast drone (ambient); the music at the centre of the scar. */
    public static final DeferredHolder<SoundEvent, SoundEvent> AZATHOTH_AMBIENT =
            register("entity.azathoth.ambient");

    public static final DeferredHolder<SoundEvent, SoundEvent> AZATHOTH_HURT =
            register("entity.azathoth.hurt");

    public static final DeferredHolder<SoundEvent, SoundEvent> AZATHOTH_DEATH =
            register("entity.azathoth.death");

    /** Yog-Sothoth: the gate's patient hum (ambient) and the sound of it sealing. */
    public static final DeferredHolder<SoundEvent, SoundEvent> YOG_SOTHOTH_AMBIENT =
            register("entity.yog_sothoth.ambient");

    public static final DeferredHolder<SoundEvent, SoundEvent> YOG_SOTHOTH_HURT =
            register("entity.yog_sothoth.hurt");

    public static final DeferredHolder<SoundEvent, SoundEvent> YOG_SOTHOTH_DEATH =
            register("entity.yog_sothoth.death");

    /** Ithaqua: the howl carried on the walking wind (ambient/hurt/death). */
    public static final DeferredHolder<SoundEvent, SoundEvent> ITHAQUA_AMBIENT =
            register("entity.ithaqua.ambient");

    public static final DeferredHolder<SoundEvent, SoundEvent> ITHAQUA_HURT =
            register("entity.ithaqua.hurt");

    public static final DeferredHolder<SoundEvent, SoundEvent> ITHAQUA_DEATH =
            register("entity.ithaqua.death");

    /** Yig: the rustle of drawn things (ambient/hurt/death). */
    public static final DeferredHolder<SoundEvent, SoundEvent> YIG_AMBIENT =
            register("entity.yig.ambient");

    public static final DeferredHolder<SoundEvent, SoundEvent> YIG_HURT =
            register("entity.yig.hurt");

    public static final DeferredHolder<SoundEvent, SoundEvent> YIG_DEATH =
            register("entity.yig.death");

    /** Atlach-Nacha: the skitter of the weaving (ambient/hurt/death). */
    public static final DeferredHolder<SoundEvent, SoundEvent> ATLACH_NACHA_AMBIENT =
            register("entity.atlach_nacha.ambient");

    public static final DeferredHolder<SoundEvent, SoundEvent> ATLACH_NACHA_HURT =
            register("entity.atlach_nacha.hurt");

    public static final DeferredHolder<SoundEvent, SoundEvent> ATLACH_NACHA_DEATH =
            register("entity.atlach_nacha.death");

    // --- Passive (mundane) fauna (design/25 passive role table) --------------------------------

    public static final DeferredHolder<SoundEvent, SoundEvent> DEER_AMBIENT = register("entity.deer.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> WOOL_HARE_AMBIENT = register("entity.wool_hare.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> MIRE_SOW_AMBIENT = register("entity.mire_sow.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> ASH_FOWL_AMBIENT = register("entity.ash_fowl.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> BURROWLING_AMBIENT = register("entity.burrowling.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> PACK_BEAST_AMBIENT = register("entity.pack_beast.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> GREY_FOX_AMBIENT = register("entity.grey_fox.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> HILL_HOUND_AMBIENT = register("entity.hill_hound.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> HEARTH_CAT_AMBIENT = register("entity.hearth_cat.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> WOOL_BEAST_AMBIENT = register("entity.wool_beast.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> BOG_BEAR_AMBIENT = register("entity.bog_bear.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIDE_GRAZER_AMBIENT = register("entity.tide_grazer.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPORE_BEE_AMBIENT = register("entity.spore_bee.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> STONE_SENTINEL_AMBIENT = register("entity.stone_sentinel.ambient");

    // --- Ambient (ambience) fauna (design/25 ambient role table) -------------------------------

    public static final DeferredHolder<SoundEvent, SoundEvent> CAVE_DRIFTER_AMBIENT = register("entity.cave_drifter.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> PALE_DRIFTER_AMBIENT = register("entity.pale_drifter.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> LANTERN_JELLY_AMBIENT = register("entity.lantern_jelly.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> MARSH_MOTE_AMBIENT = register("entity.marsh_mote.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> DROWNED_MINNOW_AMBIENT = register("entity.drowned_minnow.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> FROST_WISP_AMBIENT = register("entity.frost_wisp.ambient");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(EldritchHorror.id(name)));
    }

    private ModSounds() {
    }
}
