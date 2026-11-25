package com.sanchous98.eldritchhorror.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * One invisible, indestructible box that carries a piece of the liner's superstructure.
 *
 * <p>A Minecraft entity owns a single AABB, so the hull's own box is the deck floor and the cabin
 * block is made of these parts. Each part is positioned from a {@link ShipGeometry.PartSpec} in the
 * hull's local frame and rebuilt as the axis-aligned enclosure of its rotated box, so the cabin
 * stays solid at any heading. Parts never save, take damage, get pushed or show in the pick ray;
 * they exist only to give the deckhouse and bridge collision.
 *
 * <p>The hull moves them after it has moved itself (see {@code CoastalPacket.tick}), so a part is
 * never a tick behind the vessel it belongs to.
 */
public class ShipCollisionPart extends Entity {

    private static final EntityDataAccessor<Float> DATA_HALF_X = SynchedEntityData.defineId(
            ShipCollisionPart.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_HALF_Z = SynchedEntityData.defineId(
            ShipCollisionPart.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_BOTTOM = SynchedEntityData.defineId(
            ShipCollisionPart.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_TOP = SynchedEntityData.defineId(
            ShipCollisionPart.class, EntityDataSerializers.FLOAT);

    private @Nullable CoastalPacket hull;
    private ShipGeometry.@Nullable PartSpec spec;

    public ShipCollisionPart(EntityType<? extends ShipCollisionPart> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.blocksBuilding = true;
    }

    /** The box shape is synced, because the client must collide with the same cabin as the server. */
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_HALF_X, 0.5F);
        builder.define(DATA_HALF_Z, 0.5F);
        builder.define(DATA_BOTTOM, 0.0F);
        builder.define(DATA_TOP, 1.0F);
    }

    /**
     * Binds the part to a hull and one of its superstructure boxes.
     *
     * @param hull the vessel
     * @param spec the local box this part carries
     */
    public void configure(CoastalPacket hull, ShipGeometry.PartSpec spec) {
        this.hull = hull;
        this.spec = spec;
        this.entityData.set(DATA_HALF_X, (float) spec.halfX());
        this.entityData.set(DATA_HALF_Z, (float) spec.halfZ());
        this.entityData.set(DATA_BOTTOM, (float) spec.bottomY());
        this.entityData.set(DATA_TOP, (float) spec.topY());
        this.follow();
    }

    /** Re-seats the part on the hull's current position and heading. */
    public void follow() {
        CoastalPacket ship = this.hull;
        ShipGeometry.PartSpec part = this.spec;
        if (ship == null || part == null) {
            return;
        }
        double[] offset = ShipGeometry.rotateLocal(part.localX(), part.localZ(), ship.getYRot());
        this.setYRot(ship.getYRot());
        this.setPos(ship.getX() + offset[0], ship.getY(), ship.getZ() + offset[1]);
    }

    @Override
    public void tick() {
        // The client has no hull reference; it only needs the synced box for collision.
        if (this.level().isClientSide()) {
            return;
        }
        CoastalPacket ship = this.hull;
        if (ship == null || ship.isRemoved() || ship.level() != this.level()) {
            this.discard();
        }
    }

    @Override
    protected AABB makeBoundingBox(Vec3 position) {
        float halfX = this.entityData.get(DATA_HALF_X);
        float halfZ = this.entityData.get(DATA_HALF_Z);
        float bottom = this.entityData.get(DATA_BOTTOM);
        float top = this.entityData.get(DATA_TOP);
        double radians = Math.toRadians(this.getYRot());
        double cos = Math.abs(Math.cos(radians));
        double sin = Math.abs(Math.sin(radians));
        double x = halfX * cos + halfZ * sin;
        double z = halfX * sin + halfZ * cos;
        return new AABB(position.x - x, position.y + bottom, position.z - z,
                position.x + x, position.y + top, position.z + z);
    }

    /** A synced rotation packet must move the collision box, not only the (invisible) model. */
    @Override
    public void setYRot(float yRot) {
        if (yRot == this.getYRot()) {
            return;
        }
        super.setYRot(yRot);
        this.setBoundingBox(this.makeBoundingBox(this.position()));
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        float width = this.entityData.get(DATA_HALF_X) * 2.0F;
        float height = this.entityData.get(DATA_TOP) - this.entityData.get(DATA_BOTTOM);
        return EntityDimensions.fixed(width, height);
    }

    /** A solid surface for players: they stand on the cabin roof and cannot walk through it. */
    @Override
    public boolean canBeCollidedWith(@Nullable Entity other) {
        return true;
    }

    /** Pickable so the visible white cabin is also a large boarding target from the pier. */
    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    /** Right-clicking the cabin is right-clicking the ship. */
    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
        CoastalPacket ship = this.hull;
        if (ship == null || ship.isRemoved()) {
            return InteractionResult.PASS;
        }
        return ship.interact(player, hand, location);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    /** Never saved: it is spawned and removed with its hull. */
    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        // Declared abstract by Entity; these parts never persist.
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        // Declared abstract by Entity; these parts never persist.
    }

    /** Nothing can damage a liner's cabin block. */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return true;
    }
}
