package com.sanchous98.eldritchhorror.entity.combat;

import com.sanchous98.eldritchhorror.entity.AncientOne;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;

/**
 * One telegraphed, phase-gated attack of an Ancient One. An ability is <b>never</b> instant: the
 * controller spends {@link #telegraphTicks()} ticks showing particles, a warning sound and an
 * action-bar line before {@link #execute} lands, so a player can step out or burst the boss to
 * interrupt (see {@link BossCombat}).
 *
 * <p>Every method is server-side; nothing here touches client code. Implementations are cheap to
 * declare through {@link SimpleBossAbility#builder(String)}.
 */
public interface BossAbility {

    /** Stable id, unique per boss; used for cooldowns and for the {@code /eh boss} listing. */
    String id();

    /** Ticks between two uses of this ability (scaled by 0.7 while the boss is ENRAGED). */
    int cooldownTicks();

    /** Wind-up length. Must be at least 20 ticks (1s) so the telegraph is readable. */
    int telegraphTicks();

    /** Maximum distance to the target at which this ability may start. */
    double range();

    /** Earliest {@link AncientOne.Phase} in which this ability is available. */
    AncientOne.Phase minPhase();

    /** Extra per-ability gate (line of sight, target state, ...). Default: always usable. */
    boolean canUse(AncientOne boss, ServerLevel level, Player target);

    /** Called once per wind-up tick: particles, extra sounds, the boss facing the target. */
    void telegraphTick(AncientOne boss, ServerLevel level, Player target, int ticksLeft);

    /** Lands the attack. Called exactly once, after the wind-up, unless interrupted. */
    void execute(AncientOne boss, ServerLevel level, Player target);

    /** Action-bar line shown to every player within 32 blocks when the wind-up starts. */
    Component warning();

    /** Sound played at the boss when the wind-up starts. */
    default SoundEvent warningSound() {
        return SoundEvents.WARDEN_SONIC_CHARGE;
    }
}
