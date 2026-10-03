package com.sanchous98.eldritchhorror.classes;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.entity.BestiarySupport;
import com.sanchous98.eldritchhorror.registry.ModAttachments;
import com.sanchous98.eldritchhorror.world.city.City;
import com.sanchous98.eldritchhorror.world.threshold.Threshold;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jspecify.annotations.Nullable;

/**
 * The one-time Threshold prologue: on a player's first join they are carried to the authored
 * dimension to choose a class (pedestals → {@link ClassAPI}) and a starting city (gates → here).
 * See {@code design/29-prologue.md} and {@code docs/PROLOGUE-CONTRACT.md}.
 *
 * <p>Server-authoritative: every teleport happens server-side. The only persisted state is the
 * {@code PROLOGUE_DONE} attachment, set by {@link #complete}; a disconnect mid-prologue therefore
 * resumes in the Threshold, never re-triggering the arrival. {@link #enabled} reads the SERVER
 * config at event time only.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class Prologue {

    private Prologue() {
    }

    /** @return whether the prologue feature is enabled (SERVER config, read at runtime). */
    public static boolean enabled() {
        return ModConfig.ENABLE_PROLOGUE.get();
    }

    /** @return whether {@code player} should still be carried into the Threshold. */
    public static boolean needsPrologue(ServerPlayer player) {
        return enabled() && !player.getData(ModAttachments.PROLOGUE_DONE.get());
    }

    /**
     * Carries {@code player} into the Threshold at its fixed spawn. A no-op if the dimension or
     * spawn is unavailable (the prologue degrades rather than throwing).
     */
    public static void enter(ServerPlayer player) {
        ServerLevel target = threshold(player);
        if (target == null) {
            return;
        }
        teleport(player, target, Threshold.spawn());
    }

    /**
     * Marks the prologue complete and teleports {@code player} to {@code city}'s safe surface. Must
     * be a no-op-with-message until a class has been chosen — you pick who you are before where you
     * start.
     */
    public static void complete(ServerPlayer player, City city) {
        if (!ClassAPI.hasChosen(player)) {
            player.sendSystemMessage(Component.translatableWithFallback(
                    "prologue.eldritch_horror.choose_class",
                    "Choose a class at a pedestal before you take a gate."));
            return;
        }
        MinecraftServer server = player.level().getServer();
        ServerLevel overworld = server == null ? null : server.getLevel(Level.OVERWORLD);
        if (overworld == null) {
            return;
        }
        BlockPos spot = citySpot(overworld, city);
        if (spot == null) {
            player.sendSystemMessage(Component.translatable(
                    "prologue.eldritch_horror.no_city"));
            return;
        }
        // Only mark the prologue complete once the arrival actually happened, so a failed teleport
        // leaves the player in the Threshold to try again.
        if (teleport(player, overworld, spot)) {
            player.setData(ModAttachments.PROLOGUE_DONE.get(), true);
        }
    }

    /** First join: re-assert the class bonus; send a brand-new player into the Threshold. */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ClassAPI.reapply(player);
            if (needsPrologue(player)) {
                enter(player);
            }
        }
    }

    /** Respawn drops attribute modifiers, so re-assert the class bonus. */
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ClassAPI.reapply(player);
        }
    }

    private static @Nullable ServerLevel threshold(ServerPlayer player) {
        MinecraftServer server = player.level().getServer();
        return server == null ? null : server.getLevel(Threshold.DIMENSION);
    }

    /**
     * The city's safe standing spot. The destination chunk is force-generated first: reading the
     * height of an unloaded chunk returns the dimension's min Y (inside the bedrock), and
     * {@link BestiarySupport#surfaceSpot} returns {@code null} for unloaded chunks. Returns
     * {@code null} if no safe surface can be found.
     */
    private static @Nullable BlockPos citySpot(ServerLevel level, City city) {
        level.getChunk(city.x() >> 4, city.z() >> 4, ChunkStatus.FULL, true);
        BlockPos spot = BestiarySupport.surfaceSpot(level, city.x(), city.z(), false);
        if (spot != null) {
            return spot;
        }
        int y = level.getHeight(Heightmap.Types.OCEAN_FLOOR, city.x(), city.z());
        // getHeight of a missing chunk yields getMinY(); never teleport into the void.
        return y > level.getMinY() ? new BlockPos(city.x(), y, city.z()) : null;
    }

    /** @return whether the teleport actually happened. */
    private static boolean teleport(ServerPlayer player, ServerLevel target, BlockPos pos) {
        return player.teleportTo(target, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                Set.of(), player.getYRot(), player.getXRot(), false);
    }
}
