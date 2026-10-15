package com.sanchous98.eldritchhorror.cult;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.entity.Worshipper;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import org.jspecify.annotations.Nullable;

/**
 * The penalty for killing a cult member ({@code design/03c-reputation.md}: "Kill a cult member
 * | −40", with "that cult; opposed-pair cross-effects apply to gains, not to this").
 *
 * <p>One {@link LivingDeathEvent} listener, so a kill pays exactly once: only a death whose damage
 * source is a player (a melee hit or that player's projectile) counts; mob kills, falls, fire and
 * every other death with no player attacker leave standing alone. The penalty goes through
 * {@link CultSystem#set}, not {@link CultSystem#add}, because the opposed-pair spill is reserved
 * for gains.
 *
 * <p>Cultist → cult association ({@code design/09-cults.md}, {@code design/25-bestiary-and-
 * entities.md}): only {@link Worshipper} exposes its owning cult ({@link Worshipper#cultId()}), so
 * it is read from the mob itself — a Fallen city's Hollow Choir worshipper is penalised correctly.
 * The four combat cultists keep their identity private to the {@code entity} package and roll it
 * from the cult registry at spawn, so no readable field exists for them; the war-band is faction-
 * generic in the design, which names a cult only for the plague crone ("hag-alchemist of the
 * Hollow Choir"). {@link #MOB_CULTS} therefore binds the mob-identity-closed family to the Hollow
 * Choir — the deep-places cult of {@code design/09} that the crone and the war-band serve in
 * practice — by entity-type path.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class CultKills {

    /** {@code design/03c-reputation.md}: killing a cult member costs 40 standing with that cult. */
    private static final int KILL_PENALTY = 40;

    /**
     * The explicit cultist table: entity-type path → cult id, for the mobs whose own cult is not
     * externally readable (see the class note). The worshipper is absent on purpose — its cult is
     * read from the entity, which also covers a worshipper reassigned by a city takeover.
     */
    private static final Map<String, String> MOB_CULTS = Map.of(
            "cult_zealot", "hollow_choir",
            "cult_raider", "hollow_choir",
            "rite_binder", "hollow_choir",
            "plague_crone", "hollow_choir");

    private CultKills() {
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }
        String cultId = cultOf(event.getEntity());
        if (cultId == null) {
            return;
        }
        int before = CultSystem.get(player, cultId);
        int after = CultSystem.set(player, cultId, before - KILL_PENALTY);
        int lost = before - after;
        if (lost > 0) {
            player.sendSystemMessage(Component.translatable("cult.eldritch_horror.kill.penalty",
                    Component.translatable("cult.eldritch_horror." + cultId), lost, after));
        }
    }

    /** The dead mob's cult id, or {@code null} when it is not a cultist. */
    private static @Nullable String cultOf(LivingEntity mob) {
        if (mob instanceof Worshipper worshipper) {
            return worshipper.cultId();
        }
        Identifier key = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
        return key == null ? null : MOB_CULTS.get(key.getPath());
    }
}
