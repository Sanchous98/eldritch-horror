package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.network.SkillUnlockPayload;
import com.sanchous98.eldritchhorror.registry.ModAttachments;
import com.sanchous98.eldritchhorror.skill.Skill;
import com.sanchous98.eldritchhorror.skill.Skills;
import java.util.Set;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/**
 * The skill tree (design/13): every node with its unlocked mark, name, cost and description, plus
 * the player's unspent points. Selecting an affordable, not-yet-unlocked node asks the server to
 * spend the points ({@link SkillUnlockPayload}); the server re-validates and answers in chat.
 *
 * <p>Client-only. The screen is a pure view over the synced {@code SKILLS}/{@code SKILL_POINTS}
 * attachments — it re-reads them every frame, so an unlock (from this screen or any other source)
 * appears as soon as the server syncs it and no local mirror can drift. Closed with Esc or the
 * skill key.
 */
public final class SkillScreen extends Screen {

    private static final int ROW_HEIGHT = 22;
    private static final int MARGIN_X = 40;
    private static final int TOP = 46;

    private static final int TITLE_COLOUR = 0xFFE0D0A0;
    private static final int POINTS_COLOUR = 0xFFE8C86A;
    private static final int DONE_COLOUR = 0xFF7FBF6A;
    private static final int READY_COLOUR = 0xFFE8C86A;
    private static final int LOCKED_COLOUR = 0xFF707078;
    private static final int DESC_COLOUR = 0xFF9A9AA2;
    private static final int HINT_COLOUR = 0xFF909098;
    private static final int HOVER_FILL = 0x30FFFFFF;

    public SkillScreen() {
        super(Component.translatable("screen.eldritch_horror.skills"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (MapKeyMappings.openSkills.matches(event)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        LocalPlayer player = this.minecraft.player;
        if (event.button() == 0 && player != null) {
            int points = player.getData(ModAttachments.SKILL_POINTS.get());
            Set<String> unlocked = player.getData(ModAttachments.SKILLS.get());
            int y = TOP;
            for (Skill skill : Skills.all()) {
                if (inRow(event.x(), event.y(), y) && canUnlock(skill, unlocked, points)) {
                    ClientPacketDistributor.sendToServer(new SkillUnlockPayload(skill.id()));
                    return true;
                }
                y += ROW_HEIGHT;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        // The engine draws the background before this method; do not call extractBackground again.
        g.centeredText(this.font, this.title, this.width / 2, 12, TITLE_COLOUR);

        LocalPlayer player = this.minecraft.player;
        if (player == null) {
            return;
        }
        int points = player.getData(ModAttachments.SKILL_POINTS.get());
        Set<String> unlocked = player.getData(ModAttachments.SKILLS.get());
        g.text(this.font, Component.translatable("skill.eldritch_horror.points", points),
                MARGIN_X, 28, POINTS_COLOUR);

        int x = MARGIN_X;
        int w = this.width - MARGIN_X * 2;
        int y = TOP;
        for (Skill skill : Skills.all()) {
            drawSkill(g, skill, mouseX, mouseY, x, w, y, unlocked, points);
            y += ROW_HEIGHT;
        }
        g.centeredText(this.font, Component.translatable("skill.eldritch_horror.hint"),
                this.width / 2, this.height - 18, HINT_COLOUR);
    }

    /** One node: the marked name line with its cost, the description, and a hover cue when ready. */
    private void drawSkill(GuiGraphicsExtractor g, Skill skill, int mouseX, int mouseY,
                           int x, int w, int y, Set<String> unlocked, int points) {
        boolean has = unlocked.contains(skill.id());
        boolean ready = canUnlock(skill, unlocked, points);
        if (ready && inRow(mouseX, mouseY, y)) {
            g.fill(x - 4, y - 3, x + w, y + ROW_HEIGHT - 7, HOVER_FILL);
        }
        int colour = has ? DONE_COLOUR : (ready ? READY_COLOUR : LOCKED_COLOUR);
        g.text(this.font, Component.literal(has ? "[x] " : "[ ] ").append(skill.name()), x, y, colour);

        Component cost = Component.translatable("skill.eldritch_horror.cost", skill.cost());
        g.text(this.font, cost, x + w - this.font.width(cost), y, colour);
        g.text(this.font, skill.description(), x + 14, y + 11, DESC_COLOUR);
    }

    /** @return whether a click may spend points on {@code skill} right now. */
    private static boolean canUnlock(Skill skill, Set<String> unlocked, int points) {
        return !unlocked.contains(skill.id()) && points >= skill.cost();
    }

    private boolean inRow(double mouseX, double mouseY, int rowY) {
        return mouseX >= MARGIN_X - 4 && mouseX < this.width - MARGIN_X
                && mouseY >= rowY - 3 && mouseY < rowY + ROW_HEIGHT - 7;
    }
}
