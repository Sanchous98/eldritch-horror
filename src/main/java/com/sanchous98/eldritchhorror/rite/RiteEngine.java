package com.sanchous98.eldritchhorror.rite;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.corruption.CorruptionAPI;
import com.sanchous98.eldritchhorror.corruption.TaintAPI;
import com.sanchous98.eldritchhorror.entity.AncientOne;
import com.sanchous98.eldritchhorror.registry.ModEntities;
import com.sanchous98.eldritchhorror.sanity.SanityAPI;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.Prediction;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Server-authoritative resolution for a performed rite: applies the sanity/corruption cost, then
 * dispatches on {@link RiteDefinition.Outcome} to a concrete, bounded world effect.
 *
 * <p>This is the first real magic in the mod and the outcome stage deferred by
 * {@code design/05-ritual-engine.md} step 6. It is deliberately modest but real: buffs, a small
 * capped summon, and a placeholder rift marker made of our {@code rift_anchor} block plus chunk
 * taint. Everything is deterministic (no {@code Math.random}), radius-limited around the player,
 * touches only loaded chunks, and never floods the world.
 *
 * <p>Outcomes with no seeded content (reputation / transform / curse / sanity) return a
 * {@code ok=false} result explaining that they are not implemented for this rite — never a silent
 * no-op. {@link RiteDefinition.Outcome#SOOTHE} is the shared Ancient One non-combat solve
 * ({@code design/28}).
 */
public final class RiteEngine {

    /** Rift marker placed by {@link RiteDefinition.Outcome#OPEN_RIFT}. */
    private static final String RIFT_ANCHOR_ID = "rift_anchor";
    /** Taint added to the marker's chunk per open, and removed per close/cleanse. */
    private static final double OPEN_TAINT = 0.35;
    private static final double CLEANSE_TAINT = 0.30;
    /** Chunk radius for rift taint changes (1 ⇒ a 3×3 chunk patch); only loaded chunks. */
    private static final int CHUNK_RADIUS = 1;
    /** Block radius searched when removing rift markers (design/08: a rift within 8 blocks). */
    private static final int RIFT_REMOVE_RADIUS = 8;

    /** Lesser summon: 3 lesser swarm, refused past {@value #LESSER_CAP} within {@value #SPAWN_CAP_RADIUS} blocks. */
    private static final EntityType<?> LESSER_TYPE = ModEntities.LESSER_SWARM.get();
    private static final int LESSER_COUNT = 3;
    private static final int LESSER_CAP = 8;
    /** Star-spawn summon: 1 star-spawn, refused past {@value #STAR_CAP} within {@value #SPAWN_CAP_RADIUS} blocks. */
    private static final EntityType<?> STAR_TYPE = ModEntities.STAR_SPAWN.get();
    private static final int STAR_COUNT = 1;
    private static final int STAR_CAP = 4;
    private static final int SPAWN_RADIUS = 6;
    private static final int SPAWN_CAP_RADIUS = 16;

    /** Ward buff duration per rite tier (ticks). */
    private static final int WARD_TICKS_PER_TIER = 400;
    /** Drowned Blessing buff duration (ticks). */
    private static final int BLESSING_TICKS = 1200;

    private RiteEngine() {
    }

    /**
     * What actually happened. {@code ok=false} means the rite did not take effect (the cost is still
     * applied for known rites); {@code message} is always safe to show the performer.
     */
    public record Result(boolean ok, Component message) {
    }

    /**
     * Performs {@code rite} for {@code player}: verifies knowledge, applies the cost, resolves the
     * outcome server-side.
     */
    public static Result perform(ServerPlayer player, RiteDefinition rite) {
        if (!RiteKnowledge.knows(player, rite.id())) {
            return new Result(false, Component.literal("You do not know the rite: " + rite.id()));
        }
        ServerLevel level = player.level();

        // Cost is paid on every known-rite attempt, matching design/08-rituals.md.
        if (rite.sanity() != 0) {
            SanityAPI.add(player, rite.sanity());
        }
        if (rite.corruption() != 0) {
            CorruptionAPI.add(player, rite.corruption());
        }

        return switch (rite.outcome()) {
            case GRANT -> grant(player, rite);
            case GRANT_SKILL -> grantSkill(player, rite);
            case SPAWN -> spawn(level, player, rite);
            case OPEN_RIFT -> openRift(level, player);
            case CLOSE_RIFT -> closeRift(level, player);
            case CORRUPTION -> cleanse(level, player, rite);
            case SOOTHE -> soothe(level, player, rite);
            case REPUTATION, TRANSFORM, CURSE, SANITY ->
                    new Result(false, Component.literal("The rite's outcome " + rite.outcome()
                            + " is not implemented for " + rite.id() + "."));
        };
    }

    /**
     * The shared Ancient One non-combat solve ({@link RiteDefinition.Outcome#SOOTHE}). Finds the
     * nearest compatible {@link AncientOne} within {@link com.sanchous98.eldritchhorror.core.ModConfig#RITE_SOLVE_RADIUS}
     * (the {@code grant} field names the entity path, e.g. {@code cthulhu}) and asks it to solve.
     * Bounded: a single, already-loaded entity query; no world scan, no chunk generation, no random.
     * A disabled solve or an absent/already-answered presence fails with an explicit message.
     */
    private static Result soothe(ServerLevel level, ServerPlayer player, RiteDefinition rite) {
        String target = rite.grant();
        if (target.isEmpty()) {
            return new Result(false, Component.literal("The rite names no presence to solve."));
        }
        if (!solveEnabled(target)) {
            return new Result(false, Component.literal("That solve is disabled: " + target + "."));
        }
        int radius = ModConfig.RITE_SOLVE_RADIUS.get();
        double radiusSqr = (double) radius * radius;
        List<AncientOne> candidates = level.getEntitiesOfClass(AncientOne.class,
                player.getBoundingBox().inflate(radius),
                a -> {
                    Identifier key = BuiltInRegistries.ENTITY_TYPE.getKey(a.getType());
                    return a.isAlive() && key != null && target.equals(key.getPath());
                });
        AncientOne nearest = null;
        double best = Double.MAX_VALUE;
        for (AncientOne candidate : candidates) {
            double distance = candidate.distanceToSqr(player);
            if (distance <= radiusSqr && distance < best) {
                best = distance;
                nearest = candidate;
            }
        }
        if (nearest == null) {
            return new Result(false, Component.literal("No presence answers within " + radius + " blocks."));
        }
        int ticks = solveTicks(target);
        if (!nearest.solve(solveFor(target), ticks)) {
            return new Result(false, Component.literal("That presence is already answered."));
        }
        // Drawing the Horror off a settlement also cleanses the ground it was eating (bounded patch).
        if ("dunwich_horror".equals(target)) {
            taintPatch(level, nearest.blockPosition(), CLEANSE_TAINT, true);
            SanityAPI.add(player, 5);
        }
        // Binding the weaver also lets the scar recede (bounded patch around the site).
        if ("atlach_nacha".equals(target)) {
            taintPatch(level, nearest.blockPosition(), CLEANSE_TAINT, true);
        }
        Component reward = giveSolveReward(player, target);
        return new Result(true, solveMessage(target).copy().append(reward));
    }

    /** Per-target still/bind duration; only the {@link AncientOne.Solve#STILLED} solves use it. */
    private static int solveTicks(String target) {
        return switch (target) {
            case "shub_niggurath" -> ModConfig.RITE_STILL_SHUB_NIGGURATH_TICKS.get();
            case "ithaqua" -> ModConfig.RITE_WARD_ITHAQUA_TICKS.get();
            case "atlach_nacha" -> ModConfig.RITE_BIND_ATLACH_NACHA_TICKS.get();
            case "cthugha" -> ModConfig.RITE_QUENCH_CTHUGHA_TICKS.get();
            case "hydra" -> ModConfig.RITE_SEVER_HYDRA_TICKS.get();
            default -> 0;
        };
    }

    /**
     * The solve's <b>different reward path</b> (design/18: soothing gives a different reward than the
     * kill). Reuses existing currency items from {@code registry/items/Currency.java}; no new item is
     * invented. Returns a short suffix describing what was received, or empty if nothing was due.
     */
    private static Component giveSolveReward(ServerPlayer player, String target) {
        String itemId = switch (target) {
            case "cthulhu" -> "mark_of_favour";
            case "dunwich_horror" -> "relic_coin";
            case "shub_niggurath" -> "black_obol";
            case "azathoth" -> "void_reagent";
            case "yog_sothoth" -> "relic_coin";
            case "ithaqua" -> "mark_of_favour";
            case "yig" -> "black_obol";
            case "atlach_nacha" -> "relic_coin";
            case "nyarlathotep" -> "cult_token";
            case "cthugha" -> "relic_coin";
            case "glaaki" -> "black_obol";
            case "hydra" -> "barter_seal";
            case "nyogtha" -> "order_scrip";
            case "rhan_tegoth" -> "mark_of_favour";
            default -> "";
        };
        if (itemId.isEmpty()) {
            return Component.empty();
        }
        Item item = BuiltInRegistries.ITEM.getValue(EldritchHorror.id(itemId));
        if (item == null || item == Items.AIR) {
            return Component.empty();
        }
        ItemStack reward = new ItemStack(item, 1);
        if (!player.getInventory().add(reward)) {
            player.drop(reward, false, Prediction.SERVER_ONLY);
        }
        return Component.literal(" The presence yields " + itemId.replace('_', ' ') + ".");
    }

    /** Whether the solve for {@code target} is enabled; an unknown name is never enabled. */
    private static boolean solveEnabled(String target) {
        return switch (target) {
            case "cthulhu" -> ModConfig.ENABLE_RITE_SOOTHE_CTHULHU.get();
            case "dunwich_horror" -> ModConfig.ENABLE_RITE_CLEANSE_DUNWICH.get();
            case "shub_niggurath" -> ModConfig.ENABLE_RITE_STILL_SHUB_NIGGURATH.get();
            case "azathoth" -> ModConfig.ENABLE_RITE_STILL_AZATHOTH.get();
            case "yog_sothoth" -> ModConfig.ENABLE_RITE_SEAL_YOG_SOTHOTH.get();
            case "ithaqua" -> ModConfig.ENABLE_RITE_WARD_ITHAQUA.get();
            case "yig" -> ModConfig.ENABLE_RITE_APPEASE_YIG.get();
            case "atlach_nacha" -> ModConfig.ENABLE_RITE_BIND_ATLACH_NACHA.get();
            case "nyarlathotep" -> ModConfig.ENABLE_RITE_DENY_NYARLATHOTEP.get();
            case "cthugha" -> ModConfig.ENABLE_RITE_QUENCH_CTHUGHA.get();
            case "glaaki" -> ModConfig.ENABLE_RITE_STILL_GLAAKI.get();
            case "hydra" -> ModConfig.ENABLE_RITE_SEVER_HYDRA.get();
            case "nyogtha" -> ModConfig.ENABLE_RITE_BAR_NYOGTHA.get();
            case "rhan_tegoth" -> ModConfig.ENABLE_RITE_TOPPLE_IDOL.get();
            default -> false;
        };
    }

    /** Maps a solve target name to its {@link AncientOne.Solve} state. */
    private static AncientOne.Solve solveFor(String target) {
        return switch (target) {
            case "cthulhu" -> AncientOne.Solve.SOOTHED;
            case "dunwich_horror" -> AncientOne.Solve.BANISHED;
            case "shub_niggurath" -> AncientOne.Solve.STILLED;
            case "azathoth" -> AncientOne.Solve.SOOTHED;
            case "yog_sothoth" -> AncientOne.Solve.SOOTHED;
            case "ithaqua" -> AncientOne.Solve.STILLED;
            case "yig" -> AncientOne.Solve.SOOTHED;
            case "atlach_nacha" -> AncientOne.Solve.STILLED;
            case "nyarlathotep" -> AncientOne.Solve.SOOTHED;
            case "cthugha" -> AncientOne.Solve.STILLED;
            case "glaaki" -> AncientOne.Solve.SOOTHED;
            case "hydra" -> AncientOne.Solve.STILLED;
            case "nyogtha" -> AncientOne.Solve.SOOTHED;
            case "rhan_tegoth" -> AncientOne.Solve.SOOTHED;
            default -> AncientOne.Solve.NONE;
        };
    }

    /** Per-target success text. */
    private static Component solveMessage(String target) {
        return switch (target) {
            case "cthulhu" -> Component.literal("The dream stills: the presence sleeps, and is not killed.");
            case "dunwich_horror" -> Component.literal(
                    "The horror is drawn off downhill, away from the settlement; the ground remembers less.");
            case "shub_niggurath" -> Component.literal(
                    "The woods hold their breath; the presence is stilled for a time.");
            case "azathoth" -> Component.literal(
                    "The music at the centre stills; the will to act returns, and nothing was killed.");
            case "yog_sothoth" -> Component.literal(
                    "The gate closes; the plateau is only a plateau again.");
            case "ithaqua" -> Component.literal(
                    "A ward turns the walking wind aside; it cannot find the seam for a while.");
            case "yig" -> Component.literal(
                    "Yig is appeased; the deaths are no longer drunk, and nothing else is drawn.");
            case "atlach_nacha" -> Component.literal(
                    "The web is unbound; the scar begins to close again, for a time.");
            case "nyarlathotep" -> Component.literal(
                    "The trusted face is named and set aside; the betrayal cannot land, and nothing was killed.");
            case "cthugha" -> Component.literal(
                    "The watching flame is quenched; light is only light again, for a time.");
            case "glaaki" -> Component.literal(
                    "The same lake is stilled; the green servitors lose their pull, and nothing was killed.");
            case "hydra" -> Component.literal(
                    "The Hydra is severed; no head buds, for a time. The sword would only have grown it.");
            case "nyogtha" -> Component.literal(
                    "The way below is barred; the sound under the floor fades, and nothing was killed.");
            case "rhan_tegoth" -> Component.literal(
                    "The idol is toppled; the worship ends and the pull to kneel is broken.");
            default -> Component.literal("The presence is answered.");
        };
    }

    /** {@code ward_of_the_eye}: a tier-scaled resistance + regeneration ward. */
    private static Result grant(ServerPlayer player, RiteDefinition rite) {
        int ticks = WARD_TICKS_PER_TIER * Mth.clamp(rite.tier(), 1, 3);
        player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, ticks, 0, true, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, ticks, 0, true, false, false));
        return new Result(true, Component.literal("A ward settles over you ("
                + (ticks / 20) + "s)."));
    }

    /** {@code drowned_blessing} / {@code respec}: water buffs, or a full skill reset. */
    private static Result grantSkill(ServerPlayer player, RiteDefinition rite) {
        if ("respec".equals(rite.grant())) {
            for (MobEffectInstance instance : List.copyOf(player.getActiveEffects())) {
                if (instance.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {
                    player.removeEffect(instance.getEffect());
                }
            }
            player.setHealth(player.getMaxHealth());
            return new Result(true, Component.literal("Unmade and remade: afflictions lifted, body whole."));
        }
        player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, BLESSING_TICKS, 0, true, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, BLESSING_TICKS, 0, true, false, false));
        return new Result(true, Component.literal("The tide's blessing fills your lungs for "
                + (BLESSING_TICKS / 20) + "s."));
    }

    /** {@code call_the_lesser} / {@code summon_star_spawn}: a small, capped hostile summon. */
    private static Result spawn(ServerLevel level, ServerPlayer player, RiteDefinition rite) {
        boolean star = rite.id().contains("star");
        EntityType<?> type = star ? STAR_TYPE : LESSER_TYPE;
        int requested = star ? STAR_COUNT : LESSER_COUNT;
        int cap = star ? STAR_CAP : LESSER_CAP;

        int existing = level.getEntitiesOfClass(Mob.class, capBox(player), mob -> mob.getType() == type).size();
        int allowed = Mth.clamp(cap - existing, 0, requested);
        if (allowed == 0) {
            return new Result(false, Component.literal("Too many summoned things already stir here."));
        }

        int spawned = 0;
        for (int i = 0; i < allowed; i++) {
            BlockPos spot = surfaceSpot(level, player, SPAWN_RADIUS, i);
            if (spot != null && type.spawn(level, spot, EntitySpawnReason.MOB_SUMMONED) != null) {
                spawned++;
            }
        }
        if (spawned == 0) {
            return new Result(false, Component.literal("No clear ground to summon onto."));
        }
        return new Result(true, Component.literal(spawned + " answer the call."));
    }

    /** {@code open_rift}: place a rift marker + taint the loaded patch around it. */
    private static Result openRift(ServerLevel level, ServerPlayer player) {
        Block riftBlock = BuiltInRegistries.BLOCK.getValue(EldritchHorror.id(RIFT_ANCHOR_ID));
        if (riftBlock == null) {
            return new Result(false, Component.literal("No rift anchor block is registered."));
        }
        BlockPos anchor = surfaceSpot(level, player, SPAWN_RADIUS, 0);
        if (anchor == null) {
            return new Result(false, Component.literal("No open ground to open the Way."));
        }
        BlockState rift = riftBlock.defaultBlockState();
        level.setBlock(anchor, rift, Block.UPDATE_ALL);

        // A small, deterministic sculk cross marks the tear if the ground allows it.
        for (Direction dir : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
            BlockPos edge = anchor.below().relative(dir);
            if (level.isLoaded(edge) && level.getBlockState(edge).canBeReplaced()) {
                level.setBlock(edge, Blocks.SCULK.defaultBlockState(), Block.UPDATE_ALL);
            }
        }

        double taint = taintPatch(level, anchor, OPEN_TAINT, false);
        return new Result(true, Component.literal("The Way tears open; the land taints to "
                + String.format(java.util.Locale.ROOT, "%.2f", taint) + "."));
    }

    /** {@code close_rift}: remove nearby markers and recede the loaded patch's taint. */
    private static Result closeRift(ServerLevel level, ServerPlayer player) {
        Block riftBlock = BuiltInRegistries.BLOCK.getValue(EldritchHorror.id(RIFT_ANCHOR_ID));
        int removed = 0;
        if (riftBlock != null) {
            BlockPos origin = player.blockPosition();
            for (int dx = -RIFT_REMOVE_RADIUS; dx <= RIFT_REMOVE_RADIUS; dx++) {
                for (int dy = -RIFT_REMOVE_RADIUS; dy <= RIFT_REMOVE_RADIUS; dy++) {
                    for (int dz = -RIFT_REMOVE_RADIUS; dz <= RIFT_REMOVE_RADIUS; dz++) {
                        BlockPos p = origin.offset(dx, dy, dz);
                        if (level.isLoaded(p) && level.getBlockState(p).is(riftBlock)) {
                            level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                            removed++;
                        }
                    }
                }
            }
        }
        // Only cleanse when a rift was actually sealed here, so the rite is not a free area-cleanser.
        if (removed > 0) {
            taintPatch(level, player.blockPosition(), CLEANSE_TAINT, true);
            // design/08: sealing grants sanity +10 as its OUTCOME (the corruption +5 is its cost).
            SanityAPI.add(player, 10);
        }
        return new Result(true, Component.literal("The Way is sealed; " + removed
                + " rift marker(s) removed and the Veil recedes."));
    }

    /** {@code rite_of_cleansing}: lower the player's corruption AND the local taint. */
    private static Result cleanse(ServerLevel level, ServerPlayer player, RiteDefinition rite) {
        // design/08 defines rite_of_cleansing's outcome as corruption -20 on the player meter,
        // distinct from the chunk taint field; both recede.
        if (rite.corruption() != 0) {
            // (the corruption *cost* was already applied by perform(); the outcome delta is the
            // magnitude of the cleansing, applied here as a reduction of the meter)
            CorruptionAPI.add(player, -20);
        }
        double taint = taintPatch(level, player.blockPosition(), CLEANSE_TAINT, true);
        return new Result(true, Component.literal("Cleansing light sinks into the ground; the taint recedes to "
                + String.format(java.util.Locale.ROOT, "%.2f", taint) + "."));
    }

    /**
     * Adds ({@code lower == false}) or subtracts ({@code lower == true}) {@code amount} from every
     * loaded chunk within {@link #CHUNK_RADIUS} of {@code origin}. Unloaded chunks are skipped.
     *
     * @return the taint of the origin chunk after the change, or {@code 0} if it is unloaded
     */
    private static double taintPatch(ServerLevel level, BlockPos origin, double amount, boolean lower) {
        ChunkPos centre = ChunkPos.containing(origin);
        for (int cx = -CHUNK_RADIUS; cx <= CHUNK_RADIUS; cx++) {
            for (int cz = -CHUNK_RADIUS; cz <= CHUNK_RADIUS; cz++) {
                ChunkPos pos = new ChunkPos(centre.x() + cx, centre.z() + cz);
                if (lower) {
                    double current = TaintAPI.get(level, pos);
                    TaintAPI.set(level, pos, Math.max(0.0, current - amount));
                } else {
                    TaintAPI.add(level, pos, amount);
                }
            }
        }
        return TaintAPI.get(level, centre);
    }

    /** A loaded, clear ground position within {@code radius} of the player, deterministic per {@code salt}. */
    private static BlockPos surfaceSpot(ServerLevel level, ServerPlayer player, int radius, int salt) {
        for (int attempt = 0; attempt < 8; attempt++) {
            int angle = (salt * 8 + attempt) * 45;
            int dx = Mth.floor(Mth.cos(angle * Mth.DEG_TO_RAD) * radius);
            int dz = Mth.floor(Mth.sin(angle * Mth.DEG_TO_RAD) * radius);
            int x = player.blockPosition().getX() + dx;
            int z = player.blockPosition().getZ() + dz;
            // Only touch already-loaded chunks: getHeight would otherwise force-generate them.
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

    private static AABB capBox(ServerPlayer player) {
        return AABB.ofSize(player.position(), 2 * SPAWN_CAP_RADIUS, 32.0, 2 * SPAWN_CAP_RADIUS);
    }
}
