package com.sanchous98.eldritchhorror.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * There is no hunger system. Cancelling {@link FoodData#tick(ServerPlayer)} stops food from ever
 * being consumed (no exhaustion, no saturation drain, no starvation, no hunger-based weakness).
 * Because a fresh player starts with a full food level, vanilla health regeneration still works —
 * the sanity meter simply occupies the food bar's HUD slot instead.
 *
 * <p>See {@code design/24-sanity-and-corruption.md} ("Sanity replaces hunger").
 */
@Mixin(FoodData.class)
public abstract class FoodDataMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void eldritchhorror$disableHunger(ServerPlayer player, CallbackInfo ci) {
        ci.cancel();
    }
}
