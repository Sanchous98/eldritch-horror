package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.EldritchHorror;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * The reusable <b>mundane-weapon ward</b> the star-spawn uses
 * ({@code design/25-bestiary-and-entities.md}: "resists mundane weapons"). One mechanism, shared by
 * any future warded thing.
 *
 * <p>It does not touch the damage numbers: {@link #wardsOff} is consulted from the mob's
 * {@code hurtServer} override, which simply returns {@code false} (the hit does nothing) when the
 * ward holds. What it blocks and what it lets through:
 *
 * <ul>
 *   <li><b>Blocks</b> every damage source that is neither an item-borne player attack nor one of the
 *       bypass types below: mob melee, all projectiles (arrows, tridents, fireballs), magic, fire,
 *       lava, fall, drowning, cactus and the like.</li>
 *   <li><b>Allows</b> a {@link Player} attack only when the weapon they are holding is tagged
 *       {@code #eldritch_horror:ward_breakers} — by default the Star-Iron Sword, the meteoric blade
 *       the design already says is "bonus damage vs. star-spawn". The tag is data-driven, so more
 *       named-metal weapons can be added without touching this class.</li>
 *   <li><b>Allows</b> explosions, sonic booms, mace smashes and {@code generic_kill} from any source
 *       — a deliberate non-sword solve so the thing is never strictly unkillable.</li>
 * </ul>
 *
 * <p>Server-side only, no state, no randomness.
 */
public final class EldritchWarded {

    /** Items whose bearer can wound a warded thing (data tag, default: {@code star_iron_sword}). */
    public static final TagKey<Item> WARD_BREAKERS =
            TagKey.create(Registries.ITEM, EldritchHorror.id("ward_breakers"));

    /** Sources that always wound, whatever the attacker is holding. */
    private static final List<ResourceKey<DamageType>> BYPASSING_TYPES = List.of(
            DamageTypes.GENERIC_KILL,
            DamageTypes.SONIC_BOOM,
            DamageTypes.EXPLOSION,
            DamageTypes.PLAYER_EXPLOSION,
            DamageTypes.MACE_SMASH);

    private EldritchWarded() {
    }

    /** Whether {@code source} is resisted; {@code true} means the hit is ignored. */
    public static boolean wardsOff(DamageSource source) {
        // Bypass types first, whatever the attacker: a player mace smash or a player-sourced
        // explosion must still wound, so these are checked before the attacker branch.
        for (ResourceKey<DamageType> type : BYPASSING_TYPES) {
            if (source.is(type)) {
                return false;
            }
        }
        Entity attacker = source.getEntity();
        if (attacker instanceof Player) {
            ItemStack weapon = source.getWeaponItem();
            if (weapon == null) {
                weapon = attacker.getWeaponItem();
            }
            return weapon == null || !weapon.is(WARD_BREAKERS);
        }
        return true;
    }
}
