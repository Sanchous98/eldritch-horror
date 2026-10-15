package com.sanchous98.eldritchhorror.endgame;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.event.Rifts;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jspecify.annotations.Nullable;

/**
 * The world's rift ledger ({@code design/11-endgame.md}, the third outcome: "seal every rift without
 * banishing").
 *
 * <p>A rift is a plain {@code rift_anchor} block and chunks unload, so "every rift is closed" cannot
 * be answered by scanning. The ledger records every anchor placed while it exists — the
 * {@code open_rift} rite, the summon path — and forgets one when it closes, which lets the endgame
 * tell an <em>empty</em> world from an <em>untouched</em> one ({@link #wasEverOpen()}, so a world that
 * never had a rift is not "sealed" the moment it wakes). Positions use the same {@code "x,y,z"}
 * encoding as {@code RiftKnowledge}.
 *
 * <p>World data, like {@link EndgameState}: the ledger is a fact about the world, shared by every
 * player, and it survives restarts.
 */
public final class RiftLedger extends SavedData {

    private static final Codec<RiftLedger> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.listOf().optionalFieldOf("open", List.of()).forGetter(RiftLedger::openKeys),
            Codec.BOOL.optionalFieldOf("opened", false).forGetter(RiftLedger::wasEverOpen)
    ).apply(instance, RiftLedger::new));

    private static final SavedDataType<RiftLedger> TYPE =
            new SavedDataType<>(EldritchHorror.id("rift_ledger"), RiftLedger::new, CODEC);

    private final Set<String> open = new LinkedHashSet<>();
    private boolean opened;

    private RiftLedger() {
    }

    private RiftLedger(List<String> open, boolean opened) {
        this.open.addAll(open);
        this.opened = opened;
    }

    /** @return the overworld's ledger (created on first read). */
    public static RiftLedger of(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    /** Records a newly placed anchor. */
    public void add(BlockPos pos) {
        this.opened = true;
        if (this.open.add(key(pos))) {
            this.setDirty();
        }
    }

    /** Records a closed anchor. No-op for one the ledger never saw (e.g. an older build's rift). */
    public void remove(BlockPos pos) {
        if (this.open.remove(key(pos))) {
            this.setDirty();
        }
    }

    /** @return how many tracked rifts are currently open. */
    public int openCount() {
        return this.open.size();
    }

    /** @return whether any rift was ever recorded — an untouched world is not "sealed". */
    public boolean wasEverOpen() {
        return this.opened;
    }

    /**
     * Drops entries whose anchor is verifiably gone: the chunk is loaded and the block there is no
     * longer a rift anchor. Entries in unloaded chunks are kept (the anchor may still stand there),
     * and a malformed key is dropped. Called before counting, so a ledger left stale by an older
     * build, a world edit or an explosion cannot block the seal forever.
     *
     * @return whether anything was pruned
     */
    public boolean prune(ServerLevel level) {
        boolean changed = false;
        var iterator = this.open.iterator();
        while (iterator.hasNext()) {
            BlockPos pos = parse(iterator.next());
            if (pos == null || (level.isLoaded(pos) && !Rifts.isRift(level, pos))) {
                iterator.remove();
                changed = true;
            }
        }
        if (changed) {
            this.setDirty();
        }
        return changed;
    }

    /** The serialised shape (also the codec's getter). */
    private List<String> openKeys() {
        return List.copyOf(this.open);
    }

    private static String key(BlockPos pos) {
        return pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }

    /** {@code "x,y,z"} back to a position, or {@code null} for a malformed key. */
    private static @Nullable BlockPos parse(String key) {
        String[] parts = key.split(",");
        if (parts.length != 3) {
            return null;
        }
        try {
            return new BlockPos(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2]));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
