package com.bobroflza.tieredarcana.client;

import com.bobroflza.tieredarcana.item.WandItem;
import com.bobroflza.tieredarcana.spell.Spell;
import com.bobroflza.tieredarcana.spell.SpellRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

/**
 * Vertical mana bar just right of the hotbar, with the active spell icon (and its cooldown) beside it.
 * Only shown once the player is a "mage": they know a spell, or carry a wand/spellbook.
 */
public final class ManaHudOverlay {
    private static final int BAR_W = 6;
    private static final int BAR_H = 36;

    public static final IGuiOverlay HUD = (gui, g, partialTick, width, height) -> {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null || mc.options.hideGui || p.isSpectator()) return;
        boolean holdingWand = p.getMainHandItem().getItem() instanceof WandItem || p.getOffhandItem().getItem() instanceof WandItem;
        if (ClientArcaneData.KNOWN.isEmpty() && !holdingWand && !ClientEvents.hasSpellbook(p)) return;

        int x = width / 2 + 91 + 8;
        int y = height - BAR_H - 3;
        float frac = ClientArcaneData.maxMana <= 0 ? 0 : Math.min(1f, ClientArcaneData.mana / ClientArcaneData.maxMana);
        int filled = Math.round(BAR_H * frac);

        // frame + background
        g.fill(x - 1, y - 1, x + BAR_W + 1, y + BAR_H + 1, 0xFF120A1E);
        g.fill(x, y, x + BAR_W, y + BAR_H, 0xFF2B1B40);
        // fill (bottom-up), cyan to violet gradient
        if (filled > 0) g.fillGradient(x, y + BAR_H - filled, x + BAR_W, y + BAR_H, 0xFF6FF3FF, 0xFF8A4FFF);
        // shimmer line at the top of the fill
        if (filled > 0 && filled < BAR_H) g.fill(x, y + BAR_H - filled, x + BAR_W, y + BAR_H - filled + 1, 0xFFE8FFFF);

        Font font = mc.font;
        String txt = String.valueOf((int) ClientArcaneData.mana);
        g.drawString(font, txt, x + BAR_W / 2 - font.width(txt) / 2, y - 10, 0x9FEFFF, true);

        // active spell icon
        Spell active = SpellRegistry.get(ClientArcaneData.active);
        if (active != null) {
            int ix = x + BAR_W + 5;
            int iy = height - 19;
            g.fill(ix - 1, iy - 1, ix + 17, iy + 17, 0xFF120A1E);
            g.fill(ix, iy, ix + 16, iy + 16, 0xFF000000 | darken(active.getSchool().getColor()));
            g.blit(active.getIcon(), ix, iy, 0, 0, 16, 16, 16, 16);
            float cd = ClientArcaneData.cooldownFraction(active.getId());
            if (cd > 0) {
                int h = Math.round(16 * cd);
                g.fill(ix, iy + 16 - h, ix + 16, iy + 16, 0xB0000000);
            }
            if (holdingWand) {
                g.drawString(font, active.getName(), ix, iy - 10, 0xFFFFFF, true);
            }
        }
    };

    private static int darken(int rgb) {
        int r = (rgb >> 16 & 0xFF) / 4, gr = (rgb >> 8 & 0xFF) / 4, b = (rgb & 0xFF) / 4;
        return r << 16 | gr << 8 | b;
    }

    private ManaHudOverlay() {}
}
