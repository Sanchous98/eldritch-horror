package com.sanchous98.eldritchhorror.world.loc.style;

import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import net.minecraft.util.RandomSource;

/**
 * Paris — cultural city style. STUB: not styled yet, so the city uses the climate gothic palette
 * and the default cathedral landmark. Replace this file wholesale to give Paris its real local
 * colour (materials, landmark, flourishes). See docs/STRUCTURES-CONTRACT.md § Cultural styles;
 * copy the pattern from {@link TokyoStyle}. Shared helpers live in {@link StyleKit}.
 */
public final class ParisStyle implements CityStyle {

    @Override
    public String id() {
        return "paris";
    }

    @Override
    public Palette palette(int koppenClass, boolean coastal) {
        return Palette.fromBiome(koppenClass, coastal);
    }

    @Override
    public void landmark(StructureBuilder b, RandomSource rng, int cx, int cz, int ground, Palette p) {
        StyleKit.cathedral(b, rng, cx, cz, ground, p);
    }
}
