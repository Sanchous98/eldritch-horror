package com.sanchous98.eldritchhorror.world.cult;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.corruption.TaintAPI;
import com.sanchous98.eldritchhorror.cult.CultSystem;
import com.sanchous98.eldritchhorror.endgame.DoomClock;
import com.sanchous98.eldritchhorror.entity.BestiarySupport;
import com.sanchous98.eldritchhorror.entity.CultZealot;
import com.sanchous98.eldritchhorror.entity.RiteBinder;
import com.sanchous98.eldritchhorror.entity.StarSpawn;
import com.sanchous98.eldritchhorror.entity.Worshipper;
import com.sanchous98.eldritchhorror.registry.ModEntities;
import com.sanchous98.eldritchhorror.registry.ModSounds;
import com.sanchous98.eldritchhorror.rite.RitualAltar;
import com.sanchous98.eldritchhorror.sanity.SanityAPI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jspecify.annotations.Nullable;

/**
 * NPC cult ritual cells ({@code design/06-factions.md}, "Ritual cells", pinned 2026-11): a cell of
 * cultists left alone at a real altar advances its cult's goal without a player choosing that path.
 * This is the Hollow Choir's agency, and the doom clock's main engine ({@code design/11-endgame.md}).
 *
 * <p><b>Finding a cell.</b> Every {@value #DETECT_INTERVAL} ticks, for each overworld player, the
 * mod counts {@link Worshipper}s within {@value #CELL_RADIUS} blocks by cult. With at least {@value
 * RitualRules#MIN_CULTISTS} of one cult and a nearby valid altar (an {@code altar_core} found by
 * scanning only loaded chunks with {@link LevelChunk#findBlocks}, then validated by the shared
 * {@link RitualAltar} ring check), one ritual starts. Nothing is force-loaded: the chunk scan uses
 * {@code getChunkNow} in a ±2-chunk bounded window, and a cell is only ever considered around a
 * player.
 *
 * <p><b>The state machine.</b> Per altar (in memory, keyed by position; server lifetime is enough):
 * {@code GATHER} (default 20 s) with the leader — a {@link RiteBinder} when one is present, else the
 * nearest worshipper — and the others navigating to ring positions around the core (no teleport);
 * it ends after the gather timer or once two cultists arrive. {@code CHANT} (default 30 s) with the
 * cultists holding position, soul/smoke particles, the rite hum at the core every ~2 s and a small
 * sanity/whisper tick to players within 16 blocks. Interrupts are checked first, in the order {@link
 * RitualRules#interruption} documents: leader dead, core/ring invalid, a player inside 4 blocks of
 * the core, or too many cultists lost.
 *
 * <p><b>Completion.</b> Only {@code hollow_choir} performs the summon: 2–3 {@link StarSpawn} on
 * loaded open ground, a small taint patch, {@code ritual.success}, a local announcement and {@code
 * +doomRitualPush} to the {@link DoomClock}; the altar then rests for {@code npcRitualCooldownTicks}.
 * Every other cult still resolves a lesser rite — a faint taint and a whisper, no doom — so the
 * world is not inert between the three seed cults.
 *
 * <p><b>Interruption is the counter-play.</b> The ritual fails, the failure sound plays, the
 * interrupt relief is taken off the doom clock, the interrupting (or nearest) player gains {@code
 * +10} Order standing and the altar gets a half cooldown. The first applicable cause is announced
 * so the player can learn the rule.
 *
 * <p><b>Bounds.</b> Loaded chunks only, one cell per altar, at most {@value #MAX_CELLS} active cells,
 * per-altar cooldown, one detection pass per player per 40 ticks, and the whole system behind {@code
 * enableNpcCultRituals}. The ticker allocates only while a cell exists.
 *
 * <p><b>Cult identity.</b> Only {@link Worshipper} exposes its cult id; zealots and rite binders keep
 * theirs private. A cell is therefore <em>identified</em> by its worshippers, and any
 * {@link CultZealot}/{@link RiteBinder} within the altar radius is treated as belonging to that cell
 * (which is also what makes the binder leader preference work in a stronghold).
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class CultRituals {

    /** Detection cadence, 2 s. */
    private static final int DETECT_INTERVAL = 40;
    /** Cell update cadence, 1 s. */
    private static final int TICK_INTERVAL = 20;
    /** Chant ambience cadence, 2 s. */
    private static final int HUM_INTERVAL = 40;
    /** Cultists within this radius of a player can form a cell. */
    private static final int CELL_RADIUS = 32;
    /** Altar ring check radius around a candidate core (the built rings are 2–8). */
    private static final int RING_CHECK_RADIUS = 8;
    /** Hard cap on simultaneous cells. */
    private static final int MAX_CELLS = 8;
    /** Participants per cell, so a stronghold cannot throw its whole garrison in. */
    private static final int MAX_PARTICIPANTS = 12;
    /** Radius of the ring positions cultists walk to. */
    private static final int RING_RADIUS = 3;
    /** "Reached the ring" distance, squared (6 blocks). */
    private static final double REACH_SQR = 36.0;
    /** A player inside this distance of the core breaks the ritual, squared (4 blocks). */
    private static final double PLAYER_RING_SQR = 16.0;
    /** Announcements reach players within 64 blocks. */
    private static final int ANNOUNCE_RADIUS = 64;
    /** Sanity a chant hum takes from nearby players. */
    private static final double CHANT_SANITY = -0.2;
    /** Order standing granted to the player who breaks a ritual. */
    private static final int INTERRUPT_REWARD = 10;
    /** Taint a Hollow Choir completion adds to each loaded chunk near the altar. */
    private static final double CHOIR_TAINT = 0.17;
    /** Taint a lesser completion adds. */
    private static final double LESSER_TAINT = 0.06;

    /** Active cells keyed by their core position. */
    private static final Map<Long, RitualCell> CELLS = new HashMap<>();
    /** Per-altar cooldown end times, keyed by core position. */
    private static final Map<Long, Long> COOLDOWNS = new HashMap<>();

    /** Lazily resolved {@code altar_core}; {@link Blocks#AIR} means it is missing. */
    private static @Nullable Block altarCore;

    private CultRituals() {
    }

    /** Drops every active cell and altar cooldown, so a world switch starts clean. */
    public static void clear() {
        CELLS.clear();
        COOLDOWNS.clear();
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!ModConfig.ENABLE_NPC_CULT_RITUALS.get()) {
            if (!CELLS.isEmpty() || !COOLDOWNS.isEmpty()) {
                CELLS.clear();
                COOLDOWNS.clear();
            }
            return;
        }
        ServerLevel level = event.getServer().getLevel(Level.OVERWORLD);
        if (level == null) {
            return;
        }
        int tick = event.getServer().getTickCount();
        if (tick % TICK_INTERVAL == 0) {
            tickCells(level, tick);
        }
        if (tick % DETECT_INTERVAL == 0) {
            detect(level, tick);
        }
    }

    /** Looks for a new cell around each player; at most one per player per pass. */
    private static void detect(ServerLevel level, int tick) {
        if (CELLS.size() >= MAX_CELLS) {
            return;
        }
        int radius = ModConfig.NPC_RITUAL_RADIUS.get();
        for (ServerPlayer player : level.players()) {
            if (CELLS.size() >= MAX_CELLS) {
                return;
            }
            if (player.isSpectator()) {
                continue;
            }
            List<Worshipper> nearby = level.getEntitiesOfClass(Worshipper.class,
                    player.getBoundingBox().inflate(CELL_RADIUS),
                    worshipper -> worshipper.isAlive() && !worshipper.isRemoved());
            if (nearby.size() < RitualRules.MIN_CULTISTS) {
                continue;
            }
            Map<String, Integer> counts = new HashMap<>();
            for (Worshipper worshipper : nearby) {
                counts.merge(worshipper.cultId(), 1, Integer::sum);
            }
            for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                if (entry.getValue() < RitualRules.MIN_CULTISTS) {
                    continue;
                }
                BlockPos core = nearestCore(level, player.blockPosition(), radius);
                if (core == null || CELLS.containsKey(core.asLong()) || onCooldown(core, tick)) {
                    continue;
                }
                if (!coreValid(level, core)) {
                    continue;
                }
                startCell(level, core, entry.getKey(), tick);
                break;
            }
        }
    }

    /** Forms the cell, chooses its leader and announces the gathering. */
    private static void startCell(ServerLevel level, BlockPos core, String cultId, int tick) {
        int radius = ModConfig.NPC_RITUAL_RADIUS.get();
        List<Mob> candidates = new ArrayList<>();
        candidates.addAll(level.getEntitiesOfClass(Worshipper.class, box(core, radius + 8),
                worshipper -> worshipper.isAlive() && !worshipper.isRemoved()
                        && cultId.equals(worshipper.cultId())));
        candidates.addAll(level.getEntitiesOfClass(CultZealot.class, box(core, radius),
                zealot -> zealot.isAlive() && !zealot.isRemoved()));
        candidates.addAll(level.getEntitiesOfClass(RiteBinder.class, box(core, radius),
                binder -> binder.isAlive() && !binder.isRemoved()));
        if (candidates.size() < RitualRules.MIN_CULTISTS) {
            return;
        }
        candidates.sort((a, b) -> Double.compare(a.blockPosition().distSqr(core),
                b.blockPosition().distSqr(core)));
        Mob leader = null;
        for (Mob candidate : candidates) {
            if (candidate instanceof RiteBinder) {
                leader = candidate;
                break;
            }
        }
        if (leader == null) {
            leader = candidates.getFirst();
        }
        List<UUID> participants = new ArrayList<>();
        for (Mob candidate : candidates) {
            if (participants.size() >= MAX_PARTICIPANTS) {
                break;
            }
            participants.add(candidate.getUUID());
        }
        if (!participants.contains(leader.getUUID())) {
            participants.set(participants.size() - 1, leader.getUUID());
        }
        CELLS.put(core.asLong(), new RitualCell(core, cultId, leader.getUUID(), participants));
        announce(level, core, "npcritual.eldritch_horror.gather");
        EldritchHorror.LOGGER.info("[cult ritual] GATHER {} at {} {} {} ({} cultists)",
                cultId, core.getX(), core.getY(), core.getZ(), participants.size());
    }

    /** Advances every cell; a cell that ends is dropped. */
    private static void tickCells(ServerLevel level, int tick) {
        COOLDOWNS.values().removeIf(end -> end <= tick);
        Iterator<RitualCell> iterator = CELLS.values().iterator();
        while (iterator.hasNext()) {
            RitualCell cell = iterator.next();
            if (!level.isLoaded(cell.core())) {
                iterator.remove(); // the altar left the loaded world: no resolution, no ghost
                continue;
            }
            if (!tickCell(level, cell, tick)) {
                iterator.remove();
            }
        }
    }

    /** Runs one cell for one update; {@code false} means the cell is finished. */
    private static boolean tickCell(ServerLevel level, RitualCell cell, int tick) {
        List<Mob> members = new ArrayList<>();
        Entity leaderEntity = level.getEntity(cell.leader());
        boolean leaderAlive = leaderEntity instanceof Mob leader
                && leader.isAlive() && !leader.isRemoved();
        for (UUID id : cell.participants()) {
            Entity entity = level.getEntity(id);
            if (entity instanceof Mob mob && mob.isAlive() && !mob.isRemoved()) {
                members.add(mob);
            }
        }
        boolean coreValid = coreValid(level, cell.core());
        ServerPlayer intruder = playerInRing(level, cell.core());
        RitualRules.Interruption interruption = RitualRules.interruption(leaderAlive, coreValid,
                intruder != null, members.size(), cell.startCount());
        if (interruption != RitualRules.Interruption.NONE) {
            interrupt(level, cell, interruption, intruder, tick);
            return false;
        }
        cell.advance(TICK_INTERVAL);
        if (cell.state() == RitualRules.State.GATHER) {
            navigate(level, cell, members);
            if (RitualRules.gatherDone(cell.elapsed(), ModConfig.NPC_RITUAL_GATHER_TICKS.get(),
                    reached(members, cell.core()))) {
                cell.beginChant();
                announce(level, cell.core(), "npcritual.eldritch_horror.chant");
                EldritchHorror.LOGGER.info("[cult ritual] CHANT {} at {} {} {}", cell.cultId(),
                        cell.core().getX(), cell.core().getY(), cell.core().getZ());
            }
            return true;
        }
        hold(members);
        if (cell.elapsed() > 0 && cell.elapsed() % HUM_INTERVAL == 0) {
            hum(level, cell.core());
        }
        if (RitualRules.chantDone(cell.elapsed(), ModConfig.NPC_RITUAL_CHANT_TICKS.get())) {
            complete(level, cell, tick);
            return false;
        }
        return true;
    }

    /** Walks the leader and the others to ring positions around the core. */
    private static void navigate(ServerLevel level, RitualCell cell, List<Mob> members) {
        int size = Math.max(1, members.size());
        for (int i = 0; i < members.size(); i++) {
            double angle = i * 2.0 * Math.PI / size;
            double x = cell.core().getX() + 0.5 + Math.cos(angle) * RING_RADIUS;
            double z = cell.core().getZ() + 0.5 + Math.sin(angle) * RING_RADIUS;
            members.get(i).getNavigation().moveTo(x, cell.core().getY(), z, 0.9);
        }
    }

    /** Keeps the ring still during the chant. */
    private static void hold(List<Mob> members) {
        for (Mob member : members) {
            member.getNavigation().stop();
        }
    }

    /** @return how many participants stand within {@link #REACH_SQR} of the core. */
    private static int reached(List<Mob> members, BlockPos core) {
        double cx = core.getX() + 0.5;
        double cy = core.getY() + 0.5;
        double cz = core.getZ() + 0.5;
        int reached = 0;
        for (Mob member : members) {
            if (member.position().distanceToSqr(cx, cy, cz) <= REACH_SQR) {
                reached++;
            }
        }
        return reached;
    }

    /** Particles, the rite hum, a whisper and a small sanity tick for everyone close. */
    private static void hum(ServerLevel level, BlockPos core) {
        double x = core.getX() + 0.5;
        double y = core.getY() + 1.0;
        double z = core.getZ() + 0.5;
        level.sendParticles(ParticleTypes.SOUL, x, y, z, 6, 0.5, 0.5, 0.5, 0.01);
        level.sendParticles(ParticleTypes.SMOKE, x, y, z, 8, 0.6, 0.4, 0.6, 0.01);
        level.playSound(null, core, ModSounds.RITUAL_HUM.get(), SoundSource.AMBIENT, 0.9F, 1.0F);
        for (ServerPlayer player : level.players()) {
            if (player.blockPosition().distSqr(core) <= 16.0 * 16.0) {
                SanityAPI.add(player, CHANT_SANITY);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        ModSounds.AMBIENT_WHISPER.get(), SoundSource.AMBIENT, 0.35F, 0.9F);
            }
        }
    }

    /** Resolves the ritual: the Choir's summon, or a lesser cult rite. */
    private static void complete(ServerLevel level, RitualCell cell, int tick) {
        RitualRules.Completion tier = RitualRules.completionFor(cell.cultId());
        if (tier == RitualRules.Completion.SUMMON) {
            summonStarSpawn(level, cell.core());
            taintPatch(level, cell.core(), CHOIR_TAINT);
            level.playSound(null, cell.core(), ModSounds.RITUAL_SUCCESS.get(), SoundSource.AMBIENT,
                    1.0F, 0.9F);
            announce(level, cell.core(), "npcritual.eldritch_horror.complete.choir");
            DoomClock.addDoom(level, ModConfig.DOOM_RITUAL_PUSH.get());
        } else {
            taintPatch(level, cell.core(), LESSER_TAINT);
            level.playSound(null, cell.core(), ModSounds.RITUAL_SUCCESS.get(), SoundSource.AMBIENT,
                    0.7F, 1.2F);
            announce(level, cell.core(), "npcritual.eldritch_horror.complete.other");
        }
        int cooldown = ModConfig.NPC_RITUAL_COOLDOWN_TICKS.get();
        COOLDOWNS.put(cell.core().asLong(), (long) tick + cooldown);
        EldritchHorror.LOGGER.info("[cult ritual] COMPLETE {} ({}) at {} {} {}", cell.cultId(), tier,
                cell.core().getX(), cell.core().getY(), cell.core().getZ());
    }

    /** Calls 2–{@code npcRitualSummonCount} star-spawn on loaded open ground near the altar. */
    private static void summonStarSpawn(ServerLevel level, BlockPos core) {
        int max = Math.max(2, ModConfig.NPC_RITUAL_SUMMON_COUNT.get());
        int count = max <= 2 ? 2 : 2 + level.getRandom().nextInt(max - 1);
        for (int i = 0; i < count; i++) {
            BlockPos spot = spotNear(level, core, 5, i);
            if (spot == null) {
                continue;
            }
            StarSpawn spawn = ModEntities.STAR_SPAWN.get().spawn(level, spot,
                    EntitySpawnReason.EVENT);
            if (spawn != null) {
                spawn.setPersistenceRequired();
            }
        }
    }

    /** Taints the loaded 3×3 chunks around the altar (never force-loads). */
    private static void taintPatch(ServerLevel level, BlockPos core, double amount) {
        ChunkPos base = ChunkPos.containing(core);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                TaintAPI.add(level, new ChunkPos(base.x() + dx, base.z() + dz), amount);
            }
        }
    }

    /**
     * Breaks the cell: failure sound, doom relief, Order standing for the interrupter, announcement
     * of the first applicable cause and a half cooldown on the altar.
     */
    private static void interrupt(ServerLevel level, RitualCell cell, RitualRules.Interruption cause,
                                  @Nullable ServerPlayer intruder, int tick) {
        level.playSound(null, cell.core(), ModSounds.RITUAL_FAILURE.get(), SoundSource.AMBIENT,
                1.0F, 0.8F);
        DoomClock.relieve(level, ModConfig.DOOM_INTERRUPT_RELIEF.get());
        ServerPlayer beneficiary = intruder;
        if (beneficiary == null) {
            beneficiary = nearestPlayer(level, cell.core(), ModConfig.NPC_RITUAL_RADIUS.get());
        }
        if (beneficiary != null) {
            CultSystem.add(beneficiary, "unblinking_eye", INTERRUPT_REWARD);
            ServerPlayer rewarded = beneficiary;
            rewarded.sendSystemMessage(Component.translatable(
                    "npcritual.eldritch_horror.reward", INTERRUPT_REWARD));
        }
        String key = "npcritual.eldritch_horror.interrupted."
                + cause.name().toLowerCase(java.util.Locale.ROOT);
        if (cause == RitualRules.Interruption.PLAYER && intruder != null) {
            announce(level, cell.core(), key, intruder.getDisplayName());
        } else {
            announce(level, cell.core(), key);
        }
        int cooldown = ModConfig.NPC_RITUAL_COOLDOWN_TICKS.get() / 2;
        COOLDOWNS.put(cell.core().asLong(), (long) tick + cooldown);
        EldritchHorror.LOGGER.info("[cult ritual] INTERRUPTED {} at {} {} {}: {}", cell.cultId(),
                cell.core().getX(), cell.core().getY(), cell.core().getZ(), cause);
    }

    /** @return the nearest {@code altar_core} within {@code radius}, scanning loaded chunks only. */
    private static @Nullable BlockPos nearestCore(ServerLevel level, BlockPos center, int radius) {
        Block core = altarCore();
        if (core == null) {
            return null;
        }
        ChunkPos origin = ChunkPos.containing(center);
        int chunkRadius = Math.max(1, (radius + 15) / 16);
        double[] bestDistance = {(double) radius * radius};
        @Nullable BlockPos[] found = {null};
        for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
            for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(origin.x() + dx, origin.z() + dz);
                if (chunk == null) {
                    continue; // never load/generate a chunk to find an altar
                }
                chunk.findBlocks(state -> state.is(core), (pos, state) -> {
                    BlockPos candidate = pos.immutable();
                    if (Math.abs(candidate.getY() - center.getY()) > 3) {
                        return;
                    }
                    double distance = candidate.distSqr(center);
                    if (distance < bestDistance[0]) {
                        bestDistance[0] = distance;
                        found[0] = candidate;
                    }
                });
            }
        }
        return found[0];
    }

    /** @return whether the core is still an {@code altar_core} with a valid surrounding ring. */
    private static boolean coreValid(ServerLevel level, BlockPos core) {
        Block block = altarCore();
        if (block == null || !level.isLoaded(core) || !level.getBlockState(core).is(block)) {
            return false;
        }
        return RitualAltar.valid(level, core, RING_CHECK_RADIUS, RitualRules.MIN_MARKS);
    }

    /** A loaded standing spot near the core, tried at ring offsets; {@code null} if none fits. */
    private static @Nullable BlockPos spotNear(ServerLevel level, BlockPos core, int radius, int salt) {
        for (int attempt = 0; attempt < 12; attempt++) {
            int angle = (salt * 12 + attempt) * 30;
            int x = core.getX() + (int) Math.round(Math.cos(Math.toRadians(angle)) * radius);
            int z = core.getZ() + (int) Math.round(Math.sin(Math.toRadians(angle)) * radius);
            BlockPos spot = BestiarySupport.surfaceSpot(level, x, z, true);
            if (spot != null) {
                return spot;
            }
        }
        return null;
    }

    /** @return the player standing within 4 blocks of the core, or {@code null}. */
    private static @Nullable ServerPlayer playerInRing(ServerLevel level, BlockPos core) {
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator()) {
                continue;
            }
            if (player.blockPosition().distSqr(core) <= PLAYER_RING_SQR) {
                return player;
            }
        }
        return null;
    }

    /** @return the nearest player within {@code radius} of the core, or {@code null}. */
    private static @Nullable ServerPlayer nearestPlayer(ServerLevel level, BlockPos core, int radius) {
        ServerPlayer best = null;
        double bestDistance = (double) radius * radius;
        for (ServerPlayer player : level.players()) {
            double distance = player.blockPosition().distSqr(core);
            if (distance <= bestDistance) {
                bestDistance = distance;
                best = player;
            }
        }
        return best;
    }

    /** Sends a translatable line to players within {@value #ANNOUNCE_RADIUS} blocks of the core. */
    private static void announce(ServerLevel level, BlockPos core, String key, Object... args) {
        Component message = args.length == 0
                ? Component.translatable(key)
                : Component.translatable(key, args);
        double limit = (double) ANNOUNCE_RADIUS * ANNOUNCE_RADIUS;
        for (ServerPlayer player : level.players()) {
            if (player.blockPosition().distSqr(core) <= limit) {
                player.sendSystemMessage(message);
            }
        }
    }

    /** @return whether the altar is resting after a ritual. */
    private static boolean onCooldown(BlockPos core, int tick) {
        Long end = COOLDOWNS.get(core.asLong());
        return end != null && end > tick;
    }

    /** Bounding box around a core, grown by {@code radius}. */
    private static AABB box(BlockPos core, int radius) {
        return new AABB(core.getX() - radius, core.getY() - radius,
                core.getZ() - radius, core.getX() + radius + 1.0, core.getY() + radius + 1.0,
                core.getZ() + radius + 1.0);
    }

    /** The resolved {@code altar_core} block, or {@code null} while it is missing. */
    private static @Nullable Block altarCore() {
        if (altarCore == null) {
            altarCore = BuiltInRegistries.BLOCK.getValue(EldritchHorror.id("altar_core"));
        }
        return altarCore == Blocks.AIR ? null : altarCore;
    }
}
