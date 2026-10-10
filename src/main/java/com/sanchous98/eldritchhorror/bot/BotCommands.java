package com.sanchous98.eldritchhorror.bot;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sanchous98.eldritchhorror.corruption.CorruptionSystem;
import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.event.Rifts;
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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
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
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeHolder;
import it.unimi.dsi.fastutil.ints.IntList;
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
                .then(Commands.literal("attacktype")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .then(Commands.argument("type", StringArgumentType.string())
                                        .executes(BotCommands::attacktype))))
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
                                .then(Commands.argument("item", StringArgumentType.string())
                                        .executes(BotCommands::craft))))
                .then(Commands.literal("placeat")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .then(Commands.argument("item", StringArgumentType.string())
                                        .then(Commands.argument("x", DoubleArgumentType.doubleArg())
                                                .then(Commands.argument("y", DoubleArgumentType.doubleArg())
                                                        .then(Commands.argument("z", DoubleArgumentType.doubleArg())
                                                                .executes(BotCommands::placeat)))))))
                .then(Commands.literal("smelt")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .then(Commands.argument("item", StringArgumentType.string())
                                        .executes(BotCommands::smelt))))
                .then(Commands.literal("find")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .then(Commands.argument("block", StringArgumentType.string())
                                        .executes(BotCommands::find))))
                .then(Commands.literal("findentity")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .then(Commands.argument("type", StringArgumentType.string())
                                        .executes(BotCommands::findentity))))
                .then(Commands.literal("equip")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .then(Commands.argument("item", StringArgumentType.string())
                                        .executes(BotCommands::equip))))
                .then(Commands.literal("rift")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(BotCommands::rift)))
                .then(Commands.literal("spawnjoin")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(BotCommands::spawnjoin)))
                .then(Commands.literal("walk")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .then(Commands.argument("x", DoubleArgumentType.doubleArg())
                                        .then(Commands.argument("z", DoubleArgumentType.doubleArg())
                                                .executes(BotCommands::walk)))));
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
        vacuum(bot);
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
        if (ok) {
            // stand on the broken block and collect its dropped items
            bot.teleportTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            bot.resetFallDistance();
            vacuum(bot);
        }
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
        if (ok) {
            // stand on the broken block and collect its dropped items
            bot.teleportTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            bot.resetFallDistance();
            vacuum(bot);
        }
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

    /** {@code /eh bot placeat <name> <item> <x> <y> <z>}: places a block from the inventory. */
    private static int placeat(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if (!gate(ctx)) {
            return 0;
        }
        BotPlayer bot = require(ctx, StringArgumentType.getString(ctx, "name"));
        if (bot == null) {
            return 0;
        }
        String itemId = StringArgumentType.getString(ctx, "item");
        Identifier id = itemId.contains(":") ? Identifier.parse(itemId) : EldritchHorror.id(itemId);
        Item it = BuiltInRegistries.ITEM.getValue(id);
        if (!(it instanceof BlockItem blockItem)) {
            ctx.getSource().sendFailure(Component.literal("Not a placeable block: " + itemId));
            return 0;
        }
        BlockPos pos = BlockPos.containing(DoubleArgumentType.getDouble(ctx, "x"),
                DoubleArgumentType.getDouble(ctx, "y"), DoubleArgumentType.getDouble(ctx, "z"));
        ServerLevel level = bot.level();
        var inv = bot.getInventory();
        boolean consumed = false;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (!s.isEmpty() && s.is(it)) {
                s.shrink(1);
                consumed = true;
                break;
            }
        }
        if (!consumed) {
            ctx.getSource().sendFailure(Component.literal("No " + itemId + " in the inventory"));
            return 0;
        }
        level.setBlockAndUpdate(pos, blockItem.getBlock().defaultBlockState());
        ctx.getSource().sendSuccess(() -> Component.literal("bot " + bot.getGameProfile().name()
                + " placed " + itemId + " at " + pos.toShortString()), true);
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
            target = BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(itemId));
        }
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
                    + result.getHoverName().getString()), true);
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
            target = BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(itemId));
        }
        if (target == null || target == Items.AIR) {
            ctx.getSource().sendFailure(Component.literal("Unknown item: " + itemId));
            return 0;
        }
        ServerLevel level = bot.level();
        var inv = bot.getInventory();
        // Recipe-driven: reconstruct each crafting recipe's pattern from PlacementInfo, satisfy it
        // from the bot's inventory, then confirm by matching + assembling. This handles shaped
        // recipes with empty cells and more than four ingredients (e.g. altar_core "RRR"/"SOS").
        for (RecipeHolder<?> holder : level.getServer().getRecipeManager().getRecipes()) {
            if (!(holder.value() instanceof CraftingRecipe recipe)) {
                continue;
            }
            PlacementInfo info = recipe.placementInfo();
            if (info.isImpossibleToPlace()) {
                continue;
            }
            List<Ingredient> ingredients = info.ingredients();
            IntList slots = info.slotsToIngredientIndex();
            List<ItemStack> items = new ArrayList<>(java.util.Collections.nCopies(slots.size(), ItemStack.EMPTY));
            List<Integer> chosen = new ArrayList<>();
            boolean ok = true;
            for (int s = 0; s < slots.size(); s++) {
                int idx = slots.getInt(s);
                if (idx < 0) {
                    continue;
                }
                int slot = findMatch(inv, ingredients.get(idx));
                if (slot < 0) {
                    ok = false;
                    break;
                }
                chosen.add(slot);
                items.set(s, inv.getItem(slot).copyWithCount(1));
            }
            if (!ok) {
                continue;
            }
            java.util.Map<Integer, Integer> need = new java.util.HashMap<>();
            for (int slot : chosen) {
                need.merge(slot, 1, Integer::sum);
            }
            boolean enough = true;
            for (var e : need.entrySet()) {
                if (inv.getItem(e.getKey()).getCount() < e.getValue()) {
                    enough = false;
                }
            }
            if (!enough) {
                continue;
            }
            // The pattern's own width is unknown, so try 1..3 wide placements until one matches.
            ItemStack result = null;
            for (int w = 1; w <= 3 && result == null; w++) {
                List<ItemStack> grid = new ArrayList<>(java.util.Collections.nCopies(9, ItemStack.EMPTY));
                boolean fits = true;
                for (int s = 0; s < slots.size(); s++) {
                    ItemStack it = items.get(s);
                    if (it.isEmpty()) {
                        continue;
                    }
                    int row = s / w;
                    int col = s % w;
                    if (row > 2 || col > 2) {
                        fits = false;
                        break;
                    }
                    grid.set(row * 3 + col, it);
                }
                if (!fits) {
                    continue;
                }
                CraftingInput input = CraftingInput.of(3, 3, grid);
                if (recipe.matches(input, level)) {
                    ItemStack made = recipe.assemble(input);
                    if (!made.isEmpty() && made.is(target)) {
                        result = made;
                    }
                }
            }
            if (result == null) {
                continue;
            }
            for (int slot : chosen) {
                inv.getItem(slot).shrink(1);
            }
            inv.add(result.copy());
            ItemStack made = result;
            ctx.getSource().sendSuccess(() -> Component.literal("bot " + bot.getGameProfile().name()
                    + " crafted " + made.getCount() + "x "
                    + made.getHoverName().getString()), true);
            return 1;
        }
        ctx.getSource().sendFailure(Component.literal("No craftable recipe for " + itemId
                + " in the bot's inventory"));
        return 0;
    }

    /** Moves dropped item entities near the bot into its inventory (a bot cannot "walk over" loot). */
    private static void vacuum(BotPlayer bot) {
        for (ItemEntity ie : bot.level().getEntitiesOfClass(ItemEntity.class,
                bot.getBoundingBox().inflate(8.0))) {
            ItemStack st = ie.getItem();
            if (!st.isEmpty()) {
                bot.getInventory().add(st.copy());
                ie.discard();
            }
        }
    }

    /** First inventory slot whose stack matches the ingredient, or -1. */
    private static int findMatch(net.minecraft.world.entity.player.Inventory inv, Ingredient ingredient) {
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack st = inv.getItem(i);
            if (!st.isEmpty() && ingredient.test(st)) {
                return i;
            }
        }
        return -1;
    }

    /** {@code /eh bot find <name> <block>}: nearest block of that id within ~24 blocks. */
    private static int find(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if (!gate(ctx)) {
            return 0;
        }
        BotPlayer bot = require(ctx, StringArgumentType.getString(ctx, "name"));
        if (bot == null) {
            return 0;
        }
        String blockId = StringArgumentType.getString(ctx, "block");
        Identifier id = blockId.contains(":") ? Identifier.parse(blockId)
                : Identifier.fromNamespaceAndPath("minecraft", blockId);
        Block target = BuiltInRegistries.BLOCK.getValue(id);
        if (target == net.minecraft.world.level.block.Blocks.AIR) {
            ctx.getSource().sendFailure(Component.literal("Unknown block: " + blockId));
            return 0;
        }
        ServerLevel level = bot.level();
        BlockPos center = bot.blockPosition();
        BlockPos best = null;
        long bd = Long.MAX_VALUE;
        int r = 40;
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                for (int dy = -16; dy <= 16; dy++) {
                    BlockPos p = center.offset(dx, dy, dz);
                    if (!level.isLoaded(p) || !level.getBlockState(p).is(target)) {
                        continue;
                    }
                    long d = (long) dx * dx + (long) dy * dy + (long) dz * dz;
                    if (d < bd) {
                        bd = d;
                        best = p;
                    }
                }
            }
        }
        if (best == null) {
            ctx.getSource().sendFailure(Component.literal("No " + blockId + " within " + r + " blocks"));
            return 0;
        }
        BlockPos found = best;
        final long fd = bd;
        ctx.getSource().sendSuccess(() -> Component.literal("found " + blockId + " at "
                + found.getX() + " " + found.getY() + " " + found.getZ()
                + " (d=" + String.format(Locale.ROOT, "%.1f", Math.sqrt(fd)) + ")"), false);
        return 1;
    }

    /** {@code /eh bot findentity <name> <type>}: nearest living entity of that type within 64. */
    private static int findentity(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if (!gate(ctx)) {
            return 0;
        }
        BotPlayer bot = require(ctx, StringArgumentType.getString(ctx, "name"));
        if (bot == null) {
            return 0;
        }
        String typeId = StringArgumentType.getString(ctx, "type");
        Identifier id = typeId.contains(":") ? Identifier.parse(typeId)
                : Identifier.withDefaultNamespace(typeId);
        LivingEntity best = null;
        double bd = Double.MAX_VALUE;
        for (LivingEntity e : bot.level().getEntitiesOfClass(LivingEntity.class,
                bot.getBoundingBox().inflate(64.0), e -> e != bot && e.isAlive())) {
            if (!BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).toString().equals(id.toString())) {
                continue;
            }
            double d = bot.distanceToSqr(e);
            if (d < bd) {
                bd = d;
                best = e;
            }
        }
        if (best == null) {
            ctx.getSource().sendFailure(Component.literal("No " + typeId + " within 64 blocks"));
            return 0;
        }
        LivingEntity found = best;
        final double fd = bd;
        ctx.getSource().sendSuccess(() -> Component.literal("found " + typeId + " at "
                + found.blockPosition().getX() + " " + found.blockPosition().getY() + " "
                + found.blockPosition().getZ()
                + " (d=" + String.format(Locale.ROOT, "%.1f", Math.sqrt(fd)) + ")"), false);
        return 1;
    }

    /** {@code /eh bot equip <name> <item>}: move one matching item from the inventory to the main hand. */
    private static int equip(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
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
            target = BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(itemId));
        }
        if (target == null || target == Items.AIR) {
            ctx.getSource().sendFailure(Component.literal("Unknown item: " + itemId));
            return 0;
        }
        var inv = bot.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack st = inv.getItem(i);
            if (st.isEmpty() || !st.is(target)) {
                continue;
            }
            ItemStack hand = st.copy();
            hand.setCount(1);
            bot.setItemInHand(InteractionHand.MAIN_HAND, hand);
            st.shrink(1);
            inv.setChanged();
            ctx.getSource().sendSuccess(() -> Component.literal("bot " + bot.getGameProfile().name()
                    + " equips " + hand.getHoverName().getString()), true);
            return 1;
        }
        ctx.getSource().sendFailure(Component.literal("No " + itemId + " in the bot's inventory"));
        return 0;
    }

    /** {@code /eh bot rift <name>}: nearest rift anchor within ~64 chunks. */
    private static int rift(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if (!gate(ctx)) {
            return 0;
        }
        BotPlayer bot = require(ctx, StringArgumentType.getString(ctx, "name"));
        if (bot == null) {
            return 0;
        }
        List<BlockPos> rifts = Rifts.near(bot.level(), bot.blockPosition(), 64, 1);
        if (rifts.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("No rift within 64 chunks"));
            return 0;
        }
        BlockPos p = rifts.getFirst();
        ctx.getSource().sendSuccess(() -> Component.literal("rift at " + p.getX() + " " + p.getY()
                + " " + p.getZ() + " (d=" + String.format(Locale.ROOT, "%.1f", Math.sqrt(
                        bot.distanceToSqr(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5))) + ")"), false);
        return 1;
    }

    /** {@code /eh bot spawnjoin <name>}: spawn a bot and fire login so the prologue routes it. */
    private static int spawnjoin(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if (!gate(ctx)) {
            return 0;
        }
        String name = StringArgumentType.getString(ctx, "name");
        if (Bots.get(name) != null) {
            ctx.getSource().sendFailure(Component.literal("Bot already exists: " + name));
            return 0;
        }
        ServerLevel level = ctx.getSource().getLevel();
        BotPlayer bot = Bots.spawn(level, name);
        NeoForge.EVENT_BUS.post(new PlayerEvent.PlayerLoggedInEvent(bot));
        ctx.getSource().sendSuccess(() -> Component.literal(
                "bot " + name + " joined (prologue applies if enabled)"), true);
        return 1;
    }

    /** {@code /eh bot walk <name> <x> <z>}: surface-snapping travel toward a column.
     *  Each step moves up to 8 blocks toward (x,z) and snaps Y to the terrain surface, so a
     *  headless bot neither gets stuck inside stone nor falls into a void while travelling. */
    private static int walk(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if (!gate(ctx)) {
            return 0;
        }
        BotPlayer bot = require(ctx, StringArgumentType.getString(ctx, "name"));
        if (bot == null) {
            return 0;
        }
        double tx = DoubleArgumentType.getDouble(ctx, "x");
        double tz = DoubleArgumentType.getDouble(ctx, "z");
        ServerLevel level = bot.level();
        for (int i = 0; i < 96; i++) {
            BlockPos cur = bot.blockPosition();
            double dx = tx - cur.getX();
            double dz = tz - cur.getZ();
            if (Math.abs(dx) <= 4 && Math.abs(dz) <= 4) {
                break;
            }
            int sx = (int) Math.max(-8, Math.min(8, Math.round(dx)));
            int sz = (int) Math.max(-8, Math.min(8, Math.round(dz)));
            int nx = cur.getX() + sx;
            int nz = cur.getZ() + sz;
            level.getChunk(nx >> 4, nz >> 4);
            int ny = level.getHeight(Heightmap.Types.WORLD_SURFACE, nx, nz);
            bot.teleportTo(nx + 0.5, ny + 1.0, nz + 0.5);
            bot.resetFallDistance();
        }
        BlockPos end = bot.blockPosition();
        ctx.getSource().sendSuccess(() -> Component.literal("walked to "
                + end.getX() + " " + end.getY() + " " + end.getZ()), false);
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

    /** {@code /eh bot attacktype <name> <type>}: attacks the nearest entity of a given type. */
    private static int attacktype(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if (!gate(ctx)) {
            return 0;
        }
        BotPlayer bot = require(ctx, StringArgumentType.getString(ctx, "name"));
        if (bot == null) {
            return 0;
        }
        String typeId = StringArgumentType.getString(ctx, "type");
        Identifier id = typeId.contains(":") ? Identifier.parse(typeId) : EldritchHorror.id(typeId);
        var type = BuiltInRegistries.ENTITY_TYPE.getValue(id);
        if (type == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown entity type: " + typeId));
            return 0;
        }
        List<LivingEntity> targets = bot.level().getEntitiesOfClass(LivingEntity.class,
                bot.getBoundingBox().inflate(32.0), e -> e != bot && e.isAlive() && e.getType() == type);
        if (targets.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("No " + typeId + " within 32 blocks"));
            return 0;
        }
        LivingEntity target = targets.stream()
                .min(Comparator.comparingDouble(bot::distanceToSqr)).orElse(targets.get(0));
        bot.attack(target);
        vacuum(bot);
        // Teleport the bot onto the target's face so reach is unambiguous for the next hit.
        ctx.getSource().sendSuccess(() -> Component.literal("bot " + bot.getGameProfile().name()
                + " attacked " + target.getName().getString() + " hp=" + target.getHealth()
                + " dist=" + String.format(Locale.ROOT, "%.1f", Math.sqrt(bot.distanceToSqr(target)))), true);
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
