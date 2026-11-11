package com.github.getchoo.smoothboot.combat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** User-editable settings for the optional client-side combat helpers. */
public final class CombatConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("threadtweak-combat.json");

    public boolean anchorMacroEnabled = false;
    public int switchDelay = 0;
    public int glowstoneDelay = 0;
    public int explodeDelay = 0;
    public int totemSlot = 1;
    public boolean swapBackToAnchor = true;
    public boolean anchorPearl = false;
    public boolean pearlAimDown = true;
    public int pearlPitch = 80;
    public int pearlCooldownSeconds = 10;

    public boolean doubleAnchorEnabled = false;
    public float doubleAnchorChance = 100.0F;
    public int doubleAnchorDelayMs = 200;
    public boolean doubleAnchorGlowstone = true;
    public int doubleAnchorGlowstoneDelayMs = 0;

    public boolean doubleAnchorMacroEnabled = false;
    public int doubleAnchorMacroDelayTicks = 2;
    public boolean doubleAnchorAutoSwap = true;
    public int doubleAnchorExplodeSlot = 1;
    public boolean doubleAnchorSwapBack = true;
    public boolean doubleAnchorDamageTick = false;
    public float doubleAnchorDamageTickRange = 4.0F;
    public int doubleAnchorDamageTickMaxWaitMs = 250;

    public static CombatConfig load() {
        try {
            Files.createDirectories(PATH.getParent());
            if (Files.notExists(PATH)) {
                CombatConfig fresh = new CombatConfig();
                fresh.save();
                return fresh;
            }
            CombatConfig config = GSON.fromJson(Files.readString(PATH), CombatConfig.class);
            if (config == null) config = new CombatConfig();
            config.validate();
            config.save();
            return config;
        } catch (Exception exception) {
            return new CombatConfig();
        }
    }

    public void save() {
        try {
            Files.createDirectories(PATH.getParent());
            Files.writeString(PATH, GSON.toJson(this));
        } catch (IOException ignored) {
        }
    }

    private void validate() {
        switchDelay = clamp(switchDelay, 0, 20);
        glowstoneDelay = clamp(glowstoneDelay, 0, 20);
        explodeDelay = clamp(explodeDelay, 0, 20);
        totemSlot = clamp(totemSlot, 1, 9);
        pearlPitch = clamp(pearlPitch, 0, 90);
        pearlCooldownSeconds = clamp(pearlCooldownSeconds, 0, 60);
        doubleAnchorChance = Math.max(0, Math.min(100, doubleAnchorChance));
        doubleAnchorDelayMs = clamp(doubleAnchorDelayMs, 0, 2000);
        doubleAnchorGlowstoneDelayMs = clamp(doubleAnchorGlowstoneDelayMs, 0, 2000);
        doubleAnchorMacroDelayTicks = clamp(doubleAnchorMacroDelayTicks, 0, 20);
        doubleAnchorExplodeSlot = clamp(doubleAnchorExplodeSlot, 1, 9);
        doubleAnchorDamageTickRange = Math.max(0, Math.min(10, doubleAnchorDamageTickRange));
        doubleAnchorDamageTickMaxWaitMs = clamp(doubleAnchorDamageTickMaxWaitMs, 0, 2000);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
