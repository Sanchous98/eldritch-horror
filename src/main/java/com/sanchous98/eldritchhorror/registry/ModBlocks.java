package com.sanchous98.eldritchhorror.registry;

import com.sanchous98.eldritchhorror.EldritchHorror;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Block registry. The first blocks are expected to be the ritual altar, the corrupt stone
 * family, and the "rift" the horror bleeds through.
 */
public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(EldritchHorror.MODID);

    // Placeholder: replace with the ritual altar once the block exists.
    public static final DeferredBlock<Block> ELDRITCH_STONE =
            BLOCKS.registerSimpleBlock("eldritch_stone",
                    BlockBehaviour.Properties.of().strength(3.0f, 6.0f));

    private ModBlocks() {
    }
}
