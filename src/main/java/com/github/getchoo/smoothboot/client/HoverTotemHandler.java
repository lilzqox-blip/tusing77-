package com.github.getchoo.smoothboot.client;

import com.github.getchoo.smoothboot.mixin.client.HandledScreenAccessor;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public final class HoverTotemHandler {
    private static final int OFFHAND_SLOT = 40;
    private static final int HOTBAR_TARGET = 0; // Hotbar slot 1 (0-based)
    private static final int DELAY_TICKS = 4;
    private static final boolean ENABLE_OFFHAND_SWAP = true;
    private static final boolean ENABLE_HOTBAR_SWAP = false;

    private static final KeyBinding TOGGLE_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
        "key.threadtweak.hover_totem", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_J,
        KeyBinding.Category.create(Identifier.of("threadtweak", "main"))
    ));

    private static boolean enabled = true;
    private static int timer = 0;

    private HoverTotemHandler() {}

    public static boolean isEnabled() { return enabled; }
    public static void setEnabled(boolean value) { enabled = value; }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(HoverTotemHandler::onTick);
    }

    private static void onTick(MinecraftClient mc) {
        if (mc.player == null || mc.interactionManager == null) return;
        while (TOGGLE_KEY.wasPressed()) {
            enabled = !enabled;
            mc.player.sendMessage(Text.literal("HoverTotem: " + (enabled ? "ON" : "OFF")), true);
        }
        if (!enabled) return;
        if (timer > 0) { timer--; return; }
        if (!(mc.currentScreen instanceof HandledScreen<?> screen)) return;

        Slot slot = ((HandledScreenAccessor) screen).threadtweak$getFocusedSlot();
        if (slot == null || !slot.getStack().isOf(Items.TOTEM_OF_UNDYING)) return;
        if (slot.inventory == mc.player.getInventory() && slot.getIndex() == OFFHAND_SLOT) return;

        int syncId = screen.getScreenHandler().syncId;
        if (ENABLE_OFFHAND_SWAP && !mc.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING)) {
            mc.interactionManager.clickSlot(syncId, slot.id, OFFHAND_SLOT, SlotActionType.SWAP, mc.player);
            timer = DELAY_TICKS;
            return;
        }
        if (ENABLE_HOTBAR_SWAP && !mc.player.getInventory().getStack(HOTBAR_TARGET).isOf(Items.TOTEM_OF_UNDYING)) {
            mc.interactionManager.clickSlot(syncId, slot.id, HOTBAR_TARGET, SlotActionType.SWAP, mc.player);
            timer = DELAY_TICKS;
        }
    }
}
