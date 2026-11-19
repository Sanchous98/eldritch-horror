package com.sanchous98.eldritchhorror.skill;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.entity.AncientOne;
import java.util.stream.Collectors;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * {@link Skills#DIVINATION}: a server-side empty-hand right-click on a living entity reports its
 * type, its entity-type tags and — for an {@link AncientOne} — its aura axis, in one chat line.
 *
 * <p>Cheap and loaded-chunk-safe: the event already names an entity the player can reach, so nothing
 * is scanned and no chunk is generated. The interaction itself is not cancelled or changed.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class DivinationHandler {

    private DivinationHandler() {
    }

    /** Reads the target of an empty-hand interaction when the player has Divination unlocked. */
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        if (!(event.getTarget() instanceof LivingEntity target) || !event.getItemStack().isEmpty()) {
            return;
        }
        if (!SkillTree.has(player, Skills.DIVINATION)) {
            return;
        }
        String tags = target.getType().getTags()
                .map(tag -> tag.location().toString())
                .sorted()
                .collect(Collectors.joining(", "));
        Component tagText = tags.isEmpty()
                ? Component.translatable("skill.eldritch_horror.divination.no_tags")
                : Component.literal(tags);
        Component line;
        if (target instanceof AncientOne horror) {
            line = Component.translatable("skill.eldritch_horror.divination.ancient",
                    target.getType().getDescription(), tagText,
                    Component.literal(horror.auraAxis().name()));
        } else {
            line = Component.translatable("skill.eldritch_horror.divination.line",
                    target.getType().getDescription(), tagText);
        }
        player.sendSystemMessage(line);
    }
}
