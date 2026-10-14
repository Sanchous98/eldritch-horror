package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.codex.CodexCategory;
import com.sanchous98.eldritchhorror.codex.CodexEntry;
import com.sanchous98.eldritchhorror.codex.CodexRegistry;
import com.sanchous98.eldritchhorror.registry.ModAttachments;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

/**
 * The codex (design/22): a category sidebar with discovered/total counts and a scrollable list of
 * that category's entries. A discovered entry shows its name; an undiscovered one is hidden as
 * {@code ???}, so the screen rewards discovery without spoiling what is left.
 *
 * <p>Client-only and read-only. The entry set is rebuilt from {@link CodexRegistry}, which derives
 * every entry from the content registries on both sides; discovery is read each frame from the
 * owner-synced {@code LORE} attachment, with rite entries derived from {@code RITE_KNOWLEDGE}
 * exactly as {@code CodexAPI} does server-side. Closed with Esc or the codex key.
 */
public final class CodexScreen extends Screen {

    private static final int SIDEBAR_X = 30;
    private static final int SIDEBAR_WIDTH = 150;
    private static final int SIDEBAR_ROW = 18;
    private static final int LIST_ROW = 12;
    private static final int TOP = 32;
    private static final int BOTTOM_MARGIN = 24;

    private static final int TITLE_COLOUR = 0xFFE0D0A0;
    private static final int SELECTED_COLOUR = 0xFFB0A0D0;
    private static final int NORMAL_COLOUR = 0xFFB0B0B8;
    private static final int COUNT_COLOUR = 0xFF909098;
    private static final int UNKNOWN_COLOUR = 0xFF606068;
    private static final int HOVER_FILL = 0x30FFFFFF;
    private static final int SELECTED_FILL = 0x406A5AC0;

    private CodexCategory selected = CodexCategory.SITE;
    private List<CodexEntry> entries = List.of();
    private double scroll;

    public CodexScreen() {
        super(Component.translatable("screen.eldritch_horror.codex"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        this.scroll = 0.0;
        rebuildEntries();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (MapKeyMappings.openCodex.matches(event)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            int y = TOP;
            for (CodexCategory category : CodexCategory.ordered()) {
                if (inSidebarRow(event.x(), event.y(), y)) {
                    select(category);
                    return true;
                }
                y += SIDEBAR_ROW;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        this.scroll = Math.clamp(this.scroll - scrollY * LIST_ROW, 0.0, maxScroll());
        return true;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        // The engine draws the background before this method; do not call extractBackground again.
        LocalPlayer player = this.minecraft.player;
        Set<String> lore = player == null ? Set.of() : player.getData(ModAttachments.LORE.get());
        Set<String> rites = player == null
                ? Set.of()
                : player.getData(ModAttachments.RITE_KNOWLEDGE.get());

        g.centeredText(this.font, this.title, this.width / 2, 12, TITLE_COLOUR);
        drawSidebar(g, mouseX, mouseY, lore, rites);
        drawEntries(g, lore, rites);
    }

    /** The category column: name on the left, {@code discovered/total} on the right. */
    private void drawSidebar(GuiGraphicsExtractor g, int mouseX, int mouseY,
                             Set<String> lore, Set<String> rites) {
        int y = TOP;
        for (CodexCategory category : CodexCategory.ordered()) {
            boolean isSelected = category == this.selected;
            if (isSelected) {
                g.fill(SIDEBAR_X - 4, y - 3, SIDEBAR_X + SIDEBAR_WIDTH, y + SIDEBAR_ROW - 7,
                        SELECTED_FILL);
            } else if (inSidebarRow(mouseX, mouseY, y)) {
                g.fill(SIDEBAR_X - 4, y - 3, SIDEBAR_X + SIDEBAR_WIDTH, y + SIDEBAR_ROW - 7,
                        HOVER_FILL);
            }
            g.text(this.font, Component.translatable(
                    "codex.eldritch_horror.category." + category.id()), SIDEBAR_X, y,
                    isSelected ? SELECTED_COLOUR : NORMAL_COLOUR);
            Component count = Component.translatable("codex.eldritch_horror.count",
                    knownIn(category, lore, rites), CodexRegistry.idsIn(category).size());
            g.text(this.font, count, SIDEBAR_X + SIDEBAR_WIDTH - this.font.width(count), y,
                    COUNT_COLOUR);
            y += SIDEBAR_ROW;
        }
    }

    /** The scrollable entry list for the selected category. */
    private void drawEntries(GuiGraphicsExtractor g, Set<String> lore, Set<String> rites) {
        int x = listX();
        int w = this.width - x - 30;
        int bottom = this.height - BOTTOM_MARGIN;
        if (this.entries.isEmpty()) {
            g.text(this.font, Component.translatable("codex.eldritch_horror.empty"), x, TOP,
                    COUNT_COLOUR);
            return;
        }
        g.enableScissor(x, TOP, x + w, bottom);
        int y = TOP - (int) this.scroll;
        for (CodexEntry entry : this.entries) {
            boolean discovered = discovered(entry, lore, rites);
            Component label = discovered
                    ? entry.name()
                    : Component.translatable("codex.eldritch_horror.unknown");
            g.text(this.font, label, x, y, discovered ? TITLE_COLOUR : UNKNOWN_COLOUR);
            y += LIST_ROW;
        }
        g.disableScissor();
    }

    /** Rebuilds the visible entry list for the current selection, preserving registry order. */
    private void rebuildEntries() {
        List<CodexEntry> out = new ArrayList<>();
        for (String id : CodexRegistry.idsIn(this.selected)) {
            CodexEntry entry = CodexRegistry.byId(id);
            if (entry != null) {
                out.add(entry);
            }
        }
        this.entries = List.copyOf(out);
        this.scroll = 0.0;
    }

    private void select(CodexCategory category) {
        if (category != this.selected) {
            this.selected = category;
            rebuildEntries();
        }
    }

    /** @return whether an entry is discovered: rites live in {@code RITE_KNOWLEDGE}, all else in {@code LORE}. */
    private static boolean discovered(CodexEntry entry, Set<String> lore, Set<String> rites) {
        return entry.category() == CodexCategory.RITE
                ? rites.contains(entry.id())
                : lore.contains(entry.id());
    }

    /** @return how many entries of {@code category} the player has discovered. */
    private static int knownIn(CodexCategory category, Set<String> lore, Set<String> rites) {
        int known = 0;
        for (String id : CodexRegistry.idsIn(category)) {
            CodexEntry entry = CodexRegistry.byId(id);
            if (entry != null && discovered(entry, lore, rites)) {
                known++;
            }
        }
        return known;
    }

    private static boolean inSidebarRow(double mouseX, double mouseY, int rowY) {
        return mouseX >= SIDEBAR_X - 4 && mouseX < SIDEBAR_X + SIDEBAR_WIDTH
                && mouseY >= rowY - 3 && mouseY < rowY + SIDEBAR_ROW - 7;
    }

    private int listX() {
        return SIDEBAR_X + SIDEBAR_WIDTH + 20;
    }

    private double maxScroll() {
        double content = this.entries.size() * (double) LIST_ROW;
        double visible = this.height - BOTTOM_MARGIN - TOP;
        return Math.max(0.0, content - visible);
    }
}
