package com.sanchous98.eldritchhorror.rite;

import com.sanchous98.eldritchhorror.corruption.CorruptionAPI;
import com.sanchous98.eldritchhorror.corruption.CorruptionState;
import com.sanchous98.eldritchhorror.event.Rifts;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.MoonPhase;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * The Conditions column of {@code design/08-rituals.md}: what the world must look like before a rite
 * resolves. Checked server-side, loaded-chunks-only, before the outcome — an unmet condition refuses
 * the rite with a message naming what is missing.
 *
 * <p>Keyed by rite id (no change to the frozen {@link RiteDefinition}); the eight seed rites have a
 * row, and every Ancient One solve falls to {@link #NONE} (its gate is the learned tome and its
 * offerings). Kept deliberately modest: an enum for the categorical facts and a small numeric window
 * for midnight and the rift radius, so the whole table is readable as data.
 *
 * @param time           day / night / a midnight window
 * @param moon           any moon, or full-or-new only
 * @param weather        any, clear, rain, or thunderstorm
 * @param nearWaterOrRain the "near water, rain or high tide" alternative (drowned_blessing)
 * @param minStage       the player's corruption must be at least this band (or {@code null})
 * @param maxStage       the player's corruption must be at most this band (or {@code null})
 * @param riftWithinBlocks a rift anchor must be within this many blocks (0 = no rift needed)
 */
public record RiteConditions(
        Time time,
        Moon moon,
        Weather weather,
        boolean nearWaterOrRain,
        @Nullable CorruptionState minStage,
        @Nullable CorruptionState maxStage,
        int riftWithinBlocks) {

    public enum Time { ANY, DAY, NIGHT, MIDNIGHT }

    public enum Moon { ANY, FULL_OR_NEW }

    public enum Weather { ANY, CLEAR, RAIN, STORM }

    /** Midnight window (ticks of day): 18000 is midnight, so ±1000 is roughly 22:00–02:00. */
    private static final int MIDNIGHT_CENTER = 18000;
    private static final int MIDNIGHT_HALF_WINDOW = 1000;
    /** Ticks in a Minecraft day. */
    private static final int DAY_TICKS = 24000;
    /** Horizontal radius searched for water for {@link #nearWaterOrRain}. */
    private static final int WATER_SCAN = 6;

    /** No conditions (the Ancient One solves). */
    public static final RiteConditions NONE =
            new RiteConditions(Time.ANY, Moon.ANY, Weather.ANY, false, null, null, 0);

    private static final Map<String, RiteConditions> BY_RITE = Map.of(
            "ward_of_the_eye", new RiteConditions(Time.NIGHT, Moon.ANY, Weather.CLEAR, false, null, null, 0),
            "drowned_blessing", new RiteConditions(Time.ANY, Moon.ANY, Weather.ANY, true, null, null, 0),
            "call_the_lesser", new RiteConditions(Time.MIDNIGHT, Moon.FULL_OR_NEW, Weather.ANY, false, null, null, 0),
            "summon_star_spawn", new RiteConditions(Time.MIDNIGHT, Moon.ANY, Weather.ANY, false,
                    CorruptionState.TOUCHED, null, 0),
            "open_rift", new RiteConditions(Time.ANY, Moon.ANY, Weather.STORM, false,
                    CorruptionState.MARKED, null, 0),
            "close_rift", new RiteConditions(Time.ANY, Moon.ANY, Weather.ANY, false, null, null, 8),
            "rite_of_cleansing", new RiteConditions(Time.ANY, Moon.ANY, Weather.ANY, false, null,
                    CorruptionState.MARKED, 0),
            "respec", new RiteConditions(Time.ANY, Moon.ANY, Weather.ANY, false,
                    CorruptionState.TOUCHED, null, 0));

    /** @return the conditions for {@code rite} (never {@code null}; {@link #NONE} when unlisted). */
    public static RiteConditions of(RiteDefinition rite) {
        return BY_RITE.getOrDefault(rite.id(), NONE);
    }

    /**
     * @return {@code null} when every condition holds, otherwise a message naming the first unmet
     * one, ready to show the performer.
     */
    public @Nullable Component unmet(ServerLevel level, ServerPlayer player) {
        Component timeFailure = checkTime(level);
        if (timeFailure != null) {
            return timeFailure;
        }
        Component moonFailure = checkMoon(level, player);
        if (moonFailure != null) {
            return moonFailure;
        }
        Component weatherFailure = checkWeather(level);
        if (weatherFailure != null) {
            return weatherFailure;
        }
        if (this.nearWaterOrRain && !isRaining(level) && !isNearWater(level, player)) {
            return Component.literal("The rite needs open water or rain.");
        }
        CorruptionState stage = CorruptionState.of(CorruptionAPI.get(player));
        if (this.minStage != null && stage.ordinal() < this.minStage.ordinal()) {
            return Component.literal("Your corruption is too low: the rite needs at least "
                    + this.minStage.name().toLowerCase(java.util.Locale.ROOT) + ".");
        }
        if (this.maxStage != null && stage.ordinal() > this.maxStage.ordinal()) {
            return Component.literal("Your corruption is too high: the rite needs at most "
                    + this.maxStage.name().toLowerCase(java.util.Locale.ROOT) + ".");
        }
        if (this.riftWithinBlocks > 0 && !riftNear(level, player)) {
            return Component.literal("No rift within " + this.riftWithinBlocks + " blocks.");
        }
        return null;
    }

    private @Nullable Component checkTime(ServerLevel level) {
        return switch (this.time) {
            case ANY -> null;
            case DAY -> level.isDarkOutside() ? Component.literal("The rite must be performed by day.") : null;
            case NIGHT -> level.isDarkOutside() ? null : Component.literal("The rite must be performed at night.");
            case MIDNIGHT -> {
                int dayTime = Math.floorMod(level.getOverworldClockTime(), DAY_TICKS);
                boolean midnight = Math.abs(dayTime - MIDNIGHT_CENTER) <= MIDNIGHT_HALF_WINDOW;
                yield midnight ? null : Component.literal("The rite must be performed at midnight.");
            }
        };
    }

    private @Nullable Component checkMoon(ServerLevel level, ServerPlayer player) {
        if (this.moon != Moon.FULL_OR_NEW) {
            return null;
        }
        MoonPhase phase = level.environmentAttributes().getValue(
                net.minecraft.world.attribute.EnvironmentAttributes.MOON_PHASE,
                player.position(), null);
        boolean ok = phase == MoonPhase.FULL_MOON || phase == MoonPhase.NEW_MOON;
        return ok ? null : Component.literal("The moon must be full or new.");
    }

    private @Nullable Component checkWeather(ServerLevel level) {
        return switch (this.weather) {
            case ANY -> null;
            case CLEAR -> isRaining(level) ? Component.literal("The rite must be performed in clear weather.") : null;
            case RAIN -> isRaining(level) ? null : Component.literal("The rite must be performed in rain.");
            case STORM -> level.isThundering() ? null : Component.literal("The rite must be performed in a storm.");
        };
    }

    private static boolean isRaining(ServerLevel level) {
        return level.isRaining() || level.isThundering();
    }

    /** Bounded water scan: a single-column radius around the player, loaded cells only. */
    private static boolean isNearWater(ServerLevel level, ServerPlayer player) {
        BlockPos base = player.blockPosition();
        for (int dx = -WATER_SCAN; dx <= WATER_SCAN; dx++) {
            for (int dz = -WATER_SCAN; dz <= WATER_SCAN; dz++) {
                for (int dy = -2; dy <= 1; dy++) {
                    BlockPos pos = base.offset(dx, dy, dz);
                    if (!level.isLoaded(pos)) {
                        continue;
                    }
                    BlockState state = level.getBlockState(pos);
                    if (state.is(Blocks.WATER)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /** True when a loaded rift anchor lies within {@link #riftWithinBlocks}. */
    private boolean riftNear(ServerLevel level, ServerPlayer player) {
        List<BlockPos> rifts = Rifts.near(level, player.blockPosition(), 1, 16);
        double limit = (double) this.riftWithinBlocks * this.riftWithinBlocks;
        for (BlockPos rift : rifts) {
            if (player.distanceToSqr(rift.getX() + 0.5, rift.getY() + 0.5, rift.getZ() + 0.5) <= limit) {
                return true;
            }
        }
        return false;
    }

    /** @return a short localised-ish description for the altar prompt, e.g. "night, clear". */
    public Component describe() {
        StringBuilder sb = new StringBuilder();
        appendTime(sb);
        appendMoon(sb);
        appendWeather(sb);
        if (this.nearWaterOrRain) {
            append(sb, "water/rain");
        }
        if (this.minStage != null) {
            append(sb, "corruption≥" + this.minStage.name().toLowerCase(java.util.Locale.ROOT));
        }
        if (this.maxStage != null) {
            append(sb, "corruption≤" + this.maxStage.name().toLowerCase(java.util.Locale.ROOT));
        }
        if (this.riftWithinBlocks > 0) {
            append(sb, "rift≤" + this.riftWithinBlocks + "m");
        }
        return Component.literal(sb.isEmpty() ? "none" : sb.toString());
    }

    private void appendTime(StringBuilder sb) {
        switch (this.time) {
            case DAY -> append(sb, "day");
            case NIGHT -> append(sb, "night");
            case MIDNIGHT -> append(sb, "midnight");
            case ANY -> { }
        }
    }

    private void appendMoon(StringBuilder sb) {
        if (this.moon == Moon.FULL_OR_NEW) {
            append(sb, "full/new moon");
        }
    }

    private void appendWeather(StringBuilder sb) {
        switch (this.weather) {
            case CLEAR -> append(sb, "clear");
            case RAIN -> append(sb, "rain");
            case STORM -> append(sb, "storm");
            case ANY -> { }
        }
    }

    private static void append(StringBuilder sb, String text) {
        if (!sb.isEmpty()) {
            sb.append(", ");
        }
        sb.append(text);
    }
}
