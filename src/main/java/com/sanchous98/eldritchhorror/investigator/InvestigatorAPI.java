package com.sanchous98.eldritchhorror.investigator;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.registry.ModAttachments;
import com.sanchous98.eldritchhorror.registry.ModAttributes;
import com.sanchous98.eldritchhorror.rite.RiteKnowledge;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Server-authoritative access to a player's investigator, layered over the synced
 * {@link ModAttachments#INVESTIGATOR} attachment. Content never touches the attachment directly; it
 * calls this facade, exactly like {@code rite/RiteKnowledge} and {@code codex/CodexAPI}.
 *
 * <p>Choosing an investigator is permanent: {@link #set} is idempotent for the same one (it grants
 * once and a repeat call is a no-op, so a pedestal cannot be farmed) and <b>refuses</b> to change an
 * already-chosen different one. The gamemaster {@code /eh investigator set <id>} is intended for a
 * player who has not chosen yet (e.g. with the prologue disabled). A successful grant applies the
 * investigator's kit, starting rites and the {@code max_sanity} attribute bonus.
 * See {@code design/14-classes.md} and {@code design/29-prologue.md}.
 */
public final class InvestigatorAPI {

    /**
     * The {@code max_sanity} modifier id. A fixed id makes the grant idempotent: a re-grant
     * replaces the previous amount instead of stacking a second modifier.
     */
    private static final Identifier MAX_SANITY_MODIFIER = EldritchHorror.id("investigator_max_sanity");

    private InvestigatorAPI() {
    }

    /**
     * @return the player's chosen investigator, or {@code null} when none has been chosen yet
     */
    public static @Nullable Investigator get(ServerPlayer player) {
        return Investigator.byId(player.getData(ModAttachments.INVESTIGATOR.get()));
    }

    /** @return whether the player has chosen an investigator. */
    public static boolean hasChosen(ServerPlayer player) {
        return get(player) != null;
    }

    /**
     * Chooses {@code id} for {@code player}: grants the kit, the starting rites and the
     * investigator's {@code max_sanity} bonus. Idempotent for the same investigator; refuses a
     * different one with a message.
     */
    public static void set(ServerPlayer player, Investigator id) {
        Investigator current = get(player);
        if (current == id) {
            return; // already this investigator: idempotent, never grants a second kit
        }
        if (current != null) {
            player.sendSystemMessage(Component.translatableWithFallback(
                    "investigator.eldritch_horror.locked", "Your investigator is already chosen: %s. Ask an operator to reset it.",
                    current.displayName()));
            return;
        }
        grant(player, id);
    }

    /**
     * Gamemaster debug reset: switches {@code player} to {@code id} and re-grants that investigator's
     * kit, starting rites and bonus, even if a different one was already chosen. This is the only
     * way to change an investigator (see {@code design/29-prologue.md}).
     */
    public static void forceSet(ServerPlayer player, Investigator id) {
        grant(player, id);
    }

    /** Re-applies the stored investigator's attribute bonus; call after respawn, which drops it. */
    public static void reapply(ServerPlayer player) {
        Investigator id = get(player);
        if (id != null) {
            applyMaxSanity(player, id);
        }
    }

    /**
     * A non-committal preview of {@code id}: the fantasy, starter kit and starting rites, so a
     * player can compare every investigator before choosing one (the choice itself is permanent).
     */
    public static void preview(ServerPlayer player, Investigator id) {
        player.sendSystemMessage(Component.translatableWithFallback(
                "investigator.eldritch_horror.preview_head", "— %s —", id.displayName()));
        player.sendSystemMessage(Component.literal("").append(Investigators.occupation(id))
                .append(Component.literal(" · ")).append(id.role().displayName()));
        player.sendSystemMessage(Investigators.description(id));
        player.sendSystemMessage(Component.translatableWithFallback(
                "investigator.eldritch_horror.preview_kit", "Starts with:"));
        for (ItemStack stack : Investigators.starterKit(id)) {
            if (!stack.isEmpty()) {
                player.sendSystemMessage(Component.literal("  • ").append(stack.getHoverName()));
            }
        }
        for (String riteId : Investigators.startingRites(id)) {
            player.sendSystemMessage(Component.literal("  • ")
                    .append(Component.translatable("rite.eldritch_horror." + riteId)));
        }
        player.sendSystemMessage(Component.translatableWithFallback(
                "investigator.eldritch_horror.preview_confirm",
                "Right-click the same pedestal again to choose this investigator."));
    }

    private static void grant(ServerPlayer player, Investigator id) {
        player.setData(ModAttachments.INVESTIGATOR.get(), id.id());
        grantKit(player, id);
        for (String riteId : Investigators.startingRites(id)) {
            RiteKnowledge.add(player, riteId);
        }
        applyMaxSanity(player, id);
        player.sendSystemMessage(Component.translatableWithFallback(
                "investigator.eldritch_horror.chosen", "You are now %s.", id.displayName()));
    }

    private static void grantKit(ServerPlayer player, Investigator id) {
        for (ItemStack stack : Investigators.starterKit(id)) {
            if (stack.isEmpty()) {
                continue;
            }
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false, Prediction.SERVER_ONLY);
            }
        }
    }

    /** Adds the investigator's {@code max_sanity} fraction, replacing any previous modifier. */
    private static void applyMaxSanity(ServerPlayer player, Investigator id) {
        AttributeInstance instance = player.getAttribute(ModAttributes.MAX_SANITY);
        if (instance == null) {
            return; // attribute absent (non-player or removed): never throw
        }
        instance.removeModifier(MAX_SANITY_MODIFIER);
        // Permanent, so the attribute survives save/reload; transient modifiers are not persisted.
        double amount = Progression.maxSanityBonus(id);
        if (amount != 0.0) {
            instance.addOrReplacePermanentModifier(new AttributeModifier(
                    MAX_SANITY_MODIFIER, amount, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
    }
}
