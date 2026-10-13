package com.bobroflza.tieredarcana.client.render;

import com.bobroflza.tieredarcana.TieredArcana;
import com.bobroflza.tieredarcana.block.VoidCrystalCoreBlockEntity;
import com.bobroflza.tieredarcana.item.WandItem;
import com.bobroflza.tieredarcana.shrine.ShrineStructure;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.model.data.ModelData;

/**
 * Draws the floating, spinning Void Crystal (bigger at each shrine tier, faster during a ritual),
 * and – while the player holds a wand – miniature "ghost" blocks showing what's still needed for the next tier.
 */
public class VoidCrystalCoreRenderer implements BlockEntityRenderer<VoidCrystalCoreBlockEntity> {
    public static final ResourceLocation CRYSTAL_MODEL = TieredArcana.id("block/void_crystal");

    private final BlockRenderDispatcher blocks;

    public VoidCrystalCoreRenderer(BlockEntityRendererProvider.Context ctx) {
        this.blocks = ctx.getBlockRenderDispatcher();
    }

    @Override
    public void render(VoidCrystalCoreBlockEntity be, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        Level level = be.getLevel();
        if (level == null) return;
        float time = level.getGameTime() + partialTick;
        int tier = be.getTier();
        boolean ritual = be.isRitualActive();

        // ---- the crystal
        float scale = tier == 0 ? 0.5f : 0.6f + 0.15f * tier;
        if (ritual) scale += 0.08f * (float) Math.sin(time / 3.0) + 0.1f * be.getRitualProgress();
        float spin = tier == 0 ? 0.6f : ritual ? 6f : 1.5f;
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(CRYSTAL_MODEL);

        pose.pushPose();
        pose.translate(0.5, 0.5 + Math.sin(time / 15.0) * 0.08, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(time * spin));
        pose.scale(scale, scale, scale);
        pose.translate(-0.5, -0.5, -0.5);
        float shade = tier == 0 ? 0.55f : 1f;
        blocks.getModelRenderer().renderModel(pose.last(), buffers.getBuffer(Sheets.cutoutBlockSheet()), null, model,
                shade, shade, shade, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        pose.popPose();

        // ---- ghost preview of the next tier
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || ritual) return;
        boolean wand = player.getMainHandItem().getItem() instanceof WandItem || player.getOffhandItem().getItem() instanceof WandItem;
        if (!wand || player.distanceToSqr(be.getBlockPos().getCenter()) > 24 * 24) return;
        BlockPos core = be.getBlockPos();

        // built but not yet awakened: show the key offering hovering over the pedestal
        if (be.isAwaitingOffering()) {
            Item offering = ShrineStructure.keyOffering(be.getBuiltTier() > tier ? tier + 1 : 0);
            if (offering != null) {
                BlockPos o = ShrineStructure.PEDESTAL_OFFSET;
                pose.pushPose();
                pose.translate(o.getX() + 0.5, o.getY() + 1.35 + Math.sin(time / 8.0) * 0.06, o.getZ() + 0.5);
                pose.mulPose(Axis.YP.rotationDegrees(time * 3f));
                pose.scale(0.5f, 0.5f, 0.5f);
                Minecraft.getInstance().getItemRenderer().renderStatic(new ItemStack(offering), ItemDisplayContext.FIXED,
                        LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, pose, buffers, level, 0);
                pose.popPose();
            }
            return;
        }

        int next = be.getBuiltTier() + 1;
        if (next > ShrineStructure.MAX_TIER) return;

        float pulse = 0.45f + 0.05f * (float) Math.sin(time / 6.0);
        for (ShrineStructure.Entry e : ShrineStructure.entries(next)) {
            if (e.matches(level, core)) continue;
            BlockPos o = e.offset();
            pose.pushPose();
            pose.translate(o.getX() + 0.5, o.getY() + 0.5, o.getZ() + 0.5);
            pose.scale(pulse, pulse, pulse);
            pose.translate(-0.5, -0.5, -0.5);
            blocks.renderSingleBlock(e.display().get(), pose, buffers, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                    ModelData.EMPTY, null);
            pose.popPose();
        }
    }

    @Override
    public boolean shouldRenderOffScreen(VoidCrystalCoreBlockEntity be) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 96;
    }
}
