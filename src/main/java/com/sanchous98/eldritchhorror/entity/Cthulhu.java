package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Cthulhu — the drowned-temple Ancient One, replacing the Elder Guardian
 * ({@code design/28-ancient-ones.md}, roster ★1). Site boss: high health, strong in water, and the
 * dream-leak sanity drain is its real hook. Two phases (mid-health stirs, low-health enrages and
 * gains attack damage).
 *
 * <p>Model reuse: the vanilla {@code RavagerModel} (a large quadruped) is the placeholder; the
 * tentacled silhouette is art's job. The trigger that makes it appear once at
 * {@code eldritch_horror:site/drowned_temple} lives in {@link CthulhuSiteTrigger}.
 */
public final class Cthulhu extends AncientOne {

    /** Transient attack-damage bump applied while the enraged phase is the current band. */
    private static final Identifier ENRAGE_MODIFIER = Identifier.fromNamespaceAndPath("eldritch_horror", "cthulhu_enrage");

    public Cthulhu(EntityType<? extends Cthulhu> type, Level level) {
        super(type, level, BossEvent.BossBarColor.BLUE, Component.translatable("entity.eldritch_horror.cthulhu"));
        this.xpReward = 50;
        if (!ModConfig.CTHULHU_KILLABLE.get()) {
            this.setPermanentlyInvulnerable(true);
        }
    }

    /** 160 HP, 7 attack, 0.20 speed, 40 follow range, heavy knockback resistance. */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 160.0)
                .add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.MOVEMENT_SPEED, 0.20)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.ARMOR, 6.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8);
    }

    /** Aquatic-strong: it does not drown in the flooded hall it owns. */
    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    /** Enrage at the shared threshold; while enraged it hits harder. */
    @Override
    protected void onPhaseChanged(ServerLevel level, Phase previous, Phase now) {
        AttributeInstance attack = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack == null) {
            return;
        }
        if (now == Phase.ENRAGED) {
            attack.addOrUpdateTransientModifier(new AttributeModifier(
                    ENRAGE_MODIFIER, 3.0, AttributeModifier.Operation.ADD_VALUE));
            this.playSound(SoundEvents.ELDER_GUARDIAN_CURSE, 1.0F, 0.6F);
        } else if (previous == Phase.ENRAGED) {
            attack.removeModifier(ENRAGE_MODIFIER);
        }
    }

    /** Dream-leak: a wide, strong, single-instance sanity drain. */
    @Override
    public Axis auraAxis() {
        return Axis.SANITY;
    }

    @Override
    public boolean isAuraActive() {
        return ModConfig.ENABLE_SANITY.get() && ModConfig.ENABLE_SANITY_CTHULHU.get();
    }

    @Override
    public double auraRate() {
        // The dream deepens as it wakes: the enraged band drains faster.
        return this.phase() == Phase.ENRAGED
                ? ModConfig.SANITY_CTHULHU_RATE.get() * 1.5
                : ModConfig.SANITY_CTHULHU_RATE.get();
    }

    @Override
    public double auraRateFor(net.minecraft.server.level.ServerPlayer player) {
        // Sleeping near it is worse than being awake: the dream-leak doubles the drain.
        return player.isSleeping() ? auraRate() * 2.0 : auraRate();
    }

    @Override
    public double auraRadius() {
        return ModConfig.SANITY_CTHULHU_RADIUS.get();
    }

    // --- Presence: fewer, louder sounds ---------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.CTHULHU_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.CTHULHU_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.CTHULHU_DEATH.get();
    }

    /** Heard before seen: its presence carries across the water. */
    @Override
    protected float getSoundVolume() {
        return 2.5F;
    }

    /** Rarer than the default: a slow, vast presence, not a chatterbox. */
    @Override
    public int getAmbientSoundInterval() {
        return 160;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
        this.playSound(SoundEvents.ELDER_GUARDIAN_FLOP, 0.25F, 0.6F);
    }
}
