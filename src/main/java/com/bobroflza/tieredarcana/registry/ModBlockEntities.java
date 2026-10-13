package com.bobroflza.tieredarcana.registry;

import com.bobroflza.tieredarcana.TieredArcana;
import com.bobroflza.tieredarcana.block.ArcanePedestalBlockEntity;
import com.bobroflza.tieredarcana.block.VoidCrystalCoreBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, TieredArcana.MODID);

    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<ArcanePedestalBlockEntity>> PEDESTAL = BLOCK_ENTITIES.register("arcane_pedestal",
            () -> BlockEntityType.Builder.of(ArcanePedestalBlockEntity::new, ModBlocks.ARCANE_PEDESTAL.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<VoidCrystalCoreBlockEntity>> CORE = BLOCK_ENTITIES.register("void_crystal_core",
            () -> BlockEntityType.Builder.of(VoidCrystalCoreBlockEntity::new, ModBlocks.VOID_CRYSTAL_CORE.get()).build(null));

    private ModBlockEntities() {}
}
