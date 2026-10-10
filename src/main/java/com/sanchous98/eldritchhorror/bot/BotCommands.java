package com.sanchous98.eldritchhorror.bot;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sanchous98.eldritchhorror.corruption.CorruptionSystem;
import com.sanchous98.eldritchhorror.EldritchHorror;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.jspecify.annotations.Nullable;
import java.util.ArrayList;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;

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
                                        .executes(BotCommands::god))))
                .then(Commands.literal("mine")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(BotCommands::mine)))
                .then(Commands.literal("mineat")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .then(Commands.argument("x", DoubleArgumentType.doubleArg())
                                        .then(Commands.argument("y", DoubleArgumentType.doubleArg())
                                                .then(Commands.argument("z", DoubleArgumentType.doubleArg())
                                                        .executes(BotCommands::mineat))))))
                .then(Commands.literal("inventory")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(BotCommands::inventory)))
                .then(Commands.literal("craft")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .then(Commands.argument("item", StringArgumentType.word())
                                        .executes(BotCommands::craft))))
                .then(Commands.literal("placeat")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .then(Commands.argument("x", DoubleArgumentType.doubleArg())
                                        .then(Commands.argument("y", DoubleArgumentType.doubleArg())
                                                .then(Commands.argument("z", DoubleArgumentType.doubleArg())
                                                        .executes(BotCommands::placeat))))))
                .then(Commands.literal("smelt")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .then(Commands.argument("item", StringArgumentType.word())
                                        .executes(BotCommands::smelt))));
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

    /** {@code /eh bot mine <name>}: breaks the block the bot faces (survival drops are auto-picked). */
    private static int mine(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if (!gate(ctx)) {
            return 0;
        }
        BotPlayer bot = require(ctx, StringArgumentType.getString(ctx, "name"));
        if (bot == null) {
            return 0;
        }
        BlockPos pos = blockTarget(bot).getBlockPos();
        BlockState state = bot.level().getBlockState(pos);
        boolean ok = bot.gameMode.destroyBlock(pos);
        ctx.getSource().sendSuccess(() -> Component.literal("bot " + bot.getGameProfile().name()
                + " mined " + state.getBlock() + " at " + pos.toShortString() + " -> " + ok), true);
        return 1;
    }

    /** {@code /eh bot mineat <name> <x> <y> <z>}: breaks the block at a position. */
    private static int mineat(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
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
        boolean ok = bot.gameMode.destroyBlock(pos);
        ctx.getSource().sendSuccess(() -> Component.literal("bot " + bot.getGameProfile().name()
                + " mined " + state.getBlock() + " at " + pos.toShortString() + " -> " + ok), true);
        return 1;
    }

    /** {@code /eh bot inventory <name>}: lists the bot's inventory. */
    private static int inventory(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if (!gate(ctx)) {
            return 0;
        }
        BotPlayer bot = require(ctx, StringArgumentType.getString(ctx, "name"));
        if (bot == null) {
            return 0;
        }
        StringBuilder sb = new StringBuilder();
        var inv = bot.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (!s.isEmpty()) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(s.getCount()).append("x ").append(s.getHoverName().getString());
            }
        }
        String line = sb.length() == 0 ? "(empty)" : sb.toString();
        ctx.getSource().sendSuccess(() -> Component.literal("bot " + bot.getGameProfile().name()
                + " inventory: " + line), false);
        return 1;
    }

    /** {@code /eh bot placeat <name> <x> <y> <z>}: places the main-hand block onto the support below. */
    private static int placeat(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if (!gate(ctx)) {
            return 0;
        }
        BotPlayer bot = require(ctx, StringArgumentType.getString(ctx, "name"));
        if (bot == null) {
            return 0;
        }
        BlockPos pos = BlockPos.containing(DoubleArgumentType.getDouble(ctx, "x"),
                DoubleArgumentType.getDouble(ctx, "y"), DoubleArgumentType.getDouble(ctx, "z"));
        BlockPos below = pos.below();
        if (bot.level().getBlockState(below).isAir()) {
            ctx.getSource().sendFailure(Component.literal("No support below " + pos.toShortString()));
            return 0;
        }
        ItemStack stack = bot.getItemInHand(InteractionHand.MAIN_HAND);
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            ctx.getSource().sendFailure(Component.literal("Main hand is not a block: "
                    + stack.getHoverName().getString()));
            return 0;
        }
        BlockPlaceContext place = new BlockPlaceContext(bot, InteractionHand.MAIN_HAND, stack,
                new BlockHitResult(Vec3.atCenterOf(below), Direction.UP, below, false));
        InteractionResult result = blockItem.place(place);
        if (!result.consumesAction()) {
            // The headless bot's placement context is often rejected; fall back to a direct set
            // so a scripted playtest can still build altars etc.
            bot.level().setBlock(pos, blockItem.getBlock().defaultBlockState(), 3);
            stack.shrink(1);
            result = InteractionResult.SUCCESS;
        }
        final InteractionResult shown = result;
        final String itemName = stack.getHoverName().getString();
        ctx.getSource().sendSuccess(() -> Component.literal("bot " + bot.getGameProfile().name()
                + " placed " + itemName + " at " + pos.toShortString() + " -> " + shown), true);
        return 1;
    }

    /** {@code /eh bot smelt <name> <item>}: smelts one inventory item into {@code item} (no furnace). */
    private static int smelt(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if (!gate(ctx)) {
            return 0;
        }
        BotPlayer bot = require(ctx, StringArgumentType.getString(ctx, "name"));
        if (bot == null) {
            return 0;
        }
        String itemId = StringArgumentType.getString(ctx, "item");
        Identifier id = itemId.contains(":") ? Identifier.parse(itemId) : EldritchHorror.id(itemId);
        Item target = BuiltInRegistries.ITEM.getValue(id);
        if (target == null || target == Items.AIR) {
            ctx.getSource().sendFailure(Component.literal("Unknown item: " + itemId));
            return 0;
        }
        ServerLevel level = bot.level();
        var inv = bot.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.isEmpty()) {
                continue;
            }
            SingleRecipeInput input = new SingleRecipeInput(s.copyWithCount(1));
            var found = level.getServer().getRecipeManager()
                    .getRecipeFor(RecipeType.SMELTING, input, level);
            if (found.isEmpty()) {
                continue;
            }
            ItemStack result = found.get().value().assemble(input);
            if (result.isEmpty() || !result.is(target)) {
                continue;
            }
            s.shrink(1);
            inv.add(result.copy());
            ctx.getSource().sendSuccess(() -> Component.literal("bot " + bot.getGameProfile().name()
                    + " smelted " + result.getCount() + "x "
                    + new ItemStack(target).getHoverName().getString()), true);
            return 1;
        }
        ctx.getSource().sendFailure(Component.literal("Nothing in the inventory smelts into " + itemId));
        return 0;
    }

    /** {@code /eh bot craft <name> <item>}: crafts one of {@code item} from the bot's inventory. */
    private static int craft(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if (!gate(ctx)) {
            return 0;
        }
        BotPlayer bot = require(ctx, StringArgumentType.getString(ctx, "name"));
        if (bot == null) {
            return 0;
        }
        String itemId = StringArgumentType.getString(ctx, "item");
        Identifier id = itemId.contains(":") ? Identifier.parse(itemId) : EldritchHorror.id(itemId);
        Item target = BuiltInRegistries.ITEM.getValue(id);
        if (target == null || target == Items.AIR) {
            ctx.getSource().sendFailure(Component.literal("Unknown item: " + itemId));
            return 0;
        }
        ServerLevel level = bot.level();
        var inv = bot.getInventory();
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (!inv.getItem(i).isEmpty()) {
                slots.add(i);
            }
        }
        // Brute force subsets of the inventory (up to 4 distinct slots) against the crafting table.
        for (int[] picks : combinations(slots.size(), 4)) {
            List<ItemStack> grid = new ArrayList<>(java.util.Collections.nCopies(9, ItemStack.EMPTY));
            for (int i = 0; i < picks.length; i++) {
                grid.set(i, inv.getItem(slots.get(picks[i])).copyWithCount(1));
            }
            CraftingInput input = CraftingInput.of(3, 3, grid);
            var found = level.getServer().getRecipeManager()
                    .getRecipeFor(RecipeType.CRAFTING, input, level);
            if (found.isEmpty()) {
                continue;
            }
            ItemStack result = found.get().value().assemble(input);
            if (result.isEmpty() || !result.is(target)) {
                continue;
            }
            for (int p : picks) {
                inv.getItem(slots.get(p)).shrink(1);
            }
            inv.add(result.copy());
            ctx.getSource().sendSuccess(() -> Component.literal("bot " + bot.getGameProfile().name()
                    + " crafted " + result.getCount() + "x "
                    + new ItemStack(target).getHoverName().getString()), true);
            return 1;
        }
        ctx.getSource().sendFailure(Component.literal("No craftable recipe for " + itemId
                + " in the bot's inventory"));
        return 0;
    }

    /** Every subset of {@code 0..n-1} of size {@code 1..max}. */
    private static List<int[]> combinations(int n, int max) {
        List<int[]> out = new ArrayList<>();
        for (int k = 1; k <= max && k <= n; k++) {
            int[] idx = new int[k];
            for (int i = 0; i < k; i++) {
                idx[i] = i;
            }
            while (true) {
                out.add(idx.clone());
                int p = k - 1;
                while (p >= 0 && idx[p] == n - k + p) {
                    p--;
                }
                if (p < 0) {
                    break;
                }
                idx[p]++;
                for (int i = p + 1; i < k; i++) {
                    idx[i] = idx[i - 1] + 1;
                }
            }
        }
        return out;
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
