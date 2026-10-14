package com.sanchous98.eldritchhorror.registry.items;

import com.sanchous98.eldritchhorror.registry.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.component.Weapon;

/**
 * Weapons and worn gear (FFG traits: <em>Weapon</em>, <em>Magical Weapon</em>,
 * <em>Relic Weapon</em>).
 *
 * <p>The weapons now carry real 26.3 stats through the same shapes vanilla registers: a sword is a
 * plain {@link Item} whose {@code Properties.sword} supplies its attribute component and durability,
 * a mace uses {@link MaceItem}, a trident {@link TridentItem}, a bow {@link BowItem} and a tool the
 * generic {@code Properties.tool}. Per-hit/while-held effects recorded per item (on-kill sanity and
 * corruption, underwater damage, ward auras, corrupted scaling, sanity costs) stay flavour: there is
 * no damage hook for them in this pass, and only the four named artefact passives are wired.
 *
 * <p>Worn gear stays plain, stat-less items: real armour in 26.3 needs
 * {@code ArmorMaterial} + {@code humanoidArmor}, whose {@code assetId} points at a client
 * equipment definition ({@code assets/<ns>/equipment/<name>.json} plus worn-layer textures) — none
 * exist for this mod yet, so the gear is flavour until those assets ship.
 */
public final class Weapons {
    private Weapons() {
    }

    public static void init() {
        // Occultist's Implement — mainhand ritual focus. Iron tool (250 durability, weak melee).
        // flavour (design/16): "+ritual speed" is not wired here — the one ritual-speed hook belongs
        // to ring_of_focus (rite/RiteEngine).
        ModItems.addItem("occult_tool", p -> new Item(p.tool(ToolMaterial.IRON,
                BlockTags.MINEABLE_WITH_HOE, 0.0F, -2.0F, 0.0F)), 1);
        // Silver Blade — consecrated silver. Iron sword: 6 attack damage, 250 durability.
        // flavour (design/16): "+damage vs. eldritch entities" needs a per-victim damage hook.
        ModItems.addItem("silver_blade", p -> new Item(p.sword(ToolMaterial.IRON, 3.0F, -2.4F)), 1);
        // Hollow Pact Blade — relic weapon. Netherite sword: 8 attack damage, 2031 durability,
        // fire-resistant. flavour (design/16): sanity drain on kill and +1 corruption/kill are not wired.
        ModItems.addItem("hollow_pact_blade",
                p -> new Item(p.sword(ToolMaterial.NETHERITE, 3.0F, -2.4F).fireResistant()), 1);
        // Ritual Knife — cheap magical weapon. Wooden sword: 4 attack damage, 59 durability.
        // flavour (design/16): "enables blood rites when held" is not wired.
        ModItems.addItem("ritual_knife", p -> new Item(p.sword(ToolMaterial.WOOD, 3.0F, -2.4F)), 1);
        // Warden's Maul — heavy Order weapon. Mace: 5 attack damage + smash, 500 durability,
        // slow swing (-3.4). flavour (design/16): the while-equipped movement slow is not wired.
        ModItems.addItem("wardens_maul", p -> new MaceItem(mace(p, 500)), 1);
        // Bone Club — crude grave-bone weapon. Stone sword: 5 attack damage, 131 durability.
        ModItems.addItem("bone_club", p -> new Item(p.sword(ToolMaterial.STONE, 3.0F, -2.4F)), 1);
        // Star-Iron Sword — meteoric weapon. Already wounds star-spawn through the ward_breakers
        // item tag; stats were left out of this pass (the brief listed no melee numbers for it).
        ModItems.add("star_iron_sword", 1);
        // Tidal Trident — Choir weapon. Trident: 8 attack damage, 250 durability.
        // flavour (design/16): underwater bonus damage and corrosion on hit are not wired.
        ModItems.addItem("tidal_trident", p -> new TridentItem(p.durability(250)
                .attributes(TridentItem.createAttributes())
                .component(DataComponents.TOOL, TridentItem.createToolProperties())
                .enchantable(1)
                .component(DataComponents.WEAPON, new Weapon(1))), 1);
        // Sacrificial Dagger — magical weapon. Iron dagger: 5 attack damage at 2.0 swings/s,
        // 250 durability. flavour (design/16): "damage scales with corruption" is not wired.
        ModItems.addItem("sacrificial_dagger", p -> new Item(p.sword(ToolMaterial.IRON, 2.0F, -2.0F)), 1);
        // Censer Mace — burning incense head. Mace: 5 attack damage + smash, 300 durability.
        // flavour (design/16): the ward aura and sanity cost per swing are not wired.
        ModItems.addItem("censer_mace", p -> new MaceItem(mace(p, 300)), 1);
        // Eye Scepter — relic weapon. Gold sword: 3 attack damage (weak melee), 32 durability.
        // flavour (design/16): rift reveal and while-held sanity drain are not wired (that passive
        // belongs to eye_of_the_watcher).
        ModItems.addItem("eye_scepter", p -> new Item(p.sword(ToolMaterial.GOLD, 2.0F, -2.4F)), 1);
        // Veil Bow — relic weapon. Bow: 500 durability. flavour (design/16): piercing arrows,
        // +damage vs. eldritch and the per-shot sanity cost are not wired.
        ModItems.addItem("veil_bow", p -> new BowItem(p.durability(500).enchantable(1)), 1);

        // Investigator's Coat — chest gear; −sanity drain in darkness.
        // flavour (design/16): real armour needs an equipment asset + worn textures (see class javadoc).
        ModItems.add("investigator_coat", 1);
        // Choir Robe — chest gear; +reputation gain, +corruption gain. flavour (design/16): no equipment asset.
        ModItems.add("choir_robe", 1);
        // Warden's Plate — chest gear; heavy armour, resists corruption spread, slows movement. flavour (design/16): no equipment asset.
        ModItems.add("wardens_plate", 1);
        // Drowned Wetsuit — chest gear; water breathing and swim speed, +corruption over time. flavour (design/16): no equipment asset.
        ModItems.add("drowned_wetsuit", 1);
        // Cultist's Habit — chest gear; blends with cults (+reputation), −sanity in daylight. flavour (design/16): no equipment asset.
        ModItems.add("cultist_habit", 1);
        // Occultist's Hat — head gear; −sanity drain while reading tomes, no armour value. flavour (design/16): no equipment asset.
        ModItems.add("occultists_hat", 1);
        // Boots of Silence — feet gear; muffles footsteps, reduces pursuit/sanity from noise. flavour (design/16): no equipment asset.
        ModItems.add("boots_of_silence", 1);
        // Gloves of Precision — hands gear; +ritual accuracy and lockpicking, no combat bonus. flavour (design/16): no equipment asset.
        ModItems.add("gloves_of_precision", 1);
    }

    /** The vanilla mace's component set (both this mod's maces reuse the class); only durability varies. */
    private static Item.Properties mace(Item.Properties properties, int durability) {
        return properties.durability(durability)
                .component(DataComponents.TOOL, MaceItem.createToolProperties())
                .attributes(MaceItem.createAttributes())
                .enchantable(15)
                .component(DataComponents.WEAPON, new Weapon(1));
    }
}
