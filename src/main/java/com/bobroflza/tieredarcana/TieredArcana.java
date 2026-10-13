package com.bobroflza.tieredarcana;

import com.bobroflza.tieredarcana.config.ArcanaConfig;
import com.bobroflza.tieredarcana.network.ModNetwork;
import com.bobroflza.tieredarcana.registry.*;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.crafting.StrictNBTIngredient;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(TieredArcana.MODID)
public class TieredArcana {
    public static final String MODID = "tieredarcana";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TieredArcana() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.BLOCKS.register(bus);
        ModItems.ITEMS.register(bus);
        ModBlockEntities.BLOCK_ENTITIES.register(bus);
        ModEntities.ENTITIES.register(bus);
        ModCreativeTabs.TABS.register(bus);

        bus.addListener(this::commonSetup);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ArcanaConfig.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        ModNetwork.register();
        event.enqueueWork(() -> {
            ItemStack awkward = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.AWKWARD);
            BrewingRecipeRegistry.addRecipe(StrictNBTIngredient.of(awkward),
                    Ingredient.of(ModItems.ARCANE_DUST.get()), new ItemStack(ModItems.MINOR_MANA_POTION.get()));
            BrewingRecipeRegistry.addRecipe(Ingredient.of(ModItems.MINOR_MANA_POTION.get()),
                    Ingredient.of(Items.GLOWSTONE_DUST), new ItemStack(ModItems.MANA_POTION.get()));
            BrewingRecipeRegistry.addRecipe(Ingredient.of(ModItems.MANA_POTION.get()),
                    Ingredient.of(Items.AMETHYST_SHARD), new ItemStack(ModItems.GREATER_MANA_POTION.get()));
        });
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MODID, path);
    }
}
