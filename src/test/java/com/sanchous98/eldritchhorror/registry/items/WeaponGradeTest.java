package com.sanchous98.eldritchhorror.registry.items;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Guards the weapon grade table ({@code design/16-items.md}): every combat item is graded, the
 * ladder is ordered, and the lookup falls back to COMMON. Pure Java - no game bootstrap.
 */
class WeaponGradeTest {

    @Test
    void everyCombatItemIsGraded() {
        Map<String, WeaponGrade> table = WeaponGrade.byItem();
        assertEquals(20, table.size(), "twenty weapons and combat throwables");
        for (String id : new String[]{
                "bone_club", "woodsman_axe", "revolver_38", "automatic_45",
                "shotgun_double", "carbine_rifle", "hollow_pact_blade", "veil_bow"}) {
            assertNotNull(table.get(id), "missing grade for " + id);
        }
    }

    @Test
    void theFieldGearSitsOnTheIntendedRungs() {
        assertEquals(WeaponGrade.CRUDE, WeaponGrade.of("bone_club"));
        assertEquals(WeaponGrade.COMMON, WeaponGrade.of("revolver_38"));
        assertEquals(WeaponGrade.QUALITY, WeaponGrade.of("shotgun_double"));
        assertEquals(WeaponGrade.FINE, WeaponGrade.of("carbine_rifle"));
        assertEquals(WeaponGrade.RELIC, WeaponGrade.of("veil_bow"));
    }

    @Test
    void unknownItemsReadAsCommon() {
        assertEquals(WeaponGrade.COMMON, WeaponGrade.of("not_a_weapon"));
    }

    @Test
    void theLadderIsOrdered() {
        assertTrue(WeaponGrade.CRUDE.ordinal() < WeaponGrade.COMMON.ordinal());
        assertTrue(WeaponGrade.COMMON.ordinal() < WeaponGrade.QUALITY.ordinal());
        assertTrue(WeaponGrade.QUALITY.ordinal() < WeaponGrade.FINE.ordinal());
        assertTrue(WeaponGrade.FINE.ordinal() < WeaponGrade.RELIC.ordinal());
    }

    @Test
    void theTooltipKeyIsStable() {
        assertEquals("grade.eldritch_horror.quality", WeaponGrade.QUALITY.lineKey());
    }
}
