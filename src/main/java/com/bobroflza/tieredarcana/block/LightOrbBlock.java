package com.bobroflza.tieredarcana.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Invisible light source left behind by the Light Orb spell. Drawn with particles; breaks instantly. */
public class LightOrbBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(5, 5, 5, 11, 11, 11);

    public LightOrbBlock(Properties props) {
        super(props);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return Shapes.empty();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + 0.5, y = pos.getY() + 0.5, z = pos.getZ() + 0.5;
        level.addParticle(ParticleTypes.END_ROD, x + (random.nextDouble() - 0.5) * 0.3, y + (random.nextDouble() - 0.5) * 0.3,
                z + (random.nextDouble() - 0.5) * 0.3, 0, 0.005, 0);
        if (random.nextInt(3) == 0) level.addParticle(ParticleTypes.GLOW, x, y, z, 0, 0, 0);
    }
}
