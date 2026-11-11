package com.github.getchoo.smoothboot.combat;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.ThreadLocalRandom;

/** Fabric-native ports of the supplied AnchorMacro and DoubleAnchor features. */
public final class CombatFeaturesClient implements ClientModInitializer {
    private static final MinecraftClient MC = MinecraftClient.getInstance();
    private static CombatConfig config;
    private static KeyBinding anchorToggleKey;
    private static KeyBinding doubleAnchorToggleKey;
    private static KeyBinding doubleMacroKey;
    private static KeyBinding doubleMacroToggleKey;
    private static KeyBinding clickGuiKey;
    private static final AnchorMacro ANCHOR_MACRO = new AnchorMacro();
    private static final DoubleAnchor DOUBLE_ANCHOR = new DoubleAnchor();

    @Override
    public void onInitializeClient() {
        config = CombatConfig.load();
        anchorToggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.threadtweak.anchor_macro", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, KeyBinding.Category.MISC));
        doubleAnchorToggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.threadtweak.double_anchor", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_H, KeyBinding.Category.MISC));
        doubleMacroKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.threadtweak.double_anchor_macro", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, KeyBinding.Category.MISC));
        doubleMacroToggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.threadtweak.double_anchor_macro_toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_B, KeyBinding.Category.MISC));
        clickGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.threadtweak.click_gui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, KeyBinding.Category.MISC));
        ClientTickEvents.END_CLIENT_TICK.register(client -> tick(client));
    }

    private static void tick(MinecraftClient client) {
        while (clickGuiKey.wasPressed()) {
            if (client.currentScreen == null) client.setScreen(new ThreadTweakClickGui());
            else if (client.currentScreen instanceof ThreadTweakClickGui) client.setScreen(null);
        }
        while (anchorToggleKey.wasPressed()) {
            config.anchorMacroEnabled = !config.anchorMacroEnabled;
            config.save();
            ANCHOR_MACRO.reset();
            message("Anchor Macro " + (config.anchorMacroEnabled ? "ON" : "OFF"));
        }
        while (doubleAnchorToggleKey.wasPressed()) {
            config.doubleAnchorEnabled = !config.doubleAnchorEnabled;
            config.save();
            DOUBLE_ANCHOR.reset();
            message("Double Anchor " + (config.doubleAnchorEnabled ? "ON" : "OFF"));
        }
        while (doubleMacroToggleKey.wasPressed()) {
            config.doubleAnchorMacroEnabled = !config.doubleAnchorMacroEnabled;
            config.save();
            DOUBLE_ANCHOR.reset();
            message("Double Anchor Macro " + (config.doubleAnchorMacroEnabled ? "ON" : "OFF"));
        }
        while (doubleMacroKey.wasPressed()) {
            if (config.doubleAnchorMacroEnabled) DOUBLE_ANCHOR.startMacro();
            else message("Double Anchor Macro is OFF (press B)");
        }
        if (client.player == null || client.world == null || client.interactionManager == null) {
            ANCHOR_MACRO.reset();
            DOUBLE_ANCHOR.resetTransient();
            return;
        }
        ANCHOR_MACRO.tick();
        DOUBLE_ANCHOR.tick();
    }

    public static boolean isAnchorMacroEnabled() { return config != null && config.anchorMacroEnabled; }
    public static boolean isDoubleAnchorEnabled() { return config != null && config.doubleAnchorEnabled; }
    public static boolean isDoubleAnchorMacroEnabled() { return config != null && config.doubleAnchorMacroEnabled; }

    public static void setAnchorMacroEnabled(boolean enabled) {
        if (config == null) return;
        config.anchorMacroEnabled = enabled; config.save(); ANCHOR_MACRO.reset();
    }
    public static void setDoubleAnchorEnabled(boolean enabled) {
        if (config == null) return;
        config.doubleAnchorEnabled = enabled; config.save(); DOUBLE_ANCHOR.reset();
    }
    public static void setDoubleAnchorMacroEnabled(boolean enabled) {
        if (config == null) return;
        config.doubleAnchorMacroEnabled = enabled; config.save(); DOUBLE_ANCHOR.reset();
    }

    private static void message(String value) {
        if (MC.player != null) MC.player.sendMessage(Text.literal("§7[ThreadTweak]§r " + value), false);
    }

    private static boolean rightMouseDown() {
        return MC.getWindow() != null && GLFW.glfwGetMouseButton(MC.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
    }

    private static int findHotbar(Item item) {
        if (MC.player == null) return -1;
        for (int i = 0; i < 9; i++) if (MC.player.getInventory().getStack(i).isOf(item)) return i;
        return -1;
    }

    private static BlockHitResult hitAnchor(BlockPos pos) {
        if (MC.player == null || MC.world == null) return null;
        Vec3d eye = MC.player.getEyePos();
        double range = Math.min(4.5, MC.player.getBlockInteractionRange());
        Vec3d end = eye.add(MC.player.getRotationVec(1.0F).multiply(range));
        var result = MC.world.raycast(new net.minecraft.world.RaycastContext(
                eye, end, net.minecraft.world.RaycastContext.ShapeType.OUTLINE,
                net.minecraft.world.RaycastContext.FluidHandling.NONE, MC.player));
        if (result instanceof BlockHitResult blockHit && blockHit.getType() == HitResult.Type.BLOCK
                && blockHit.getBlockPos().equals(pos)) return blockHit;
        return null;
    }

    private static boolean interact(BlockPos pos) {
        BlockHitResult hit = hitAnchor(pos);
        if (hit == null || MC.player == null || MC.interactionManager == null) return false;
        MC.interactionManager.interactBlock(MC.player, Hand.MAIN_HAND, hit);
        MC.player.swingHand(Hand.MAIN_HAND);
        return true;
    }

    private static final class AnchorMacro {
        private BlockPos trackedPos;
        private int previousCharge = -1;
        private int unchangedTicks;
        private int switchTicks;
        private int glowTicks;
        private int explodeTicks;
        private int stage;
        private int savedSlot = -1;
        private BlockPos refusedPos;
        private long lastPearlMs;
        private boolean pearlPending;
        private boolean swapBackPending;

        void reset() {
            trackedPos = null;
            previousCharge = -1;
            unchangedTicks = switchTicks = glowTicks = explodeTicks = stage = 0;
            savedSlot = -1;
            refusedPos = null;
            pearlPending = swapBackPending = false;
        }

        void tick() {
            if (!config.anchorMacroEnabled || MC.currentScreen != null || !rightMouseDown()) {
                if (!rightMouseDown()) { clearTracking(); refusedPos = null; }
                tickPearl();
                swapBack();
                return;
            }
            if (MC.player.isUsingItem()) return;
            if (!(MC.crosshairTarget instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK
                    || !MC.world.getBlockState(hit.getBlockPos()).isOf(Blocks.RESPAWN_ANCHOR)) {
                clearTracking();
                return;
            }
            BlockPos pos = hit.getBlockPos();
            if (pos.equals(refusedPos)) return;
            BlockState state = MC.world.getBlockState(pos);
            int charges = state.get(RespawnAnchorBlock.CHARGES);
            MC.options.useKey.setPressed(false);
            if (trackedPos != null && trackedPos.equals(pos) && previousCharge == charges && unchangedTicks < 10) {
                unchangedTicks++;
                return;
            }
            if (trackedPos != null && trackedPos.equals(pos) && previousCharge == 0 && charges == 0) {
                if (++unchangedTicks >= 10) {
                    message("Server did not confirm anchor charge; release right-click to retry");
                    refusedPos = pos.toImmutable();
                    clearTracking();
                    return;
                }
            }
            if (charges == 0) {
                if (!holding(Items.GLOWSTONE)) {
                    if (switchTicks++ < config.switchDelay) return;
                    switchTicks = 0;
                    int slot = findHotbar(Items.GLOWSTONE);
                    if (slot < 0) return;
                    MC.player.getInventory().setSelectedSlot(slot);
                    return;
                }
                if (glowTicks++ < config.glowstoneDelay) return;
                glowTicks = 0;
                if (interact(pos)) track(pos, 0);
                return;
            }
            int totemSlot = config.totemSlot - 1;
            if (MC.player.getInventory().getSelectedSlot() != totemSlot) {
                if (switchTicks++ < config.switchDelay) return;
                switchTicks = 0;
                MC.player.getInventory().setSelectedSlot(totemSlot);
                return;
            }
            if (explodeTicks++ < config.explodeDelay) return;
            explodeTicks = 0;
            if (interact(pos)) {
                track(pos, charges);
                swapBackPending = true;
                startPearl();
            }
        }

        private boolean holding(Item item) {
            return MC.player.getMainHandStack().isOf(item) || MC.player.getOffHandStack().isOf(item);
        }
        private void track(BlockPos pos, int charge) { trackedPos = pos.toImmutable(); previousCharge = charge; unchangedTicks = 0; }
        private void clearTracking() { trackedPos = null; previousCharge = -1; unchangedTicks = 0; switchTicks = glowTicks = explodeTicks = 0; }

        private void startPearl() {
            if (!config.anchorPearl || System.currentTimeMillis() - lastPearlMs < config.pearlCooldownSeconds * 1000L) return;
            int slot = findHotbar(Items.ENDER_PEARL);
            if (slot < 0) return;
            savedSlot = MC.player.getInventory().getSelectedSlot();
            pearlPending = true;
            stage = 1;
        }
        private void tickPearl() {
            if (!pearlPending || MC.player == null || MC.currentScreen != null) return;
            int pearlSlot = findHotbar(Items.ENDER_PEARL);
            if (pearlSlot < 0) { pearlPending = false; stage = 0; return; }
            MC.options.useKey.setPressed(false);
            if (stage == 1) {
                MC.player.getInventory().setSelectedSlot(pearlSlot);
                stage = 2;
            } else if (stage == 2) {
                float oldPitch = MC.player.getPitch();
                if (config.pearlAimDown) MC.player.setPitch(config.pearlPitch);
                MC.interactionManager.interactItem(MC.player, Hand.MAIN_HAND);
                MC.player.setPitch(oldPitch);
                lastPearlMs = System.currentTimeMillis();
                stage = 3;
            } else {
                if (savedSlot >= 0 && savedSlot < 9) MC.player.getInventory().setSelectedSlot(savedSlot);
                pearlPending = false;
                stage = 0;
            }
        }
        private void swapBack() {
            if (!swapBackPending || stage != 0 || MC.currentScreen != null) return;
            swapBackPending = false;
            if (config.swapBackToAnchor) {
                int slot = findHotbar(Items.RESPAWN_ANCHOR);
                if (slot >= 0) MC.player.getInventory().setSelectedSlot(slot);
            }
        }
    }

    private static final class DoubleAnchor {
        private long nextDoubleAt;
        private long glowAt;
        private BlockPos pendingGlowPos;
        private boolean wasRightDown;
        private int macroStage;
        private int macroTicks;
        private BlockPos macroPos;
        private int macroStartSlot = -1;
        private BlockHitResult macroStartHit;
        private long damageWaitStartedAt;

        void reset() { resetTransient(); macroStage = 0; }
        void resetTransient() { pendingGlowPos = null; nextDoubleAt = glowAt = 0; wasRightDown = false; }

        void tick() {
            if (!config.doubleAnchorEnabled && macroStage == 0) return;
            if (MC.currentScreen != null || MC.player == null) return;
            long now = System.currentTimeMillis();
            boolean down = rightMouseDown();
            if (config.doubleAnchorEnabled && down && !wasRightDown && MC.crosshairTarget instanceof BlockHitResult hit
                    && MC.world.getBlockState(hit.getBlockPos()).isOf(Blocks.RESPAWN_ANCHOR)
                    && MC.world.getBlockState(hit.getBlockPos()).get(RespawnAnchorBlock.CHARGES) > 0
                    && MC.player.getMainHandStack().isOf(Items.RESPAWN_ANCHOR)
                    && ThreadLocalRandom.current().nextFloat(100.0F) <= config.doubleAnchorChance) {
                nextDoubleAt = now + config.doubleAnchorDelayMs;
                pendingGlowPos = hit.getBlockPos().toImmutable();
            }
            wasRightDown = down;
            if (pendingGlowPos != null && now >= nextDoubleAt) {
                if (MC.world.getBlockState(pendingGlowPos).isOf(Blocks.RESPAWN_ANCHOR)) interact(pendingGlowPos);
                glowAt = now + config.doubleAnchorGlowstoneDelayMs;
                nextDoubleAt = Long.MAX_VALUE;
            }
            if (pendingGlowPos != null && nextDoubleAt == Long.MAX_VALUE && now >= glowAt) {
                if (config.doubleAnchorGlowstone && MC.world.getBlockState(pendingGlowPos).isOf(Blocks.RESPAWN_ANCHOR)
                        && MC.world.getBlockState(pendingGlowPos).get(RespawnAnchorBlock.CHARGES) == 0) {
                    int slot = findHotbar(Items.GLOWSTONE);
                    if (slot >= 0) {
                        MC.player.getInventory().setSelectedSlot(slot);
                        interact(pendingGlowPos);
                    }
                }
                pendingGlowPos = null;
            }
            tickMacro();
        }

        void startMacro() {
            if (!config.doubleAnchorMacroEnabled || MC.player == null || MC.world == null
                    || !(MC.crosshairTarget instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) return;
            BlockPos clicked = hit.getBlockPos();
            BlockPos place = MC.world.getBlockState(clicked).isReplaceable() ? clicked : clicked.offset(hit.getSide());
            if (MC.player.getBoundingBox().intersects(new Box(place))) { message("Double Anchor macro aborted: player blocks placement"); return; }
            macroPos = place.toImmutable();
            macroStartHit = hit;
            macroStartSlot = MC.player.getInventory().getSelectedSlot();
            macroStage = 1;
            macroTicks = 0;
            message("Double Anchor macro started");
        }

        private void tickMacro() {
            if (macroStage == 0 || MC.player == null || MC.world == null) return;
            if (macroTicks++ < config.doubleAnchorMacroDelayTicks) return;
            macroTicks = 0;
            switch (macroStage) {
                case 1 -> {
                    int slot = findHotbar(Items.RESPAWN_ANCHOR);
                    if (config.doubleAnchorAutoSwap) {
                        if (slot < 0) { abort("no respawn anchor"); return; }
                        MC.player.getInventory().setSelectedSlot(slot);
                    } else if (!MC.player.getMainHandStack().isOf(Items.RESPAWN_ANCHOR)) {
                        abort("hold a respawn anchor (auto swap is OFF)"); return;
                    }
                    MC.interactionManager.interactBlock(MC.player, Hand.MAIN_HAND, macroStartHit);
                    MC.player.swingHand(Hand.MAIN_HAND);
                    macroStage = 2;
                }
                case 2 -> {
                    if (!isAnchor(macroPos)) { abort("anchor missing"); return; }
                    int slot = findHotbar(Items.GLOWSTONE);
                    if (config.doubleAnchorAutoSwap) {
                        if (slot < 0) { abort("no glowstone"); return; }
                        MC.player.getInventory().setSelectedSlot(slot);
                    } else if (!MC.player.getMainHandStack().isOf(Items.GLOWSTONE)) {
                        abort("hold glowstone (auto swap is OFF)"); return;
                    }
                    if (!interactAtTop(macroPos)) { abort("charge failed"); return; }
                    macroStage = 3;
                }
                case 3 -> {
                    if (!isCharged(macroPos)) { abort("anchor not charged"); return; }
                    if (config.doubleAnchorAutoSwap) {
                        MC.player.getInventory().setSelectedSlot(config.doubleAnchorExplodeSlot - 1);
                    } else if (MC.player.getMainHandStack().isOf(Items.GLOWSTONE)) {
                        abort("switch away from glowstone (auto swap is OFF)"); return;
                    }
                    if (!interactAtTop(macroPos)) { abort("explode failed"); return; }
                    int anchorSlot = findHotbar(Items.RESPAWN_ANCHOR);
                    if (config.doubleAnchorAutoSwap) {
                        if (anchorSlot < 0) { abort("no respawn anchor for double-place"); return; }
                        MC.player.getInventory().setSelectedSlot(anchorSlot);
                    } else if (!MC.player.getMainHandStack().isOf(Items.RESPAWN_ANCHOR)) {
                        abort("hold a respawn anchor for double-place"); return;
                    }
                    if (!interactAtTop(macroPos)) { abort("double placement failed"); return; }
                    macroStage = 4;
                }
                case 4 -> {
                    if (!isAnchor(macroPos)) { abort("second anchor missing"); return; }
                    int slot = findHotbar(Items.GLOWSTONE);
                    if (config.doubleAnchorAutoSwap) {
                        if (slot < 0) { abort("no glowstone (2)"); return; }
                        MC.player.getInventory().setSelectedSlot(slot);
                    } else if (!MC.player.getMainHandStack().isOf(Items.GLOWSTONE)) {
                        abort("hold glowstone for second charge"); return;
                    }
                    if (!interactAtTop(macroPos)) { abort("second charge failed"); return; }
                    macroStage = 5;
                    damageWaitStartedAt = System.currentTimeMillis();
                }
                case 5 -> {
                    if (!isCharged(macroPos)) { abort("second anchor not charged"); return; }
                    if (config.doubleAnchorDamageTick && hasNearbyDamageTick()
                            && System.currentTimeMillis() - damageWaitStartedAt < config.doubleAnchorDamageTickMaxWaitMs) return;
                    if (config.doubleAnchorAutoSwap) {
                        MC.player.getInventory().setSelectedSlot(config.doubleAnchorExplodeSlot - 1);
                    } else if (MC.player.getMainHandStack().isOf(Items.GLOWSTONE)) {
                        abort("switch away from glowstone before final interaction"); return;
                    }
                    if (!interactAtTop(macroPos)) { abort("second explosion failed"); return; }
                    finishMacro();
                }
                default -> finishMacro();
            }
        }

        private boolean isAnchor(BlockPos pos) { return MC.world.getBlockState(pos).isOf(Blocks.RESPAWN_ANCHOR); }
        private boolean isCharged(BlockPos pos) { return isAnchor(pos) && MC.world.getBlockState(pos).get(RespawnAnchorBlock.CHARGES) > 0; }
        private boolean interactAtTop(BlockPos pos) {
            if (MC.player == null || MC.interactionManager == null || !isAnchor(pos)) return false;
            Vec3d hitPos = new Vec3d(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
            BlockHitResult hit = new BlockHitResult(hitPos, Direction.UP, pos, false);
            MC.interactionManager.interactBlock(MC.player, Hand.MAIN_HAND, hit);
            MC.player.swingHand(Hand.MAIN_HAND);
            return true;
        }
        private boolean hasNearbyDamageTick() {
            if (MC.player == null || MC.world == null) return false;
            for (var player : MC.world.getPlayers()) {
                if (player != MC.player && !player.isSpectator()
                        && MC.player.distanceTo(player) <= config.doubleAnchorDamageTickRange && player.hurtTime > 0) return true;
            }
            return false;
        }
        private void abort(String reason) { message("Double Anchor macro aborted: " + reason); finishMacro(); }
        private void finishMacro() {
            if (config.doubleAnchorAutoSwap && config.doubleAnchorSwapBack && macroStartSlot >= 0 && macroStartSlot < 9)
                MC.player.getInventory().setSelectedSlot(macroStartSlot);
            macroStage = 0;
            macroPos = null;
            macroStartHit = null;
            macroStartSlot = -1;
            damageWaitStartedAt = 0;
            macroTicks = 0;
            message("Double Anchor macro finished");
        }
    }
}
