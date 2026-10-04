package com.sanchous98.eldritchhorror.registry.blocks;

import com.sanchous98.eldritchhorror.classes.ClassId;
import com.sanchous98.eldritchhorror.registry.ModBlocks;
import com.sanchous98.eldritchhorror.registry.ModItems;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;

/**
 * The Threshold prologue's blocks: three class pedestals, the 24-variant city gate portal and the
 * solid frame the gate arches are built from.
 *
 * <p>Registered through the shared {@link ModBlocks#BLOCKS} register so they land in
 * {@link ModBlocks#ALL} (creative tab) and get a {@code BlockItem}, exactly like the other block
 * categories. The five static fields register themselves at class-initialisation time;
 * {@link #init()} exists only as the explicit entry point {@link ModBlocks#registerCategories()}
 * calls, which forces that initialisation. Placeholder textures reference vanilla blocks until the
 * final art lands (see the asset JSONs).
 */
public final class PrologueBlocks {

    private PrologueBlocks() {
    }

    /** Brass-and-paper investigator pedestal; softly lit. */
    public static final DeferredBlock<Block> INVESTIGATOR_PEDESTAL = register(
            "investigator_pedestal",
            p -> new ClassPedestalBlock(ClassId.INVESTIGATOR, p),
            p -> p.strength(3.5f, 6.0f).lightLevel(s -> 7));

    /** Tome-lectern occultist pedestal; softly lit. */
    public static final DeferredBlock<Block> OCCULTIST_PEDESTAL = register(
            "occultist_pedestal",
            p -> new ClassPedestalBlock(ClassId.OCCULTIST, p),
            p -> p.strength(3.5f, 6.0f).lightLevel(s -> 7));

    /** Black-altar cultist pedestal; a dim red ember. */
    public static final DeferredBlock<Block> CULTIST_PEDESTAL = register(
            "cultist_pedestal",
            p -> new ClassPedestalBlock(ClassId.CULTIST, p),
            p -> p.strength(4.0f, 8.0f).lightLevel(s -> 4));

    /**
     * A destination gate portal; its {@code city} property (0–23) selects the curated city. No
     * collision so a player can walk into the portal column.
     */
    public static final DeferredBlock<Block> CITY_GATE = register(
            "city_gate",
            CityGateBlock::new,
            p -> p.strength(6.0f, 12.0f).noCollision().lightLevel(s -> 11));

    /** Solid deepslate frame (two posts + lintel) carrying each city's gate arch. */
    public static final DeferredBlock<Block> CITY_GATE_FRAME = register(
            "city_gate_frame",
            Block::new,
            p -> p.strength(5.0f, 6.0f));

    /**
     * Forces this class to initialise, registering the five blocks. Called once from
     * {@link ModBlocks#registerCategories()}; the actual registration lives in the field
     * initialisers above so the {@link DeferredBlock} constants stay {@code final}.
     */
    public static void init() {
        // Intentionally empty: touching this method initialises the class and runs the field
        // initialisers that register the blocks.
    }

    /** Registers one block plus its {@code BlockItem}, recording both in the shared lists. */
    private static DeferredBlock<Block> register(
            String id,
            Function<BlockBehaviour.Properties, ? extends Block> factory,
            UnaryOperator<BlockBehaviour.Properties> properties) {
        DeferredBlock<Block> block = ModBlocks.BLOCKS.registerBlock(id, factory, properties);
        ModBlocks.ALL.add(block);
        ModItems.ALL.add(ModItems.ITEMS.registerSimpleBlockItem(id, block));
        return block;
    }
}
