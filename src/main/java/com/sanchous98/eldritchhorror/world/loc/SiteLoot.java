package com.sanchous98.eldritchhorror.world.loc;

import com.sanchous98.eldritchhorror.EldritchHorror;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * The loot tables the fixed sites draw their chests from ({@code design/16}, {@code design/21}).
 * Four flavours ship under {@code data/eldritch_horror/loot_table/chests/}: common ruin supplies,
 * the Order's sanctified stores, cult offerings, and rare veil relics. Kept tiny and data-driven;
 * the tables themselves are JSON.
 *
 * <p>{@link #fillRoom} is the one placement helper: it drops a bounded, deterministic pair of
 * chests inside a built interior, on the floor and only where the cell is replaceable, so a chest
 * can never overwrite a wall or door.
 */
public final class SiteLoot {

    public static final ResourceKey<LootTable> COMMON = key("chests/common");
    public static final ResourceKey<LootTable> ORDER = key("chests/order");
    public static final ResourceKey<LootTable> CULT = key("chests/cult");
    public static final ResourceKey<LootTable> RELIC = key("chests/relic");

    private SiteLoot() {
    }

    private static ResourceKey<LootTable> key(String path) {
        return ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE,
                EldritchHorror.id(path));
    }

    /**
     * Places up to two chests from {@code table} inside the interior box, on {@code floorY + 1}, at
     * two deterministic corners. A cell that is not replaceable (a wall, door or prop) is left
     * alone, so the number placed is 0–2 but never destructive.
     */
    public static void fillRoom(StructureBuilder b, int x0, int z0, int x1, int z1,
                                int floorY, ResourceKey<LootTable> table) {
        int ax = Math.min(x0, x1) + 1;
        int bx = Math.max(x0, x1) - 1;
        int az = Math.min(z0, z1) + 1;
        int bz = Math.max(z0, z1) - 1;
        if (ax > bx || az > bz) {
            return; // too small to hold a chest without clipping the wall
        }
        place(b, ax, floorY + 1, az, table);
        place(b, bx, floorY + 1, bz, table);
    }

    /** Places one chest at {@code (x,y,z)} if the cell is replaceable (never over a wall/prop). */
    public static void place(StructureBuilder b, int x, int y, int z, ResourceKey<LootTable> table) {
        if (b.isReplaceable(x, y, z)) {
            b.chest(x, y, z, table);
        }
    }
}
