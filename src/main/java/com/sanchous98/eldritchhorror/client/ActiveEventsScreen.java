package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.registry.ModAttachments;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

/**
 * The active world-events journal (design/19): one row per running event with its name, the
 * start-line flavour text and the time it has left.
 *
 * <p>Client-only and read-only. Definitions come from {@link Events} (common, lazily initialised);
 * the live set is the owner-synced {@code ACTIVE_EVENTS} attachment, refreshed by the server once
 * a second, so countdowns move without a custom packet. Closed with Esc or the events key.
 */
public final class ActiveEventsScreen extends Screen {

    private static final int MARGIN_X = 40;
    private static final int TOP = 44;
    private static final int ROW_HEIGHT = 26;
    private static final int TITLE_COLOUR = 0xFFE0D0A0;
    private static final int NAME_COLOUR = 0xFFB0A0D0;
    private static final int DESC_COLOUR = 0xFF9A9AA2;
    private static final int TIME_COLOUR = 0xFF7FBF6A;

    public ActiveEventsScreen() {
        super(Component.translatable("screen.eldritch_horror.events"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (MapKeyMappings.openEvents != null && MapKeyMappings.openEvents.matches(event)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        LocalPlayer player = this.minecraft.player;
        List<String> active = player == null
                ? List.of()
                : player.getData(ModAttachments.ACTIVE_EVENTS.get());
        g.centeredText(this.font, this.title, this.width / 2, 12, TITLE_COLOUR);
        if (active.isEmpty()) {
            g.text(this.font, Component.translatable("events.eldritch_horror.empty"),
                    MARGIN_X, TOP, DESC_COLOUR);
            return;
        }
        int y = TOP;
        for (String entry : active) {
            int at = entry.indexOf('@');
            String id = at < 0 ? entry : entry.substring(0, at);
            int remaining = at < 0 ? 0 : seconds(entry.substring(at + 1));
            g.text(this.font, displayName(id), MARGIN_X, y, NAME_COLOUR);
            g.text(this.font,
                    Component.translatableWithFallback("event.eldritch_horror." + id + ".start", id),
                    MARGIN_X, y + 10, DESC_COLOUR);
            Component time = Component.translatable("events.eldritch_horror.remaining", remaining);
            g.text(this.font, time, this.width - MARGIN_X - this.font.width(time), y, TIME_COLOUR);
            y += ROW_HEIGHT;
            if (y > this.height - 24) {
                break;
            }
        }
    }

    /** {@code darkness_pulse} → "Darkness Pulse", unless a translation provides a better name. */
    private static Component displayName(String id) {
        return Component.translatableWithFallback("event.eldritch_horror." + id + ".name",
                humanise(id));
    }

    private static String humanise(String id) {
        StringBuilder out = new StringBuilder(id.length());
        for (String part : id.split("_")) {
            if (part.isEmpty()) {
                continue;
            }
            if (out.length() > 0) {
                out.append(' ');
            }
            out.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return out.toString();
    }

    /** Reads the leading/trailing digits of an {@code "12s"} label; {@code 0} if there are none. */
    private static int seconds(String text) {
        int end = text.length();
        while (end > 0 && !Character.isDigit(text.charAt(end - 1))) {
            end--;
        }
        int start = end;
        while (start > 0 && Character.isDigit(text.charAt(start - 1))) {
            start--;
        }
        if (start == end) {
            return 0;
        }
        try {
            return Integer.parseInt(text.substring(start, end));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }
}
