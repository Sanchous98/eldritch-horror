package com.sanchous98.eldritchhorror.world.rift;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sanchous98.eldritchhorror.EldritchHorror;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jspecify.annotations.Nullable;

/**
 * The world's gate links (design/26, "The rift gate"): which {@code rift_anchor} stands two-way
 * connected to which.
 *
 * <p>Like {@link com.sanchous98.eldritchhorror.endgame.RiftLedger} and
 * {@link com.sanchous98.eldritchhorror.world.ward.WardLedger}, this is per-level
 * {@link SavedData}: a link survives a restart and is shared by everyone in that level. Because the
 * ledger is per-level, a link can never cross dimensions — the linking path refuses that before a
 * pair is stored.
 *
 * <p>A link is symmetric and stored once. Each pair is canonicalised by its two endpoint keys, so
 * adding the same pair twice cannot create a duplicate; a gate may have at most one twin, so
 * {@link #add} first drops any link already touching either endpoint.
 */
public final class GateLinks extends SavedData {

    /** One undirected pair of gate positions. The endpoints are stored in canonical order. */
    public record Link(int ax, int ay, int az, int bx, int by, int bz) {

        /** @return the first endpoint. */
        public BlockPos a() {
            return new BlockPos(this.ax, this.ay, this.az);
        }

        /** @return the second endpoint. */
        public BlockPos b() {
            return new BlockPos(this.bx, this.by, this.bz);
        }

        /** @return the other endpoint when {@code pos} is one of the two, else {@code null}. */
        public @Nullable BlockPos other(BlockPos pos) {
            if (this.a().equals(pos)) {
                return this.b();
            }
            if (this.b().equals(pos)) {
                return this.a();
            }
            return null;
        }
    }

    private static final Codec<Link> LINK_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("ax").forGetter(Link::ax),
            Codec.INT.fieldOf("ay").forGetter(Link::ay),
            Codec.INT.fieldOf("az").forGetter(Link::az),
            Codec.INT.fieldOf("bx").forGetter(Link::bx),
            Codec.INT.fieldOf("by").forGetter(Link::by),
            Codec.INT.fieldOf("bz").forGetter(Link::bz)
    ).apply(instance, Link::new));

    private static final Codec<GateLinks> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            LINK_CODEC.listOf().optionalFieldOf("links", List.of()).forGetter(GateLinks::linkList)
    ).apply(instance, GateLinks::new));

    private static final SavedDataType<GateLinks> TYPE =
            new SavedDataType<>(EldritchHorror.id("gate_links"), GateLinks::new, CODEC);

    private final List<Link> links = new ArrayList<>();

    private GateLinks() {
    }

    private GateLinks(List<Link> links) {
        this.links.addAll(links);
    }

    /** @return this level's gate-link ledger (created on first read). */
    public static GateLinks of(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    /**
     * Links {@code a} and {@code b} two-way. Any existing link touching either endpoint is dropped
     * first, because a gate has exactly one twin; linking the same pair again is therefore a no-op
     * in effect, and moving one gate to a new twin retires the old pair.
     */
    public void add(BlockPos a, BlockPos b) {
        if (a.equals(b)) {
            return;
        }
        this.remove(a);
        this.remove(b);
        this.links.add(canonical(a, b));
        this.setDirty();
    }

    /** @return the twin of {@code pos}, or {@code null} when it has no link. */
    public @Nullable BlockPos linked(BlockPos pos) {
        for (Link link : this.links) {
            BlockPos other = link.other(pos);
            if (other != null) {
                return other;
            }
        }
        return null;
    }

    /**
     * Drops every link touching {@code pos} (its twin becomes unlinked too).
     *
     * @return how many pairs were removed
     */
    public int remove(BlockPos pos) {
        int before = this.links.size();
        this.links.removeIf(link -> link.other(pos) != null);
        int removed = before - this.links.size();
        if (removed > 0) {
            this.setDirty();
        }
        return removed;
    }

    /** @return an immutable snapshot of every stored pair. */
    public List<Link> links() {
        return List.copyOf(this.links);
    }

    /** @return how many pairs are stored. */
    public int linkCount() {
        return this.links.size();
    }

    /** The serialised shape (also the codec's getter). */
    private List<Link> linkList() {
        return List.copyOf(this.links);
    }

    /** Orders the two endpoints by their encoded key so the same pair always has one representation. */
    private static Link canonical(BlockPos a, BlockPos b) {
        if (key(a).compareTo(key(b)) <= 0) {
            return new Link(a.getX(), a.getY(), a.getZ(), b.getX(), b.getY(), b.getZ());
        }
        return new Link(b.getX(), b.getY(), b.getZ(), a.getX(), a.getY(), a.getZ());
    }

    private static String key(BlockPos pos) {
        return pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }
}
