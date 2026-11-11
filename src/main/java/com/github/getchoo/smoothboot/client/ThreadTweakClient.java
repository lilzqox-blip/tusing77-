package com.github.getchoo.smoothboot.client;

import net.fabricmc.api.ClientModInitializer;

public final class ThreadTweakClient implements ClientModInitializer {
    @Override public void onInitializeClient() { HoverTotemHandler.init(); }
}
