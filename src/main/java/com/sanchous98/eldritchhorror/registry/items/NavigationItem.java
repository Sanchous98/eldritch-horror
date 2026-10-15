package com.sanchous98.eldritchhorror.registry.items;

import com.sanchous98.eldritchhorror.event.RiftKnowledge;
import com.sanchous98.eldritchhorror.event.Rifts;
import com.sanchous98.eldritchhorror.world.city.Cities;
import com.sanchous98.eldritchhorror.world.city.City;
import com.sanchous98.eldritchhorror.world.city.CityState;
import com.sanchous98.eldritchhorror.world.city.CityStates;
import com.sanchous98.eldritchhorror.world.loc.Location;
import com.sanchous98.eldritchhorror.world.loc.Locations;
import com.sanchous98.eldritchhorror.world.loc.city.CityLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * A navigation instrument ({@code design/22-map-and-knowledge.md}): right-clicking reports a bearing
 * to the nearest thing of the kind this one knows how to find. Every lookup reads an existing
 * server-side knowledge registry — the curated cities ({@link Cities}), the registered fixed
 * locations ({@link Locations}) and the holder's discovered rifts ({@link RiftKnowledge}) — so no
 * map data is duplicated and no chunk is force-loaded.
 *
 * <p>The report carries live coordinates, so the classes send a literal rather than a lang key,
 * exactly like the investigator ability messages.
 */
public final class NavigationItem extends Item {

    /** What the instrument points at. */
    public enum Target {
        /** The nearest curated settlement (known from the start; design/22). */
        SETTLEMENT,
        /**
         * The nearest curated city in the best (safest) derived {@link CityState}, for the compass
         * of quiet; a rift within ~{@value #RIFT_SPIN_RADIUS} blocks makes its needle spin wildly.
         */
        SAFE_SETTLEMENT,
        /** The nearest registered structure or ruin (cities are the city map's business). */
        SITE,
        /** The nearest rift the holder has discovered. */
        RIFT,
        /** The nearest fixed ritual-altar site. */
        ALTAR,
        /** The holder's own position ("fixed by the shifting stars"). */
        POSITION
    }

    /** A rift this close ("about sixteen blocks") sends the compass of quiet spinning. */
    private static final int RIFT_SPIN_RADIUS = 16;

    private final Target target;

    public NavigationItem(Properties properties, Target target) {
        super(properties);
        this.target = target;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(Component.literal(report(serverPlayer)));
        }
        return InteractionResult.SUCCESS;
    }

    /** The one-line report for this instrument's target (live data, so a literal). */
    private String report(ServerPlayer player) {
        return switch (this.target) {
            case SETTLEMENT -> nearestCity(player);
            case SAFE_SETTLEMENT -> nearestSafeCity(player);
            case SITE -> nearestSite(player);
            case RIFT -> nearestRift(player);
            case ALTAR -> nearestAltar(player);
            case POSITION -> "The stars fix your position: "
                    + player.getBlockX() + ", " + player.getBlockY() + ", "
                    + player.getBlockZ() + ".";
        };
    }

    /**
     * The nearest curated city in the best (safest) derived state: a {@link CityState} read from
     * the loaded district's taint ({@link CityStates#stateOf}). Ties on state go to the closer
     * city. A rift within {@value #RIFT_SPIN_RADIUS} blocks overrides the calm: the needle spins.
     */
    private static String nearestSafeCity(ServerPlayer player) {
        ServerLevel level = player.level();
        City best = null;
        CityState bestState = null;
        double bestDistance = Double.MAX_VALUE;
        for (City city : Cities.all()) {
            double distance = player.distanceToSqr(city.x(), player.getY(), city.z());
            CityState state = CityStates.stateOf(level, city);
            if (best == null || state.ordinal() < bestState.ordinal()
                    || (state == bestState && distance < bestDistance)) {
                best = city;
                bestState = state;
                bestDistance = distance;
            }
        }
        String report = best == null
                ? "The compass of quiet finds no settlement to trust."
                : "The compass of quiet settles toward " + best.name() + " ("
                        + bestState.displayName().getString() + "): "
                        + bearing(player, new BlockPos(best.x(), player.getBlockY(), best.z())) + ".";
        if (riftNear(player, level)) {
            report += " The needle spins wildly.";
        }
        return report;
    }

    /** Whether a loaded rift anchor sits within {@value #RIFT_SPIN_RADIUS} blocks (never force-loads). */
    private static boolean riftNear(ServerPlayer player, ServerLevel level) {
        double radiusSqr = (double) RIFT_SPIN_RADIUS * RIFT_SPIN_RADIUS;
        for (BlockPos pos : Rifts.near(level, player.blockPosition(), 1, 8)) {
            if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)
                    <= radiusSqr) {
                return true;
            }
        }
        return false;
    }

    private static String nearestCity(ServerPlayer player) {
        City nearest = null;
        double best = Double.MAX_VALUE;
        for (City city : Cities.all()) {
            double distance = player.distanceToSqr(city.x(), player.getY(), city.z());
            if (distance < best) {
                best = distance;
                nearest = city;
            }
        }
        if (nearest == null) {
            return "The map shows no settlements.";
        }
        BlockPos site = new BlockPos(nearest.x(), player.getBlockY(), nearest.z());
        return "The city map names " + nearest.name() + ": " + bearing(player, site) + ".";
    }

    private static String nearestSite(ServerPlayer player) {
        Location nearest = null;
        double best = Double.MAX_VALUE;
        for (Location loc : Locations.all()) {
            if (loc instanceof CityLocation) {
                continue;
            }
            double distance = player.distanceToSqr(Locations.xOf(loc), player.getY(), Locations.zOf(loc));
            if (distance < best) {
                best = distance;
                nearest = loc;
            }
        }
        if (nearest == null) {
            return "The lens reads no ruins.";
        }
        BlockPos site = new BlockPos(Locations.xOf(nearest), player.getBlockY(), Locations.zOf(nearest));
        return "The lens reads ruins: " + nearest.id().replace('_', ' ') + " "
                + bearing(player, site) + ".";
    }

    private static String nearestRift(ServerPlayer player) {
        BlockPos nearest = null;
        double best = Double.MAX_VALUE;
        for (String key : RiftKnowledge.known(player)) {
            BlockPos pos = RiftKnowledge.parse(key);
            if (pos == null) {
                continue;
            }
            double distance = player.distanceToSqr(
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            if (distance < best) {
                best = distance;
                nearest = pos;
            }
        }
        if (nearest == null) {
            return "The compass of longing finds no rift you know.";
        }
        return "The compass of longing tugs " + bearing(player, nearest) + ".";
    }

    private static String nearestAltar(ServerPlayer player) {
        Location nearest = null;
        double best = Double.MAX_VALUE;
        for (Location loc : Locations.byId("ritual_altar")) {
            double distance = player.distanceToSqr(Locations.xOf(loc), player.getY(), Locations.zOf(loc));
            if (distance < best) {
                best = distance;
                nearest = loc;
            }
        }
        if (nearest == null) {
            return "The veil compass finds no ritual site.";
        }
        BlockPos site = new BlockPos(Locations.xOf(nearest), player.getBlockY(), Locations.zOf(nearest));
        return "The veil compass points to a ritual site: " + bearing(player, site) + ".";
    }

    /** {@code "<direction>, N blocks"} from the player to {@code target} (shared with the lens). */
    static String bearing(ServerPlayer player, BlockPos target) {
        int dx = target.getX() - player.getBlockX();
        int dz = target.getZ() - player.getBlockZ();
        Direction direction = Direction.getNearest(dx, 0, dz, Direction.NORTH);
        int distance = (int) Math.round(Math.sqrt(player.distanceToSqr(
                target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5)));
        return direction.getName() + ", " + distance + " blocks";
    }
}
