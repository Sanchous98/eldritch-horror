package com.sanchous98.eldritchhorror.world.city.quest;

import com.sanchous98.eldritchhorror.world.city.City;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.game.ClientboundTrackedWaypointPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.waypoints.Waypoint;
import net.minecraft.world.waypoints.WaypointStyleAssets;

/**
 * The contract's dot on the vanilla locator bar ({@code ClientboundTrackedWaypointPacket}).
 *
 * <p>One stable UUID per city ({@code eh_cityquest:<city id>}) keeps the waypoint identity across
 * steps: the first packet tracks it at the giver, every advance moves the same dot with
 * {@code updateWaypointPosition}, and completion untracks it. The uuid is derived from the city,
 * not the player, so two players in the same city get independent dots (the client keys them per
 * connection) while a re-join naturally reuses the same id.
 *
 * <p>Packets go only to the contract owner's connection. The in-memory {@link #SENT} map is purely
 * an optimisation — it stops the 1 Hz ticker from re-sending an unchanged dot — and is cleared on
 * logout so a fresh session gets a fresh {@code addWaypointPosition}.
 */
public final class ContractWaypoint {

    /** Gold, matching the Order's coin and the "objective" feel of the dot. */
    private static final int GOLD = 0xFFAA00;

    private static final Map<UUID, Sent> SENT = new ConcurrentHashMap<>();

    private ContractWaypoint() {
    }

    /** Tracks the dot on {@code step} for {@code player}, adding it on first use and updating it after. */
    public static void show(ServerPlayer player, City city, CityQuestChain.Step step, int index,
                            ServerLevel level) {
        UUID playerId = player.getUUID();
        Sent last = SENT.get(playerId);
        if (last != null && last.city().equals(city.id()) && last.step() == index) {
            return;
        }
        Vec3i position = new Vec3i(step.x(), CityQuests.objectiveY(level, step), step.z());
        UUID waypoint = id(city);
        ClientboundTrackedWaypointPacket packet = last != null && last.city().equals(city.id())
                ? ClientboundTrackedWaypointPacket.updateWaypointPosition(waypoint, icon(), position)
                : ClientboundTrackedWaypointPacket.addWaypointPosition(waypoint, icon(), position);
        player.connection.send(packet);
        SENT.put(playerId, new Sent(city.id(), index));
    }

    /** Untracks the dot when the contract is complete. */
    public static void hide(ServerPlayer player, City city) {
        player.connection.send(ClientboundTrackedWaypointPacket.removeWaypoint(id(city)));
        Sent last = SENT.get(player.getUUID());
        if (last != null && last.city().equals(city.id())) {
            SENT.remove(player.getUUID());
        }
    }

    /** Drops the session cache for a player who left (the client clears its bar on disconnect). */
    public static void forget(ServerPlayer player) {
        SENT.remove(player.getUUID());
    }

    /** The stable waypoint id of a city contract. */
    public static UUID id(City city) {
        return UUID.nameUUIDFromBytes(("eh_cityquest:" + city.id())
                .getBytes(StandardCharsets.UTF_8));
    }

    /** Gold, default style: a small round dot with our colour. */
    private static Waypoint.Icon icon() {
        Waypoint.Icon icon = new Waypoint.Icon();
        icon.style = WaypointStyleAssets.DEFAULT;
        icon.color = Optional.of(GOLD);
        return icon;
    }

    /** Last dot sent to one player: city and step index (both immutable contract facts). */
    private record Sent(String city, int step) {
    }
}
