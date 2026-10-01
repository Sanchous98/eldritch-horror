package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Hearth cat — the cat/ocelot role: a settlement cat <b>unsettled by the wrong, not the dark</b>.
 * Family: mundane (warning). It runs a small sanity drain while a tainted/wrong thing is near (the
 * shared {@link DreadAura}), so it reads as unease rather than a night-spook.
 */
public final class HearthCat extends MundaneAnimal implements DreadAura {

    /** Radius in which a wrong thing unsettles the cat (design: unsettled by the wrong, not the dark). */
    private static final double WRONG_RADIUS = 12.0;

    /** Set server-side when a wrong thing is near; gates the warning aura. */
    private boolean alarmed;

    public HearthCat(EntityType<? extends HearthCat> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return animalAttributes()
                .add(Attributes.MAX_HEALTH, 8.0)
                .add(Attributes.MOVEMENT_SPEED, 0.30);
    }

    @Override
    protected void serverBehavior(ServerLevel level) {
        if (this.tickCount % 20 != 0) {
            return;
        }
        AABB box = this.getBoundingBox().inflate(WRONG_RADIUS);
        this.alarmed = !level.getEntitiesOfClass(Mob.class, box,
                mob -> mob != this && mob.isAlive()
                        && (mob instanceof DreadAura
                                || mob.getType().getCategory() == MobCategory.MONSTER))
                .isEmpty();
    }

    @Override
    public Axis auraAxis() {
        return Axis.SANITY;
    }

    @Override
    public boolean isAuraActive() {
        // Only unsettled while a wrong thing is actually near; the dark alone does not move it.
        return this.alarmed && ModConfig.ENABLE_SANITY.get() && ModConfig.ENABLE_SANITY_HEARTH_CAT.get();
    }

    @Override
    public double auraRate() {
        return ModConfig.SANITY_HEARTH_CAT_RATE.get();
    }

    @Override
    public double auraRadius() {
        return ModConfig.SANITY_HEARTH_CAT_RADIUS.get();
    }

    @Override
    public int auraMaxStack() {
        return ModConfig.SANITY_HEARTH_CAT_MAX.get();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.HEARTH_CAT_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.HEARTH_CAT_AMBIENT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.HEARTH_CAT_AMBIENT.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.SHEEP_STEP, 0.08F, 1.4F);
    }
}
