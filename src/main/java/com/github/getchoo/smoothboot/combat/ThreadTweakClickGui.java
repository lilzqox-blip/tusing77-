package com.github.getchoo.smoothboot.combat;

import com.github.getchoo.smoothboot.client.HoverTotemHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** Compact dark-purple module ClickGUI, opened with Right Shift. */
public final class ThreadTweakClickGui extends Screen {
    private static final int PANEL_WIDTH = 250;
    private static final int PANEL_HEIGHT = 218;
    private static final int ACCENT = 0xFF9B70D6;
    private static final int PANEL = 0xF0181720;
    private static final int ROW = 0xFF292633;

    public ThreadTweakClickGui() { super(Text.literal("TUSING-style ClickGUI")); }

    @Override
    protected void init() {
        int left = (width - PANEL_WIDTH) / 2;
        int top = (height - PANEL_HEIGHT) / 2;
        int x = left + 14;
        int w = PANEL_WIDTH - 28;
        int y = top + 48;
        int h = 27;
        addDrawableChild(toggleButton(x, y, w, h, "Anchor Macro", CombatFeaturesClient.isAnchorMacroEnabled(),
                value -> CombatFeaturesClient.setAnchorMacroEnabled(value)));
        y += 35;
        addDrawableChild(toggleButton(x, y, w, h, "Double Anchor", CombatFeaturesClient.isDoubleAnchorEnabled(),
                value -> CombatFeaturesClient.setDoubleAnchorEnabled(value)));
        y += 35;
        addDrawableChild(toggleButton(x, y, w, h, "Double Anchor Macro", CombatFeaturesClient.isDoubleAnchorMacroEnabled(),
                value -> CombatFeaturesClient.setDoubleAnchorMacroEnabled(value)));
        y += 35;
        addDrawableChild(toggleButton(x, y, w, h, "HoverTotem", HoverTotemHandler.isEnabled(),
                HoverTotemHandler::setEnabled));
    }

    private ButtonWidget toggleButton(int x, int y, int w, int h, String name, boolean enabled,
                                      java.util.function.Consumer<Boolean> setter) {
        return ButtonWidget.builder(label(name, enabled), button -> {
            boolean next = !setterState(name);
            setter.accept(next);
            button.setMessage(label(name, next));
        }).dimensions(x, y, w, h).build();
    }

    private boolean setterState(String name) {
        return switch (name) {
            case "Anchor Macro" -> CombatFeaturesClient.isAnchorMacroEnabled();
            case "Double Anchor" -> CombatFeaturesClient.isDoubleAnchorEnabled();
            case "Double Anchor Macro" -> CombatFeaturesClient.isDoubleAnchorMacroEnabled();
            default -> HoverTotemHandler.isEnabled();
        };
    }

    private Text label(String name, boolean enabled) {
        return Text.literal(name + "  [" + (enabled ? "ON" : "OFF") + "]");
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        int left = (width - PANEL_WIDTH) / 2;
        int top = (height - PANEL_HEIGHT) / 2;
        context.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, PANEL);
        context.fill(left, top, left + PANEL_WIDTH, top + 3, ACCENT);
        context.drawText(textRenderer, "TUSING CLIENT", left + 14, top + 13, 0xFFFFFFFF, false);
        context.drawText(textRenderer, "MODULES", left + 14, top + 32, 0xFFB9B3C5, false);
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, "Right Shift: close", width / 2, top + PANEL_HEIGHT - 17, 0xFFAAA4B5);
    }

    @Override public boolean shouldPause() { return false; }
}
