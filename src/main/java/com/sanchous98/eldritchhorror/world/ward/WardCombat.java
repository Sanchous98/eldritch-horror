package com.sanchous98.eldritchhorror.world.ward;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.entity.AncientOne;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/**
 * The ward's damage window (design/26, "Warding and sealing"; design/18-bosses.md: the Leviathan is
 * "weak to silver and warding" and a {@code ward} interaction "opens a damage window").
 *
 * <p>When a player standing in an unexpired ward strikes an {@link AncientOne}, the blow is scaled
 * by {@code wardBossDamageBonus} (+25% by default). The ward does not enable the fight by itself:
 * it only matters for a boss that is configured killable — the Ancient One's own config gate still
 * decides whether it can be hurt or must be soothed with a rite. Server-side only.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class WardCombat {

    private WardCombat() {
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        if (!ModConfig.ENABLE_WARDS.get() || event.getNewDamage() <= 0.0F) {
            return;
        }
        if (!(event.getEntity() instanceof AncientOne)) {
            return;
        }
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)
                || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        if (WardLedger.of(level).wardAt(level, player.blockPosition()) == null) {
            return;
        }
        event.setNewDamage(WardMath.scaledBossDamage(event.getNewDamage(),
                ModConfig.WARD_BOSS_DAMAGE_BONUS.get()));
    }
}
