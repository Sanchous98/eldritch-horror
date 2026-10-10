package com.sanchous98.eldritchhorror.bot;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sanchous98.eldritchhorror.corruption.CorruptionSystem;
import com.sanchous98.eldritchhorror.sanity.SanitySystem;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.jspecify.annotations.Nullable;

/**
 * {@code /eh bot ...}: dev-only administration of {@link BotPlayer}s for automated playtesting.
 *
 * <p>Every sub-command is gated behind dev mode ({@code -Deh.dev=true} or {@code EH_DEV=true}) and
 * gamemaster permissions. The command tree is registered unconditionally (the config is not loaded
 * when commands are built), so the gate runs in the handler.
 */
public final class BotCommands {

    private BotCommands() {
    }

    /** Builds the {@code bot} literal to attach under {@code /eh}. */
    public static LiteralArgumentBuilder<CommandSourceStack> node() {
        return Commands.literal("bot")
                .then(Commands.literal("spawn")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(BotCommands::spawn)))
                .then(Commands.literal("remove")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(BotCommands::remove)))
                .then(Commands.literal("list").executes(BotCommands::list))
                .then(Commands.literal("tp")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .then(Commands.argument("x", DoubleArgumentType.doubleArg())
                                        .then(Commands.argument("y", DoubleArgumentType.doubleArg())
                                                .then(Commands.argument("z", DoubleArgumentType.doubleArg())
                                                        .executes(BotCommands::tp))))))
                .then(Commands.literal("move")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .then(Commands.argument("dx", DoubleArgumentType.doubleArg())
                                        .then(Commands.argument("dz", DoubleArgumentType.doubleArg())
                                                .executes(BotCommands::move)))))
                .then(Commands.literal("use")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(BotCommands::use)))
                .then(Commands.literal("useitem")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(BotCommands::useitem)))
                .then(Commands.literal("useat")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .then(Commands.argument("x", DoubleArgumentType.doubleArg())
                                        .then(Commands.argument("y", DoubleArgumentType.doubleArg())
                                                .then(Commands.argument("z", DoubleArgumentType.doubleArg())
                                                        .executes(BotCommands::useat))))))
                .then(Commands.literal("attack")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(BotCommands::attack)))
                .then(Commands.literal("say")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .then(Commands.argument("text", StringArgumentType.greedyString())
                                        .executes(BotCommands::say))))
                .then(Commands.literal("sense")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(BotCommands::sense)))
                .then(Commands.literal("god")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .then(Commands.argument("value", BoolArgumentType.bool())
                                        .executes(BotCommands::god))));
    }

    /** {@code /eh bot spawn <name>}: adds a bot at the level's respawn point. */
    private static int spawn(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if (!gate(ctx)) {
            return 0;
        }
        String name = StringArgumentType.getString(ctx, "name");
        if (Bots.get(name) != null) {
            ctx.getSource().sendFailure(Component.literal("Bot already exists: " + name));
            return 0;
        }
        ServerLevel level = ctx.getSource().getLevel();
        Bots.spawn(level, name);
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Spawned bot " + name + " in " + level.dimension().identifier()), true);
        return 1;
    }

    /** {@code /eh bot remove <name>}: removes a bot and fires its logout. */
    private static int remove(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if (!gate(ctx)) {
            return 0;
        }
        String name = StringArgumentType.getString(ctx, "name");
        if (!Bots.remove(ctx.getSource().getServer(), name)) {
            ctx.getSource().sendFailure(Component.literal("Unknown bot: " + name));
            return 0;
        }
        ctx.getSource().sendSuccess(() -> Component.literal("Removed bot " + name), true);
        return 1;
    }

    /** {@code /eh bot list}: name, position, dimension and sanity for every bot. */
    private static int list(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if (!gate(ctx)) {
            return 0;
        }
        List<BotPlayer> bots = Bots.all();
        if (bots.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal("No bots."), false);
            return 1;
        }
        for (BotPlayer bot : bots) {
            String line = String.format(Locale.ROOT, "%s @ %.1f, %.1f, %.1f in %s sanity=%.2f",
                    bot.getGameProfile().name(), bot.getX(), bot.getY(), bot.getZ(),
                    bot.level().dimension().identifier(), SanitySystem.get(bot));
            ctx.getSource().sendSuccess(() -> Component.literal(line), false);
        }
        return 1;
    }

    /** {@code /eh bot tp <name> <x> <y> <z>}: teleports a bot. */
    private static int tp(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if (!gate(ctx)) {
            return 0;
        }
        BotPlayer bot = require(ctx, StringArgumentType.getString(ctx, "name"));
        if (bot == null) {
            return 0;
        }
        double x = DoubleArgumentType.getDouble(ctx, "x");
        double y = DoubleArgumentType.getDouble(ctx, "y");
        double z = DoubleArgumentType.getDouble(ctx, "z");
        bot.teleportTo(x, y, z);
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Teleported " + bot.getGameProfile().name() + " to " + x + ", " + y + ", " + z), true);
        return 1;
    }

    /** {@code /eh bot move <name> <dx> <dz>}: teleports a bot by a horizontal delta. */
    private static int move(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if (!gate(ctx)) {
            return 0;
        }
        BotPlayer bot = require(ctx, StringArgumentType.getString(ctx, "name"));
        if (bot == null) {
            return 0;
        }
        double dx = DoubleArgumentType.getDouble(ctx, "dx");
        double dz = DoubleArgumentType.getDouble(ctx, "dz");
        bot.teleportTo(bot.getX() + dx, bot.getY(), bot.getZ() + dz);
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Moved " + bot.getGameProfile().name() + " by " + dx + ", " + dz), true);
        return 1;
    }

    /**
     * {@code /eh bot use <name>}: activates the block the bot is looking at (or the block under its
     * feet) through {@link net.minecraft.server.level.ServerPlayerGameMode#useItemOn}. This is the
     * same interaction path a client's use-item-on packet reaches, minus the packet plumbing.
     */
    private static int use(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if (!gate(ctx)) {
            return 0;
        }
        BotPlayer bot = require(ctx, StringArgumentType.getString(ctx, "name"));
        if (bot == null) {
            return 0;
        }
        BlockHitResult hit = blockTarget(bot);
        ItemStack stack = bot.getItemInHand(InteractionHand.MAIN_HAND);
        InteractionResult result = bot.gameMode.useItemOn(bot, bot.level(), stack, InteractionHand.MAIN_HAND, hit);
        BlockPos pos = hit.getBlockPos();
        ctx.getSource().sendSuccess(() -> Component.literal("bot " + bot.getGameProfile().name()
                + " used " + stack.getHoverName().getString() + " on " + pos.toShortString()
                + " -> " + result), true);
        return 1;
    }

    /** {@code /eh bot useat <name> <x> <y> <z>}: use a specific block position (bypasses look). */
    private static int useat(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if (!gate(ctx)) {
            return 0;
        }
        BotPlayer bot = require(ctx, StringArgumentType.getString(ctx, "name"));
        if (bot == null) {
            return 0;
        }
        BlockPos pos = BlockPos.containing(DoubleArgumentType.getDouble(ctx, "x"),
                DoubleArgumentType.getDouble(ctx, "y"), DoubleArgumentType.getDouble(ctx, "z"));
        BlockState state = bot.level().getBlockState(pos);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        ItemStack stack = bot.getItemInHand(InteractionHand.MAIN_HAND);
        InteractionResult result = bot.gameMode.useItemOn(bot, bot.level(), stack,
                InteractionHand.MAIN_HAND, hit);
        ctx.getSource().sendSuccess(() -> Component.literal("bot " + bot.getGameProfile().name()
                + " used on " + pos.toShortString() + " (" + state.getBlock() + ") -> " + result), true);
        return 1;
    }

    /** {@code /eh bot useitem <name>}: uses the main-hand item (consumables apply their deltas). */
    private static int useitem(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if (!gate(ctx)) {
            return 0;
        }
        BotPlayer bot = require(ctx, StringArgumentType.getString(ctx, "name"));
        if (bot == null) {
            return 0;
        }
        ItemStack stack = bot.getItemInHand(InteractionHand.MAIN_HAND);
        // Post the right-click-item event first so tome handlers (rite knowledge) fire.
        NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickItem(bot, InteractionHand.MAIN_HAND));
        InteractionResult result = stack.use(bot.level(), bot, InteractionHand.MAIN_HAND);
        ctx.getSource().sendSuccess(() -> Component.literal("bot " + bot.getGameProfile().name()
                + " used item " + stack.getHoverName().getString() + " -> " + result), true);
        return 1;
    }

    /** {@code /eh bot attack <name>}: attacks the nearest living entity within 5 blocks. */    private static int attack(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if (!gate(ctx)) {
            return 0;
        }
        BotPlayer bot = require(ctx, StringArgumentType.getString(ctx, "name"));
        if (bot == null) {
            return 0;
        }
        List<LivingEntity> targets = bot.level().getEntitiesOfClass(LivingEntity.class,
                bot.getBoundingBox().inflate(5.0), e -> e != bot && e.isAlive());
        if (targets.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal(
                    "No living entity within 5 blocks of " + bot.getGameProfile().name()));
            return 0;
        }
        LivingEntity target = targets.stream()
                .min(Comparator.comparingDouble(bot::distanceToSqr))
                .orElse(targets.get(0));
        bot.attack(target);
        ctx.getSource().sendSuccess(() -> Component.literal("bot " + bot.getGameProfile().name()
                + " attacked " + target.getName().getString()), true);
        return 1;
    }

    /** {@code /eh bot say <name> <text>}: broadcasts a chat line attributed to the bot. */
    private static int say(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if (!gate(ctx)) {
            return 0;
        }
        BotPlayer bot = require(ctx, StringArgumentType.getString(ctx, "name"));
        if (bot == null) {
            return 0;
        }
        String text = StringArgumentType.getString(ctx, "text");
        ctx.getSource().getServer().getPlayerList().broadcastSystemMessage(
                Component.literal("[" + bot.getGameProfile().name() + "] " + text), false);
        return 1;
    }

    /** {@code /eh bot god <name> <value>}: toggle a bot's invulnerability (for boss-fight testing). */
    private static int god(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if (!gate(ctx)) {
            return 0;
        }
        BotPlayer bot = require(ctx, StringArgumentType.getString(ctx, "name"));
        if (bot == null) {
            return 0;
        }
        boolean value = BoolArgumentType.getBool(ctx, "value");
        bot.setGameMode(value ? net.minecraft.world.level.GameType.CREATIVE
                : net.minecraft.world.level.GameType.SURVIVAL);
        ctx.getSource().sendSuccess(() -> Component.literal("bot " + bot.getGameProfile().name()
                + " creative=" + value), true);
        return 1;
    }

    /** {@code /eh bot sense <name>}: a compact JSON snapshot of the bot for an external agent. */
    private static int sense(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if (!gate(ctx)) {
            return 0;
        }
        BotPlayer bot = require(ctx, StringArgumentType.getString(ctx, "name"));
        if (bot == null) {
            return 0;
        }
        List<LivingEntity> near = new java.util.ArrayList<>(bot.level().getEntitiesOfClass(
                LivingEntity.class, bot.getBoundingBox().inflate(12.0), e -> e != bot && e.isAlive()));
        near.sort(Comparator.comparingDouble(bot::distanceToSqr));
        StringBuilder ents = new StringBuilder();
        for (int i = 0; i < near.size() && i < 6; i++) {
            LivingEntity e = near.get(i);
            if (ents.length() > 0) {
                ents.append(',');
            }
            ents.append('"').append(e.getName().getString().replace(' ', '_').replace('"', '_'))
                    .append(':').append(String.format(Locale.ROOT, "%.1f", Math.sqrt(bot.distanceToSqr(e))))
                    .append('"');
        }
        BlockPos aheadPos = blockTarget(bot).getBlockPos();
        String ahead = BuiltInRegistries.BLOCK.getKey(bot.level().getBlockState(aheadPos).getBlock())
                .toString();
        String json = String.format(Locale.ROOT,
                "{\"name\":\"%s\",\"dim\":\"%s\",\"pos\":[%.1f,%.1f,%.1f],\"health\":%.1f,"
                        + "\"onGround\":%b,\"sanity\":%.2f,\"corruption\":%.2f,"
                        + "\"ahead\":\"%s\",\"entities\":[%s]}",
                bot.getGameProfile().name(), bot.level().dimension().identifier(),
                bot.getX(), bot.getY(), bot.getZ(), bot.getHealth(), bot.onGround(),
                SanitySystem.get(bot), CorruptionSystem.get(bot), ahead, ents);
        ctx.getSource().sendSuccess(() -> Component.literal(json), false);
        return 1;
    }

    /** The block the bot is looking at, falling back to the block under its feet. */
    private static BlockHitResult blockTarget(BotPlayer bot) {
        HitResult hit = bot.pick(5.0, 0.0F, false);
        if (hit instanceof BlockHitResult blockHit) {
            return blockHit;
        }
        BlockPos feet = bot.blockPosition().below();
        return new BlockHitResult(Vec3.atCenterOf(feet), Direction.UP, feet, false);
    }

    /** Resolves a bot or sends a failure and returns {@code null}. */
    private static @Nullable BotPlayer require(CommandContext<CommandSourceStack> ctx, String name) {
        BotPlayer bot = Bots.get(name);
        if (bot == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown bot: " + name));
        }
        return bot;
    }

    /** Dev-mode + gamemaster gate, evaluated in the handler (the config is not loaded at registration). */
    private static boolean gate(CommandContext<CommandSourceStack> ctx) {
        if (!devMode()) {
            ctx.getSource().sendFailure(Component.literal(
                    "Bot commands require dev mode (-Deh.dev=true or EH_DEV=true)."));
            return false;
        }
        if (!ctx.getSource().permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
            ctx.getSource().sendFailure(Component.literal("Requires gamemaster permissions."));
            return false;
        }
        return true;
    }

    /** Dev mode if {@code -Deh.dev=true} (system property) or {@code EH_DEV=true} (env). */
    private static boolean devMode() {
        return Boolean.getBoolean("eh.dev") || "true".equalsIgnoreCase(System.getenv("EH_DEV"));
    }
}
