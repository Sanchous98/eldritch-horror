package com.sanchous98.eldritchhorror.registry.blocks;

import com.sanchous98.eldritchhorror.registry.ModBlocks;
import com.sanchous98.eldritchhorror.registry.ModItems;
import com.sanchous98.eldritchhorror.registry.items.SignatureItem;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * The Threshold prologue's blocks: the 12-variant investigator pedestal, the 24-variant city gate
 * portal and the solid frame the gate arches are built from. Also registers the shared
 * {@code signature_charm} item every investigator's active is fired through.
 *
 * <p>Registered through the shared {@link ModBlocks#BLOCKS} register so they land in
 * {@link ModBlocks#ALL} (creative tab) and get a {@code BlockItem}, exactly like the other block
 * categories. The static fields register themselves at class-initialisation time; {@link #init()}
 * exists only as the explicit entry point {@link ModBlocks#registerCategories()} calls, which forces
 * that initialisation (and the item registration below it). Placeholder textures reference vanilla
 * blocks until the final art lands (see the asset JSONs).
 */
public final class PrologueBlocks {

    private PrologueBlocks() {
    }

    /**
     * The single investigator pedestal, property-carrying ({@code investigator=0..11}); each of the
     * 12 Threshold pedestals is one variant. Brass-and-paper; softly lit.
     */
    public static final DeferredBlock<Block> INVESTIGATOR_PEDESTAL = register(
            "investigator_pedestal",
            InvestigatorPedestalBlock::new,
            p -> p.strength(3.5f, 6.0f).lightLevel(s -> 7));

    /**
     * The shared signature item. Stack size 1; right-click fires the owner's
     * {@link com.sanchous98.eldritchhorror.investigator.SignatureAbilities active}. Registered here
     * because this is the prologue's content entry point; recorded in {@link ModItems#ALL} so it is
     * discoverable in the creative tab.
     */
    public static final DeferredItem<SignatureItem> SIGNATURE_CHARM = ModItems.ITEMS.registerItem(
            "signature_charm", p -> new SignatureItem(p.stacksTo(1)));

    static {
        ModItems.ALL.add(SIGNATURE_CHARM);
    }

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
     * Forces this class to initialise, registering the blocks and the signature item. Called once
     * from {@link ModBlocks#registerCategories()}; the actual registration lives in the field
     * initialisers above so the {@link DeferredBlock}/{@link DeferredItem} constants stay
     * {@code final}.
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
