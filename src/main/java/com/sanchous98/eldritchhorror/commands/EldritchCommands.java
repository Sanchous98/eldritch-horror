package com.sanchous98.eldritchhorror.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sanchous98.eldritchhorror.codex.CodexAPI;
import com.sanchous98.eldritchhorror.codex.CodexCategory;
import com.sanchous98.eldritchhorror.codex.CodexEntry;
import com.sanchous98.eldritchhorror.codex.CodexRegistry;
import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.corruption.CorruptionSystem;
import com.sanchous98.eldritchhorror.corruption.TaintAPI;
import com.sanchous98.eldritchhorror.cult.CultDefinition;
import com.sanchous98.eldritchhorror.cult.CultRank;
import com.sanchous98.eldritchhorror.cult.CultService;
import com.sanchous98.eldritchhorror.cult.CultServices;
import com.sanchous98.eldritchhorror.cult.CultSystem;
import com.sanchous98.eldritchhorror.endgame.Endgame;
import com.sanchous98.eldritchhorror.endgame.EndgameState;
import com.sanchous98.eldritchhorror.endgame.DoomClock;
import com.sanchous98.eldritchhorror.endgame.RiftLedger;
import com.sanchous98.eldritchhorror.cult.Cults;
import com.sanchous98.eldritchhorror.entity.AncientOne;
import com.sanchous98.eldritchhorror.entity.BestiarySupport;
import com.sanchous98.eldritchhorror.entity.Worshipper;
import com.sanchous98.eldritchhorror.event.EldritchEvent;
import com.sanchous98.eldritchhorror.event.EventTicker;
import com.sanchous98.eldritchhorror.event.Events;
import com.sanchous98.eldritchhorror.event.Rifts;
import com.sanchous98.eldritchhorror.investigator.Investigator;
import com.sanchous98.eldritchhorror.investigator.InvestigatorAPI;
import com.sanchous98.eldritchhorror.investigator.Prologue;
import com.sanchous98.eldritchhorror.registry.ModEntities;
import com.sanchous98.eldritchhorror.rite.RiteDefinition;
import com.sanchous98.eldritchhorror.rite.RiteEngine;
import com.sanchous98.eldritchhorror.rite.RiteKnowledge;
import com.sanchous98.eldritchhorror.rite.Rites;
import com.sanchous98.eldritchhorror.sanity.SanitySystem;
import com.sanchous98.eldritchhorror.world.city.Cities;
import com.sanchous98.eldritchhorror.world.city.City;
import com.sanchous98.eldritchhorror.world.city.CityFallShock;
import com.sanchous98.eldritchhorror.world.city.CityLedger;
import com.sanchous98.eldritchhorror.world.city.CityState;
import com.sanchous98.eldritchhorror.world.city.CityStates;
import com.sanchous98.eldritchhorror.world.ward.WardLedger;
import com.sanchous98.eldritchhorror.world.ward.WardMath;
import com.sanchous98.eldritchhorror.world.rift.GateLinks;
import com.sanchous98.eldritchhorror.world.rift.RiftGates;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Admin/debug commands for the RPG meters: {@code /eh sanity|corruption get|set|add}, cult
 * reputation, and the rite framework ({@code /eh rite <id>}, {@code /eh rites}). These are what
 * make the stubs testable before gameplay reads them.
 */
@EventBusSubscriber(modid = com.sanchous98.eldritchhorror.EldritchHorror.MODID)
public final class EldritchCommands {

    /**
     * Gamemaster gate for a state-mutating subcommand, matching the in-handler check used by
     * {@code investigator set}, {@code prologue reset}, {@code home}, {@code codex learn} and
     * {@code cultist}. Read-only subcommands are left ungated.
     */
    private static final java.util.function.Predicate<CommandSourceStack> GAMEMASTER =
            source -> source.permissions().hasPermission(
                    net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER);

    /** Radial width (blocks) of one band in the {@code /eh city lights} histogram. */
    private static final int LIGHT_BAND = 32;
    /** A light counts as "ground level" within this many blocks of the caller's Y. */
    private static final int GROUND_SPAN = 6;
    /**
     * Upper bound on chunks examined by {@code /eh city lights}. Only chunks that are already
     * loaded are ever seen, so this is a safety cap for pathological render distances.
     */
    private static final int MAX_SCAN_CHUNKS = 1024;

    /**
     * The boss ids accepted by {@code /eh boss}. Values are registry suppliers: the command tree
     * never dereferences a holder (AGENTS: no {@code DeferredHolder.get()} in static init).
     */
    private static final Map<String, Supplier<? extends EntityType<? extends AncientOne>>> BOSSES =
            bossTypes();

    private static Map<String, Supplier<? extends EntityType<? extends AncientOne>>> bossTypes() {
        Map<String, Supplier<? extends EntityType<? extends AncientOne>>> map = new LinkedHashMap<>();
        map.put("abhoth", ModEntities.ABHOTH);
        map.put("atlach_nacha", ModEntities.ATLACH_NACHA);
        map.put("azathoth", ModEntities.AZATHOTH);
        map.put("chaugnar_faugn", ModEntities.CHAUGNAR_FAUGN);
        map.put("choir_leviathan", ModEntities.CHOIR_LEVIATHAN);
        map.put("cthugha", ModEntities.CTHUGHA);
        map.put("cthulhu", ModEntities.CTHULHU);
        map.put("dunwich_horror", ModEntities.DUNWICH_HORROR);
        map.put("glaaki", ModEntities.GLAAKI);
        map.put("hastur", ModEntities.HASTUR);
        map.put("hydra", ModEntities.HYDRA);
        map.put("ithaqua", ModEntities.ITHAQUA);
        map.put("ithaqual_shade", ModEntities.ITHAQUAL_SHADE);
        map.put("nephren_ka", ModEntities.NEPHREN_KA);
        map.put("nyarlathotep", ModEntities.NYARLATHOTEP);
        map.put("nyogtha", ModEntities.NYOGTHA);
        map.put("rhan_tegoth", ModEntities.RHAN_TEGOTH);
        map.put("shub_niggurath", ModEntities.SHUB_NIGGURATH);
        map.put("the_horror", ModEntities.THE_HORROR);
        map.put("tulzscha", ModEntities.TULZSCHA);
        map.put("yig", ModEntities.YIG);
        map.put("yog_sothoth", ModEntities.YOG_SOTHOTH);
        map.put("zstylzhemghi", ModEntities.ZSTYLZHEMGHI);
        return java.util.Collections.unmodifiableMap(map);
    }

    private EldritchCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        LiteralArgumentBuilder<CommandSourceStack> eh =
                Commands.literal("eh")
                        .then(Commands.literal("sanity")
                                .then(getNode("sanity", SanitySystem::get))
                                .then(setNode("sanity", SanitySystem::set))
                                .then(addNode("sanity", SanitySystem::add)))
                        .then(Commands.literal("corruption")
                                .then(getNode("corruption", CorruptionSystem::get))
                                .then(setNode("corruption", CorruptionSystem::set))
                                .then(addNode("corruption", CorruptionSystem::add)))
                        .then(Commands.literal("taint")
                                .then(Commands.literal("get").executes(ctx -> {
                                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                                    net.minecraft.world.level.chunk.LevelChunk chunk = ownChunk(p);
                                    if (chunk == null) {
                                        ctx.getSource().sendFailure(Component.literal("chunk not loaded"));
                                        return 0;
                                    }
                                    double v = CorruptionSystem.getTaint(chunk);
                                    ctx.getSource().sendSuccess(
                                            () -> Component.literal("taint = " + fmt(v)), false);
                                    return 1;
                                }))
                                .then(Commands.literal("set")
                                        .requires(GAMEMASTER)
                                        .then(Commands.argument("value", DoubleArgumentType.doubleArg())
                                                .executes(ctx -> {
                                                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                                                    net.minecraft.world.level.chunk.LevelChunk chunk = ownChunk(p);
                                                    if (chunk == null) {
                                                        ctx.getSource().sendFailure(Component.literal("chunk not loaded"));
                                                        return 0;
                                                    }
                                                    double v = DoubleArgumentType.getDouble(ctx, "value");
                                                    CorruptionSystem.setTaint(chunk, v);
                                                    ctx.getSource().sendSuccess(
                                                            () -> Component.literal("taint set to " + fmt(v)), true);
                                                    return 1;
                                                })))
                                .then(Commands.literal("add")
                                        .requires(GAMEMASTER)
                                        .then(Commands.argument("delta", DoubleArgumentType.doubleArg())
                                                .executes(ctx -> {
                                                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                                                    net.minecraft.world.level.chunk.LevelChunk chunk = ownChunk(p);
                                                    if (chunk == null) {
                                                        ctx.getSource().sendFailure(Component.literal("chunk not loaded"));
                                                        return 0;
                                                    }
                                                    double d = DoubleArgumentType.getDouble(ctx, "delta");
                                                    double now = CorruptionSystem.addTaint(chunk, d);
                                                    ctx.getSource().sendSuccess(
                                                            () -> Component.literal("taint = " + fmt(now)), true);
                                                    return 1;
                                                })))
                                .then(Commands.literal("purify")
                                        .requires(GAMEMASTER)
                                        .executes(EldritchCommands::purifyTaint)))
                        .then(Commands.literal("rep")
                                .then(Commands.literal("get")
                                        .then(Commands.argument("cult", StringArgumentType.word())
                                                .executes(ctx -> {
                                                    String cult = StringArgumentType.getString(ctx, "cult");
                                                    if (unknownCult(ctx, cult)) {
                                                        return 0;
                                                    }
                                                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                                                    int n = CultSystem.get(p, cult);
                                                    Component rank = CultSystem.rank(p, cult);
                                                    ctx.getSource().sendSuccess(
                                                            () -> Component.literal(cult + " rep = " + n + " (").append(rank).append(")"), false);
                                                    return 1;
                                                })))
                                .then(Commands.literal("set")
                                        .requires(GAMEMASTER)
                                        .then(Commands.argument("cult", StringArgumentType.word())
                                                .then(Commands.argument("value", IntegerArgumentType.integer(-100, 100))
                                                        .executes(ctx -> {
                                                            String cult = StringArgumentType.getString(ctx, "cult");
                                                            if (unknownCult(ctx, cult)) {
                                                                return 0;
                                                            }
                                                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                                                            int v = IntegerArgumentType.getInteger(ctx, "value");
                                                            int now = CultSystem.set(p, cult, v);
                                                            ctx.getSource().sendSuccess(
                                                                    () -> Component.literal(cult + " rep set to " + now), true);
                                                            return 1;
                                                        }))))
                                .then(Commands.literal("add")
                                        .requires(GAMEMASTER)
                                        .then(Commands.argument("cult", StringArgumentType.word())
                                                .then(Commands.argument("delta", IntegerArgumentType.integer(-100, 100))
                                                        .executes(ctx -> {
                                                            String cult = StringArgumentType.getString(ctx, "cult");
                                                            if (unknownCult(ctx, cult)) {
                                                                return 0;
                                                            }
                                                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                                                            int d = IntegerArgumentType.getInteger(ctx, "delta");
                                                            int now = CultSystem.add(p, cult, d);
                                                            ctx.getSource().sendSuccess(
                                                                    () -> Component.literal(cult + " rep = " + now), true);
                                                            return 1;
                                                        }))))
                                .then(Commands.literal("list").executes(ctx -> {
                                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                                    StringBuilder sb = new StringBuilder();
                                    for (var def : Cults.all()) {
                                        if (sb.length() > 0) {
                                            sb.append(' ');
                                        }
                                        sb.append(def.id()).append('=').append(CultSystem.get(p, def.id()));
                                    }
                                    String line = sb.toString();
                                    ctx.getSource().sendSuccess(() -> Component.literal(line), false);
                                    return 1;
                                })))
                        .then(Commands.literal("rite")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .executes(EldritchCommands::performRite)))
                        .then(Commands.literal("rites").executes(EldritchCommands::listRites))
                        .then(Commands.literal("investigator")
                                .then(Commands.literal("get").executes(EldritchCommands::getInvestigator))
                                .then(Commands.literal("set")
                                        .requires(GAMEMASTER)
                                        .then(Commands.argument("id", StringArgumentType.word())
                                                .executes(EldritchCommands::setInvestigator))))
                        .then(Commands.literal("cult")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .executes(EldritchCommands::cultInfo)))
                        .then(Commands.literal("service")
                                .then(Commands.argument("cult", StringArgumentType.word())
                                        .then(Commands.argument("service", StringArgumentType.word())
                                                .executes(EldritchCommands::performService))));
        // Commands are registered unconditionally: the config is not loaded yet when the command
        // tree is built (it is built while datapacks load), so reading ModConfig here would throw
        // "Cannot get config value before config is loaded". The per-feature toggle is checked in
        // the handler instead.
        eh.then(Commands.literal("codex")
                        .executes(EldritchCommands::listCodex)
                        .then(Commands.literal("learn")
                                .requires(GAMEMASTER)
                                .then(Commands.argument("id", StringArgumentType.greedyString())
                                        .executes(EldritchCommands::learnCodex))))
                .then(Commands.literal("codexentries").executes(EldritchCommands::listCodexEntries));
        eh.then(Commands.literal("event")
                        .requires(GAMEMASTER)
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(EldritchCommands::forceEvent)))
                .then(Commands.literal("events").executes(EldritchCommands::listEvents));
        // Debug travel to the starting city (respawn handles the normal case).
        eh.then(Commands.literal("home").requires(GAMEMASTER).executes(EldritchCommands::goHome));
        // Debug: report the nearest curated city's condition (derived from district taint).
        eh.then(Commands.literal("city")
                .executes(EldritchCommands::cityState)
                .then(Commands.literal("lights").executes(EldritchCommands::cityLights))
                .then(Commands.literal("taint")
                        .requires(GAMEMASTER)
                        .then(Commands.argument("city", StringArgumentType.word())
                                .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0, 1.0))
                                        .executes(EldritchCommands::setCityTaint)))));
        // The player's local contract in the nearest curated city (design/21).
        eh.then(Commands.literal("cityquest").executes(EldritchCommands::cityQuest));
        // Debug: reset the one-time prologue and return to the Threshold.
        eh.then(Commands.literal("prologue")
                .then(Commands.literal("reset").requires(GAMEMASTER)
                        .executes(EldritchCommands::resetPrologue)));
        // Quest journal (server-authoritative progress; synced to the owner for a future screen).
        eh.then(Commands.literal("quests").executes(EldritchCommands::listQuests));
        // Skill tree.
        eh.then(Commands.literal("skill")
                .executes(EldritchCommands::listSkills)
                .then(Commands.literal("unlock")
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(EldritchCommands::unlockSkill))));
        // The endgame decision (design/11): readable by anyone, settable for testing/repair.
        eh.then(Commands.literal("endgame")
                .executes(EldritchCommands::getEndgame)
                .then(Commands.literal("set")
                        .requires(GAMEMASTER)
                        .then(Commands.argument("outcome", StringArgumentType.word())
                                .executes(EldritchCommands::setEndgame))));
        // Wards (design/26): what haven currently covers the caller, with radius and seconds left.
        eh.then(Commands.literal("ward").executes(EldritchCommands::listWards));
        // Rift gates (design/26): the caller's pending mark and the nearest gate's link state.
        eh.then(Commands.literal("gate").executes(EldritchCommands::gateInfo));
        // The doom clock (design/11): meter, passive rate, open rifts, awakening and outcome.
        eh.then(Commands.literal("doom").executes(EldritchCommands::doomInfo));
        // Scheduled ships (design/30): nearest port, line state, next departure, hull positions.
        // "board" is a debug ride hook: it calls the same fare interaction right-click uses.
        eh.then(Commands.literal("ports")
                .executes(EldritchCommands::portsInfo));
        eh.then(Commands.literal("ship")
                .executes(EldritchCommands::shipInfo)
                .then(Commands.literal("board").executes(EldritchCommands::boardShip)));
        // Scheduled trains (design/30, stage 2): nearest station, line/timetable state, consist
        // positions, the rail under the locomotive, and the same fare path as right-click.
        eh.then(Commands.literal("train")
                .executes(EldritchCommands::trainInfo)
                .then(Commands.literal("board").executes(EldritchCommands::boardTrain)));
        // One-shot world pre-generation for cities and rail corridors (tick-sliced).
        eh.then(Commands.literal("pregen")
                .requires(GAMEMASTER)
                .then(Commands.literal("cities").executes(ctx -> pregen(ctx, "cities")))
                .then(Commands.literal("rail").executes(ctx -> pregen(ctx, "rail")))
                .then(Commands.literal("all").executes(ctx -> pregen(ctx, "all")))
                .then(Commands.literal("world").executes(ctx -> pregen(ctx, "world")))
                .then(Commands.literal("radius")
                        .then(Commands.argument("blocks", IntegerArgumentType.integer(256, 4096))
                                .executes(EldritchCommands::pregenRadius)))
                .then(Commands.literal("status").executes(EldritchCommands::pregenStatus))
                .then(Commands.literal("stop").executes(EldritchCommands::pregenStop)));
        // Probe hook for ritual cells: spawn persistent cultists of one cult at the caller.
        eh.then(Commands.literal("cultist")
                .requires(GAMEMASTER)
                .then(Commands.argument("cult", StringArgumentType.word())
                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 32))
                                .executes(EldritchCommands::spawnCultists))));
        // Server-side bot players for automated playtesting (dev-mode gated inside the handlers).
        eh.then(com.sanchous98.eldritchhorror.bot.BotCommands.node());
        // Boss combat probe: list every Ancient One id, or summon one at the caller and print its
        // telegraphed abilities (`docs/BOSS-COMBAT.md`).
        eh.then(Commands.literal("boss")
                .requires(GAMEMASTER)
                .then(Commands.literal("list").executes(EldritchCommands::listBosses))
                .then(Commands.argument("id", StringArgumentType.word())
                        .executes(EldritchCommands::summonBoss)));
        dispatcher.register(eh);
    }

    /** {@code /eh skill}: skill points and nodes. */
    private static int listSkills(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Skill points: " + com.sanchous98.eldritchhorror.skill.SkillTree.points(player)), false);
        for (com.sanchous98.eldritchhorror.skill.Skill skill
                : com.sanchous98.eldritchhorror.skill.Skills.all()) {
            boolean has = com.sanchous98.eldritchhorror.skill.SkillTree.has(player, skill.id());
            ctx.getSource().sendSuccess(() -> Component.literal((has ? "[x] " : "[ ] ")
                    + skill.id() + " (" + skill.cost() + ") — ").append(skill.description()), false);
        }
        return 1;
    }

    /** {@code /eh skill unlock <id>}: spend a point on a node. */
    private static int unlockSkill(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        String id = StringArgumentType.getString(ctx, "id");
        if (com.sanchous98.eldritchhorror.skill.Skills.byId(id) == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown skill: " + id));
            return 0;
        }
        if (com.sanchous98.eldritchhorror.skill.SkillTree.unlock(player, id)) {
            ctx.getSource().sendSuccess(() -> Component.literal("Unlocked skill: " + id), true);
            return 1;
        }
        ctx.getSource().sendFailure(Component.literal(
                "Cannot unlock " + id + " (already known, or not enough points)."));
        return 0;
    }

    /** {@code /eh ward}: the wards covering the caller, with radius and whole seconds left (design/26). */
    private static int listWards(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        if (!ModConfig.ENABLE_WARDS.get()) {
            ctx.getSource().sendSuccess(() -> Component.literal("none (wards disabled)"), false);
            return 1;
        }
        ServerLevel level = player.level();
        WardLedger ledger = WardLedger.of(level);
        long now = level.getGameTime();
        ledger.prune(now);
        int shown = 0;
        for (WardLedger.Ward ward : ledger.wards()) {
            double dx = player.getX() - ward.x();
            double dz = player.getZ() - ward.z();
            if (!WardMath.covers(dx, dz, ward.radius())) {
                continue;
            }
            long seconds = WardMath.remainingTicks(now, ward.expiresAtGameTime()) / 20L;
            ctx.getSource().sendSuccess(() -> Component.literal("ward at " + ward.x() + ","
                    + ward.y() + "," + ward.z() + " r=" + ward.radius() + " " + seconds
                    + "s left"), false);
            shown++;
        }
        if (shown == 0) {
            ctx.getSource().sendSuccess(() -> Component.literal("none"), false);
        }
        return 1;
    }

    /**
     * {@code /eh gate}: the caller's pending mark (if any) and the link state of the nearest gate
     * within 16 blocks. A probe command: it reads only loaded chunks and never links or travels.
     */
    private static int gateInfo(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        RiftGates.Mark mark = RiftGates.pending(player.getUUID());
        if (mark == null) {
            ctx.getSource().sendSuccess(() -> Component.literal("mark: none"), false);
        } else {
            ctx.getSource().sendSuccess(() -> Component.literal("mark: "
                    + mark.dimension().identifier() + " " + mark.pos().getX() + ","
                    + mark.pos().getY() + "," + mark.pos().getZ()), false);
        }
        if (!ModConfig.ENABLE_RIFT_GATES.get()) {
            ctx.getSource().sendSuccess(() -> Component.literal("gates: disabled"), false);
        }
        ServerLevel level = player.level();
        GateLinks links = GateLinks.of(level);
        BlockPos here = player.blockPosition();
        BlockPos nearest = null;
        double best = 16.0 * 16.0;
        for (BlockPos gate : Rifts.near(level, here, 1, 64)) {
            double dx = gate.getX() - here.getX();
            double dy = gate.getY() - here.getY();
            double dz = gate.getZ() - here.getZ();
            double distance = dx * dx + dy * dy + dz * dz;
            if (distance <= best) {
                best = distance;
                nearest = gate;
            }
        }
        if (nearest == null) {
            ctx.getSource().sendSuccess(() -> Component.literal("gate within 16: none"), false);
            return 1;
        }
        BlockPos twin = links.linked(nearest);
        Component line = twin == null
                ? Component.literal("gate " + nearest.getX() + "," + nearest.getY() + ","
                        + nearest.getZ() + " -> unlinked")
                : Component.literal("gate " + nearest.getX() + "," + nearest.getY() + ","
                        + nearest.getZ() + " -> " + twin.getX() + "," + twin.getY() + ","
                        + twin.getZ());
        ctx.getSource().sendSuccess(() -> line, false);
        return 1;
    }

    /**
     * {@code /eh doom}: the overworld meter, its passive rate per minute, the tracked open rifts,
     * whether the 70% awakening has fired and the endgame outcome. A read-only probe.
     */
    private static int doomInfo(CommandContext<CommandSourceStack> ctx) {
        ServerLevel overworld = ctx.getSource().getServer().overworld();
        DoomClock clock = DoomClock.of(overworld);
        int rifts = RiftLedger.of(overworld).openCount();
        double rate = DoomClock.ratePerMinute(overworld);
        String outcome = Endgame.outcome(overworld).name().toLowerCase(java.util.Locale.ROOT);
        String line = String.format(java.util.Locale.ROOT,
                "doom = %.2f/100 (%.2f/min), open rifts %d, awakened %s, outcome %s",
                clock.doom(), rate, rifts, clock.awakened(), outcome);
        ctx.getSource().sendSuccess(() -> Component.literal(line), false);
        return 1;
    }

    /** {@code /eh ship}: nearest port, line state, next departure and live hull positions. */
    private static int shipInfo(CommandContext<CommandSourceStack> ctx) {
        ServerLevel overworld = ctx.getSource().getServer().overworld();
        net.minecraft.world.phys.Vec3 at = ctx.getSource().getPosition();
        for (String line : com.sanchous98.eldritchhorror.world.transport.ShipService.describe(
                overworld, at.x, at.z)) {
            ctx.getSource().sendSuccess(() -> Component.literal(line), false);
        }
        return 1;
    }

    /** {@code /eh ports}: every harbour the service knows, with its berth and berth level. */
    private static int portsInfo(CommandContext<CommandSourceStack> ctx) {
        com.sanchous98.eldritchhorror.world.EarthMap map =
                com.sanchous98.eldritchhorror.world.EarthMaps.getOrNull();
        for (com.sanchous98.eldritchhorror.world.transport.Port port
                : com.sanchous98.eldritchhorror.world.transport.Ports.all()) {
            String line = String.format(java.util.Locale.ROOT,
                    "%s: berth (%d, %d) y=%d shore (%d, %d) sea=%s",
                    port.cityId(), port.waterX(), port.waterZ(), port.waterY(),
                    port.shoreX(), port.shoreZ(),
                    map != null && !map.isLand(port.waterX(), port.waterZ()));
            ctx.getSource().sendSuccess(() -> Component.literal(line), false);
        }
        return 1;
    }

    /**
     * {@code /eh ship board}: debug ride hook. Finds the nearest packet within 16 blocks and calls
     * the same {@code tryBoard} used by right-click, so the window/fare/seat refusals are exercised
     * by the probe rather than bypassed.
     */
    private static int boardShip(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if (!ModConfig.ENABLE_SHIPS.get()) {
            ctx.getSource().sendFailure(Component.literal("Ships are disabled (enableShips=false)."));
            return 0;
        }
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        com.sanchous98.eldritchhorror.entity.CoastalPacket best = null;
        double bestDistance = 16.0 * 16.0;
        for (com.sanchous98.eldritchhorror.entity.CoastalPacket ship
                : player.level().getEntitiesOfClass(
                        com.sanchous98.eldritchhorror.entity.CoastalPacket.class,
                        player.getBoundingBox().inflate(16.0))) {
            double distance = ship.distanceToSqr(player);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = ship;
            }
        }
        if (best == null) {
            ctx.getSource().sendFailure(Component.literal("No packet within 16 blocks."));
            return 0;
        }
        boolean boarded = best.tryBoard(player);
        if (!boarded) {
            ctx.getSource().sendFailure(Component.literal("Boarding refused."));
            return 0;
        }
        String key = best.serviceKey();
        ctx.getSource().sendSuccess(() -> Component.literal("Boarded " + key), false);
        return 1;
    }

    /** {@code /eh train}: nearest station, line/timetable state and live consist positions. */
    private static int trainInfo(CommandContext<CommandSourceStack> ctx) {
        ServerLevel overworld = ctx.getSource().getServer().overworld();
        net.minecraft.world.phys.Vec3 at = ctx.getSource().getPosition();
        for (String line : com.sanchous98.eldritchhorror.world.rail.TrainService.describe(
                overworld, at.x, at.z)) {
            ctx.getSource().sendSuccess(() -> Component.literal(line), false);
        }
        return 1;
    }

    /**
     * {@code /eh train board}: debug ride hook. Finds the nearest train car within 16 blocks and
     * calls the same {@code tryBoard} used by right-click, so the window/fare/seat refusals are
     * exercised by the probe rather than bypassed.
     */
    private static int boardTrain(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if (!ModConfig.ENABLE_TRAINS.get()) {
            ctx.getSource().sendFailure(Component.literal("Trains are disabled (enableTrains=false)."));
            return 0;
        }
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        com.sanchous98.eldritchhorror.entity.TrainCar best = null;
        double bestDistance = 16.0 * 16.0;
        net.minecraft.world.phys.AABB search = new net.minecraft.world.phys.AABB(
                player.position(), player.position()).inflate(16.0);
        for (com.sanchous98.eldritchhorror.entity.TrainCar car
                : player.level().getEntitiesOfClass(
                        com.sanchous98.eldritchhorror.entity.TrainCar.class, search)) {
            double distance = car.distanceToSqr(player);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = car;
            }
        }
        if (best == null) {
            ctx.getSource().sendFailure(Component.literal("No train car within 16 blocks."));
            return 0;
        }
        boolean boarded = best.tryBoard(player);
        if (!boarded) {
            ctx.getSource().sendFailure(Component.literal("Boarding refused."));
            return 0;
        }
        com.sanchous98.eldritchhorror.entity.TrainLocomotive lead = best.locomotive();
        String key = lead == null ? "train" : lead.serviceKey();
        ctx.getSource().sendSuccess(() -> Component.literal("Boarded " + key), false);
        return 1;
    }

    /** {@code /eh pregen <cities|rail|all>}: start the tick-sliced pre-generation queue. */
    private static int pregen(CommandContext<CommandSourceStack> ctx, String scope) {
        String result = com.sanchous98.eldritchhorror.world.PregenService.start(
                ctx.getSource().getServer(), scope);
        ctx.getSource().sendSuccess(() -> Component.literal(result), true);
        return 1;
    }

    /** {@code /eh pregen radius <blocks>}: cities plus a square margin, plus rail corridors. */
    private static int pregenRadius(CommandContext<CommandSourceStack> ctx) {
        int blocks = IntegerArgumentType.getInteger(ctx, "blocks");
        String result = com.sanchous98.eldritchhorror.world.PregenService.startRadius(
                ctx.getSource().getServer(), blocks);
        ctx.getSource().sendSuccess(() -> Component.literal(result), true);
        return 1;
    }

    /** {@code /eh pregen status}: queue progress and ETA. */
    private static int pregenStatus(CommandContext<CommandSourceStack> ctx) {        ctx.getSource().sendSuccess(() -> Component.literal(
                com.sanchous98.eldritchhorror.world.PregenService.status()), false);
        return 1;
    }

    /** {@code /eh pregen stop}: cancel the current queue. */
    private static int pregenStop(CommandContext<CommandSourceStack> ctx) {
        com.sanchous98.eldritchhorror.world.PregenService.stop();
        ctx.getSource().sendSuccess(() -> Component.literal("pregen stopped"), true);
        return 1;
    }

    /**
     * {@code /eh cultist <cult> <count>}: the ritual-cell probe. Spawns persistent worshippers of one
     * cult around the caller, so a cell can form without depending on a site's random population.
     * Gamemaster-gated like the other mutation commands.
     */
    private static int spawnCultists(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if (!ctx.getSource().permissions().hasPermission(
                net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)) {
            ctx.getSource().sendFailure(Component.literal("Requires gamemaster permissions."));
            return 0;
        }
        String cultId = StringArgumentType.getString(ctx, "cult");
        if (unknownCult(ctx, cultId)) {
            return 0;
        }
        int count = IntegerArgumentType.getInteger(ctx, "count");
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ServerLevel level = player.level();
        BlockPos centre = player.blockPosition();
        int spawned = 0;
        for (int i = 0; i < count; i++) {
            int angle = i * 360 / count;
            int x = centre.getX() + (int) Math.round(Math.cos(Math.toRadians(angle)) * 3.0);
            int z = centre.getZ() + (int) Math.round(Math.sin(Math.toRadians(angle)) * 3.0);
            BlockPos spot = BestiarySupport.surfaceSpot(level, x, z, true);
            if (spot == null) {
                spot = BestiarySupport.surfaceSpot(level, centre.getX(), centre.getZ(), true);
            }
            if (spot == null) {
                continue;
            }
            Worshipper worshipper = ModEntities.WORSHIPPER.get().spawn(level, spot,
                    EntitySpawnReason.COMMAND);
            if (worshipper == null) {
                continue;
            }
            worshipper.setCult(cultId, CultRank.MEMBER);
            worshipper.setPersistenceRequired();
            spawned++;
        }
        int total = spawned;
        ctx.getSource().sendSuccess(() -> Component.literal(
                "spawned " + total + " " + cultId + " cultist(s)"), true);
        return spawned > 0 ? 1 : 0;
    }

    /** {@code /eh endgame}: the world's decision so far (design/11). */
    private static int getEndgame(CommandContext<CommandSourceStack> ctx) {
        String outcome = Endgame.outcome(ctx.getSource().getLevel()).name()
                .toLowerCase(java.util.Locale.ROOT);
        ctx.getSource().sendSuccess(() -> Component.literal("endgame = " + outcome), false);
        return 1;
    }

    /** {@code /eh endgame set <none|banished|summoned>}: a testing and repair aid for the fork. */
    private static int setEndgame(CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "outcome");
        for (EndgameState.Outcome value : EndgameState.Outcome.values()) {
            if (value.name().equalsIgnoreCase(name)) {
                EndgameState.of(ctx.getSource().getLevel()).setOutcome(value);
                ctx.getSource().sendSuccess(() -> Component.literal("endgame set to " + name), true);
                return 1;
            }
        }
        ctx.getSource().sendFailure(Component.literal(
                "Unknown outcome: " + name + " (none | banished | summoned)"));
        return 0;
    }

    /** {@code /eh quests}: the journal and the caller's progress. */
    private static int listQuests(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        java.util.List<com.sanchous98.eldritchhorror.quest.Quest> quests =
                com.sanchous98.eldritchhorror.quest.Quests.all();
        if (quests.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal("No quests are loaded."), false);
            return 1;
        }
        com.sanchous98.eldritchhorror.investigator.Investigator chosen =
                com.sanchous98.eldritchhorror.investigator.InvestigatorAPI.get(player);
        String chosenId = chosen == null ? "" : chosen.id();
        com.sanchous98.eldritchhorror.quest.Quest next = null;
        for (com.sanchous98.eldritchhorror.quest.Quest quest : quests) {
            if (!quest.forInvestigator(chosenId)) {
                continue; // another investigator's field-kit tasks
            }
            int progress = com.sanchous98.eldritchhorror.quest.QuestJournal.progress(player, quest.id());
            int target = quest.target();
            boolean done = progress >= target;
            // Repeatables report their cadence and time left, so a debug session can see the
            // cooldown without touching the saved attachment.
            String cadence = "";
            if (quest.repeat().periodSeconds() > 0L) {
                long remaining = com.sanchous98.eldritchhorror.quest.QuestJournal
                        .cooldownSeconds(player, quest);
                cadence = remaining > 0L
                        ? " [" + quest.repeat().key() + ", " + remaining + "s]"
                        : " [" + quest.repeat().key() + ", ready]";
            }
            String suffix = cadence;
            ctx.getSource().sendSuccess(() -> Component.literal((done ? "[x] " : "[ ] ")
                    + quest.id() + " " + Math.min(progress, target) + "/" + target + suffix + " — ")
                    .append(quest.description()), false);
            if (!done && next == null) {
                next = quest;
            }
        }
        if (next != null) {
            com.sanchous98.eldritchhorror.quest.Quest hint = next;
            ctx.getSource().sendSuccess(() -> Component.literal("Next: ").append(hint.name())
                    .append(Component.literal(" — ")).append(hint.description()), false);
        }
        return 1;
    }

    /** {@code /eh prologue reset}: clear PROLOGUE_DONE and teleport back to the Threshold. */
    private static int resetPrologue(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if (!ctx.getSource().permissions().hasPermission(
                net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)) {
            ctx.getSource().sendFailure(Component.literal("Requires gamemaster permissions."));
            return 0;
        }
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        if (!Prologue.reset(player)) {
            ctx.getSource().sendFailure(Component.literal("The Threshold dimension is not loaded."));
            return 0;
        }
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Prologue reset: returned to the Threshold."), false);
        return 1;
    }

    /** {@code /eh city}: report the nearest curated city and its corruption-driven state. */
    /** {@code /eh city taint <id> <0..1>}: test/admin override of the authoritative city taint. */
    private static int setCityTaint(CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "city");
        double value = DoubleArgumentType.getDouble(ctx, "value");
        City found = null;
        for (City candidate : Cities.all()) {
            if (candidate.id().equalsIgnoreCase(name) || candidate.name().equalsIgnoreCase(name)) {
                found = candidate;
                break;
            }
        }
        if (found == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown city: " + name));
            return 0;
        }
        City city = found;
        ServerLevel level = ctx.getSource().getServer().overworld();
        CityState before = CityStates.stateOf(level, city);
        CityLedger.of(level).setTaint(city, value, level.getGameTime());
        CityState state = CityStates.stateOf(level, city);
        if (before != CityState.FALLEN && state == CityState.FALLEN) {
            CityFallShock.apply(level, city);
        }
        ctx.getSource().sendSuccess(() -> Component.literal(
                city.name() + ": taint " + value + " -> " + state), true);
        return 1;
    }

    /** {@code /eh cityquest}: the caller's contract in the nearest curated city. */
    private static int cityQuest(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        if (!(player.level() instanceof ServerLevel level)) {
            ctx.getSource().sendFailure(Component.literal("Not in a server level."));
            return 0;
        }
        var city = com.sanchous98.eldritchhorror.world.city.quest.CityQuests
                .nearestCity(level, player, 48);
        if (city == null) {
            ctx.getSource().sendFailure(Component.literal("No curated city nearby."));
            return 0;
        }
        String line = com.sanchous98.eldritchhorror.world.city.quest.CityQuests
                .summary(player, city);
        ctx.getSource().sendSuccess(() -> Component.literal(line), false);
        return 1;
    }

    private static int cityState(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {        ServerPlayer player = ctx.getSource().getPlayerOrException();
        if (!(player.level() instanceof ServerLevel level)) {
            ctx.getSource().sendFailure(Component.literal("Not in a server level."));
            return 0;
        }
        City city = CityStates.nearestCity(level, player.blockPosition());
        if (city == null) {
            ctx.getSource().sendFailure(Component.literal("You are not inside a curated city."));
            return 0;
        }
        CityState state = CityStates.stateOf(level, city);
        ctx.getSource().sendSuccess(() -> Component.literal(
                city.name() + ": " + state.name().toLowerCase(java.util.Locale.ROOT)
                        + " (shelter x" + state.recoveryMultiplier() + ")"), false);
        return 1;
    }

    /**
     * {@code /eh city lights}: counts the light-emitting blocks of the nearest curated city's
     * district, in chunks that are <em>already loaded</em> — never forcing a load or a generation.
     *
     * <p>Emission is read from the block state itself, so torches, lanterns, campfires, glowstone
     * and any modded block with a positive light level all count without a hardcoded block list.
     * The histogram is split into 32-block radial bands from the city centre; blocks within
     * {@value #GROUND_SPAN} blocks of the caller's Y are also reported as "ground". The scan is
     * bounded by {@value #MAX_SCAN_CHUNKS} chunks and is read-only.
     */
    private static int cityLights(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        if (!(player.level() instanceof ServerLevel level)) {
            ctx.getSource().sendFailure(Component.literal("Not in a server level."));
            return 0;
        }
        City city = CityStates.nearestCity(level, player.blockPosition());
        if (city == null) {
            ctx.getSource().sendFailure(Component.literal("You are not inside a curated city."));
            return 0;
        }
        int radius = new com.sanchous98.eldritchhorror.world.loc.city.CityLocation(city).radius();
        int bandCount = (radius + LIGHT_BAND - 1) / LIGHT_BAND;
        int[] bands = new int[bandCount];
        int total = 0;
        int ground = 0;
        int scanned = 0;
        boolean capped = false;
        long radiusSq = (long) radius * radius;
        int centreX = city.x();
        int centreZ = city.z();
        int centreY = player.getBlockY();
        int centreChunkX = centreX >> 4;
        int centreChunkZ = centreZ >> 4;
        int chunkRadius = (radius >> 4) + 1;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        scan:
        for (int cx = centreChunkX - chunkRadius; cx <= centreChunkX + chunkRadius; cx++) {
            for (int cz = centreChunkZ - chunkRadius; cz <= centreChunkZ + chunkRadius; cz++) {
                int minX = cx << 4;
                int minZ = cz << 4;
                int nearestX = Math.clamp(centreX, minX, minX + 15);
                int nearestZ = Math.clamp(centreZ, minZ, minZ + 15);
                long offX = nearestX - centreX;
                long offZ = nearestZ - centreZ;
                if (offX * offX + offZ * offZ > radiusSq) {
                    continue; // the whole chunk lies outside the district circle
                }
                LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
                if (chunk == null) {
                    continue; // unloaded: skip rather than force-load
                }
                if (++scanned > MAX_SCAN_CHUNKS) {
                    capped = true;
                    break scan;
                }
                for (int index = 0; index < chunk.getSections().length; index++) {
                    LevelChunkSection section = chunk.getSection(index);
                    if (section.hasOnlyAir() || !section.maybeHas(
                            state -> state.getLightEmission(level, BlockPos.ZERO) > 0)) {
                        continue;
                    }
                    int baseY = chunk.getSectionYFromSectionIndex(index) << 4;
                    for (int ly = 0; ly < 16; ly++) {
                        int y = baseY + ly;
                        boolean atGround = Math.abs(y - centreY) <= GROUND_SPAN;
                        for (int lx = 0; lx < 16; lx++) {
                            int x = minX + lx;
                            long dx = x - centreX;
                            for (int lz = 0; lz < 16; lz++) {
                                BlockState state = section.getBlockState(lx, ly, lz);
                                if (state.getLightEmission(level, cursor.set(x, y, minZ + lz)) <= 0) {
                                    continue;
                                }
                                long dz = minZ + lz - centreZ;
                                long distanceSq = dx * dx + dz * dz;
                                if (distanceSq > radiusSq) {
                                    continue;
                                }
                                int band = (int) (Math.sqrt(distanceSq) / LIGHT_BAND);
                                bands[Math.min(band, bandCount - 1)]++;
                                total++;
                                if (atGround) {
                                    ground++;
                                }
                            }
                        }
                    }
                }
            }
        }
        CityState state = CityStates.stateOf(level, city);
        StringBuilder line = new StringBuilder(128);
        line.append(city.name())
                .append(": ").append(state.name().toLowerCase(java.util.Locale.ROOT))
                .append(", r=").append(radius)
                .append(", lights ").append(total)
                .append(" (ground +/-").append(GROUND_SPAN).append(' ').append(ground).append(')')
                .append(", bands");
        for (int i = 0; i < bands.length; i++) {
            line.append(' ').append(i * LIGHT_BAND).append('-')
                    .append(Math.min((i + 1) * LIGHT_BAND, radius)).append(':').append(bands[i]);
        }
        line.append(", chunks ").append(scanned).append(capped ? "+ (capped)" : "");
        String text = line.toString();
        ctx.getSource().sendSuccess(() -> Component.literal(text), false);
        return 1;
    }

    /** {@code /eh home}: debug teleport to the city chosen in the prologue (respawn does it normally). */
    private static int goHome(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if (!ctx.getSource().permissions().hasPermission(
                net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)) {
            ctx.getSource().sendFailure(Component.literal("Requires gamemaster permissions."));
            return 0;
        }
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        if (Prologue.recallHome(p)) {
            ctx.getSource().sendSuccess(() -> Component.literal("Returned to your starting city."), false);
            return 1;
        }
        ctx.getSource().sendFailure(Component.literal("No starting city is set for you."));
        return 0;
    }

    /** {@code /eh ...}: refuses when the feature's command toggle is off (config read at runtime). */
    private static boolean commandsDisabled(CommandContext<CommandSourceStack> ctx, boolean enabled) {
        if (!enabled) {
            ctx.getSource().sendFailure(Component.literal("That command is disabled in the config."));
            return true;
        }
        return false;
    }

    /**
     * {@code /eh event <id>}: force-starts an event on the caller for testing, bypassing the master
     * switch, the per-event toggle and cooldowns.
     */
    private static int forceEvent(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if (commandsDisabled(ctx, ModConfig.ENABLE_EVENT_COMMANDS.get())) {
            return 0;
        }
        String id = StringArgumentType.getString(ctx, "id");
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        int tick = ctx.getSource().getServer().getTickCount();
        EldritchEvent started = EventTicker.forceStart(player, id, tick);
        if (started == null) {
            ctx.getSource().sendFailure(Component.literal(
                    "Event not started (unknown id, events disabled, or not in the overworld): " + id));
            return 0;
        }
        ctx.getSource().sendSuccess(() -> Component.literal("[" + started.id() + "] started ("
                + (started.duration() / 20) + "s, trigger " + started.trigger() + ")"), true);
        return 1;
    }

    /** {@code /eh events}: lists what is active for the caller, then the known event ids. */
    private static int listEvents(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if (commandsDisabled(ctx, ModConfig.ENABLE_EVENT_COMMANDS.get())) {
            return 0;
        }
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        int tick = ctx.getSource().getServer().getTickCount();
        List<String> active = EventTicker.activeFor(player, tick);
        String activeLine = active.isEmpty() ? "none" : String.join(" ", active);
        ctx.getSource().sendSuccess(() -> Component.literal("Active events: " + activeLine), false);
        StringBuilder known = new StringBuilder();
        for (EldritchEvent def : Events.all()) {
            if (known.length() > 0) {
                known.append(' ');
            }
            known.append(def.id());
        }
        ctx.getSource().sendSuccess(() -> Component.literal("Known events: " + known), false);
        return 1;
    }

    /**
     * {@code /eh rite <id>}: performs a known rite for the caller through {@link RiteEngine}. The
     * engine applies the documented sanity/corruption cost and reports exactly what happened —
     * success or an explicit not-implemented/failure reason (never a silent no-op).
     */
    private static int performRite(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String id = StringArgumentType.getString(ctx, "id");
        RiteDefinition rite = Rites.byId(id);
        if (rite == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown rite: " + id));
            return 0;
        }
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        if (!RiteKnowledge.knows(p, id)) {
            ctx.getSource().sendFailure(Component.literal("You do not know the rite: " + id));
            return 0;
        }
        RiteEngine.Result result = RiteEngine.perform(p, rite);
        if (result.ok()) {
            ctx.getSource().sendSuccess(() -> Component.literal("[")
                    .append(rite.name()).append("] ").append(result.message()), true);
            return 1;
        }
        ctx.getSource().sendFailure(Component.literal("[")
                .append(rite.name()).append("] ").append(result.message()));
        return 0;
    }

    /** {@code /eh rites}: lists the rites the caller knows. */
    private static int listRites(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        var known = RiteKnowledge.known(p);
        String message = known.isEmpty()
                ? "You know no rites."
                : "Known rites: " + String.join(" ", new java.util.TreeSet<>(known));
        ctx.getSource().sendSuccess(() -> Component.literal(message), false);
        return 1;
    }

    /** {@code /eh investigator get}: prints the caller's chosen investigator, or "none". */
    private static int getInvestigator(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if (commandsDisabled(ctx, ModConfig.ENABLE_INVESTIGATOR_COMMANDS.get())) {
            return 0;
        }
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        Investigator id = InvestigatorAPI.get(p);
        if (id == null) {
            ctx.getSource().sendSuccess(() -> Component.literal("none"), false);
        } else {
            ctx.getSource().sendSuccess(() -> Component.literal(id.id() + " (").append(id.displayName())
                    .append(Component.literal(")")), false);
        }
        return 1;
    }

    /**
     * {@code /eh investigator set <id>}: gamemaster debug reset that switches the caller to
     * {@code id} and re-grants that investigator's kit and rites, even if a different one was already
     * chosen (the only way to change an investigator).
     */
    private static int setInvestigator(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if (commandsDisabled(ctx, ModConfig.ENABLE_INVESTIGATOR_COMMANDS.get())) {
            return 0;
        }
        if (!ctx.getSource().permissions().hasPermission(
                net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)) {
            ctx.getSource().sendFailure(Component.literal("Requires gamemaster permissions."));
            return 0;
        }
        String id = StringArgumentType.getString(ctx, "id");
        Investigator target = Investigator.byId(id);
        if (target == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown investigator: " + id));
            return 0;
        }
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        // A fresh player follows the normal path (the field kit is earned through the two tasks);
        // a gamemaster switching an already-chosen investigator is a reset and keeps the debug kit.
        if (InvestigatorAPI.hasChosen(p)) {
            InvestigatorAPI.forceSet(p, target);
        } else {
            InvestigatorAPI.set(p, target);
        }
        ctx.getSource().sendSuccess(() -> Component.literal("investigator set to " + target.id()), true);
        return 1;
    }

    /**
     * {@code /eh cult <id>}: prints the cult's domain, the caller's reputation and rank, and every
     * service with its required rank — marking which are unlocked at the current standing.
     */
    private static int cultInfo(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String id = StringArgumentType.getString(ctx, "id");
        CultDefinition def = Cults.byId(id);
        if (def == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown cult: " + id));
            return 0;
        }
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        int rep = CultSystem.get(p, id);
        CultRank rank = CultSystem.rankOf(p, id);
        ctx.getSource().sendSuccess(() -> Component.empty()
                .append(Component.translatable("cult.eldritch_horror." + id))
                .append(Component.literal(" — " + def.domain())), false);
        ctx.getSource().sendSuccess(() -> Component.literal("rep = " + rep + " (")
                .append(rank.display()).append(Component.literal(")")), false);
        for (CultService service : def.services()) {
            boolean unlocked = rank.atLeast(service.minRank());
            Component req = Component.translatable("service.eldritch_horror.requires",
                    service.minRank().display());
            String mark = unlocked ? "[x] " : "[ ] ";
            ctx.getSource().sendSuccess(() -> Component.literal(mark)
                    .append(service.name())
                    .append(Component.literal(" ("))
                    .append(req)
                    .append(Component.literal(")")), false);
        }
        return 1;
    }

    /**
     * {@code /eh service <cult> <service>}: performs a rank-gated service for the caller. A rank
     * shortfall is an explicit failure naming the requirement — never a silent no-op.
     */
    private static int performService(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String cultId = StringArgumentType.getString(ctx, "cult");
        CultDefinition def = Cults.byId(cultId);
        if (def == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown cult: " + cultId));
            return 0;
        }
        String serviceId = StringArgumentType.getString(ctx, "service");
        CultService service = def.service(serviceId);
        if (service == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown service: "
                    + serviceId + " for cult " + cultId));
            return 0;
        }
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        CultServices.Outcome outcome = CultServices.perform(p, cultId, service);
        if (outcome.ok()) {
            ctx.getSource().sendSuccess(() -> outcome.message(), true);
            return 1;
        }
        ctx.getSource().sendFailure(outcome.message());
        return 0;
    }

    /**
     * {@code /eh codex}: lists the caller's discovered entries, grouped by category. Names are the
     * entry's display name; ids are shown too so the debug {@code learn} command has a target.
     */
    private static int listCodex(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if (commandsDisabled(ctx, ModConfig.ENABLE_CODEX_COMMANDS.get())) {
            return 0;
        }
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        var known = CodexAPI.all(p);
        if (known.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal("Your codex is empty."), false);
            return 1;
        }
        ctx.getSource().sendSuccess(() -> Component.literal("Codex - " + known.size() + " entries"), false);
        for (CodexCategory category : CodexCategory.ordered()) {
            List<Component> names = new java.util.ArrayList<>();
            for (CodexEntry entry : CodexRegistry.all().values()) {
                if (entry.category() == category && known.contains(entry.id())) {
                    names.add(entry.name());
                }
            }
            if (names.isEmpty()) {
                continue;
            }
            ctx.getSource().sendSuccess(() -> {
                Component line = Component.literal("  " + category.id() + " (" + names.size() + "): ");
                for (int i = 0; i < names.size(); i++) {
                    if (i > 0) {
                        line = line.copy().append(Component.literal(", "));
                    }
                    line = line.copy().append(names.get(i));
                }
                return line;
            }, false);
        }
        return 1;
    }

    /**
     * {@code /eh codex learn <id>}: debug discovery of one entry. Gated behind gamemaster
     * permissions, like the other operator mutation tools.
     */
    private static int learnCodex(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if (commandsDisabled(ctx, ModConfig.ENABLE_CODEX_COMMANDS.get())) {
            return 0;
        }
        String id = StringArgumentType.getString(ctx, "id");
        if (!ctx.getSource().permissions().hasPermission(
                net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)) {
            ctx.getSource().sendFailure(Component.literal("Requires gamemaster permissions."));
            return 0;
        }
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        CodexEntry entry = CodexRegistry.byId(id);
        if (entry == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown codex entry: " + id));
            return 0;
        }
        boolean learned = CodexAPI.learn(p, id);
        ctx.getSource().sendSuccess(() -> Component.literal(learned
                ? "[" + entry.category().id() + "] learned " + id
                : "Already known: " + id), true);
        return 1;
    }

    /** {@code /eh codexentries}: lists every known entry id by category (debug/authoring aid). */
    private static int listCodexEntries(CommandContext<CommandSourceStack> ctx) {
        if (commandsDisabled(ctx, ModConfig.ENABLE_CODEX_COMMANDS.get())) {
            return 0;
        }
        if (!ctx.getSource().permissions().hasPermission(
                net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)) {
            ctx.getSource().sendFailure(Component.literal("gamemaster permission required"));
            return 0;
        }
        for (CodexCategory category : CodexCategory.ordered()) {
            List<String> ids = CodexRegistry.idsIn(category);
            ctx.getSource().sendSuccess(() -> Component.literal(category.id() + " (" + ids.size()
                    + "): " + String.join(", ", ids)), false);
        }
        return 1;
    }

    /**
     * {@code /eh taint purify}: zeroes the taint of every loaded chunk near the caller. A
     * debug tool to observe the terrain effect stopping; unloaded chunks are left untouched and
     * no chunk is ever forced to load.
     */
    private static int purifyTaint(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer p = ctx.getSource().getPlayerOrException();
        if (!(p.level() instanceof ServerLevel level)) {
            return 0;
        }
        ChunkPos centre = p.chunkPosition();
        int cleared = 0;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                ChunkPos pos = new ChunkPos(centre.x() + dx, centre.z() + dz);
                double before = TaintAPI.get(level, pos);
                if (before > 0.0) {
                    TaintAPI.set(level, pos, 0.0);
                    cleared++;
                }
            }
        }
        int n = cleared;
        ctx.getSource().sendSuccess(() -> Component.literal("taint purified in " + n
                + " loaded chunk(s)"), true);
        return 1;
    }

    /** Sends a failure message and returns {@code true} when {@code cultId} is not a known cult. */
    private static boolean unknownCult(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx, String cultId) {
        if (Cults.byId(cultId) == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown cult: " + cultId));
            return true;
        }
        return false;
    }

    /**
     * The player's own chunk without ever loading/generating one. It is always loaded (the player
     * stands in it), but {@code getChunkNow} is nullable, so callers get {@code null} rather than an NPE.
     */
    private static net.minecraft.world.level.chunk.@org.jspecify.annotations.Nullable LevelChunk ownChunk(ServerPlayer p) {
        var cp = p.chunkPosition();
        return p.level().getChunkSource().getChunkNow(cp.x(), cp.z());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> getNode(String label, Meter meter) {
        return Commands.literal("get").executes(ctx -> {
            ServerPlayer p = ctx.getSource().getPlayerOrException();
            ctx.getSource().sendSuccess(
                    () -> Component.literal(label + " = " + fmt(meter.get(p))), false);
            return 1;
        });
    }

    private static LiteralArgumentBuilder<CommandSourceStack> setNode(String label, Setter setter) {
        return Commands.literal("set")
                .requires(GAMEMASTER)
                .then(Commands.argument("value", DoubleArgumentType.doubleArg())
                        .executes(ctx -> {
                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                            double v = DoubleArgumentType.getDouble(ctx, "value");
                            setter.set(p, v);
                            ctx.getSource().sendSuccess(
                                    () -> Component.literal(label + " set to " + fmt(v)), true);
                            return 1;
                        }));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> addNode(String label, Adder adder) {
        return Commands.literal("add")
                .requires(GAMEMASTER)
                .then(Commands.argument("delta", DoubleArgumentType.doubleArg())
                        .executes(ctx -> {
                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                            double d = DoubleArgumentType.getDouble(ctx, "delta");
                            double now = adder.add(p, d);
                            ctx.getSource().sendSuccess(
                                    () -> Component.literal(label + " = " + fmt(now)), true);
                            return 1;
                        }));
    }

    /** {@code /eh boss list}: every Ancient One the probe can summon. */
    private static int listBosses(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(() -> Component.literal(
                "boss ids: " + String.join(", ", BOSSES.keySet())), false);
        return BOSSES.size();
    }

    /**
     * {@code /eh boss <id>}: summons that Ancient One at the caller and prints its ability ids, so
     * the telegraph/interrupt loop can be exercised without waiting for a spawn roll.
     */
    private static int summonBoss(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if (!ctx.getSource().permissions().hasPermission(
                net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)) {
            ctx.getSource().sendFailure(Component.literal("Requires gamemaster permissions."));
            return 0;
        }
        String id = StringArgumentType.getString(ctx, "id").toLowerCase(java.util.Locale.ROOT);
        Supplier<? extends EntityType<? extends AncientOne>> holder = BOSSES.get(id);
        if (holder == null) {
            ctx.getSource().sendFailure(Component.literal(
                    "Unknown boss '" + id + "'. Ids: " + String.join(", ", BOSSES.keySet())));
            return 0;
        }
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ServerLevel level = player.level();
        AncientOne boss = holder.get().spawn(level, player.blockPosition(), EntitySpawnReason.COMMAND);
        if (boss == null) {
            ctx.getSource().sendFailure(Component.literal("Could not summon " + id + " here (no room)."));
            return 0;
        }
        boss.setPersistenceRequired();
        boss.setTarget(player);
        List<String> abilities = boss.bossCombat().abilityIds();
        ctx.getSource().sendSuccess(() -> Component.literal("Summoned " + id + " ("
                + Math.round(boss.getMaxHealth()) + " HP). Telegraphed abilities: "
                + String.join(", ", abilities)), true);
        return 1;
    }

    private static String fmt(double v) {
        return String.format(java.util.Locale.ROOT, "%.2f", v);
    }

    private interface Meter {
        double get(ServerPlayer p);
    }

    private interface Setter {
        double set(ServerPlayer p, double v);
    }

    private interface Adder {
        double add(ServerPlayer p, double d);
    }
}
