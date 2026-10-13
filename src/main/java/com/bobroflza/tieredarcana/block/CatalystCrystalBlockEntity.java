package com.bobroflza.tieredarcana.block;

import com.bobroflza.tieredarcana.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** Same single-item storage as the pedestal, with its own block entity type. */
public class CatalystCrystalBlockEntity extends ArcanePedestalBlockEntity {
    public CatalystCrystalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CATALYST.get(), pos, state);
    }
}
