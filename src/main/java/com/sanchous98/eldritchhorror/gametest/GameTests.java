package com.sanchous98.eldritchhorror.gametest;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.endgame.Endgame;
import com.sanchous98.eldritchhorror.endgame.EndgameState;
import com.sanchous98.eldritchhorror.endgame.RiftLedger;
import com.sanchous98.eldritchhorror.entity.AncientOne;
import com.sanchous98.eldritchhorror.registry.ModEntities;
import com.sanchous98.eldritchhorror.rite.RitualAltar;
import com.sanchous98.eldritchhorror.world.rift.GateLinks;
import com.sanchous98.eldritchhorror.world.ward.WardLedger;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Headless GameTest functions for the systems that only need a {@link ServerLevel}.
 *
 * <p>26.3 runs tests from datapack {@code data/<namespace>/test_instance/*.json} files, each naming
 * a {@code TEST_FUNCTION} registry entry. Vanilla's {@code TestFunctionLoader} list is drained when
 * {@code BuiltInRegistries} bootstraps — which happens before mod constructors run under the
 * gametest main — so a loader registered from the mod constructor never executes. The functions are
 * therefore registered through the ordinary {@link DeferredRegister} path into
 * {@code Registries.TEST_FUNCTION}; the datapack JSONs in {@code data/eldritch_horror/test_instance}
 * refer to these names.
 *
 * <p>Every test builds only what it needs from the shared {@code gametest_platform} structure
 * (a 12×6×12 clean room with a stone floor) and uses positions unique to its own grid cell; the
 * endgame outcome is reset after the seal test, so the tests are independent of the order they run
 * in and can be retried.
 */
public final class GameTests {

    /** The {@code TEST_FUNCTION} entries the datapack test instances resolve by name. */
    private static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, EldritchHorror.MODID);

    /** Block ids used by the tests, resolved through the registry so a missing id fails the test. */
    private static final String ALTAR_CORE = "altar_core";
    private static final String RUNE_STONE = "rune_stone";
    private static final String RIFT_ANCHOR = "rift_anchor";

    private GameTests() {
    }

    /**
     * Registers the test functions on the mod event bus. Called once from the mod constructor.
     *
     * @param modEventBus the mod's event bus
     */
    public static void register(IEventBus modEventBus) {
        FUNCTIONS.register(modEventBus);
        FUNCTIONS.register("altar_ring", () -> GameTests::altarRing);
        FUNCTIONS.register("ward_haven", () -> GameTests::wardHaven);
        FUNCTIONS.register("gate_links", () -> GameTests::gateLinks);
        FUNCTIONS.register("endgame_seal", () -> GameTests::endgameSeal);
    }

    /**
     * The altar pattern (design/05): a core ringed by marks at radius 2–3 is valid; the same marks
     * hugging the core are a heap, not a ring, and are refused.
     */
    private static void altarRing(GameTestHelper helper) {
        Block core = block(helper, ALTAR_CORE);
        Block rune = block(helper, RUNE_STONE);
        BlockPos corePos = new BlockPos(6, 2, 6);
        helper.setBlock(corePos, core);
        // Eight marks on the circle: the four axis points at radius 2 and the four diagonals at
        // (2, 2), all outside RitualAltar's "too close" radius and inside the scan radius 3.
        int[][] ring = {
                {2, 0}, {-2, 0}, {0, 2}, {0, -2},
                {2, 2}, {2, -2}, {-2, 2}, {-2, -2},
        };
        for (int[] offset : ring) {
            helper.setBlock(corePos.offset(offset[0], 0, offset[1]), rune);
        }
        helper.assertTrue(RitualAltar.valid(helper.getLevel(), helper.absolutePos(corePos), 3, 8),
                "eight marks surrounding the core must form a valid altar");

        for (int[] offset : ring) {
            helper.setBlock(corePos.offset(offset[0], 0, offset[1]), Blocks.AIR);
        }
        // A pile hugging the core: eight marks, all within one block of it. They are skipped by
        // the distance filter, so the pattern has no marks and no angular coverage.
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx != 0 || dz != 0) {
                    helper.setBlock(corePos.offset(dx, 0, dz), rune);
                }
            }
        }
        helper.assertFalse(RitualAltar.valid(helper.getLevel(), helper.absolutePos(corePos), 3, 8),
                "a heap of marks hugging the core must not count as a ring");
        helper.succeed();
    }

    /**
     * The ward haven (design/26): a raised ward covers a point inside its radius and not a far one,
     * and a ward whose duration has lapsed is pruned on the next read.
     */
    private static void wardHaven(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        WardLedger ledger = WardLedger.of(level);
        BlockPos centre = helper.absolutePos(new BlockPos(6, 2, 6));
        BlockPos far = centre.offset(40, 0, 40);

        ledger.raise(level, centre, 4, 200);
        helper.assertTrue(ledger.wardAt(level, centre.offset(3, 0, 0)) != null,
                "a point inside the ward radius must be sheltered");
        helper.assertTrue(ledger.wardAt(level, far) == null,
                "a point far outside the ward radius must not be sheltered");

        ledger.raise(level, far, 2, 1);
        helper.assertTrue(ledger.wardAt(level, far) != null,
                "a freshly raised ward must cover its own centre");

        helper.runAtTickTime(helper.getTick() + 3, () -> {
            helper.assertTrue(ledger.wardAt(level, far) == null,
                    "an expired ward must stop covering");
            helper.assertFalse(ledger.wards().stream().anyMatch(ward -> ward.pos().equals(far)),
                    "an expired ward must be pruned from the ledger");
            helper.assertTrue(ledger.wardAt(level, centre) != null,
                    "pruning the short ward must not remove the long one");
            helper.succeed();
        });
    }

    /**
     * Rift-gate links (design/26): {@code add} links both directions, the pair is canonical (adding
     * twice is one link), and {@code remove} drops both directions. The arrival-spot search is pure
     * arithmetic and already covered by {@code GateMathTest}.
     */
    private static void gateLinks(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        GateLinks links = GateLinks.of(level);
        BlockPos a = helper.absolutePos(new BlockPos(2, 2, 2));
        BlockPos b = helper.absolutePos(new BlockPos(9, 2, 2));
        BlockPos c = helper.absolutePos(new BlockPos(2, 2, 9));
        links.remove(a);
        links.remove(b);
        links.remove(c);

        links.add(a, b);
        helper.assertTrue(b.equals(links.linked(a)), "a must link to b");
        helper.assertTrue(a.equals(links.linked(b)), "b must link back to a");
        helper.assertTrue(links.linked(c) == null, "an unrelated gate must stay unlinked");

        links.add(a, b);
        helper.assertTrue(links.linkCount() == 1, "re-adding the same pair must not duplicate it");

        helper.assertTrue(links.remove(a) == 1, "removing a gate must drop exactly one pair");
        helper.assertTrue(links.linked(a) == null, "the removed gate must be unlinked");
        helper.assertTrue(links.linked(b) == null, "the twin must be unlinked too");

        links.add(a, b);
        links.add(a, c);
        helper.assertTrue(c.equals(links.linked(a)), "a gate may have only one twin: the newest link wins");
        helper.assertTrue(links.linked(b) == null, "the old twin is retired by relinking");
        helper.assertTrue(links.linkCount() == 1, "relinking must replace, not add, a pair");

        links.remove(a);
        helper.assertTrue(links.linkCount() == 0, "cleanup must leave no link behind");
        helper.succeed();
    }

    /**
     * The seal (design/11, third outcome): while a tracked rift is open {@code trySeal} refuses; once
     * the ledger is empty (but was once used) and the outcome is undecided, it seals the world. The
     * state is reset afterwards so the test can be retried.
     */
    private static void endgameSeal(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Block rift = block(helper, RIFT_ANCHOR);
        RiftLedger ledger = RiftLedger.of(level);
        EndgameState state = EndgameState.of(level);
        BlockPos local = new BlockPos(9, 2, 9);
        BlockPos riftPos = helper.absolutePos(local);

        state.setOutcome(EndgameState.Outcome.NONE);
        ledger.remove(riftPos);
        helper.setBlock(local, rift);
        ledger.add(riftPos);
        helper.assertTrue(level.getBlockState(riftPos).is(rift),
                "the rift anchor must stand, found " + level.getBlockState(riftPos));
        helper.assertTrue(ledger.openCount() == 1, "the ledger must record the open rift");
        AncientOne horror = helper.spawn(ModEntities.THE_HORROR.get(), new BlockPos(5, 2, 5));

        helper.assertFalse(Endgame.trySeal(level, horror),
                "an open rift must refuse the seal");
        helper.assertTrue(state.outcome() == EndgameState.Outcome.NONE,
                "a refused seal must not record an outcome");
        helper.assertTrue(ledger.openCount() == 1, "the open rift must stay tracked");

        helper.setBlock(local, Blocks.AIR);
        ledger.remove(riftPos);
        helper.assertTrue(ledger.wasEverOpen() && ledger.openCount() == 0,
                "the ledger must remember it once held a rift");
        helper.assertTrue(Endgame.trySeal(level, horror),
                "an emptied, previously used ledger must seal the world");
        helper.assertTrue(state.outcome() == EndgameState.Outcome.SEALED,
                "a successful seal must record SEALED");
        helper.assertFalse(Endgame.trySeal(level, horror),
                "a decided world must not seal twice");

        state.setOutcome(EndgameState.Outcome.NONE);
        helper.succeed();
    }

    /** Resolves a block by registry id, failing the test instead of throwing an NPE. */
    private static Block block(GameTestHelper helper, String id) {
        Block block = BuiltInRegistries.BLOCK.getValue(EldritchHorror.id(id));
        helper.assertTrue(block != null && block != Blocks.AIR, "missing block: " + id);
        return Objects.requireNonNull(block);
    }
}
