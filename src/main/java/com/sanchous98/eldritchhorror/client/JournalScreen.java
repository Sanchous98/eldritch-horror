package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.quest.Quest;
import com.sanchous98.eldritchhorror.registry.ModAttachments;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jspecify.annotations.Nullable;

/**
 * The quest journal (design/13): the acts in stage order, each with a {@code [x]}/{@code [ ]} mark,
 * name, progress counter and one-line description, the unmet prerequisite as "after …" and the
 * first unfinished quest repeated as "Next: …".
 *
 * <p>Client-only and read-only. Definitions come from the server-synced {@link ClientQuests}
 * mirror; progress is read each frame from the owner-synced {@code QUESTS} attachment, so a quest
 * that completes while the journal is open updates immediately. Closed with Esc or the journal key.
 */
public final class JournalScreen extends Screen {

    private static final int ROW_HEIGHT = 22;
    private static final int MARGIN_X = 40;
    private static final int TOP = 32;
    private static final int BOTTOM_MARGIN = 28;

    private static final int TITLE_COLOUR = 0xFFE0D0A0;
    private static final int NEXT_COLOUR = 0xFFB0A0D0;
    private static final int DONE_COLOUR = 0xFF7FBF6A;
    private static final int TODO_COLOUR = 0xFFE8C86A;
    private static final int DESC_COLOUR = 0xFF9A9AA2;

    private double scroll;

    public JournalScreen() {
        super(Component.translatable("screen.eldritch_horror.journal"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        this.scroll = 0.0;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        // The key that opened the journal closes it again (Esc is handled by the base class).
        if (MapKeyMappings.openJournal.matches(event)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        this.scroll = Math.clamp(this.scroll - scrollY * ROW_HEIGHT, 0.0, maxScroll());
        return true;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        // The engine draws the background before this method; do not call extractBackground again.
        LocalPlayer player = this.minecraft.player;
        Map<String, Integer> progress = player == null
                ? Map.of()
                : player.getData(ModAttachments.QUESTS.get());
        List<Quest> quests = ClientQuests.all();

        g.centeredText(this.font, this.title, this.width / 2, 12, TITLE_COLOUR);
        if (quests.isEmpty()) {
            g.text(this.font, Component.translatable("journal.eldritch_horror.empty"),
                    MARGIN_X, TOP, DESC_COLOUR);
            return;
        }

        int x = MARGIN_X;
        int w = this.width - MARGIN_X * 2;
        int bottom = this.height - BOTTOM_MARGIN;

        g.enableScissor(x, TOP, x + w, bottom);
        int y = TOP - (int) this.scroll;
        Quest next = null;
        for (Quest quest : quests) {
            boolean done = completed(quest, progress);
            if (!done && next == null) {
                next = quest;
            }
            drawQuest(g, quest, progress, x, w, y, done);
            y += ROW_HEIGHT;
        }
        g.disableScissor();

        if (next != null) {
            g.text(this.font, Component.translatable("journal.eldritch_horror.next", next.name()),
                    x, this.height - 18, NEXT_COLOUR);
        }
    }

    /** One journal entry: the marked name line with its counter, plus the description below. */
    private void drawQuest(GuiGraphicsExtractor g, Quest quest, Map<String, Integer> progress,
                           int x, int w, int y, boolean done) {
        int colour = done ? DONE_COLOUR : TODO_COLOUR;
        g.text(this.font, Component.literal(done ? "[x] " : "[ ] ").append(quest.name()), x, y, colour);

        int current = Math.min(progress.getOrDefault(quest.id(), 0), quest.target());
        Component counter = Component.literal(current + "/" + quest.target());
        g.text(this.font, counter, x + w - this.font.width(counter), y, colour);

        MutableComponent line = quest.description().copy();
        Component after = unmetRequirement(quest, progress);
        if (after != null) {
            line.append(" (").append(after).append(")");
        }
        g.text(this.font, line, x + 14, y + 11, DESC_COLOUR);
    }

    /** @return whether {@code quest} has reached its target in the synced progress map. */
    private static boolean completed(Quest quest, Map<String, Integer> progress) {
        return progress.getOrDefault(quest.id(), 0) >= quest.target();
    }

    /**
     * The "after …" text for a quest whose prerequisite is not finished yet, or {@code null} when
     * there is no prerequisite or it is already complete. An unknown prerequisite id (the client
     * has not been synced for it) counts as unmet and is shown by id.
     */
    private static @Nullable Component unmetRequirement(Quest quest, Map<String, Integer> progress) {
        if (quest.requires().isEmpty()) {
            return null;
        }
        Quest prerequisite = ClientQuests.byId(quest.requires());
        if (prerequisite != null && completed(prerequisite, progress)) {
            return null;
        }
        Component name = prerequisite == null
                ? Component.literal(quest.requires())
                : prerequisite.name();
        return Component.translatable("journal.eldritch_horror.after", name);
    }

    private double maxScroll() {
        double content = ClientQuests.all().size() * (double) ROW_HEIGHT;
        double visible = this.height - BOTTOM_MARGIN - TOP;
        return Math.max(0.0, content - visible);
    }
}
