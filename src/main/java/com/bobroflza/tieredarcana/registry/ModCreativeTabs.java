package com.bobroflza.tieredarcana.registry;

import com.bobroflza.tieredarcana.TieredArcana;
import com.bobroflza.tieredarcana.item.ManaGemItem;
import com.bobroflza.tieredarcana.item.SpellScrollItem;
import com.bobroflza.tieredarcana.spell.Spell;
import com.bobroflza.tieredarcana.spell.SpellRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TieredArcana.MODID);

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.tieredarcana"))
            .icon(() -> new ItemStack(ModItems.APPRENTICE_WAND.get()))
            .displayItems((params, out) -> {
                out.accept(ModItems.SPELLBOOK.get());
                out.accept(ModItems.APPRENTICE_WAND.get());
                out.accept(ModItems.ARCANE_DUST.get());
                out.accept(ModItems.ARCANE_ORE.get());
                out.accept(ModItems.DEEPSLATE_ARCANE_ORE.get());
                out.accept(ModItems.ARCANE_RUNESTONE.get());
                out.accept(ModItems.ARCANE_PEDESTAL.get());
                out.accept(ModItems.VOID_CRYSTAL_CORE.get());
                out.accept(ModItems.MINOR_MANA_POTION.get());
                out.accept(ModItems.MANA_POTION.get());
                out.accept(ModItems.GREATER_MANA_POTION.get());
                out.accept(ModItems.MANA_CRYSTAL.get());
                out.accept(ModItems.MANA_GEM.get());
                ItemStack full = new ItemStack(ModItems.MANA_GEM.get());
                ManaGemItem.setStored(full, ((ManaGemItem) ModItems.MANA_GEM.get()).getCapacity());
                out.accept(full);
                for (Spell s : SpellRegistry.all()) {
                    out.accept(SpellScrollItem.of(ModItems.SPELL_SCROLL.get(), s));
                }
            })
            .build());

    private ModCreativeTabs() {}
}
