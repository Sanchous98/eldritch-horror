package com.sanchous98.eldritchhorror.registry;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.registry.blocks.CorruptedBlocks;
import com.sanchous98.eldritchhorror.registry.blocks.PrologueBlocks;
import com.sanchous98.eldritchhorror.registry.blocks.RitualBlocks;
import com.sanchous98.eldritchhorror.registry.blocks.RiftBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * Block registry — the single shared surface every block category registers through.
 *
 * <p>Categories live one file per group under {@code registry/blocks/} and each block calls
 * {@link #add(String, UnaryOperator)}. A matching {@link net.minecraft.world.item.BlockItem} is
 * registered automatically (and recorded in {@link ModItems#ALL} so it shows up in the creative
 * tab). Blocks are <b>content stubs</b>: they exist, place and break, but carry no behaviour yet.
 */
public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(EldritchHorror.MODID);

    /** Every registered block, in category-init order. */
    public static final List<DeferredBlock<?>> ALL = new ArrayList<>();

    /**
     * Registers one plain block plus its BlockItem. The canonical helper every block category
     * uses.
     *
     * @param id   the block path (snake_case)
     * @param props tweaks to the default properties (strength, light, …)
     */
    public static DeferredBlock<Block> add(String id, UnaryOperator<BlockBehaviour.Properties> props) {
        DeferredBlock<Block> block = BLOCKS.registerSimpleBlock(id, props);
        ALL.add(block);
        // Record the BlockItem in the shared item list so the creative tab picks it up.
        ModItems.ALL.add((net.neoforged.neoforge.registries.DeferredItem<?>)
                ModItems.ITEMS.registerSimpleBlockItem(id, block));
        return block;
    }

    /** Forces every block category class to initialise and register its blocks. */
    public static void registerCategories() {
        RitualBlocks.init();
        RiftBlocks.init();
        CorruptedBlocks.init();
        PrologueBlocks.init();
    }

    private ModBlocks() {
    }
}
