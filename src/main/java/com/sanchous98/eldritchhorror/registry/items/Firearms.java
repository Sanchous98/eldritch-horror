package com.sanchous98.eldritchhorror.registry.items;

import com.sanchous98.eldritchhorror.entity.TearGasFlask;
import com.sanchous98.eldritchhorror.entity.ThrownDynamite;
import com.sanchous98.eldritchhorror.registry.ModItems;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * The board game's mundane tier ({@code design/16-items.md}, FFG {@code Item — Weapon} assets):
 * ammunition, propellant, the two one-shot throwables and the four firearms.
 *
 * <p>Kept out of {@link Weapons} on purpose: those are the occult/relic blades with on-hit magic,
 * while this file is the 1920s field kit, and the asset generator treats each category file as one
 * icon family. The static fields register through {@link ModItems#add} / {@link ModItems#addItem}
 * when the class initialises; {@link #init()} only exists to force that initialisation from
 * {@code ModItems.registerCategories()}.
 *
 * <p>Grades are acquisition tiers, not a second stat system: the revolver and the axe are common
 * (craftable early), the automatic and the shotgun are quality (iron/copper and city/Order loot),
 * the carbine is fine (loot-only). The board game's "+N during Combat" numbers become damage here.
 */
public final class Firearms {

    /** Brass Cartridge — ammunition; one is spent per shot (two for the shotgun). */
    public static final DeferredItem<Item> CARTRIDGE = ModItems.add("cartridge", 64);

    /** Blasting Powder — coal + consecrated salt; the propellant behind Dynamite. */
    public static final DeferredItem<Item> BLASTING_POWDER = ModItems.add("blasting_powder", 16);

    /** Dynamite — thrown; a short entity-only blast (no block damage). */
    public static final DeferredItem<ThrowableGearItem> DYNAMITE = ModItems.addItem("dynamite",
            p -> new ThrowableGearItem(p, (level, owner, stack) -> new ThrownDynamite(level, owner, stack),
                    0.9F, 1.0F, SoundEvents.TNT_PRIMED), 16);

    /** Tear Gas — thrown; Weakness + Blindness cloud, no damage. */
    public static final DeferredItem<ThrowableGearItem> TEAR_GAS = ModItems.addItem("tear_gas",
            p -> new ThrowableGearItem(p, (level, owner, stack) -> new TearGasFlask(level, owner, stack),
                    1.1F, 1.0F, SoundEvents.SPLASH_POTION_THROW), 16);

    public static final DeferredItem<FirearmItem> REVOLVER_38 = ModItems.addItem("revolver_38",
            p -> gunItem(p, "revolver_38", 250, FirearmLadder.REVOLVER), 1);

    public static final DeferredItem<FirearmItem> AUTOMATIC_45 = ModItems.addItem("automatic_45",
            p -> gunItem(p, "automatic_45", 300, FirearmLadder.AUTOMATIC), 1);

    public static final DeferredItem<FirearmItem> SHOTGUN_DOUBLE = ModItems.addItem("shotgun_double",
            p -> gunItem(p, "shotgun_double", 220, FirearmLadder.SHOTGUN), 1);

    public static final DeferredItem<FirearmItem> CARBINE_RIFLE = ModItems.addItem("carbine_rifle",
            p -> gunItem(p, "carbine_rifle", 350, FirearmLadder.CARBINE), 1);

    private Firearms() {
    }

    /** Forces this class to initialise (and therefore register its items). */
    public static void init() {
    }

    /** One firearm: durability, spec, the shared shot sound and the lazily resolved cartridge. */
    private static FirearmItem gunItem(Item.Properties properties, String id, int durability,
                                       FirearmSpec spec) {
        return new FirearmItem(Weapons.lore(properties.durability(durability).enchantable(10), id),
                spec, SoundEvents.CROSSBOW_SHOOT, () -> CARTRIDGE.get());
    }
}
