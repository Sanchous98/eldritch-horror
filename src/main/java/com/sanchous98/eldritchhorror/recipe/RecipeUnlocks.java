package com.sanchous98.eldritchhorror.recipe;

import com.sanchous98.eldritchhorror.EldritchHorror;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Makes every mod recipe known from the first join.
 *
 * <p>Vanilla recipes are unlocked piecemeal by advancements, which leaves a fresh player with an
 * almost empty recipe book and no way to discover that a bone club or a silver blade exists. This
 * mod's crafting is part of its field kit: everything the mod ships is awarded on login, so the
 * recipe book is a complete catalogue from the start. Vanilla recipes keep their normal unlock
 * progression.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class RecipeUnlocks {

    private RecipeUnlocks() {
    }

    /** Awards every {@code eldritch_horror} recipe to the joining player. */
    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!(player.level() instanceof ServerLevel level) || level.getServer() == null) {
            return;
        }
        List<RecipeHolder<?>> modRecipes = level.getServer().getRecipeManager().getRecipes().stream()
                .filter(holder -> holder.id().identifier().getNamespace().equals(EldritchHorror.MODID))
                .toList();
        if (!modRecipes.isEmpty()) {
            player.awardRecipes(modRecipes);
        }
    }
}
