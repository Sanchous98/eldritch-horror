package com.sanchous98.eldritchhorror.world.threshold;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.investigator.Investigator;
import com.sanchous98.eldritchhorror.registry.blocks.CityGateBlock;
import com.sanchous98.eldritchhorror.registry.blocks.InvestigatorPedestalBlock;
import com.sanchous98.eldritchhorror.registry.blocks.PrologueBlocks;
import com.sanchous98.eldritchhorror.world.city.Cities;
import com.sanchous98.eldritchhorror.world.city.City;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.entity.SignTextSlot;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/**
 * Stamps the authored Threshold hub once, when the server starts.
 *
 * <p>The dimension's chunk generator is a plain flat plane (see {@code dimension/threshold.json}),
 * so everything that makes the Threshold a place is built here instead: a central observatory
 * platform with the 12 investigator pedestals, a ring of 24 city gates, and a barrier wall that keeps
 * players on the island. The layout is fully deterministic — fixed coordinates, no randomness — so
 * it is reproducible across worlds and can be reviewed as code.
 *
 * <p>Idempotent: the obelisk base at the hub centre is a unique marker block, and the whole stamp
 * is skipped if it is already present. That makes the event safe on every subsequent server start
 * and cheap when the dimension has never been visited.
 *
 * <p>The stamp runs inside force-loaded chunks and releases its tickets afterwards, so the hub
 * chunks are generated (not left permanently loaded) and the island is written to disk. Placement
 * uses {@link Block#UPDATE_ALL} because the hub is small and settled before any player can arrive.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class ThresholdPlacement {

    /** Radius of the raised central platform, in blocks. */
    private static final int PLATFORM_RADIUS = 12;

    /** Radius of the inner ring on which the 12 investigator pedestals stand. */
    private static final int PEDESTAL_RADIUS = 8;

    /** Radius of the ring on which the 24 gates stand. */
    private static final int GATE_RADIUS = 34;
    /** Inner/outer radius of the decorative path band beneath the gates. */
    private static final int GATE_PATH_INNER = GATE_RADIUS - 2;
    private static final int GATE_PATH_OUTER = GATE_RADIUS + 2;

    /** Radius of the surrounding barrier wall (the island's hard edge). */
    private static final int ISLAND_RADIUS = 48;
    /** The barrier is two blocks thick, so no diagonal gap exists at this radius. */
    private static final int BARRIER_INNER = ISLAND_RADIUS - 1;

    /** Height of the island's enclosing wall, in blocks. */
    private static final int BARRIER_HEIGHT = 4;

    /** Number of destination gates, one per curated city. */
    private static final int GATE_COUNT = 24;
    /** Height of the walk-through opening: the portal column fills this many blocks above the floor. */
    private static final int GATE_OPENING_HEIGHT = 3;
    /** The lintel block sits directly on top of the opening. */
    private static final int GATE_LINTEL_Y = Threshold.FLOOR_Y + GATE_OPENING_HEIGHT;
    /** The standing sign sits one block above the lintel. */
    private static final int GATE_SIGN_Y = GATE_LINTEL_Y + 1;

    /** How many chunks either side of the origin to force-load around the island. */
    private static final int FORCELOAD_CHUNK_RADIUS = 4;

    /** The obelisk base doubles as the "hub already stamped" marker. */
    private static final BlockPos MARKER = new BlockPos(0, Threshold.FLOOR_Y, 0);

    private ThresholdPlacement() {
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        ServerLevel level = event.getServer().getLevel(Threshold.DIMENSION);
        if (level == null) {
            EldritchHorror.LOGGER.warn("threshold: dimension {} is not loaded; hub not stamped",
                    Threshold.DIMENSION.identifier());
            return;
        }
        if (isStamped(level)) {
            return;
        }
        stamp(level);
        EldritchHorror.LOGGER.info("threshold: stamped hub at {}", Threshold.spawn());
    }

    /** @return whether the hub marker is already present, meaning the hub needs no stamping. */
    private static boolean isStamped(ServerLevel level) {
        return level.getBlockState(MARKER).is(Blocks.CHISELED_DEEPSLATE);
    }

    private static void stamp(ServerLevel level) {
        ServerChunkCache chunks = level.getChunkSource();
        holdHubChunks(chunks, true);
        try {
            generateHubChunks(level);
            buildPlatform(level);
            buildObelisk(level);
            buildPedestals(level);
            buildGateRing(level);
            buildBarrier(level);
        } finally {
            holdHubChunks(chunks, false);
        }
    }

    /**
     * Adds/removes a radius-0 {@link TicketType#FORCED} ticket on every hub chunk. Radius 0 keeps
     * the chunk at FULL without promoting it to a ticking chunk, matching {@code CityRenderer}.
     */
    private static void holdHubChunks(ServerChunkCache chunks, boolean hold) {
        for (int cx = -FORCELOAD_CHUNK_RADIUS; cx <= FORCELOAD_CHUNK_RADIUS; cx++) {
            for (int cz = -FORCELOAD_CHUNK_RADIUS; cz <= FORCELOAD_CHUNK_RADIUS; cz++) {
                ChunkPos pos = new ChunkPos(cx, cz);
                if (hold) {
                    chunks.addTicketWithRadius(TicketType.FORCED, pos, 0);
                } else {
                    chunks.removeTicketWithRadius(TicketType.FORCED, pos, 0);
                }
            }
        }
    }

    /** Forces the hub chunks to FULL synchronously so the placements that follow cannot miss. */
    private static void generateHubChunks(ServerLevel level) {
        for (int cx = -FORCELOAD_CHUNK_RADIUS; cx <= FORCELOAD_CHUNK_RADIUS; cx++) {
            for (int cz = -FORCELOAD_CHUNK_RADIUS; cz <= FORCELOAD_CHUNK_RADIUS; cz++) {
                level.getChunk(cx, cz, ChunkStatus.FULL, true);
            }
        }
    }

    /** The observatory floor: a polished disc with a contrasting rim, set into the flat floor. */
    private static void buildPlatform(ServerLevel level) {
        fillDisc(level, 0, 0, PLATFORM_RADIUS, Threshold.FLOOR_Y, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
        // A contrasting rim so the platform reads as a deliberate structure.
        fillRing(level, 0, 0, PLATFORM_RADIUS - 1, PLATFORM_RADIUS, Threshold.FLOOR_Y,
                Blocks.CHISELED_DEEPSLATE.defaultBlockState());
    }

    /** The central obelisk/instrument marking the safe room's centre. */
    private static void buildObelisk(ServerLevel level) {
        for (int y = 0; y < 4; y++) {
            set(level, 0, Threshold.FLOOR_Y + y, 0, Blocks.CHISELED_DEEPSLATE.defaultBlockState());
        }
        set(level, 0, Threshold.FLOOR_Y + 4, 0, Blocks.SEA_LANTERN.defaultBlockState());
    }

    /** Places the 12 investigator pedestals on an inner ring around the obelisk, each raised on a
     * plinth with a named sign so the choice is obvious in-game. */
    private static void buildPedestals(ServerLevel level) {
        List<Investigator> investigators = List.of(Investigator.values());
        for (int i = 0; i < investigators.size(); i++) {
            double angle = i * (2.0 * Math.PI / investigators.size());
            int x = (int) Math.round(PEDESTAL_RADIUS * Math.cos(angle));
            int z = (int) Math.round(PEDESTAL_RADIUS * Math.sin(angle));
            Investigator investigator = investigators.get(i);
            placePedestal(level, x, z, i, investigator);
        }
    }

    /**
     * One raised pedestal: a chiseled-deepslate plinth on the platform, the investigator pedestal
     * variant ({@code investigator=i}) above it, and a standing sign (facing the obelisk) carrying the
     * investigator's name on line 0 and their role on line 1.
     */
    private static void placePedestal(ServerLevel level, int x, int z, int index,
                                      Investigator investigator) {
        set(level, x, Threshold.FLOOR_Y, z, Blocks.CHISELED_DEEPSLATE.defaultBlockState());
        BlockState pedestal = PrologueBlocks.INVESTIGATOR_PEDESTAL.get().defaultBlockState()
                .setValue(InvestigatorPedestalBlock.INVESTIGATOR, index);
        set(level, x, Threshold.FLOOR_Y + 1, z, pedestal);
        Direction facing = inwardFacing(Math.atan2(z, x));
        placeSign(level, new BlockPos(x, Threshold.FLOOR_Y + 2, z), facing,
                List.of(investigator.displayName(), investigator.role().displayName()));
    }

    /**
     * Lays the path band and raises the 24 evenly-spaced city gate arches around the platform. Each
     * arch is one block wide and three tall, oriented along the dominant tangent axis so the player
     * walks through it toward the ring centre, with a named standing sign over the lintel.
     */
    private static void buildGateRing(ServerLevel level) {
        fillRing(level, 0, 0, GATE_PATH_INNER, GATE_PATH_OUTER, Threshold.FLOOR_Y,
                Blocks.DEEPSLATE_TILES.defaultBlockState());
        List<City> cities = Cities.all();
        for (int i = 0; i < GATE_COUNT; i++) {
            double angle = Math.toRadians(i * (360.0 / GATE_COUNT));
            int cx = (int) Math.round(GATE_RADIUS * Math.cos(angle));
            int cz = (int) Math.round(GATE_RADIUS * Math.sin(angle));
            double tangentX = -Math.sin(angle);
            double tangentZ = Math.cos(angle);
            boolean alongX = Math.abs(tangentX) >= Math.abs(tangentZ);
            buildGateArch(level, cx, cz, i, alongX);
            if (i < cities.size()) {
                placeGateSign(level, cx, cz, angle, cities.get(i).name());
            }
        }
    }

    /**
     * Builds one frame arch and its portal column. When {@code alongX} the opening runs along the X
     * axis (posts offset in Z); otherwise along Z (posts offset in X). The two posts and the lintel
     * form a 3 x 3 frame around the non-solid portal column.
     */
    private static void buildGateArch(ServerLevel level, int cx, int cz, int city, boolean alongX) {
        BlockState frame = PrologueBlocks.CITY_GATE_FRAME.get().defaultBlockState();
        if (alongX) {
            set(level, cx, GATE_LINTEL_Y, cz - 1, frame);
            set(level, cx, GATE_LINTEL_Y, cz, frame);
            set(level, cx, GATE_LINTEL_Y, cz + 1, frame);
            for (int y = Threshold.FLOOR_Y; y < GATE_LINTEL_Y; y++) {
                set(level, cx, y, cz - 1, frame);
                set(level, cx, y, cz + 1, frame);
            }
        } else {
            set(level, cx - 1, GATE_LINTEL_Y, cz, frame);
            set(level, cx, GATE_LINTEL_Y, cz, frame);
            set(level, cx + 1, GATE_LINTEL_Y, cz, frame);
            for (int y = Threshold.FLOOR_Y; y < GATE_LINTEL_Y; y++) {
                set(level, cx - 1, y, cz, frame);
                set(level, cx + 1, y, cz, frame);
            }
        }
        BlockState gate = PrologueBlocks.CITY_GATE.get().defaultBlockState()
                .setValue(CityGateBlock.CITY, city);
        for (int y = Threshold.FLOOR_Y; y < GATE_LINTEL_Y; y++) {
            set(level, cx, y, cz, gate);
        }
    }

    /**
     * Places a standing sign on top of an arch lintel, rotated to face the ring centre, with the
     * city name word-wrapped to the sign's four lines.
     */
    private static void placeGateSign(ServerLevel level, int cx, int cz, double angle, String name) {
        Direction inward = inwardFacing(angle);
        placeSign(level, new BlockPos(cx, GATE_SIGN_Y, cz), inward, name);
    }

    /** Places a standing sign at {@code pos}, rotated to {@code facing}, with {@code name} on the front. */
    private static void placeSign(ServerLevel level, BlockPos pos, Direction facing, String name) {
        placeSign(level, pos, facing, wrapToFourLines(name));
    }

    /** Places a standing sign at {@code pos}, rotated to {@code facing}, with the given front lines. */
    private static void placeSign(ServerLevel level, BlockPos pos, Direction facing, List<Component> front) {
        BlockState sign = Blocks.OAK_SIGN.defaultBlockState()
                .setValue(StandingSignBlock.ROTATION, RotationSegment.convertToSegment(facing));
        set(level, pos, sign);
        if (level.getBlockEntity(pos) instanceof SignBlockEntity signEntity) {
            List<Component> lines = padToFourLines(front);
            signEntity.setText(new SignText(lines, lines, DyeColor.BLACK, false), SignTextSlot.FRONT);
        }
    }

    /** Pads (or truncates) {@code lines} to exactly the sign's four display lines. */
    private static List<Component> padToFourLines(List<Component> lines) {
        List<Component> padded = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {
            padded.add(i < lines.size() ? lines.get(i) : Component.empty());
        }
        return padded;
    }

    /** @return the nearest cardinal direction pointing from the gate back toward the ring centre. */
    private static Direction inwardFacing(double angle) {
        int dx = (int) Math.round(-Math.cos(angle));
        int dz = (int) Math.round(-Math.sin(angle));
        Direction direction = Direction.getNearest(dx, 0, dz, Direction.NORTH);
        return direction == null ? Direction.NORTH : direction;
    }

    /**
     * Word-wraps {@code text} into at most four sign lines (<=15 chars each), padding the remainder
     * with empty components. Falls back to hard splitting when a single word is too long.
     */
    private static List<Component> wrapToFourLines(String text) {
        List<String> lines = new ArrayList<>(4);
        StringBuilder current = new StringBuilder();
        for (String word : text.split(" ")) {
            if (word.isEmpty()) {
                continue;
            }
            while (word.length() > 15) {
                appendLine(lines, current.toString());
                current.setLength(0);
                appendLine(lines, word.substring(0, 15));
                word = word.substring(15);
            }
            if (current.isEmpty()) {
                current.append(word);
            } else if (current.length() + 1 + word.length() <= 15) {
                current.append(' ').append(word);
            } else {
                appendLine(lines, current.toString());
                current.setLength(0);
                current.append(word);
            }
        }
        if (!current.isEmpty()) {
            appendLine(lines, current.toString());
        }
        while (lines.size() < 4) {
            lines.add("");
        }
        List<Component> components = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {
            components.add(lines.get(i).isEmpty() ? Component.empty() : Component.literal(lines.get(i)));
        }
        return components;
    }

    /** Appends a non-blank line, ignoring overflow past the sign's four lines. */
    private static void appendLine(List<String> lines, String line) {
        if (!line.isEmpty() && lines.size() < 4) {
            lines.add(line);
        }
    }

    /** The invisible wall at the island's edge, so a player cannot walk off into the flat plane. */
    private static void buildBarrier(ServerLevel level) {
        BlockState barrier = Blocks.BARRIER.defaultBlockState();
        for (int y = 0; y < BARRIER_HEIGHT; y++) {
            fillRing(level, 0, 0, BARRIER_INNER, ISLAND_RADIUS, Threshold.FLOOR_Y + y, barrier);
        }
    }

    /** Fills every block within {@code radius} of ({@code cx}, {@code cz}) on layer {@code y}. */
    private static void fillDisc(ServerLevel level, int cx, int cz, int radius, int y, BlockState state) {
        int radiusSq = radius * radius;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz <= radiusSq) {
                    set(level, cx + dx, y, cz + dz, state);
                }
            }
        }
    }

    /** Fills the annulus between {@code inner} and {@code outer} (inclusive) on layer {@code y}. */
    private static void fillRing(ServerLevel level, int cx, int cz, int inner, int outer, int y, BlockState state) {
        int innerSq = inner * inner;
        int outerSq = outer * outer;
        for (int dx = -outer; dx <= outer; dx++) {
            for (int dz = -outer; dz <= outer; dz++) {
                int distSq = dx * dx + dz * dz;
                if (distSq >= innerSq && distSq <= outerSq) {
                    set(level, cx + dx, y, cz + dz, state);
                }
            }
        }
    }

    private static void set(ServerLevel level, int x, int y, int z, BlockState state) {
        level.setBlock(new BlockPos(x, y, z), state, Block.UPDATE_ALL);
    }

    private static void set(ServerLevel level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state, Block.UPDATE_ALL);
    }
}
