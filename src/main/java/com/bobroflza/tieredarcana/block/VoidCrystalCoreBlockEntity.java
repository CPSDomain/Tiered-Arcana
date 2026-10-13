package com.bobroflza.tieredarcana.block;

import com.bobroflza.tieredarcana.config.ArcanaConfig;
import com.bobroflza.tieredarcana.item.ManaGemItem;
import com.bobroflza.tieredarcana.item.SpellScrollItem;
import com.bobroflza.tieredarcana.mana.ArcanePlayerData;
import com.bobroflza.tieredarcana.registry.ModBlockEntities;
import com.bobroflza.tieredarcana.shrine.ShrineStructure;
import com.bobroflza.tieredarcana.spell.Spell;
import com.bobroflza.tieredarcana.spell.SpellRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.UUID;

public class VoidCrystalCoreBlockEntity extends BlockEntity {
    private static final DustParticleOptions CYAN = new DustParticleOptions(new Vector3f(0.3f, 1f, 0.95f), 1.0f);
    private static final DustParticleOptions PINK = new DustParticleOptions(new Vector3f(1f, 0.45f, 0.95f), 1.3f);
    private static final int GEM_CHARGE_PER_SECOND = 10;

    private int tier;
    private boolean checkedOnce;
    // ritual state
    private int ritualTicks = -1;
    @Nullable private UUID ritualPlayer;
    @Nullable private ResourceLocation ritualSpell;

    public VoidCrystalCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CORE.get(), pos, state);
    }

    public int getTier() { return tier; }
    public boolean isRitualActive() { return ritualTicks >= 0; }

    /** 0..1 progress of the running ritual (client copy is updated a few times per second). */
    public float getRitualProgress() {
        return ritualTicks < 0 ? 0 : Math.min(1f, ritualTicks / (float) ArcanaConfig.RITUAL_TICKS.get());
    }

    @Nullable
    private ArcanePedestalBlockEntity pedestal() {
        if (level == null) return null;
        return level.getBlockEntity(worldPosition.offset(ShrineStructure.PEDESTAL_OFFSET)) instanceof ArcanePedestalBlockEntity p ? p : null;
    }

    private AABB circle() {
        int r = ShrineStructure.circleRadius(tier);
        BlockPos p = worldPosition;
        return new AABB(p.getX() - r, p.getY() - 2, p.getZ() - r, p.getX() + r + 1, p.getY() + 1, p.getZ() + r + 1);
    }

    // =====================================================================
    // Ticking
    // =====================================================================

    public static void serverTick(Level level, BlockPos pos, BlockState state, VoidCrystalCoreBlockEntity be) {
        ServerLevel sl = (ServerLevel) level;
        long time = level.getGameTime();

        if (!be.checkedOnce || time % 20 == 0) {
            be.checkedOnce = true;
            be.updateTier(sl);
        }

        if (be.tier > 0) {
            if (time % 20 == 0) be.empowerCircle(sl, time);
            if (time % 8 == 0) be.ambientParticles(sl);
        }

        if (be.ritualTicks >= 0) be.tickRitual(sl);
    }

    private void updateTier(ServerLevel sl) {
        int newTier = ShrineStructure.computeTier(sl, worldPosition);
        if (newTier == tier) return;
        int old = tier;
        tier = newTier;
        if (newTier > old) {
            sl.playSound(null, worldPosition, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1f, 1.2f);
            sl.sendParticles(ParticleTypes.END_ROD, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5,
                    40, 0.4, 0.4, 0.4, 0.15);
            for (ServerPlayer p : sl.getEntitiesOfClass(ServerPlayer.class, new AABB(worldPosition).inflate(12))) {
                p.displayClientMessage(Component.translatable("message.tieredarcana.shrine_awakened", newTier)
                        .withStyle(ChatFormatting.LIGHT_PURPLE), true);
            }
        } else {
            sl.playSound(null, worldPosition, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1f, 1f);
        }
        if (ritualSpell != null) {
            Spell s = SpellRegistry.get(ritualSpell);
            if (s == null || s.getTier() > tier) cancelRitual(sl, "message.tieredarcana.ritual_broken");
        }
        sync();
    }

    /** Once a second: regen boost + attunement for players in the circle, and charge a Mana Gem on the pedestal. */
    private void empowerCircle(ServerLevel sl, long time) {
        for (ServerPlayer p : sl.getEntitiesOfClass(ServerPlayer.class, circle())) {
            p.getCapability(ArcanePlayerData.CAP).ifPresent(d -> {
                d.setShrineBoost(time + 30, tier);
                if (d.attune(tier)) {
                    p.displayClientMessage(Component.translatable("message.tieredarcana.attuned", tier)
                            .withStyle(ChatFormatting.AQUA), true);
                }
            });
        }
        ArcanePedestalBlockEntity ped = pedestal();
        if (ped != null && ped.getItem().getItem() instanceof ManaGemItem gem) {
            if (gem.charge(ped.getItem(), GEM_CHARGE_PER_SECOND * tier) > 0) {
                ped.changed();
                BlockPos pp = ped.getBlockPos();
                sl.sendParticles(CYAN, pp.getX() + 0.5, pp.getY() + 1.1, pp.getZ() + 0.5, 4, 0.15, 0.1, 0.15, 0);
            }
        }
    }

    private void ambientParticles(ServerLevel sl) {
        List<BlockPos> runes = ShrineStructure.runes(worldPosition, tier);
        if (runes.isEmpty()) return;
        BlockPos r = runes.get(sl.random.nextInt(runes.size()));
        sl.sendParticles(CYAN, r.getX() + 0.5, r.getY() + 1.05, r.getZ() + 0.5, 1, 0.3, 0, 0.3, 0);
    }

    // =====================================================================
    // Player interaction
    // =====================================================================

    public void onActivated(ServerPlayer player) {
        ServerLevel sl = (ServerLevel) level;
        if (sl == null) return;
        if (isRitualActive()) {
            msg(player, "message.tieredarcana.ritual_running", ChatFormatting.LIGHT_PURPLE);
            return;
        }
        updateTier(sl);
        if (tier == 0) {
            int missing = ShrineStructure.countMissing(sl, worldPosition, 1);
            player.displayClientMessage(Component.translatable("message.tieredarcana.shrine_incomplete", missing)
                    .withStyle(ChatFormatting.RED), false);
            return;
        }
        ArcanePlayerData data = player.getCapability(ArcanePlayerData.CAP).orElse(null);
        if (data == null) return;
        if (data.attune(tier)) {
            player.displayClientMessage(Component.translatable("message.tieredarcana.attuned", tier).withStyle(ChatFormatting.AQUA), false);
        }

        ArcanePedestalBlockEntity ped = pedestal();
        ItemStack onPedestal = ped == null ? ItemStack.EMPTY : ped.getItem();
        Spell spell = SpellScrollItem.getSpell(onPedestal);

        if (spell != null) {
            if (data.knows(spell.getId())) {
                player.displayClientMessage(Component.translatable("message.tieredarcana.already_known", spell.getName())
                        .withStyle(ChatFormatting.YELLOW), false);
            } else if (spell.getTier() > tier) {
                player.displayClientMessage(Component.translatable("message.tieredarcana.need_shrine_tier", spell.getName(), spell.getTier())
                        .withStyle(ChatFormatting.RED), false);
            } else {
                startRitual(sl, player, spell);
            }
            return;
        }
        if (onPedestal.getItem() instanceof ManaGemItem) {
            player.displayClientMessage(Component.translatable("message.tieredarcana.gem_charging",
                    ManaGemItem.getStored(onPedestal)).withStyle(ChatFormatting.AQUA), false);
            return;
        }
        int next = ShrineStructure.MAX_TIER > tier ? tier + 1 : 0;
        player.displayClientMessage(Component.translatable("message.tieredarcana.shrine_status", tier).withStyle(ChatFormatting.LIGHT_PURPLE), false);
        if (next == 0) {
            player.displayClientMessage(Component.translatable("message.tieredarcana.shrine_max").withStyle(ChatFormatting.GRAY), false);
        }
    }

    private static void msg(Player p, String key, ChatFormatting color) {
        p.displayClientMessage(Component.translatable(key).withStyle(color), true);
    }

    // =====================================================================
    // Ritual
    // =====================================================================

    private void startRitual(ServerLevel sl, ServerPlayer player, Spell spell) {
        ritualTicks = 0;
        ritualPlayer = player.getUUID();
        ritualSpell = spell.getId();
        sl.playSound(null, worldPosition, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 1.2f, 0.8f);
        player.displayClientMessage(Component.translatable("message.tieredarcana.ritual_start", spell.getName())
                .withStyle(ChatFormatting.LIGHT_PURPLE), false);
        sync();
    }

    private void tickRitual(ServerLevel sl) {
        Spell spell = SpellRegistry.get(ritualSpell);
        ArcanePedestalBlockEntity ped = pedestal();
        if (spell == null || ped == null || SpellScrollItem.getSpell(ped.getItem()) != spell) {
            cancelRitual(sl, "message.tieredarcana.ritual_broken");
            return;
        }
        ritualTicks++;
        int total = ArcanaConfig.RITUAL_TICKS.get();
        float progress = ritualTicks / (float) total;
        double cx = worldPosition.getX() + 0.5, cy = worldPosition.getY() + 0.5, cz = worldPosition.getZ() + 0.5;
        BlockPos pp = ped.getBlockPos();

        // beam: pedestal -> crystal
        if (ritualTicks % 2 == 0) {
            sl.sendParticles(ParticleTypes.END_ROD, pp.getX() + 0.5, pp.getY() + 1.0, pp.getZ() + 0.5, 0, 0, 1, 0, 0.12);
            sl.sendParticles(ParticleTypes.ENCHANT, cx, cy, cz, 6, 0.6, 0.6, 0.6, 0.6);
        }
        // runes light up one by one
        List<BlockPos> runes = ShrineStructure.runes(worldPosition, tier);
        int lit = Math.round(progress * runes.size());
        if (ritualTicks % 4 == 0) {
            for (int i = 0; i < lit && i < runes.size(); i++) {
                BlockPos r = runes.get(i);
                sl.sendParticles(CYAN, r.getX() + 0.5, r.getY() + 1.05, r.getZ() + 0.5, 2, 0.25, 0.02, 0.25, 0);
            }
            sl.sendParticles(PINK, cx, cy, cz, 3, 0.3, 0.3, 0.3, 0);
        }
        if (ritualTicks % 20 == 0) {
            sl.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 1f, 0.6f + progress);
            sync();
        }
        if (ritualTicks >= total) completeRitual(sl, spell, ped);
    }

    private void completeRitual(ServerLevel sl, Spell spell, ArcanePedestalBlockEntity ped) {
        ServerPlayer player = ritualPlayer == null ? null : (ServerPlayer) sl.getPlayerByUUID(ritualPlayer);
        boolean inRange = player != null && player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY(), worldPosition.getZ() + 0.5) <= 10 * 10;
        if (!inRange) {
            cancelRitual(sl, "message.tieredarcana.ritual_away");
            return;
        }
        ArcanePlayerData data = player.getCapability(ArcanePlayerData.CAP).orElse(null);
        if (data != null && data.learn(spell.getId())) {
            ped.setItem(ItemStack.EMPTY);
            double cx = worldPosition.getX() + 0.5, cy = worldPosition.getY() + 0.5, cz = worldPosition.getZ() + 0.5;
            sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, cx, cy, cz, 80, 0.4, 0.4, 0.4, 0.45);
            sl.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1, player.getZ(), 30, 0.4, 0.8, 0.4, 0.05);
            sl.playSound(null, worldPosition, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 1f, 1f);
            sl.playSound(null, worldPosition, SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.BLOCKS, 1f, 0.8f);
            player.displayClientMessage(Component.translatable("message.tieredarcana.learned", spell.getName())
                    .withStyle(ChatFormatting.GOLD), false);
        }
        resetRitual();
    }

    private void cancelRitual(ServerLevel sl, String reasonKey) {
        if (ritualPlayer != null) {
            Player p = sl.getPlayerByUUID(ritualPlayer);
            if (p != null) p.displayClientMessage(Component.translatable(reasonKey).withStyle(ChatFormatting.RED), false);
        }
        sl.playSound(null, worldPosition, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1f, 0.7f);
        resetRitual();
    }

    private void resetRitual() {
        ritualTicks = -1;
        ritualPlayer = null;
        ritualSpell = null;
        sync();
    }

    // =====================================================================
    // Save / sync
    // =====================================================================

    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Tier", tier);
        if (ritualTicks >= 0 && ritualPlayer != null && ritualSpell != null) {
            tag.putInt("RitualTicks", ritualTicks);
            tag.putUUID("RitualPlayer", ritualPlayer);
            tag.putString("RitualSpell", ritualSpell.toString());
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        tier = tag.getInt("Tier");
        if (tag.contains("RitualTicks")) {
            ritualTicks = tag.getInt("RitualTicks");
            ritualPlayer = tag.hasUUID("RitualPlayer") ? tag.getUUID("RitualPlayer") : null;
            ritualSpell = ResourceLocation.tryParse(tag.getString("RitualSpell"));
        } else {
            ritualTicks = -1;
            ritualPlayer = null;
            ritualSpell = null;
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition).inflate(6);
    }
}
