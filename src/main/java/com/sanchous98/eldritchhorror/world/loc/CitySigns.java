package com.sanchous98.eldritchhorror.world.loc;

import com.sanchous98.eldritchhorror.EldritchHorror;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.entity.SignTextSlot;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Deferred sign text for authored cities. Writing {@link SignBlockEntity#setText} during worldgen
 * calls {@code markUpdated} → {@code Level.blockEntityChanged}, which waits on the chunk system and
 * can stall generation for minutes. The builder places the sign block during worldgen and queues
 * its text here; the server applies it on a tick once the chunk is loaded.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class CitySigns {

    private static final Map<ResourceKey<Level>, Map<BlockPos, String[]>> PENDING =
            new ConcurrentHashMap<>();

    private CitySigns() {
    }

    /** Queues one sign's lines for the dimension that owns it. Safe from worldgen threads. */
    public static void queue(ResourceKey<Level> dimension, BlockPos pos, String... lines) {
        String[] copy = new String[4];
        for (int i = 0; i < copy.length; i++) {
            copy[i] = i < lines.length && lines[i] != null ? lines[i] : "";
        }
        PENDING.computeIfAbsent(dimension, ignored -> new ConcurrentHashMap<>())
                .put(pos.immutable(), copy);
    }

    /** Applies queued text once the owning chunk is loaded; retries next tick otherwise. */
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (PENDING.isEmpty()) {
            return;
        }
        for (Map.Entry<ResourceKey<Level>, Map<BlockPos, String[]>> entry : PENDING.entrySet()) {
            ServerLevel level = event.getServer().getLevel(entry.getKey());
            if (level == null) {
                continue;
            }
            Iterator<Map.Entry<BlockPos, String[]>> signs = entry.getValue().entrySet().iterator();
            while (signs.hasNext()) {
                Map.Entry<BlockPos, String[]> signEntry = signs.next();
                BlockPos pos = signEntry.getKey();
                if (!level.isLoaded(pos)) {
                    continue;
                }
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (!(blockEntity instanceof SignBlockEntity sign)) {
                    signs.remove(); // the block is gone; nothing to write
                    continue;
                }
                List<Component> messages = new java.util.ArrayList<>(SignText.LINES);
                for (String line : signEntry.getValue()) {
                    messages.add(Component.literal(line));
                }
                while (messages.size() < SignText.LINES) {
                    messages.add(Component.empty());
                }
                sign.setLevel(level);
                sign.setText(new SignText(messages, messages, DyeColor.BLACK, false),
                        SignTextSlot.FRONT);
                signs.remove();
            }
        }
    }

    /** Drops every pending sign when the world goes away. */
    public static void clear() {
        PENDING.clear();
    }

    /** Stops a server stop from keeping the old dimension's queue alive. */
    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        clear();
    }
}
