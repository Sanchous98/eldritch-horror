package com.sanchous98.eldritchhorror.mixin;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * There is no experience/enchanting system. Cancelling the experience-granting methods stops XP
 * orbs, the enchanting table and the anvil from ever advancing the bar. The experience bar's HUD
 * slot is instead drawn as the corruption meter.
 *
 * <p>Magic in this mod is <b>rituals only</b>; enchanting has no role. See
 * {@code design/24-sanity-and-corruption.md} ("corruption replaces experience").
 */
@Mixin(Player.class)
public abstract class PlayerExperienceMixin {

    @Inject(method = "giveExperiencePoints", at = @At("HEAD"), cancellable = true)
    private void eldritchhorror$disableXpPoints(int amount, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "giveExperienceLevels", at = @At("HEAD"), cancellable = true)
    private void eldritchhorror$disableXpLevels(int levels, CallbackInfo ci) {
        ci.cancel();
    }
}
