package com.sanchous98.eldritchhorror.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sanchous98.eldritchhorror.corruption.CorruptionSystem;
import com.sanchous98.eldritchhorror.cult.CultSystem;
import com.sanchous98.eldritchhorror.cult.Cults;
import com.sanchous98.eldritchhorror.rite.RiteDefinition;
import com.sanchous98.eldritchhorror.rite.RiteEngine;
import com.sanchous98.eldritchhorror.rite.RiteKnowledge;
import com.sanchous98.eldritchhorror.rite.Rites;
import com.sanchous98.eldritchhorror.sanity.SanitySystem;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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
        dispatcher.register(
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
                                    net.minecraft.world.level.chunk.LevelChunk chunk =
                                            p.level().getChunkAt(p.blockPosition());
                                    double v = CorruptionSystem.getTaint(chunk);
                                    ctx.getSource().sendSuccess(
                                            () -> Component.literal("taint = " + fmt(v)), false);
                                    return 1;
                                }))
                                .then(Commands.literal("set")
                                        .then(Commands.argument("value", DoubleArgumentType.doubleArg())
                                                .executes(ctx -> {
                                                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                                                    net.minecraft.world.level.chunk.LevelChunk chunk =
                                                            p.level().getChunkAt(p.blockPosition());
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
                                                    net.minecraft.world.level.chunk.LevelChunk chunk =
                                                            p.level().getChunkAt(p.blockPosition());
                                                    double d = DoubleArgumentType.getDouble(ctx, "delta");
                                                    double now = CorruptionSystem.addTaint(chunk, d);
                                                    ctx.getSource().sendSuccess(
                                                            () -> Component.literal("taint = " + fmt(now)), true);
                                                    return 1;
                                                }))))
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
                                        .executes(ctx -> performRite(ctx))))
                        .then(Commands.literal("rites").executes(ctx -> listRites(ctx))));
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
        if (known.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal("You know no rites."), false);
            return 1;
        }
        String line = String.join(" ", new java.util.TreeSet<>(known));
        ctx.getSource().sendSuccess(() -> Component.literal("Known rites: " + line), false);
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
