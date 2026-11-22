package com.sanchous98.eldritchhorror.registry.items;

/**
 * The four firearms' stat ladder ({@code design/16-items.md}), kept in a Minecraft-free class so
 * {@code FirearmSpecTest} can read it without bootstrapping the game registries.
 *
 * <p>{@link Firearms} owns registration and references these constants; the values themselves are
 * pure data and are the part worth unit-testing (damage/cooldown ordering, pellet counts, pierce).
 */
public final class FirearmLadder {

    /** .38 Revolver — common sidearm: one round, short cooldown, 250 shots. */
    public static final FirearmSpec REVOLVER = new FirearmSpec(5.0F, 2.8F, 1.0F, 12, 1, 1, 0);

    /** .45 Automatic — quality sidearm: harder hit, faster cycle, 300 shots. */
    public static final FirearmSpec AUTOMATIC = new FirearmSpec(6.0F, 3.0F, 1.5F, 8, 1, 1, 0);

    /**
     * Double-barreled Shotgun — quality: both barrels at once, two cartridges, slow, 220 shots.
     * One heavy charge rather than five pellets: vanilla's ten-tick damage immunity collapses a
     * multi-projectile volley into a single hit, and a single hard-hitting round is honest about
     * that instead of promising damage the engine cannot deliver.
     */
    public static final FirearmSpec SHOTGUN = new FirearmSpec(10.0F, 2.8F, 2.0F, 28, 1, 2, 0);

    /** Carbine Rifle — fine: long, flat, piercing shot, 350 shots. */
    public static final FirearmSpec CARBINE = new FirearmSpec(8.0F, 4.0F, 0.6F, 36, 1, 1, 1);

    private FirearmLadder() {
    }
}
