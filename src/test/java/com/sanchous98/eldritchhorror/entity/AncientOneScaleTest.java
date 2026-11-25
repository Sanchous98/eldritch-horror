package com.sanchous98.eldritchhorror.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sanchous98.eldritchhorror.registry.ModEntities;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Guards the 1:1 world resize of the 23 Ancient Ones.
 *
 * <p>The headless table is the same one {@code ModEntities} builds the hitboxes from, so this test
 * is the resize contract: every registered presence is at least 24 blocks tall and 10 wide, its eye
 * height is 0.85 of the body, and its melee reach is at least 0.4 of the body. It prints the
 * id/size/reach table so a reviewer can see the whole roster without a client.
 */
class AncientOneScaleTest {

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void tableCoversEveryRegisteredAncientOne() {
        assertEquals(23, AncientOneSizes.count(), "23 Ancient Ones");
        assertEquals(ModEntities.ancientOneIds(), AncientOneSizes.ids(),
                "every registered Ancient One has exactly one size row");
    }

    @Test
    void everyAncientOneMeetsTheCityScaleFloor() {
        StringBuilder table = new StringBuilder(
                String.format("%-18s %-7s %7s %7s %8s %8s%n",
                        "id", "tier", "height", "width", "eye", "melee"));
        for (AncientOneSizes.Size size : AncientOneSizes.all()) {
            assertTrue(size.height() >= 24.0F, size.id() + " height " + size.height());
            assertTrue(size.width() >= 10.0F, size.id() + " width " + size.width());
            assertEquals(size.height() * 0.85F, size.eyeHeight(), 0.01F,
                    size.id() + " eye height");
            assertTrue(size.meleeReach() >= 0.4 * size.height(),
                    size.id() + " melee reach " + size.meleeReach());
            assertTrue(size.height() >= size.tier().minHeight()
                            && size.height() <= size.tier().maxHeight(),
                    size.id() + " height outside " + size.tier() + " band");
            assertTrue(size.width() >= size.tier().minWidth()
                            && size.width() <= size.tier().maxWidth(),
                    size.id() + " width outside " + size.tier() + " band");
            table.append(String.format("%-18s %-7s %7.1f %7.1f %8.1f %8.1f%n",
                    size.id(), size.tier(), size.height(), size.width(),
                    size.eyeHeight(), size.meleeReach()));
        }
        System.out.print(table);
    }

    @Test
    void rendererScaleKeepsTheModelTopOnTheHitbox() {
        for (AncientOneSizes.Size size : AncientOneSizes.all()) {
            assertEquals(size.renderScale(),
                    size.baseScale() * size.height() / size.baseHeight(), 0.0001F,
                    size.id() + " renderer scale does not follow the hitbox ratio");
            assertTrue(size.abilityScale() > 0.0, size.id() + " ability scale");
            assertTrue(size.warningRadius() >= 32.0, size.id() + " warning radius");
        }
    }
}
