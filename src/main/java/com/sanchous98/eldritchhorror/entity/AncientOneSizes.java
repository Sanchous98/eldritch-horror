package com.sanchous98.eldritchhorror.entity;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * The canonical body size of every Ancient One, in blocks.
 *
 * <p>The world is 1:1 (1 block = 1 m), so the old 6–9 block presences read as pets next to a
 * 20–30 block city and a 330 block Eiffel Tower. This table is the single source of truth for the
 * hitbox ({@code ModEntities}), the eye height, the melee reach, the distance scaling of abilities,
 * the dread presence and the renderer scale. A test asserts the tier floors and the 0.85 eye
 * height; nothing may hardcode a boss size anywhere else.
 *
 * <p>Tiers:
 * <ul>
 *   <li>{@link Tier#COSMIC} — the four outer gods: 112–128 tall, 40–56 wide;</li>
 *   <li>{@link Tier#MAJOR} — the site and roaming presences: 64–96 tall, 24–40 wide;</li>
 *   <li>{@link Tier#LESSER} — the farm bosses: 24–32 tall, 10–16 wide, deliberately local.</li>
 * </ul>
 *
 * <p>{@code baseHeight}/{@code baseScale} are the pre-resize hitbox height and renderer scale. The
 * renderer scale is derived from them so the visible model keeps the same top-to-hitbox ratio it
 * had before: {@code baseScale × height / baseHeight}.
 */
public final class AncientOneSizes {

    /** The old 8-block major-presence baseline that ability distances were authored against. */
    public static final double DESIGN_BASELINE_HEIGHT = 8.0;

    /** At or below this height a presence is a farm boss: local abilities, no arena-wide reach. */
    public static final double FARMABLE_MAX_HEIGHT = 32.0;

    /** Coarse size band; every registered Ancient One belongs to exactly one. */
    public enum Tier {
        COSMIC(112.0F, 128.0F, 40.0F, 56.0F),
        MAJOR(64.0F, 96.0F, 24.0F, 40.0F),
        LESSER(24.0F, 32.0F, 10.0F, 16.0F);

        private final float minHeight;
        private final float maxHeight;
        private final float minWidth;
        private final float maxWidth;

        Tier(float minHeight, float maxHeight, float minWidth, float maxWidth) {
            this.minHeight = minHeight;
            this.maxHeight = maxHeight;
            this.minWidth = minWidth;
            this.maxWidth = maxWidth;
        }

        public float minHeight() {
            return this.minHeight;
        }

        public float maxHeight() {
            return this.maxHeight;
        }

        public float minWidth() {
            return this.minWidth;
        }

        public float maxWidth() {
            return this.maxWidth;
        }
    }

    /** One presence: hitbox, pre-resize reference and the derived reach/presence numbers. */
    public record Size(String id, Tier tier, float height, float width,
                       float baseHeight, float baseScale) {

        /** Vanilla's eye height convention, pinned here so a test can hold it. */
        public float eyeHeight() {
            return this.height * 0.85F;
        }

        /** Melee reach from the centre: about half the body height. */
        public double meleeReach() {
            return this.height * 0.5;
        }

        /** How far the non-damaging dread presence is felt: six body heights. */
        public double dreadRadius() {
            return this.height * 6.0;
        }

        /** Abilities scale against the old 8-block major baseline; farm bosses stay local. */
        public double abilityScale() {
            double raw = this.height / DESIGN_BASELINE_HEIGHT;
            return this.tier == Tier.LESSER ? Math.min(raw, 2.5) : raw;
        }

        /** Action-bar warning range: the presence's voice carries about 1.5 body heights. */
        public double warningRadius() {
            return Math.max(32.0, this.height * 1.5);
        }

        /** Loudness of the wind-up/sting, from 1.4 up to 8.0 (vanilla caps the audible range). */
        public float soundVolume() {
            return Math.max(1.4F, Math.min(8.0F, this.height / 16.0F));
        }

        /** Renderer scale that keeps the model top where the hitbox top is. */
        public float renderScale() {
            return this.baseScale * this.height / this.baseHeight;
        }

        /** Shadow radius, kept proportional to the body but capped so it stays a shadow. */
        public float shadow(float base) {
            return (float) (base * Math.min(8.0, (double) this.height / this.baseHeight));
        }

        /** Does this presence need the giant movement/collision treatment? */
        public boolean giant() {
            return this.height >= 24.0F;
        }
    }

    private static final Map<String, Size> BY_ID = new LinkedHashMap<>();

    static {
        // Cosmic: outer gods, city-scale.
        add("azathoth", Tier.COSMIC, 128.0F, 56.0F, 6.5F, 1.20F);
        add("shub_niggurath", Tier.COSMIC, 120.0F, 52.0F, 8.0F, 1.60F);
        add("yog_sothoth", Tier.COSMIC, 116.0F, 48.0F, 8.0F, 1.45F);
        add("nyarlathotep", Tier.COSMIC, 112.0F, 40.0F, 3.5F, 1.45F);

        // Major: the site and roaming presences.
        add("the_horror", Tier.MAJOR, 96.0F, 40.0F, 9.0F, 1.55F);
        add("cthulhu", Tier.MAJOR, 96.0F, 40.0F, 8.0F, 1.85F);
        add("hastur", Tier.MAJOR, 88.0F, 36.0F, 7.0F, 2.20F);
        add("ithaqua", Tier.MAJOR, 80.0F, 30.0F, 5.5F, 2.10F);
        add("yig", Tier.MAJOR, 72.0F, 28.0F, 4.5F, 2.30F);
        add("rhan_tegoth", Tier.MAJOR, 72.0F, 32.0F, 5.0F, 1.60F);
        add("choir_leviathan", Tier.MAJOR, 72.0F, 36.0F, 5.0F, 1.70F);
        add("glaaki", Tier.MAJOR, 68.0F, 30.0F, 4.2F, 1.90F);
        add("nyogtha", Tier.MAJOR, 68.0F, 36.0F, 4.0F, 1.60F);
        add("atlach_nacha", Tier.MAJOR, 64.0F, 40.0F, 3.4F, 1.40F);
        add("dunwich_horror", Tier.MAJOR, 64.0F, 26.0F, 4.5F, 1.50F);
        add("nephren_ka", Tier.MAJOR, 64.0F, 24.0F, 3.6F, 1.75F);

        // Lesser/farmable: smaller, local, still one-shot-able by a prepared player.
        add("chaugnar_faugn", Tier.LESSER, 30.0F, 14.0F, 4.0F, 1.45F);
        add("cthugha", Tier.LESSER, 28.0F, 12.0F, 4.5F, 1.65F);
        add("tulzscha", Tier.LESSER, 28.0F, 12.0F, 3.6F, 1.25F);
        add("hydra", Tier.LESSER, 26.0F, 16.0F, 4.5F, 1.50F);
        add("ithaqual_shade", Tier.LESSER, 26.0F, 10.0F, 3.8F, 1.27F);
        add("abhoth", Tier.LESSER, 24.0F, 14.0F, 3.6F, 1.80F);
        add("zstylzhemghi", Tier.LESSER, 24.0F, 12.0F, 3.8F, 1.45F);
    }

    private AncientOneSizes() {
    }

    private static void add(String id, Tier tier, float height, float width,
                            float baseHeight, float baseScale) {
        Size previous = BY_ID.put(id, new Size(id, tier, height, width, baseHeight, baseScale));
        if (previous != null) {
            throw new IllegalStateException("duplicate Ancient One size: " + id);
        }
    }

    /** The size of {@code id}; throws if a boss is added without a row. */
    public static Size of(String id) {
        Size size = BY_ID.get(id);
        if (size == null) {
            throw new IllegalArgumentException("no size registered for Ancient One: " + id);
        }
        return size;
    }

    /** Every row, in declaration order (cosmic, major, lesser). */
    public static Collection<Size> all() {
        return BY_ID.values();
    }

    /** Every registered id, in declaration order. */
    public static Set<String> ids() {
        return BY_ID.keySet();
    }

    /** How many Ancient Ones the table covers. */
    public static int count() {
        return BY_ID.size();
    }
}
