package com.bobroflza.tieredarcana.client.render;

import com.bobroflza.tieredarcana.block.ArcanePedestalBlock;
import com.bobroflza.tieredarcana.block.ArcanePedestalBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Floats and slowly spins the item placed on a pedestal or Catalyst Crystal. */
public class ArcanePedestalRenderer<T extends ArcanePedestalBlockEntity> implements BlockEntityRenderer<T> {
    public ArcanePedestalRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(T be, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        ItemStack stack = be.getItem();
        if (stack.isEmpty() || be.getLevel() == null) return;
        float time = be.getLevel().getGameTime() + partialTick;
        pose.pushPose();
        double h = be.getBlockState().getBlock() instanceof ArcanePedestalBlock b ? b.itemHeight() : 1.15;
        pose.translate(0.5, h + Math.sin(time / 10.0) * 0.05, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(time * 2f));
        pose.scale(0.6f, 0.6f, 0.6f);
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, light, overlay, pose, buffers,
                be.getLevel(), (int) be.getBlockPos().asLong());
        pose.popPose();
    }
}
