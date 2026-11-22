package com.sanchous98.eldritchhorror.world.loc.city.plan;

/**
 * One authored anchor in a city plan: an NPC, quest board, shop, inn, healer, landmark, secret or
 * sign. Coordinates are relative to the city centre and ground: {@code dx} is east, {@code dy} is
 * up and {@code dz} is south from {@code (cx, ground, cz)}.
 *
 * @param id stable authored id, unique within the city (e.g. {@code "tea_house"})
 * @param kind what the runtime should spawn or attach here
 * @param dx east offset in blocks from the city centre
 * @param dy offset in blocks above the city ground
 * @param dz south offset in blocks from the city centre
 * @param data free-form authored hint (profession, quest id, sign text key, ...); use {@code ""}
 *             when there is nothing to pass on
 */
public record CityPoi(String id, Kind kind, int dx, int dy, int dz, String data) {

    /** The kind of anchor a plan declares. */
    public enum Kind {
        NPC,
        QUEST_BOARD,
        SHOP,
        INN,
        HEALER,
        LANDMARK,
        SECRET,
        SIGN,
        /** A rail station/yard; the rail network will use this anchor. */
        STATION,
        /** A harbour/dock water or quay point; the ship network will use this anchor. */
        PORT
    }
}
