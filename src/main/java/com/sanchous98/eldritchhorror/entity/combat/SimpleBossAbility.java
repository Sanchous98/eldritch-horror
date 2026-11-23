package com.sanchous98.eldritchhorror.entity.combat;

import com.sanchous98.eldritchhorror.entity.AncientOne;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;

/**
 * Declarative {@link BossAbility}: a builder with sensible defaults (200-tick cooldown, 30-tick
 * wind-up, 12-block range, DORMANT, a smoke telegraph). Boss classes only override the bits that
 * matter, which keeps the per-entity ability tables readable.
 */
public final class SimpleBossAbility implements BossAbility {

    /** Extra gate, e.g. line of sight or "only on the ground". */
    @FunctionalInterface
    public interface Availability {
        boolean test(AncientOne boss, ServerLevel level, Player target);
    }

    /** Per-tick wind-up effect. */
    @FunctionalInterface
    public interface Telegraph {
        void tick(AncientOne boss, ServerLevel level, Player target, int ticksLeft);
    }

    /** The attack itself. */
    @FunctionalInterface
    public interface Action {
        void run(AncientOne boss, ServerLevel level, Player target);
    }

    private final String id;
    private final int cooldownTicks;
    private final int telegraphTicks;
    private final double range;
    private final AncientOne.Phase minPhase;
    private final Component warning;
    private final SoundEvent warningSound;
    private final Availability availability;
    private final Telegraph telegraph;
    private final Action action;

    private SimpleBossAbility(Builder builder) {
        this.id = builder.id;
        this.cooldownTicks = builder.cooldownTicks;
        this.telegraphTicks = builder.telegraphTicks;
        this.range = builder.range;
        this.minPhase = builder.minPhase;
        this.warning = builder.warning;
        this.warningSound = builder.warningSound;
        this.availability = builder.availability;
        this.telegraph = builder.telegraph;
        this.action = builder.action;
    }

    public static Builder builder(String id) {
        return new Builder(id);
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public int cooldownTicks() {
        return this.cooldownTicks;
    }

    @Override
    public int telegraphTicks() {
        return this.telegraphTicks;
    }

    @Override
    public double range() {
        return this.range;
    }

    @Override
    public AncientOne.Phase minPhase() {
        return this.minPhase;
    }

    @Override
    public boolean canUse(AncientOne boss, ServerLevel level, Player target) {
        return this.availability.test(boss, level, target);
    }

    @Override
    public void telegraphTick(AncientOne boss, ServerLevel level, Player target, int ticksLeft) {
        this.telegraph.tick(boss, level, target, ticksLeft);
    }

    @Override
    public void execute(AncientOne boss, ServerLevel level, Player target) {
        this.action.run(boss, level, target);
    }

    @Override
    public Component warning() {
        return this.warning;
    }

    @Override
    public SoundEvent warningSound() {
        return this.warningSound;
    }

    /** Fluent builder; every setter is optional. */
    public static final class Builder {
        private final String id;
        private int cooldownTicks = 200;
        private int telegraphTicks = 30;
        private double range = 12.0;
        private AncientOne.Phase minPhase = AncientOne.Phase.DORMANT;
        private Component warning = Component.literal("The presence gathers itself — move!");
        private SoundEvent warningSound = SoundEvents.WARDEN_SONIC_CHARGE;
        private Availability availability = (boss, level, target) -> true;
        private Telegraph telegraph = (boss, level, target, ticksLeft) ->
                BossAbilities.telegraphBurst(level, boss, ParticleTypes.SMOKE, 3);
        private Action action = (boss, level, target) -> {
        };

        private Builder(String id) {
            this.id = id;
        }

        public Builder cooldown(int ticks) {
            this.cooldownTicks = ticks;
            return this;
        }

        public Builder telegraph(int ticks) {
            this.telegraphTicks = ticks;
            return this;
        }

        public Builder range(double blocks) {
            this.range = blocks;
            return this;
        }

        public Builder minPhase(AncientOne.Phase phase) {
            this.minPhase = phase;
            return this;
        }

        public Builder warning(String text) {
            this.warning = Component.literal(text);
            return this;
        }

        public Builder sound(SoundEvent sound) {
            this.warningSound = sound;
            return this;
        }

        public Builder onlyWhen(Availability gate) {
            this.availability = gate;
            return this;
        }

        public Builder onTelegraph(Telegraph windUp) {
            this.telegraph = windUp;
            return this;
        }

        public Builder onExecute(Action attack) {
            this.action = attack;
            return this;
        }

        public SimpleBossAbility build() {
            return new SimpleBossAbility(this);
        }
    }
}
