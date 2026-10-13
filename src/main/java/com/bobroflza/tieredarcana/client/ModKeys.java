package com.bobroflza.tieredarcana.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public final class ModKeys {
    public static final String CATEGORY = "key.categories.tieredarcana";

    public static final KeyMapping OPEN_SPELLBOOK = new KeyMapping("key.tieredarcana.open_spellbook",
            KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, CATEGORY);
    public static final KeyMapping NEXT_SPELL = new KeyMapping("key.tieredarcana.next_spell",
            KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, CATEGORY);
    public static final KeyMapping PREV_SPELL = new KeyMapping("key.tieredarcana.prev_spell",
            KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, CATEGORY);

    private ModKeys() {}
}
