package com.bobroflza.tieredarcana.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** The radiant crystal that crowns each obelisk spire at Tier 4. Decorative + part of the multiblock. */
public class AstralCrystalBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(3, 0, 3, 13, 15, 13);

    public AstralCrystalBlock(Properties props) {
        super(props);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(2) == 0) {
            level.addParticle(ParticleTypes.END_ROD, pos.getX() + 0.2 + random.nextDouble() * 0.6, pos.getY() + 1.0,
                    pos.getZ() + 0.2 + random.nextDouble() * 0.6, 0, 0.04, 0);
        }
    }
}
