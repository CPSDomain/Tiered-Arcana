package com.bobroflza.tieredarcana.client;

import com.bobroflza.tieredarcana.TieredArcana;
import com.bobroflza.tieredarcana.client.render.ArcanePedestalRenderer;
import com.bobroflza.tieredarcana.client.render.VoidCrystalCoreRenderer;
import com.bobroflza.tieredarcana.client.screen.SpellbookScreen;
import com.bobroflza.tieredarcana.item.SpellScrollItem;
import com.bobroflza.tieredarcana.item.WandItem;
import com.bobroflza.tieredarcana.network.CycleSpellPacket;
import com.bobroflza.tieredarcana.network.ModNetwork;
import com.bobroflza.tieredarcana.registry.ModBlockEntities;
import com.bobroflza.tieredarcana.registry.ModEntities;
import com.bobroflza.tieredarcana.registry.ModItems;
import com.bobroflza.tieredarcana.spell.Spell;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public final class ClientEvents {
    private ClientEvents() {}

    @Mod.EventBusSubscriber(modid = TieredArcana.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ModBus {
        @SubscribeEvent
        public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(ModEntities.MAGIC_MISSILE.get(), NoopRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.PEDESTAL.get(), ArcanePedestalRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.CORE.get(), VoidCrystalCoreRenderer::new);
        }

        @SubscribeEvent
        public static void extraModels(ModelEvent.RegisterAdditional event) {
            event.register(VoidCrystalCoreRenderer.CRYSTAL_MODEL);
        }

        @SubscribeEvent
        public static void overlays(RegisterGuiOverlaysEvent event) {
            event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "mana", ManaHudOverlay.HUD);
        }

        @SubscribeEvent
        public static void keys(RegisterKeyMappingsEvent event) {
            event.register(ModKeys.OPEN_SPELLBOOK);
            event.register(ModKeys.NEXT_SPELL);
            event.register(ModKeys.PREV_SPELL);
        }

        @SubscribeEvent
        public static void itemColors(RegisterColorHandlersEvent.Item event) {
            // layer1 of the spell scroll (the wax seal) takes the colour of the spell's school
            event.register((stack, layer) -> {
                if (layer != 1) return 0xFFFFFFFF;
                Spell s = SpellScrollItem.getSpell(stack);
                return s == null ? 0xFFAAAAAA : 0xFF000000 | s.getSchool().getColor();
            }, ModItems.SPELL_SCROLL.get());
        }
    }

    @Mod.EventBusSubscriber(modid = TieredArcana.MODID, value = Dist.CLIENT)
    public static final class ForgeBus {
        @SubscribeEvent
        public static void clientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.screen != null) return;
            while (ModKeys.NEXT_SPELL.consumeClick()) ModNetwork.sendToServer(new CycleSpellPacket(true));
            while (ModKeys.PREV_SPELL.consumeClick()) ModNetwork.sendToServer(new CycleSpellPacket(false));
            while (ModKeys.OPEN_SPELLBOOK.consumeClick()) {
                if (hasSpellbook(mc.player)) mc.setScreen(new SpellbookScreen());
            }
        }

        /** Sneak + mouse wheel while holding a wand cycles the active spell. */
        @SubscribeEvent
        public static void scroll(InputEvent.MouseScrollingEvent event) {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer p = mc.player;
            if (p == null || mc.screen != null || !p.isShiftKeyDown()) return;
            if (!(p.getMainHandItem().getItem() instanceof WandItem) && !(p.getOffhandItem().getItem() instanceof WandItem)) return;
            double delta = event.getScrollDelta();
            if (delta == 0) return;
            ModNetwork.sendToServer(new CycleSpellPacket(delta < 0));
            event.setCanceled(true);
        }
    }

    public static boolean hasSpellbook(LocalPlayer player) {
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (inv.getItem(i).is(ModItems.SPELLBOOK.get())) return true;
        }
        return false;
    }
}
