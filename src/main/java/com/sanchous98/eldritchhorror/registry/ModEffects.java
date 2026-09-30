package com.sanchous98.eldritchhorror.registry;

import com.sanchous98.eldritchhorror.EldritchHorror;
import it.unimi.dsi.fastutil.ints.Int2DoubleFunction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Mob effect registry: {@code madness} (the sanity-floor penalty), {@code marked} (the horror's
 * attention at zero sanity) and {@code corrupted} (reserved for the corruption batch).
 *
 * <p>Verified against the 26.3 decompiled sources: {@code MobEffect} exposes the protected
 * constructor {@code MobEffect(MobEffectCategory, int)}, so the three effects below use it and
 * declare their category and colour. Attribute penalties are declared in the constructor rather
 * than ticked in code, so they are applied/removed by the vanilla effect lifecycle.
 */
public final class ModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, EldritchHorror.MODID);

    /** Madness: the escalating penalty while sanity is fraying. Each level slows the player a little. */
    public static final DeferredHolder<MobEffect, MadnessEffect> MADNESS =
            MOB_EFFECTS.register("madness", MadnessEffect::new);

    /** Marked: applied once sanity reaches zero — the horror has noticed, and you are slower and weaker. */
    public static final DeferredHolder<MobEffect, MarkedEffect> MARKED =
            MOB_EFFECTS.register("marked", MarkedEffect::new);

    /** Corrupted: registered now; the corruption batch applies it. Inert on its own. */
    public static final DeferredHolder<MobEffect, CorruptedEffect> CORRUPTED =
            MOB_EFFECTS.register("corrupted", CorruptedEffect::new);

    private ModEffects() {
    }

    /** {@code madness}: −2% movement speed per amplifier (I, II, …). */
    public static final class MadnessEffect extends MobEffect {
        public MadnessEffect() {
            super(MobEffectCategory.HARMFUL, 0x6A0DAD);
            Int2DoubleFunction curve = amp -> -0.02 * (amp + 1);
            this.addAttributeModifier(Attributes.MOVEMENT_SPEED,
                    EldritchHorror.id("effect/madness_speed"),
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, curve);
        }
    }

    /** {@code marked}: −15% movement speed and −15% attack damage. */
    public static final class MarkedEffect extends MobEffect {
        public MarkedEffect() {
            super(MobEffectCategory.HARMFUL, 0x7A2FB0);
            this.addAttributeModifier(Attributes.MOVEMENT_SPEED,
                    EldritchHorror.id("effect/marked_speed"),
                    -0.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            this.addAttributeModifier(Attributes.ATTACK_DAMAGE,
                    EldritchHorror.id("effect/marked_damage"),
                    -0.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        }
    }

    /** {@code corrupted}: a placeholder penalty reserved for the corruption batch. */
    public static final class CorruptedEffect extends MobEffect {
        public CorruptedEffect() {
            super(MobEffectCategory.HARMFUL, 0x3A2A4A);
        }
    }
}
