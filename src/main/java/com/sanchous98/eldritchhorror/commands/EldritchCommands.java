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
import com.sanchous98.eldritchhorror.cult.Cults;
import com.sanchous98.eldritchhorror.event.EldritchEvent;
import com.sanchous98.eldritchhorror.event.EventTicker;
import com.sanchous98.eldritchhorror.event.Events;
import com.sanchous98.eldritchhorror.investigator.Investigator;
import com.sanchous98.eldritchhorror.investigator.InvestigatorAPI;
import com.sanchous98.eldritchhorror.rite.RiteDefinition;
import com.sanchous98.eldritchhorror.rite.RiteEngine;
import com.sanchous98.eldritchhorror.rite.RiteKnowledge;
import com.sanchous98.eldritchhorror.rite.Rites;
import com.sanchous98.eldritchhorror.sanity.SanitySystem;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
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
                                .then(Commands.literal("purify").executes(EldritchCommands::purifyTaint)))
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
                                .then(Commands.argument("id", StringArgumentType.greedyString())
                                        .executes(EldritchCommands::learnCodex))))
                .then(Commands.literal("codexentries").executes(EldritchCommands::listCodexEntries));
        eh.then(Commands.literal("event")
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(EldritchCommands::forceEvent)))
                .then(Commands.literal("events").executes(EldritchCommands::listEvents));
        dispatcher.register(eh);
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
            ctx.getSource().sendFailure(Component.literal("Unknown event (or not in the overworld): " + id));
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
        InvestigatorAPI.forceSet(p, target);
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
