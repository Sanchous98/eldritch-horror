package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.registry.ModEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Shared skeleton for the mundane (passive) bestiary roster ({@code design/25-bestiary-and-entities.md},
 * "Passive role"). These replace the food/hunger animals but must <b>not</b> be food — hunger is
 * removed — so they exist for ambience, contrast and taint. One base class so the roster shares its
 * goals, attributes, inert breeding and the single bounded "turns tainted" hook instead of copying
 * them per mob.
 *
 * <p>Breeding and feeding are inert ({@link #getBreedOffspring}, {@link #isFood}) because the world
 * is not a farm; {@link #taintable()} lets {@code mire_sow}/{@code hill_hound}/{@code bog_bear} opt
 * into {@link BestiarySupport#maybeTaintConvert}. Server behaviour hooks override
 * {@link #serverBehavior}.
 */
public abstract class MundaneAnimal extends Animal {

    protected MundaneAnimal(EntityType<? extends MundaneAnimal> type, Level level) {
        super(type, level);
    }

    /** Common passive goals: float, panic when hurt, wander, watch the player. */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.4));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.9));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    /** Base quadruped attributes shared by the roster (health, speed, follow range). */
    public static AttributeSupplier.Builder animalAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    /** Whether this animal can turn {@code tainted_fauna} when its chunk is tainted. */
    protected boolean taintable() {
        return false;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        // Check on a cheap cadence, not every tick; cooldown + chance live in the shared helper.
        if (this.tickCount % 40 == 0 && this.taintable()) {
            BestiarySupport.maybeTaintConvert(this, ModEntities.TAINTED_FAUNA.get(),
                    ModConfig.ENABLE_TAINT_CONVERSION.get(), ModConfig.TAINT_CONVERSION_CHANCE.get(),
                    ModConfig.TAINT_CONVERSION_COOLDOWN_TICKS.get(), (int) level.getGameTime());
        }
        this.serverBehavior(level);
    }

    /** Per-mob server-side hook (warnings, deterrents); default does nothing. */
    protected void serverBehavior(ServerLevel level) {
    }

    /** Mundane things do not breed here; the abstraction is satisfied inertly. */
    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return null;
    }

    /** Not livestock: nothing tempts or feeds it. */
    @Override
    public boolean isFood(ItemStack itemStack) {
        return false;
    }
}
