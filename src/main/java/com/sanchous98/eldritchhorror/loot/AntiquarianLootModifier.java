package com.sanchous98.eldritchhorror.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.investigator.Investigator;
import com.sanchous98.eldritchhorror.investigator.InvestigatorAPI;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

/**
 * Aldous Pemberton's passive: the Antiquarian finds better loot. A global loot modifier adds one
 * bonus item when Aldous is involved — as the killer of a mob (entity drops) or the opener of a
 * site chest ({@code eldritch_horror:chests/*}). Bounded: a single extra stack, a fixed chance, and
 * only for the matching investigator.
 *
 * <p>The context carries the acting player in {@code ATTACKING_ENTITY} (a kill) or
 * {@code THIS_ENTITY} (a container's opener), and NeoForge sets the queried loot-table id, so a
 * site chest can be told apart from an unrelated table.
 */
public final class AntiquarianLootModifier extends LootModifier {

    /** Chance an eligible loot roll yields one extra item. */
    private static final float BONUS_CHANCE = 0.35f;

    /** Equal-weight bonus pool, by registry path (relic_coin weighted 3× as the common result). */
    private static final String[] POOL = {
            "relic_coin", "relic_coin", "relic_coin",
            "silver_reagent", "star_reagent",
            "mark_of_favour", "barter_seal", "order_scrip",
    };

    /** Modifier codec (base condition/priority only). */
    public static final MapCodec<AntiquarianLootModifier> CODEC =
            RecordCodecBuilder.mapCodec(inst -> codecStart(inst)
                    .apply(inst, AntiquarianLootModifier::new));

    public AntiquarianLootModifier(Optional<Holder<LootItemCondition>> condition, int priority) {
        super(condition, priority);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot,
                                                 LootContext context) {
        ServerPlayer player = actingPlayer(context);
        if (player == null || InvestigatorAPI.get(player) != Investigator.ALDOUS_PEMBERTON) {
            return generatedLoot;
        }
        // Applies to a kill and to our site chests; ignore unrelated loot the Antiquarian triggers.
        if (context.getOptional(LootContextParams.ATTACKING_ENTITY) == null && !isSiteChest(context)) {
            return generatedLoot;
        }
        if (player.getRandom().nextFloat() >= BONUS_CHANCE) {
            return generatedLoot;
        }
        Item item = BuiltInRegistries.ITEM.getValue(
                EldritchHorror.id(POOL[player.getRandom().nextInt(POOL.length)]));
        if (item != Items.AIR) {
            generatedLoot.add(new ItemStack(item));
        }
        return generatedLoot;
    }

    /** The player whose action produced this loot: the killer, else the container's opener. */
    private static ServerPlayer actingPlayer(LootContext context) {
        if (context.getOptional(LootContextParams.ATTACKING_ENTITY) instanceof ServerPlayer killer) {
            return killer;
        }
        if (context.getOptional(LootContextParams.THIS_ENTITY) instanceof ServerPlayer opener) {
            return opener;
        }
        return null;
    }

    /** @return whether the queried table is one of this mod's site chest tables. */
    private static boolean isSiteChest(LootContext context) {
        Identifier id = context.getQueriedLootTableId();
        return EldritchHorror.MODID.equals(id.getNamespace()) && id.getPath().startsWith("chests/");
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
