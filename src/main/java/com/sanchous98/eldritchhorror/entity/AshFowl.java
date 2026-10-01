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
 * Ash fowl — the chicken/parrot role: a common bird whose <b>silence before a presence is the
 * tell</b>. Family: mundane (warning). It watches for our nearby hostile/tainted mobs and goes
 * quiet while one is close, and the hush itself is a very small sanity drain (the shared
 * {@link DreadAura}), so the warning is felt before it is seen.
 */
public final class AshFowl extends MundaneAnimal implements DreadAura {

    /** How far the bird listens for a presence; loaded entities only. */
    private static final double PRESENCE_RADIUS = 16.0;

    /** Set server-side when a wrong thing is near; also gates the warning aura. */
    private boolean alarmed;

    public AshFowl(EntityType<? extends AshFowl> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return animalAttributes()
                .add(Attributes.MAX_HEALTH, 4.0)
                .add(Attributes.MOVEMENT_SPEED, 0.30);
    }

    @Override
    protected void serverBehavior(ServerLevel level) {
        if (this.tickCount % 20 != 0) {
            return;
        }
        AABB box = this.getBoundingBox().inflate(PRESENCE_RADIUS);
        this.alarmed = !level.getEntitiesOfClass(Mob.class, box,
                mob -> mob != this && mob.isAlive()
                        && (mob instanceof DreadAura
                                || mob.getType().getCategory() == MobCategory.MONSTER))
                .isEmpty();
        this.setSilent(this.alarmed);
    }

    // --- Warning hush (DreadAura) ---------------------------------------------------------------

    @Override
    public Axis auraAxis() {
        return Axis.SANITY;
    }

    @Override
    public boolean isAuraActive() {
        // The bird only unsettles while it is actually hushing before a presence.
        return this.alarmed && ModConfig.ENABLE_SANITY.get() && ModConfig.ENABLE_SANITY_ASH_FOWL.get();
    }

    @Override
    public double auraRate() {
        return ModConfig.SANITY_ASH_FOWL_RATE.get();
    }

    @Override
    public double auraRadius() {
        return ModConfig.SANITY_ASH_FOWL_RADIUS.get();
    }

    @Override
    public int auraMaxStack() {
        return ModConfig.SANITY_ASH_FOWL_MAX.get();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.ASH_FOWL_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.ASH_FOWL_AMBIENT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ASH_FOWL_AMBIENT.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.CHICKEN_STEP.value(), 0.12F, 1.0F);
    }
}
