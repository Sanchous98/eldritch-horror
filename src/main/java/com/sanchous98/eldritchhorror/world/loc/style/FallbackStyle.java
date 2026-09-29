package com.sanchous98.eldritchhorror.world.loc.style;

import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import net.minecraft.util.RandomSource;

/** The gothic default: climate palette + a plain spire. Used for any city without a style. */
public final class FallbackStyle implements CityStyle {

    @Override
    public String id() {
        return "gothic";
    }

    @Override
    public Palette palette(int koppenClass, boolean coastal) {
        return Palette.fromBiome(koppenClass, coastal);
    }

    @Override
    public void landmark(StructureBuilder b, RandomSource rng, int cx, int cz, int ground, Palette p) {
        b.spire(cx, cz, ground + 1, 18);
    }
}
