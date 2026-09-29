package com.sanchous98.eldritchhorror.world.loc;

/**
 * A place the player can find, explore, and later fight in. Implementations are one file each:
 * {@code world/loc/<type>/<Name>.java}. See {@code docs/STRUCTURES-CONTRACT.md}.
 */
public interface Location {

    /** Stable id, used to seed the RNG. Must never change once shipped. */
    String id();

    /** Metropolis / Town / Minor — see {@link Tier}. */
    Tier tier();

    /** Build the location through the builder. Called once per generating chunk. */
    void build(StructureBuilder b);

    /** Half-extent in blocks: chunks farther than this are skipped. */
    int radius();
}
