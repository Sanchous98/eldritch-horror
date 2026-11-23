package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.world.rail.TrainTimetable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * One car of a scheduled train ({@code design/30-transport.md}, stage 2): the locomotive itself or
 * one of its coupled wagons. Each car has {@value TrainTimetable#MAX_SEATS} seats and is boarded
 * only through the fare interaction while the consist's boarding window is open.
 *
 * <p>The car is a plain entity, not a vanilla minecart: movement is server-driven along the cached
 * line (a minecart's own rail physics would fight it), and the client draws a full carriage model
 * ({@code client/model/TrainCarModel}). A wagon holds no tickets of its own — the locomotive's 5×5
 * ticket covers the whole consist.
 *
 * <p>The carriage is modelled at real scale: 14 blocks long for a wagon (16 for the locomotive),
 * 3 wide and about 3.8 tall, with a walkable floor, window bands and twelve chairs in
 * {@link TrainSeats}. The entity box stays small (the dimensions are the car cross-section); the
 * renderer uses {@link #renderBounds} so the whole length is frustum-culled as one object.
 */
public abstract class TrainCar extends Entity {
    /** Wagon body length, in blocks (the locomotive overrides this). */
    private static final double WAGON_LENGTH = 14.0;

    private @Nullable TrainLocomotive locomotive;
    private int carIndex;

    protected TrainCar(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    /**
     * Wires a freshly spawned car to its locomotive.
     *
     * @param locomotive the consist leader
     * @param index      0 for the locomotive, 1..n for wagons
     */
    public void configure(TrainLocomotive locomotive, int index) {
        this.locomotive = locomotive;
        this.carIndex = Math.max(0, index);
    }

    /** @return the consist leader, or {@code null} before {@link #configure}. */
    public @Nullable TrainLocomotive locomotive() {
        return this.locomotive;
    }

    /** @return this car's index in the consist. */
    public int carIndex() {
        return this.carIndex;
    }

    /** Breaks the link to the locomotive (called when the consist is torn down). */
    protected void detach() {
        this.locomotive = null;
    }

    /** @return this car's configured seat count (never above the twelve physical chairs). */
    public int seats() {
        return TrainTimetable.seats(ModConfig.TRAIN_SEATS.get());
    }

    /** @return the car's length in blocks, used for culling. */
    public double carLength() {
        return WAGON_LENGTH;
    }

    /**
     * A frustum-culling box that covers the whole carriage at any heading. The entity box itself is
     * only the cross-section (a rotated 14-block body cannot be expressed by
     * {@link EntityDimensions}), so the renderer asks for this inflated box instead.
     *
     * @param partialTicks interpolation fraction
     * @return a box around the interpolated position, wide enough for the full car length
     */
    public AABB renderBounds(float partialTicks) {
        double half = this.carLength() * 0.5 + 1.0;
        return this.getInterpolatedBoundingBox(partialTicks)
                .inflate(half, 0.5, half);
    }

    /** @return whether the consist's boarding window is open. */
    public boolean isBoardingOpen() {
        return this.locomotive != null && this.locomotive.isBoardingOpen();
    }

    /**
     * The one boarding path, shared by right-click and {@code /eh train board}: checks the window,
     * a free seat and the fare, charges it, then seats the player.
     *
     * @param player the player trying to board
     * @return true when the player is now a passenger of this car
     */
    public boolean tryBoard(ServerPlayer player) {
        if (!this.isBoardingOpen()) {
            player.sendSystemMessage(
                    Component.translatable("message.eldritch_horror.train.not_boarding"), true);
            return false;
        }
        TrainLocomotive lead = this.locomotive();
        if (lead != null && lead.trainTicks() == 0) {
            // A consist that has not ticked since it was spawned is a startup dud (its chunk was
            // loaded but not yet entity-ticking); the service replaces it within a few seconds.
            player.sendSystemMessage(
                    Component.translatable("message.eldritch_horror.train.not_ready"), true);
            return false;
        }
        if (this.getPassengers().size() >= this.seats()) {
            player.sendSystemMessage(
                    Component.translatable("message.eldritch_horror.train.full"), true);
            return false;
        }
        int fare = Math.max(0, ModConfig.TRAIN_FARE.get());
        int carried = countObols(player);
        if (carried < fare) {
            player.sendSystemMessage(
                    Component.translatable("message.eldritch_horror.train.no_fare", fare), true);
            return false;
        }
        if (fare > 0) {
            takeObols(player, fare);
        }
        if (!player.startRiding(this, true, true)) {
            if (fare > 0) {
                giveObols(player, fare);
            }
            player.sendSystemMessage(
                    Component.translatable("message.eldritch_horror.train.not_boarding"), true);
            return false;
        }
        player.sendSystemMessage(
                Component.translatable("message.eldritch_horror.train.boarded", fare), true);
        return true;
    }

    private static Item obol() {
        return BuiltInRegistries.ITEM.getValue(EldritchHorror.id("black_obol"));
    }

    private static int countObols(Player player) {
        Item obol = obol();
        if (obol == Items.AIR) {
            return 0;
        }
        int total = 0;
        for (ItemStack stack : player.getInventory()) {
            if (stack.getItem() == obol) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static void takeObols(Player player, int count) {
        Item obol = obol();
        int left = count;
        for (ItemStack stack : player.getInventory()) {
            if (left <= 0) {
                break;
            }
            if (stack.getItem() != obol) {
                continue;
            }
            int taken = Math.min(left, stack.getCount());
            stack.shrink(taken);
            left -= taken;
        }
    }

    static void giveObols(Player player, int count) {
        Item obol = obol();
        if (obol != Items.AIR) {
            player.getInventory().placeItemBackInInventory(new ItemStack(obol, count),
                    Prediction.SERVER_ONLY);
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
        if (this.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }
        return tryBoard(serverPlayer) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().size() < this.seats();
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions,
                                               float scale) {
        int index = Math.max(0, this.getPassengers().indexOf(passenger));
        double[] seat = TrainSeats.offset(index, this instanceof TrainLocomotive);
        return new Vec3(seat[0], TrainSeats.RIDE_HEIGHT, seat[1])
                .yRot(-this.getYRot() * (float) (Math.PI / 180.0));
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean hurtServer(net.minecraft.server.level.ServerLevel level,
                              net.minecraft.world.damagesource.DamageSource source, float damage) {
        return true;
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder data) {
        // No synced fields: position and yaw are enough for the placeholder renderer.
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        // A consist is process state, not world state; never saved.
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        // A consist is process state, not world state; never saved.
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && this.locomotive != null
                && this.locomotive.isRemoved()) {
            this.discard();
        }
    }
}
