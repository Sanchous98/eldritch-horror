package com.sanchous98.eldritchhorror.registry.items;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.registry.ModItems;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAssets;

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
 * <p><b>The eight worn pieces are real armour.</b> 26.3 has no {@code ArmorItem}: vanilla registers
 * armour as a plain {@link Item} whose {@code Item.Properties.humanoidArmor(ArmorMaterial,
 * ArmorType)} sets durability ({@code type.getDurability(material.durability())}), the armour
 * attribute modifiers, the enchantability, the repair tag and the {@code EQUIPPABLE} component.
 * The {@link ArmorMaterial}'s {@code assetId} points at {@code assets/eldritch_horror/equipment/
 * <id>.json}, which names the worn sheet at {@code textures/entity/equipment/humanoid/<id>.png};
 * both are written by {@code tools/gen_assets.py}. The recorded flavour stays flavour and is
 * carried as a {@code LORE} line beside an {@code .armor} line stating the real values (both
 * locales).
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

        // Investigator's Coat — chest armour (iron-ish, light): −sanity drain in darkness stays
        // flavour (design/16; the darkness source answers to lantern_of_steadfast instead).
        ModItems.addItem("investigator_coat",
                p -> new Item(armor(p, COAT, ArmorType.CHESTPLATE, "investigator_coat")), 1);
        // Choir Robe — chest armour (iron-ish): +reputation gain and +corruption gain stay flavour.
        ModItems.addItem("choir_robe",
                p -> new Item(armor(p, ROBE, ArmorType.CHESTPLATE, "choir_robe")), 1);
        // Warden's Plate — chest armour (iron): the warden_sigil's spread suppression is NOT doubled
        // onto the plate; its slow stays flavour (design/16).
        ModItems.addItem("wardens_plate",
                p -> new Item(armor(p, PLATE, ArmorType.CHESTPLATE, "wardens_plate")), 1);
        // Drowned Wetsuit — chest armour (leather-ish): water breathing / swim speed stay flavour.
        ModItems.addItem("drowned_wetsuit",
                p -> new Item(armor(p, WETSUIT, ArmorType.CHESTPLATE, "drowned_wetsuit")), 1);
        // Cultist's Habit — chest armour (leather-ish): +reputation blending / daylight drain stay
        // flavour (design/16).
        ModItems.addItem("cultist_habit",
                p -> new Item(armor(p, HABIT, ArmorType.CHESTPLATE, "cultist_habit")), 1);
        // Occultist's Hat — head armour (light): the tome-reading drain stays flavour.
        ModItems.addItem("occultists_hat",
                p -> new Item(armor(p, HAT, ArmorType.HELMET, "occultists_hat")), 1);
        // Boots of Silence — feet armour (light): the muffled footsteps stay flavour.
        ModItems.addItem("boots_of_silence",
                p -> new Item(armor(p, BOOTS, ArmorType.BOOTS, "boots_of_silence")), 1);
        // Gloves of Precision — hands gear; 26.3 has no hand slot, so they are worn in the chest
        // slot, whose vanilla model also covers the arms (judgement call, noted in the javadoc
        // above). +ritual accuracy / lockpicking stay flavour (design/16).
        ModItems.addItem("gloves_of_precision",
                p -> new Item(armor(p, GLOVES, ArmorType.CHESTPLATE, "gloves_of_precision")), 1);
    }

    // --- the eight worn sets' materials (design/16) ------------------------------------------------
    // Durability is the per-slot multiplier (chestplate 16, helmet 11, boots 13); defence is the
    // armour points that slot gains. Iron-ish: plate/robe/coat. Leather-ish: wetsuit/habit. Light:
    // hat/boots/gloves. Each set owns its equipment asset, so each look is its own worn sheet.

    /** Iron chestplate-equivalent plate: 6 armour, 240 durability. */
    private static final ArmorMaterial PLATE = material(15, 6, ArmorType.CHESTPLATE,
            SoundEvents.ARMOR_EQUIP_IRON, ItemTags.REPAIRS_IRON_ARMOR, "wardens_plate");
    /** A robe a step under the plate: 5 armour, 160 durability. */
    private static final ArmorMaterial ROBE = material(10, 5, ArmorType.CHESTPLATE,
            SoundEvents.ARMOR_EQUIP_LEATHER, ItemTags.REPAIRS_LEATHER_ARMOR, "choir_robe");
    /** A heavy coat: 5 armour, 192 durability. */
    private static final ArmorMaterial COAT = material(12, 5, ArmorType.CHESTPLATE,
            SoundEvents.ARMOR_EQUIP_LEATHER, ItemTags.REPAIRS_LEATHER_ARMOR, "investigator_coat");
    /** Diver's gear, leather-ish: 3 armour, 96 durability. */
    private static final ArmorMaterial WETSUIT = material(6, 3, ArmorType.CHESTPLATE,
            SoundEvents.ARMOR_EQUIP_LEATHER, ItemTags.REPAIRS_LEATHER_ARMOR, "drowned_wetsuit");
    /** Plain habit, leather-ish: 3 armour, 80 durability. */
    private static final ArmorMaterial HABIT = material(5, 3, ArmorType.CHESTPLATE,
            SoundEvents.ARMOR_EQUIP_LEATHER, ItemTags.REPAIRS_LEATHER_ARMOR, "cultist_habit");
    /** Light hat: 1 armour, 55 durability. */
    private static final ArmorMaterial HAT = material(5, 1, ArmorType.HELMET,
            SoundEvents.ARMOR_EQUIP_LEATHER, ItemTags.REPAIRS_LEATHER_ARMOR, "occultists_hat");
    /** Light boots: 1 armour, 65 durability. */
    private static final ArmorMaterial BOOTS = material(5, 1, ArmorType.BOOTS,
            SoundEvents.ARMOR_EQUIP_LEATHER, ItemTags.REPAIRS_LEATHER_ARMOR, "boots_of_silence");
    /** Light gloves (chest slot): 1 armour, 80 durability. */
    private static final ArmorMaterial GLOVES = material(5, 1, ArmorType.CHESTPLATE,
            SoundEvents.ARMOR_EQUIP_LEATHER, ItemTags.REPAIRS_LEATHER_ARMOR, "gloves_of_precision");

    /**
     * One worn set's {@link ArmorMaterial}: the durability multiplier, its single slot's defence,
     * the equip sound, the repair tag and the equipment asset key {@code
     * eldritch_horror:<id>} that resolves under {@code assets/eldritch_horror/equipment/}.
     */
    private static ArmorMaterial material(int durability, int defense, ArmorType type,
                                          Holder<SoundEvent> equipSound, TagKey<Item> repair,
                                          String asset) {
        return new ArmorMaterial(durability, Map.of(type, defense), 15, equipSound, 0.0F, 0.0F,
                repair, ResourceKey.create(EquipmentAssets.ROOT_ID, EldritchHorror.id(asset)));
    }

    /**
     * {@code humanoidArmor} plus the two locale tooltip lines: {@code <id>.armor} states the real
     * armour value and durability, {@code <id>.tooltip} the recorded flavour. Both render through
     * the {@code LORE} component, so no client class is involved.
     */
    private static Item.Properties armor(Item.Properties properties, ArmorMaterial material,
                                         ArmorType type, String id) {
        return properties.humanoidArmor(material, type)
                .component(DataComponents.LORE, new ItemLore(List.of(
                        Component.translatable("item.eldritch_horror." + id + ".armor"),
                        Component.translatable("item.eldritch_horror." + id + ".tooltip"))));
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
