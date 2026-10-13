package com.bobroflza.tieredarcana.registry;

import com.bobroflza.tieredarcana.TieredArcana;
import com.bobroflza.tieredarcana.block.ArcanePedestalBlock;
import com.bobroflza.tieredarcana.block.CatalystCrystalBlock;
import com.bobroflza.tieredarcana.block.LightOrbBlock;
import com.bobroflza.tieredarcana.block.VoidCrystalCoreBlock;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, TieredArcana.MODID);

    public static final RegistryObject<Block> ARCANE_ORE = BLOCKS.register("arcane_ore",
            () -> new DropExperienceBlock(BlockBehaviour.Properties.copy(Blocks.IRON_ORE).lightLevel(s -> 3), UniformInt.of(2, 5)));

    public static final RegistryObject<Block> DEEPSLATE_ARCANE_ORE = BLOCKS.register("deepslate_arcane_ore",
            () -> new DropExperienceBlock(BlockBehaviour.Properties.copy(Blocks.DEEPSLATE_IRON_ORE).lightLevel(s -> 3), UniformInt.of(2, 5)));

    public static final RegistryObject<Block> ARCANE_RUNESTONE = BLOCKS.register("arcane_runestone",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.DEEPSLATE_BRICKS).mapColor(MapColor.COLOR_CYAN).lightLevel(s -> 10)));

    public static final RegistryObject<Block> ARCANE_PEDESTAL = BLOCKS.register("arcane_pedestal",
            () -> new ArcanePedestalBlock(BlockBehaviour.Properties.copy(Blocks.POLISHED_DEEPSLATE).noOcclusion().lightLevel(s -> 4)));

    public static final RegistryObject<Block> CATALYST_CRYSTAL = BLOCKS.register("catalyst_crystal",
            () -> new CatalystCrystalBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK)
                    .strength(1.5f, 6f).sound(SoundType.AMETHYST_CLUSTER).noOcclusion().lightLevel(s -> 9)
                    .pushReaction(PushReaction.DESTROY)));

    public static final RegistryObject<Block> VOID_CRYSTAL_BLOCK = BLOCKS.register("void_crystal_block",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE)
                    .strength(3f, 9f).sound(SoundType.AMETHYST).lightLevel(s -> 7).requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> VOID_CRYSTAL_CORE = BLOCKS.register("void_crystal_core",
            () -> new VoidCrystalCoreBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK)
                    .strength(3f, 1200f).sound(SoundType.AMETHYST).noOcclusion().lightLevel(s -> 15)
                    .pushReaction(PushReaction.BLOCK).requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> LIGHT_ORB = BLOCKS.register("light_orb",
            () -> new LightOrbBlock(BlockBehaviour.Properties.of().replaceable().noCollission().instabreak()
                    .noLootTable().lightLevel(s -> 15).sound(SoundType.AMETHYST).noOcclusion().pushReaction(PushReaction.DESTROY)));

    private ModBlocks() {}
}
