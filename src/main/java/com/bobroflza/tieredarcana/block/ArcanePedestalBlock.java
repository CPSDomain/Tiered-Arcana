package com.bobroflza.tieredarcana.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

/** Holds one item: a spell scroll for a ritual, or a Mana Gem to recharge. */
public class ArcanePedestalBlock extends BaseEntityBlock {
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(2, 0, 2, 14, 3, 14),
            Block.box(4, 3, 4, 12, 11, 12),
            Block.box(2, 11, 2, 14, 14, 14));

    public ArcanePedestalBlock(Properties props) {
        super(props);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return shape();
    }

    protected VoxelShape shape() {
        return SHAPE;
    }

    /** Height above the block's base where the held item floats (used by the renderer). */
    public double itemHeight() {
        return 1.15;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ArcanePedestalBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof ArcanePedestalBlockEntity pedestal)) return InteractionResult.PASS;
        ItemStack held = player.getItemInHand(hand);
        if (pedestal.getItem().isEmpty()) {
            if (held.isEmpty()) return InteractionResult.PASS;
            if (!level.isClientSide) {
                pedestal.setItem(player.getAbilities().instabuild ? held.copyWithCount(1) : held.split(1));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (!level.isClientSide) {
            ItemStack out = pedestal.getItem();
            pedestal.setItem(ItemStack.EMPTY);
            ItemHandlerHelper.giveItemToPlayer(player, out);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof ArcanePedestalBlockEntity pedestal) {
            Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, pedestal.getItem());
        }
        super.onRemove(state, level, pos, newState, moving);
    }
}
