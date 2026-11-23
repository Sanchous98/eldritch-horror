package com.sanchous98.eldritchhorror.sanity;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.registry.ModEffects;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * What happens when a mind that is already at zero takes another large hit: the excess does not
 * simply vanish. It collapses into <b>Stupor</b> (nausea, pulsing blindness, slowness) and, for a
 * very large blow, a capped bite of health. A splash of Smelling Salts (Waking) cures the Stupor
 * instantly; solo players can also wait it out.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class SanityCollapse {

    private static final Map<UUID, Long> LAST = new ConcurrentHashMap<>();

    private SanityCollapse() {
    }

    /** @param loss the size of the sanity loss that hit a player already at zero */
    public static void trigger(ServerPlayer player, double loss) {
        if (!ModConfig.ENABLE_SANITY_COLLAPSE.get()) {
            return;
        }
        long now = player.level().getGameTime();
        Long last = LAST.get(player.getUUID());
        if (last != null && now - last < ModConfig.SANITY_COLLAPSE_COOLDOWN.get()) {
            return;
        }
        LAST.put(player.getUUID(), now);
        player.addEffect(new MobEffectInstance(ModEffects.STUPOR,
                ModConfig.SANITY_COLLAPSE_STUPOR_TICKS.get(), 0, false, true, true));
        if (loss >= ModConfig.SANITY_COLLAPSE_DAMAGE_THRESHOLD.get()) {
            float damage = (float) Math.min(ModConfig.SANITY_COLLAPSE_DAMAGE_CAP.get(),
                    loss * ModConfig.SANITY_COLLAPSE_DAMAGE_FACTOR.get());
            if (damage > 0.0F) {
                player.hurt(player.damageSources().magic(), damage);
            }
        }
        player.sendSystemMessage(Component.translatable("sanity.collapse.eldritch_horror"));
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        LAST.remove(event.getEntity().getUUID());
    }
}
