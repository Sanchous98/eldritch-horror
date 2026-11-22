package com.sanchous98.eldritchhorror.world.loc.city.plan;

import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import java.util.List;

/**
 * One hand-authored city: the deliberate, block-level layout of a single curated city. City plans
 * are one file each under {@code world/loc/city/plan/<City>Plan.java}; see
 * {@code docs/CITY-PLAN-CONTRACT.md}.
 *
 * <p>There is no procedural layout and no shared prefab library: a plan places every block it
 * wants through {@link StructureBuilder}. {@link #build} is called once per generating chunk, so
 * it must produce the same whole-city layout on every call — the builder keeps only the slice that
 * falls inside the chunk currently generating.
 */
public interface CityPlan {

    /** Lowercase city key, e.g. {@code "shanghai"}; spaces and underscores are equivalent. */
    String cityId();

    /** Authored half-size in blocks: 300..400 (600..800 across). */
    int halfExtent();

    /**
     * Writes the city through {@code b}. Called once per generating chunk.
     *
     * <p>Must be deterministic and chunk-independent: no layout {@link StructureBuilder#rng()},
     * no static mutable state, no reads of the generating chunk ({@link StructureBuilder#isReplaceable}
     * is chunk-local). {@code b.groundY}/{@code b.isLand} are global terrain reads and are fine.
     * Coordinates are absolute world coordinates; {@code ground} is the flat-heart surface Y.
     *
     * <p>Prefer the terrain-aware overload {@link #build(StructureBuilder, CityPlanContext, Palette)}:
     * the shared pass keeps a flat authored heart and grades it into the natural terrain over the
     * rim. Use {@code ground} in the heart and {@code ctx.surface().at(x,z)} on the rim/outskirts;
     * never flatten the whole district into a plate cut out of the landscape.
     */
    void build(StructureBuilder b, int cx, int cz, int ground, Palette p);

    /**
     * Terrain-aware entry point: the same build with the shared {@link CityPlanContext}, whose
     * {@link CityPlanContext#surface()} gives the exact paved surface for every authored column.
     * Plans should override this method and sit lots on {@code ctx.surface().at(x,z)}; the default
     * keeps older plans (which place everything at {@code ground}) compiling.
     */
    default void build(StructureBuilder b, CityPlanContext ctx, Palette p) {
        build(b, ctx.cx(), ctx.cz(), ctx.ground(), p);
    }

    /**
     * Authored NPC / quest / POI anchors, with coordinates relative to {@code (cx, ground, cz)}.
     * Default: none. Not consumed at runtime yet.
     */
    default List<CityPoi> pois() {
        return List.of();
    }
}
