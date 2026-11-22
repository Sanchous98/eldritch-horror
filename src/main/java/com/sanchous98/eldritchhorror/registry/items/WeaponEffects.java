package com.sanchous98.eldritchhorror.registry.items;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.corruption.CorruptionAPI;
import com.sanchous98.eldritchhorror.sanity.SanityAPI;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.ArrowLooseEvent;

/**
 * The weapons' recorded on-hit effects ({@code design/16-items.md}, the weapons table): the silver
 * blade and star-iron sword's target bonuses, the sacrificial dagger's corruption scaling, the
 * tidal trident's underwater cut, the censer mace's paid ward, the hollow pact blade's price on a
 * kill, and the veil bow's shot cost, eldritch bonus and piercing head.
 *
 * <p>One bus subscriber, two damage hooks. {@link LivingDamageEvent.Pre} runs after armour and
 * enchantment reduction but before absorption, so a bonus is applied to the damage the target will
 * actually take; {@link LivingDeathEvent} catches the killing blow exactly once. Everything is
 * gated on a {@link ServerPlayer} attacker (or an arrow's owner), reads only the held stack and one
 * entity-type tag, and never scans a chunk. No registry value is resolved in a static initialiser:
 * the item lookups happen inside the handler, at event time.
 *
 * <p><b>Numbers.</b> Small by design: the silver blade and star-iron sword add
 * {@value #SILVER_BONUS} and {@value #STAR_IRON_BONUS} damage respectively, the bow
 * {@value WeaponMath#VEIL_BOW_ELDRITCH_BONUS}; the dagger scales to {@link WeaponMath#DAGGER_BONUS_AT_FULL}
 * and the trident to {@link WeaponMath#WATER_BONUS} at most. The costs are one sanity per censer
 * swing or veil arrow, and {@value #PACT_SANITY_COST} sanity plus {@value #PACT_CORRUPTION_GAIN}
 * corruption per pact-blade kill. There is no natural {@code ModConfig} item/combat section, so these
 * are named constants with this javadoc rather than new config keys, matching how the item pass
 * records flavour.
 *
 * <p><b>Charge.</b> The flat bonuses (silver blade, star-iron sword, axe) and the axe's sanity price
 * only land on a full-charge swing ({@code getAttackStrengthScale} &gt; 0.9), so spam-clicking gets
 * the base hit and nothing more. The multiplicative effects (dagger, trident) apply to every hit.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class WeaponEffects {

    /**
     * The mod's eldritch mobs: Ancient Ones, lesser/star-spawn horrors, the tainted and the cult
     * war-band — but never the mundane animals ({@code design/25-bestiary-and-entities.md}). Kept in
     * the data tag so a later mob joins the bonus without a code change.
     */
    private static final TagKey<EntityType<?>> ELDRITCH =
            TagKey.create(Registries.ENTITY_TYPE, EldritchHorror.id("eldritch"));

    /**
     * Star-spawn alone, as a separate small tag: the star-iron sword's bonus is narrower than the
     * silver blade's and should follow future star-spawn variants registered in data, not a hard
     * entity id. One extra tag keeps both bonuses declarative.
     */
    private static final TagKey<EntityType<?>> STAR_SPAWN =
            TagKey.create(Registries.ENTITY_TYPE, EldritchHorror.id("star_spawn"));

    /** Silver blade: flat bonus against an eldritch target (an iron sword's 6 → 8). */
    private static final float SILVER_BONUS = 2.0F;

    /** Star-iron sword: flat bonus against a star-spawn (meteoric iron finds its kin). */
    private static final float STAR_IRON_BONUS = 2.0F;

    /** Axe: the board game's "spend 2 Sanity to reroll" as a heavy blow on eldritch targets. */
    private static final double AXE_SANITY_COST = 2.0;

    /** Axe: extra damage of the paid heavy blow. */
    private static final float AXE_ELDRITCH_BONUS = 3.0F;

    /** Bull whip: the slow applied by a landing lash, in ticks. */
    private static final int WHIP_SLOW_TICKS = 60;

    /** Bull whip: horizontal shove of a landing lash. */
    private static final double WHIP_KNOCKBACK = 0.6;

    /** Censer mace: sanity paid for the ward it raises on every landing swing. */
    private static final double CENSER_SANITY_COST = 1.0;

    /** Censer mace: the ward lasts three seconds (60 ticks), refreshed by the next hit. */
    private static final int CENSER_WARD_TICKS = 60;

    /** Hollow pact blade: sanity paid when a kill is credited to it. */
    private static final double PACT_SANITY_COST = 4.0;

    /** Hollow pact blade: corruption gained per kill ({@code design/16}: "+1/kill"). */
    private static final double PACT_CORRUPTION_GAIN = 1.0;

    /** Veil bow: sanity paid for each arrow actually loosed. */
    private static final double BOW_SANITY_COST = 1.0;

    private WeaponEffects() {
    }

    /**
     * All weapon damage modifiers, melee and projectile. Melee is identified by the direct entity
     * being the player — a thrown trident or an arrow is not a swing, so the trident's underwater
     * bonus is melee-only. The arrow branch reads the firing weapon stored on the arrow.
     */
    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Pre event) {
        if (event.getNewDamage() <= 0.0F) {
            return;
        }
        DamageSource source = event.getSource();
        LivingEntity victim = event.getEntity();
        if (source.getDirectEntity() instanceof ServerPlayer player) {
            applyMelee(event, player, victim);
            return;
        }
        if (source.getDirectEntity() instanceof AbstractArrow arrow
                && arrow.getOwner() instanceof ServerPlayer) {
            ItemStack bow = arrow.getWeaponItem();
            if (bow != null && bow.getItem() instanceof VeilBowItem
                    && victim.getType().builtInRegistryHolder().is(ELDRITCH)) {
                event.setNewDamage(WeaponMath.bonusDamage(event.getNewDamage(),
                        WeaponMath.VEIL_BOW_ELDRITCH_BONUS, true));
            }
        }
    }

    /** The held-weapon branch of {@link #onDamage}; at most one weapon can be in the main hand. */
    private static void applyMelee(LivingDamageEvent.Pre event, ServerPlayer player,
                                   LivingEntity victim) {
        ItemStack held = player.getMainHandItem();
        float damage = event.getNewDamage();
        // A flat bonus is the reward for a full swing: a 0-cooldown spam click lands at a fraction
        // of the charge and must not collect it (nor the axe's paid blow).
        boolean charged = player.getAttackStrengthScale(0.5F) > 0.9F;
        if (isHeld(held, "silver_blade")) {
            if (charged) {
                event.setNewDamage(WeaponMath.bonusDamage(damage, SILVER_BONUS,
                        victim.getType().builtInRegistryHolder().is(ELDRITCH)));
            }
        } else if (isHeld(held, "star_iron_sword")) {
            if (charged) {
                event.setNewDamage(WeaponMath.bonusDamage(damage, STAR_IRON_BONUS,
                        victim.getType().builtInRegistryHolder().is(STAR_SPAWN)));
            }
        } else if (isHeld(held, "sacrificial_dagger")) {
            event.setNewDamage(WeaponMath.scaled(damage,
                    WeaponMath.daggerMultiplier(CorruptionAPI.get(player))));
        } else if (isHeld(held, "tidal_trident")) {
            event.setNewDamage(WeaponMath.scaled(damage,
                    WeaponMath.waterMultiplier(player.isUnderWater())));
        } else if (isHeld(held, "censer_mace")) {
            raiseCenserWard(player);
        } else if (isHeld(held, "woodsman_axe")) {
            // FFG Axe: "spend 2 Sanity to reroll" - here a paid heavy blow, and only against the
            // eldritch, so the axe is a boss-fight tool rather than a strict sword upgrade.
            if (charged && victim.getType().builtInRegistryHolder().is(ELDRITCH)
                    && SanityAPI.get(player) >= AXE_SANITY_COST) {
                event.setNewDamage(WeaponMath.bonusDamage(damage, AXE_ELDRITCH_BONUS, true));
                SanityAPI.add(player, -AXE_SANITY_COST);
            }
        } else if (isHeld(held, "bull_whip")) {
            lash(player, victim);
        }
    }

    /** The whip's board-game reroll becomes control: a shove and a short slow. */
    private static void lash(ServerPlayer player, LivingEntity victim) {
        victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, WHIP_SLOW_TICKS, 1,
                true, false));
        Vec3 away = victim.position().subtract(player.position());
        if (away.horizontalDistanceSqr() > 1.0E-4) {
            Vec3 push = away.normalize().scale(WHIP_KNOCKBACK);
            victim.push(push.x, 0.25, push.z);
        }
    }

    /** The censer's ward: a short Resistance I at a sanity price, paid per landing swing. */
    private static void raiseCenserWard(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, CENSER_WARD_TICKS, 0,
                true, false));
        SanityAPI.add(player, -CENSER_SANITY_COST);
    }

    /**
     * The hollow pact blade's price, paid once per credited kill: the blade "remembers the hand that
     * signed for it". The killing weapon decides — a projectile or explosion kill never charges a
     * blade the player merely happens to hold, and swapping away before the blow lands does not
     * dodge the price.
     */
    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ItemStack weapon = event.getSource().getWeaponItem();
        if (weapon == null || !isHeld(weapon, "hollow_pact_blade")) {
            return;
        }
        CorruptionAPI.add(player, PACT_CORRUPTION_GAIN);
        SanityAPI.add(player, -PACT_SANITY_COST);
    }

    /**
     * The veil bow's per-shot sanity cost. {@link ArrowLooseEvent} fires inside
     * {@code BowItem.releaseUsing} only when an actual shot will be loosed ({@code hasAmmo} and a
     * non-zero draw), so a dry or cancelled pull costs nothing.
     */
    @SubscribeEvent
    public static void onArrowLoose(ArrowLooseEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!(event.getBow().getItem() instanceof VeilBowItem)
                || !event.hasAmmo() || event.getCharge() <= 0) {
            return;
        }
        SanityAPI.add(player, -BOW_SANITY_COST);
    }

    /** Whether {@code stack} is the registered item named {@code id}. */
    private static boolean isHeld(ItemStack stack, String id) {
        Item item = BuiltInRegistries.ITEM.getValue(EldritchHorror.id(id));
        return stack.is(item);
    }
}
