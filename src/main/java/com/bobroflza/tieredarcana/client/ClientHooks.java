package com.bobroflza.tieredarcana.client;

import com.bobroflza.tieredarcana.client.screen.SpellbookScreen;
import net.minecraft.client.Minecraft;

/** Client-only entry points called from common code via DistExecutor. */
public final class ClientHooks {
    private ClientHooks() {}

    public static void openSpellbook() {
        Minecraft.getInstance().setScreen(new SpellbookScreen());
    }
}
