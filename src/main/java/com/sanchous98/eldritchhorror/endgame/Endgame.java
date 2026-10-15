package com.sanchous98.eldritchhorror.endgame;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.corruption.CorruptionAPI;
import com.sanchous98.eldritchhorror.corruption.CorruptionSystem;
import com.sanchous98.eldritchhorror.corruption.TaintAPI;
import com.sanchous98.eldritchhorror.cult.CultDefinition;
import com.sanchous98.eldritchhorror.cult.CultSystem;
import com.sanchous98.eldritchhorror.cult.Cults;
import com.sanchous98.eldritchhorror.entity.AncientOne;
import com.sanchous98.eldritchhorror.event.RiftKnowledge;
import com.sanchous98.eldritchhorror.event.Rifts;
import com.sanchous98.eldritchhorror.quest.QuestJournal;
import com.sanchous98.eldritchhorror.registry.ModEntities;
import com.sanchous98.eldritchhorror.registry.ModSounds;
import com.sanchous98.eldritchhorror.skill.SkillTree;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.Prediction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jspecify.annotations.Nullable;

/**
 * The endgame world work behind the fork rites ({@code design/11-endgame.md}). The rite engine finds
 * The Horror and calls one of the two paths; everything the choice <b>does to the world</b> lives
 * here, bounded and loaded-chunks-only:
 *
 * <ul>
 *   <li><b>Banish</b> ({@code design/11} path A): the presence is drawn off, the loaded rifts close,
 *       the local taint recedes, the performer's corruption largely resets (and any permanent floor
 *       from a prior summon is lifted), a portion of progression and reputation is paid, and the
 *       Order's token is granted.</li>
 *   <li><b>Summon</b> (path B): the presence is claimed, the performer's corruption is pinned at the
 *       Claimed band <b>permanently</b> (the {@code CORRUPTION_FLOOR} attachment), the Choir's
 *       standing rises, rifts tear open around them, and the Choir's crown is granted.</li>
 * </ul>
 *
 * <p>No entity is spawned here: the Horror is awakened by its own rite ({@code rite/Rites.java}), so
 * this class only needs the {@link AncientOne} it is resolving.
 */
public final class Endgame {

    /** Loaded-chunk radius scanned when closing rifts (2 ⇒ a 5×5 patch). */
    private static final int RIFT_SCAN_CHUNKS = 2;
    /** Hard cap on rifts closed or opened by one resolution. */
    private static final int RIFT_CAP = 32;
    /** Rifts opened by the summon (bounded; the rest is flavour). */
    private static final int SUMMON_RIFTS = 2;
    /** Taint added to (summon) or removed from (banish) a rift's chunk. */
    private static final double RIFT_TAINT = 0.30;
    /** Corruption lost by the banish (design/11: "a large part of your corruption resets"). */
    private static final double BANISH_CORRUPTION = 60.0;
    /** Skill points lost by the banish (design/11: "a portion of progression"). */
    private static final int BANISH_POINTS = 1;
    /** Fraction of every cult's reputation paid by the banish (design/11: "and reputation"). */
    private static final double BANISH_REPUTATION = 0.25;

    private Endgame() {
    }

    /** @return the overworld's decision so far (never {@code null}). */
    public static EndgameState.Outcome outcome(ServerLevel level) {
        return EndgameState.of(level).outcome();
    }

    /**
     * Path A: banish the awakened Horror. The world recedes — loaded rifts close, taint lowers, the
     * performer pays corruption, progression and reputation.
     *
     * @return the one-line result for the rite's message
     */
    public static Component banish(ServerLevel level, ServerPlayer player, AncientOne horror) {
        horror.solve(AncientOne.Solve.BANISHED, 0);
        EndgameState.of(level).setOutcome(EndgameState.Outcome.BANISHED);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.ENDGAME_BANISH.get(), SoundSource.AMBIENT, 0.9F, 1.0F);
        // A prior summon's permanent floor is exactly what the banishment is for: lift it first.
        CorruptionAPI.setFloor(player, 0.0);
        CorruptionAPI.add(player, -BANISH_CORRUPTION);
        SkillTree.addPoints(player, -BANISH_POINTS);
        for (CultDefinition cult : Cults.all()) {
            int current = CultSystem.get(player, cult.id());
            CultSystem.set(player, cult.id(), current - (int) Math.round(current * BANISH_REPUTATION));
        }
        int closed = closeRifts(level, player);
        grant(player, "banishment_focus", 1);
        return Component.literal("The Horror is drawn off; " + closed
                + " rift(s) close, the taint recedes, and much of you is spent.");
    }

    /**
     * Path B: summon the awakened Horror. The world tips — the performer's corruption is pinned at
     * the Claimed band permanently, the Choir rises, rifts tear open.
     *
     * @return the one-line result for the rite's message
     */
    public static Component summon(ServerLevel level, ServerPlayer player, AncientOne horror) {
        horror.solve(AncientOne.Solve.SUMMONED, 0);
        EndgameState.of(level).setOutcome(EndgameState.Outcome.SUMMONED);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.ENDGAME_SUMMON.get(), SoundSource.AMBIENT, 0.9F, 1.0F);
        // design/11: "corruption Claimed becomes permanent" — a floor, not a value, so every cleanse
        // path (rite, service, event) is refused by the meter itself, with no per-path special case.
        CorruptionAPI.setFloor(player, ModConfig.CORRUPTION_CLAIMED.get() * CorruptionSystem.DEFAULT_MAX);
        CultSystem.add(player, "hollow_choir", 20);
        int opened = openRifts(level, player);
        grant(player, "hollow_crown", 1);
        return Component.literal("The Horror is summoned; " + opened
                + " rift(s) tear open, and your corruption will not recede.");
    }

    /**
     * The third outcome ({@code design/11-endgame.md}): the Horror has reached the decision while
     * every rift the ledger knows is already closed. Nothing holds it here and nothing was banished —
     * the world stands sealed. Called from the Horror's escalation clock every second at the decision
     * stage, so a rift closed <em>during</em> the wait seals too; a no-op once any outcome is
     * recorded, and on a world that never had a rift.
     *
     * @return whether this call sealed the world
     */
    public static boolean trySeal(ServerLevel level, AncientOne horror) {
        if (EndgameState.of(level).outcome() != EndgameState.Outcome.NONE) {
            return false;
        }
        RiftLedger ledger = RiftLedger.of(level);
        ledger.prune(level); // a stale entry must never block the seal (loaded chunks only)
        if (!ledger.wasEverOpen() || ledger.openCount() > 0) {
            return false;
        }
        seal(level, horror);
        return true;
    }

    /** Records the seal, recedes the Horror and tells everyone the world held. */
    private static void seal(ServerLevel level, AncientOne horror) {
        EndgameState.of(level).setOutcome(EndgameState.Outcome.SEALED);
        horror.solve(AncientOne.Solve.BANISHED, 0); // it recedes: never banished, only left
        level.playSound(null, horror.getX(), horror.getY(), horror.getZ(),
                ModSounds.ENDGAME_SEAL.get(), SoundSource.AMBIENT, 1.0F, 1.0F);
        for (ServerPlayer player : level.players()) {
            player.sendSystemMessage(Component.literal("The Horror comes for the decision and finds "
                    + "no rift left open. Nothing holds it here; the world stands sealed."));
            QuestJournal.add(player, "the_seal", 1);
        }
    }

    /** Removes up to {@link #RIFT_CAP} rift anchors in the loaded 5×5 chunk patch; returns the count. */
    private static int closeRifts(ServerLevel level, ServerPlayer player) {
        List<BlockPos> rifts = Rifts.near(level, player.blockPosition(), RIFT_SCAN_CHUNKS, RIFT_CAP);
        int closed = 0;
        for (BlockPos rift : rifts) {
            if (level.setBlock(rift, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL)) {
                closed++;
                RiftLedger.of(level).remove(rift);
            }
            lowerTaint(level, ChunkPos.containing(rift));
        }
        return closed;
    }

    /** Opens up to {@link #SUMMON_RIFTS} rifts at loaded surface spots around the player. */
    private static int openRifts(ServerLevel level, ServerPlayer player) {
        Block anchor = BuiltInRegistries.BLOCK.getValue(EldritchHorror.id("rift_anchor"));
        if (anchor == Blocks.AIR) {
            return 0;
        }
        int opened = 0;
        for (int i = 0; i < SUMMON_RIFTS; i++) {
            BlockPos spot = surfaceSpot(level, player.blockPosition(), 8, i);
            if (spot == null || !level.setBlock(spot, anchor.defaultBlockState(), Block.UPDATE_ALL)) {
                continue;
            }
            opened++;
            RiftLedger.of(level).add(spot);
            RiftKnowledge.discover(player, spot);
            ChunkPos chunk = ChunkPos.containing(spot);
            LevelChunk loaded = level.getChunkSource().getChunkNow(chunk.x(), chunk.z());
            if (loaded != null) {
                TaintAPI.set(loaded, Math.min(1.0, TaintAPI.get(loaded) + RIFT_TAINT));
            }
        }
        return opened;
    }

    /** Removes {@link #RIFT_TAINT} from a loaded chunk (never force-loads). */
    private static void lowerTaint(ServerLevel level, ChunkPos chunk) {
        LevelChunk loaded = level.getChunkSource().getChunkNow(chunk.x(), chunk.z());
        if (loaded != null) {
            TaintAPI.set(loaded, Math.max(0.0, TaintAPI.get(loaded) - RIFT_TAINT));
        }
    }

    /** A loaded surface spot around {@code centre}, or {@code null} (never force-loads a chunk). */
    private static @Nullable BlockPos surfaceSpot(ServerLevel level, BlockPos centre, int radius, int salt) {
        for (int attempt = 0; attempt < 8; attempt++) {
            int angle = (salt * 8 + attempt) * 45;
            int x = centre.getX() + Mth.floor(Mth.cos(angle * Mth.DEG_TO_RAD) * radius);
            int z = centre.getZ() + Mth.floor(Mth.sin(angle * Mth.DEG_TO_RAD) * radius);
            if (!level.hasChunkAt(x, z)) {
                continue;
            }
            int y = level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
            BlockPos pos = new BlockPos(x, y, z);
            if (level.isLoaded(pos) && level.getBlockState(pos).canBeReplaced()
                    && !level.getBlockState(pos.below()).canBeReplaced()) {
                return pos;
            }
        }
        return null;
    }

    /** Hands over an endgame token; a full pack drops it instead of losing it. */
    private static void grant(ServerPlayer player, String itemPath, int count) {
        Item item = BuiltInRegistries.ITEM.getValue(EldritchHorror.id(itemPath));
        if (item == Items.AIR) {
            return;
        }
        ItemStack stack = new ItemStack(item, count);
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false, Prediction.SERVER_ONLY);
        }
    }

    /** @return whether the endgame presence is already awake within the rite radius of {@code player}. */
    public static boolean horrorNear(ServerLevel level, ServerPlayer player, int radius) {
        return !level.getEntitiesOfClass(AncientOne.class, player.getBoundingBox().inflate(radius),
                candidate -> candidate.getType() == ModEntities.THE_HORROR.get()).isEmpty();
    }

    /** @return the nearest awakened Horror within {@code radius}, or {@code null}. */
    public static @Nullable AncientOne nearestHorror(ServerLevel level, ServerPlayer player, int radius) {
        AncientOne nearest = null;
        double best = Double.MAX_VALUE;
        for (AncientOne candidate : level.getEntitiesOfClass(AncientOne.class,
                player.getBoundingBox().inflate(radius),
                other -> other.isAlive() && other.getType() == ModEntities.THE_HORROR.get())) {
            double distance = candidate.distanceToSqr(player);
            if (distance < best) {
                best = distance;
                nearest = candidate;
            }
        }
        return nearest;
    }
}
