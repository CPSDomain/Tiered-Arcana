package com.bobroflza.tieredarcana.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * The pink crystal tip on top of each Tier 2 Catalyst Pylon. Holds one ritual ingredient,
 * exactly like the pedestal (right-click to place / take).
 */
public class CatalystCrystalBlock extends ArcanePedestalBlock {
    private static final VoxelShape SHAPE = Block.box(4, 0, 4, 12, 12, 12);

    public CatalystCrystalBlock(Properties props) {
        super(props);
    }

    @Override
    protected VoxelShape shape() {
        return SHAPE;
    }

    @Override
    public double itemHeight() {
        return 1.05;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CatalystCrystalBlockEntity(pos, state);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(4) == 0) {
            level.addParticle(ParticleTypes.END_ROD, pos.getX() + 0.3 + random.nextDouble() * 0.4, pos.getY() + 0.8,
                    pos.getZ() + 0.3 + random.nextDouble() * 0.4, 0, 0.02, 0);
        }
    }
}
