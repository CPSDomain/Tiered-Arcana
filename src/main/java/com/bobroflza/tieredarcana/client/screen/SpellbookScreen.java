package com.bobroflza.tieredarcana.client.screen;

import com.bobroflza.tieredarcana.client.ClientArcaneData;
import com.bobroflza.tieredarcana.network.ModNetwork;
import com.bobroflza.tieredarcana.network.SelectSpellPacket;
import com.bobroflza.tieredarcana.spell.School;
import com.bobroflza.tieredarcana.spell.Spell;
import com.bobroflza.tieredarcana.spell.SpellRegistry;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The spellbook: one tab per school, a grid of that school's spells on the left page and details on the right.
 * Clicking a learned spell makes it the active spell the wand will cast.
 */
public class SpellbookScreen extends Screen {
    private static final int W = 260, H = 172;
    private static final int SLOT = 22, COLS = 4;
    private static final int TAB_W = 26, TAB_H = 20;

    private static School tab = School.ARCANE;
    @Nullable private static Spell selected;

    private int left, top;

    public SpellbookScreen() {
        super(Component.translatable("screen.tieredarcana.spellbook"));
    }

    @Override
    protected void init() {
        left = (width - W) / 2;
        top = (height - H) / 2 + 8;
        if (selected == null) selected = SpellRegistry.get(ClientArcaneData.active);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    // ------------------------------------------------------------------ layout helpers

    private int tabX(int i) { return left + 8 + i * (TAB_W + 3); }
    private int tabY() { return top - TAB_H + 2; }
    private int slotX(int i) { return left + 16 + (i % COLS) * SLOT; }
    private int slotY(int i) { return top + 30 + (i / COLS) * SLOT; }

    // ------------------------------------------------------------------ rendering

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        School[] schools = School.values();

        // tabs (behind the book)
        for (int i = 0; i < schools.length; i++) {
            School s = schools[i];
            boolean active = s == tab;
            int x = tabX(i), y = tabY() + (active ? 0 : 3);
            g.fill(x, y, x + TAB_W, top + 2, 0xFF1B1128);
            g.fill(x + 1, y + 1, x + TAB_W - 1, top + 2, 0xFF000000 | (active ? s.getColor() : dim(s.getColor())));
            List<Spell> spells = SpellRegistry.bySchool(s);
            if (!spells.isEmpty()) {
                RenderSystem.setShaderColor(1, 1, 1, active ? 1f : 0.6f);
                g.blit(spells.get(0).getIcon(), x + 5, y + 3, 0, 0, 16, 16, 16, 16);
                RenderSystem.setShaderColor(1, 1, 1, 1);
            }
        }

        // book body
        g.fill(left - 2, top - 2, left + W + 2, top + H + 2, 0xFF120A1E);
        g.fill(left, top, left + W, top + H, 0xFF3A2556);
        g.fill(left + 6, top + 6, left + W / 2 - 2, top + H - 6, 0xFF1D1330);      // left page
        g.fill(left + W / 2 + 2, top + 6, left + W - 6, top + H - 6, 0xFF1D1330);  // right page
        g.fill(left + W / 2 - 1, top + 4, left + W / 2 + 1, top + H - 4, 0xFF120A1E); // spine
        // gilded corner accents
        int gold = 0xFFB98CFF;
        g.fill(left, top, left + 10, top + 1, gold); g.fill(left, top, left + 1, top + 10, gold);
        g.fill(left + W - 10, top + H - 1, left + W, top + H, gold); g.fill(left + W - 1, top + H - 10, left + W, top + H, gold);

        // ---- left page: school + spell grid
        g.drawString(font, tab.getName().copy().withStyle(ChatFormatting.BOLD), left + 14, top + 13, tab.getColor(), false);
        List<Spell> spells = SpellRegistry.bySchool(tab);
        Spell hovered = null;
        for (int i = 0; i < spells.size(); i++) {
            Spell s = spells.get(i);
            int x = slotX(i), y = slotY(i);
            boolean known = ClientArcaneData.KNOWN.contains(s.getId());
            boolean isActive = s.getId().equals(ClientArcaneData.active);
            boolean hover = mouseX >= x && mouseX < x + 20 && mouseY >= y && mouseY < y + 20;
            if (hover) hovered = s;

            int border = isActive ? 0xFFFFD452 : s == selected ? 0xFFE0E0FF : hover ? 0xFF9C86C0 : 0xFF0F0919;
            g.fill(x, y, x + 20, y + 20, border);
            g.fill(x + 1, y + 1, x + 19, y + 19, known ? 0xFF000000 | dim(s.getSchool().getColor()) : 0xFF140D1F);
            if (!known) RenderSystem.setShaderColor(0.25f, 0.25f, 0.3f, 1f);
            g.blit(s.getIcon(), x + 2, y + 2, 0, 0, 16, 16, 16, 16);
            RenderSystem.setShaderColor(1, 1, 1, 1);
            if (!known) g.drawString(font, String.valueOf(s.getTier()), x + 14, y + 12, 0x8A7FA0, true);
            float cd = known ? ClientArcaneData.cooldownFraction(s.getId()) : 0;
            if (cd > 0) {
                int h = Math.round(18 * cd);
                g.fill(x + 1, y + 19 - h, x + 19, y + 19, 0xA0000000);
            }
        }

        // mana + shrine info at the bottom of the left page
        int infoY = top + H - 34;
        g.drawString(font, Component.translatable("screen.tieredarcana.mana", (int) ClientArcaneData.mana, ClientArcaneData.maxMana),
                left + 14, infoY, 0x7FE6FF, false);
        int barX = left + 14, barY = infoY + 11, barW = W / 2 - 30;
        g.fill(barX, barY, barX + barW, barY + 4, 0xFF0F0919);
        int fill = ClientArcaneData.maxMana <= 0 ? 0 : Math.round(barW * Math.min(1f, ClientArcaneData.mana / ClientArcaneData.maxMana));
        if (fill > 0) g.fillGradient(barX, barY, barX + fill, barY + 4, 0xFF6FF3FF, 0xFF8A4FFF);
        g.drawString(font, Component.translatable("screen.tieredarcana.shrine_tier", ClientArcaneData.shrineTier),
                left + 14, infoY + 18, 0xB98CFF, false);

        // ---- right page: details
        renderDetails(g, selected != null ? selected : hovered);

        super.render(g, mouseX, mouseY, partialTick);

        if (hovered != null) {
            g.renderTooltip(font, hovered.getName(), mouseX, mouseY);
        } else {
            for (int i = 0; i < schools.length; i++) {
                int x = tabX(i), y = tabY();
                if (mouseX >= x && mouseX < x + TAB_W && mouseY >= y && mouseY < top) {
                    g.renderTooltip(font, schools[i].getName(), mouseX, mouseY);
                }
            }
        }
    }

    private void renderDetails(GuiGraphics g, @Nullable Spell s) {
        int x = left + W / 2 + 10, y = top + 13, w = W / 2 - 22;
        if (s == null) {
            for (FormattedCharSequence line : font.split(Component.translatable("screen.tieredarcana.hint"), w)) {
                g.drawString(font, line, x, y, 0x9C86C0, false);
                y += 10;
            }
            return;
        }
        boolean known = ClientArcaneData.KNOWN.contains(s.getId());
        boolean isActive = s.getId().equals(ClientArcaneData.active);

        g.drawString(font, s.getName().copy().withStyle(ChatFormatting.BOLD), x, y, 0xFFFFFF, false);
        y += 12;
        g.drawString(font, Component.translatable("screen.tieredarcana.tier_school", s.getTier(), s.getSchool().getName()), x, y, 0x9C86C0, false);
        y += 14;
        g.drawString(font, Component.translatable("screen.tieredarcana.cost", s.getManaCost()), x, y, 0x7FE6FF, false);
        y += 10;
        g.drawString(font, Component.translatable("screen.tieredarcana.cooldown", String.format("%.1f", s.getCooldownTicks() / 20f)), x, y, 0x7FE6FF, false);
        y += 14;
        for (FormattedCharSequence line : font.split(s.getDescription(), w)) {
            g.drawString(font, line, x, y, 0xD8CCEA, false);
            y += 10;
        }

        Component status;
        int color;
        if (isActive) {
            status = Component.translatable("screen.tieredarcana.status.active");
            color = 0xFFD452;
        } else if (known) {
            status = Component.translatable("screen.tieredarcana.status.known");
            color = 0x8BE07A;
        } else {
            status = Component.translatable("screen.tieredarcana.status.unknown", s.getTier());
            color = 0xE07A7A;
        }
        int sy = top + H - 16 - 10 * (font.split(status, w).size() - 1);
        for (FormattedCharSequence line : font.split(status, w)) {
            g.drawString(font, line, x, sy, color, false);
            sy += 10;
        }
    }

    // ------------------------------------------------------------------ input

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        School[] schools = School.values();
        for (int i = 0; i < schools.length; i++) {
            int x = tabX(i), y = tabY();
            if (mouseX >= x && mouseX < x + TAB_W && mouseY >= y && mouseY < top) {
                if (tab != schools[i]) {
                    tab = schools[i];
                    selected = null;
                    click(1.2f);
                }
                return true;
            }
        }
        List<Spell> spells = SpellRegistry.bySchool(tab);
        for (int i = 0; i < spells.size(); i++) {
            int x = slotX(i), y = slotY(i);
            if (mouseX >= x && mouseX < x + 20 && mouseY >= y && mouseY < y + 20) {
                Spell s = spells.get(i);
                selected = s;
                if (ClientArcaneData.KNOWN.contains(s.getId()) && !s.getId().equals(ClientArcaneData.active)) {
                    ClientArcaneData.active = s.getId(); // optimistic; server confirms with a sync
                    ModNetwork.sendToServer(new SelectSpellPacket(s.getId()));
                    click(1.6f);
                } else {
                    click(1.0f);
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (minecraft != null && minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void click(float pitch) {
        if (minecraft != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, pitch));
        }
    }

    private static int dim(int rgb) {
        int r = (rgb >> 16 & 0xFF) / 3, g = (rgb >> 8 & 0xFF) / 3, b = (rgb & 0xFF) / 3;
        return r << 16 | g << 8 | b;
    }
}
