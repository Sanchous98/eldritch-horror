package com.sanchous98.eldritchhorror.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.sanchous98.eldritchhorror.corruption.CorruptionSystem;
import com.sanchous98.eldritchhorror.sanity.SanitySystem;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Admin/debug commands for the RPG meters: {@code /eh sanity|corruption get|set|add}. These are
 * what make the stubs testable before gameplay reads them.
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
                                .then(addNode("corruption", CorruptionSystem::add))));
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
