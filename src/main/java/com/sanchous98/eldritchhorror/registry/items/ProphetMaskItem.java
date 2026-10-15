package com.sanchous98.eldritchhorror.registry.items;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.registry.ModAttributes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Prophet's Mask (design/16), carried (any inventory slot) — all three recorded effects:
 *
 * <ul>
 *   <li><b>"+25% summoning power"</b> is read as <b>+25% to the {@code summon_star_spawn} spawn
 *       count</b> while carried — the only summoning hook that exists: 1 star-spawn becomes 2
 *       ({@code Math.ceil(1 × 1.25)}), still refused past the rite's {@code STAR_CAP}. The hook
 *       lives in {@code rite/RiteEngine.spawn} beside that cap.</li>
 *   <li><b>"villagers flee"</b> — nearby villagers are pushed into the vanilla panic activity and
 *       given the holder as the danger they run from. The brain memories are used rather than
 *       {@code Villager#setLastHurtByMob}, whose override files a village reputation event: wearing
 *       the mask must not cost standing, only calm.</li>
 *   <li><b>"sanity floor is lower"</b> — a small negative {@code max_sanity} transient modifier
 *       while carried, reconciled once a second by {@code sanity/SanityTicker} (a transient
 *       modifier is not saved, so removing it when the mask leaves the inventory is the ticker's
 *       job).</li>
 * </ul>
 */
public final class ProphetMaskItem extends Item {

    /** The carried max-sanity penalty: 10 points off the ceiling (design/16: "floor is lower"). */
    private static final double FLOOR_PENALTY = -10.0;
    /** Attribute modifier id; transient, re-asserted by the sanity ticker. */
    private static final Identifier FLOOR_MODIFIER = EldritchHorror.id("prophet_mask_floor");
    /** Villagers inside this radius panic; checked twice a second, not every tick. */
    private static final double PANIC_RADIUS = 8.0;
    private static final int PANIC_CHECK_INTERVAL_TICKS = 10;

    public ProphetMaskItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack itemStack, ServerLevel level, Entity owner,
                              @Nullable EquipmentSlot slot) {
        if (!(owner instanceof ServerPlayer player)) {
            return;
        }
        reconcile(player);
        if (level.getGameTime() % PANIC_CHECK_INTERVAL_TICKS != 0) {
            return;
        }
        for (Villager villager : level.getEntitiesOfClass(Villager.class,
                player.getBoundingBox().inflate(PANIC_RADIUS), Villager::isAlive)) {
            Brain<Villager> brain = villager.getBrain();
            brain.setMemory(MemoryModuleType.HURT_BY_ENTITY, player);
            brain.setActiveActivityIfPossible(Activity.PANIC);
        }
    }

    /**
     * Applies the floor penalty while {@code player} carries the mask, and removes it otherwise.
     * Called from the item's tick (immediate, while carried) and from {@code SanityTicker} (the
     * removal pass, once a second, for a mask that has left the inventory).
     */
    public static void reconcile(ServerPlayer player) {
        AttributeInstance instance = player.getAttribute(ModAttributes.MAX_SANITY);
        if (instance == null) {
            return; // attribute absent (non-player or removed): never throw
        }
        if (CarriedItems.carries(player, "prophet_mask")) {
            instance.addOrUpdateTransientModifier(new AttributeModifier(
                    FLOOR_MODIFIER, FLOOR_PENALTY, AttributeModifier.Operation.ADD_VALUE));
        } else {
            instance.removeModifier(FLOOR_MODIFIER);
        }
    }
}
