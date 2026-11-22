package com.sanchous98.eldritchhorror.registry.items;

/**
 * The numbers behind one firearm ({@code design/16-items.md}). A record rather than four loose
 * constructor arguments per gun: the four guns differ only in these values, and the validation in
 * the compact constructor keeps a typo (negative pellets, zero velocity) from reaching the game.
 *
 * @param damage        damage per pellet, applied after armour
 * @param velocity      projectile speed in blocks/tick handed to {@code shootFromRotation}
 * @param spread        aim uncertainty in degrees; 0 is a perfectly flat shot
 * @param cooldownTicks item cooldown between shots (26.3 {@code ItemCooldowns})
 * @param pellets       projectiles per trigger pull (a shotgun fires several)
 * @param ammoPerShot   cartridges consumed per trigger pull
 * @param pierce        extra bodies a single pellet passes through before stopping
 */
public record FirearmSpec(float damage, float velocity, float spread, int cooldownTicks,
                          int pellets, int ammoPerShot, int pierce) {

    public FirearmSpec {
        if (damage <= 0.0F) {
            throw new IllegalArgumentException("damage must be positive");
        }
        if (velocity <= 0.0F) {
            throw new IllegalArgumentException("velocity must be positive");
        }
        if (spread < 0.0F) {
            throw new IllegalArgumentException("spread must not be negative");
        }
        if (cooldownTicks < 0) {
            throw new IllegalArgumentException("cooldown must not be negative");
        }
        if (pellets < 1) {
            throw new IllegalArgumentException("a shot fires at least one pellet");
        }
        if (ammoPerShot < 1) {
            throw new IllegalArgumentException("a shot spends at least one cartridge");
        }
        if (pierce < 0) {
            throw new IllegalArgumentException("pierce must not be negative");
        }
    }
}
