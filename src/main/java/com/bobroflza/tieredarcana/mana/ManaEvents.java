package com.bobroflza.tieredarcana.mana;

import com.bobroflza.tieredarcana.TieredArcana;
import com.bobroflza.tieredarcana.config.ArcanaConfig;
import com.bobroflza.tieredarcana.network.ModNetwork;
import com.bobroflza.tieredarcana.network.SyncArcaneDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TieredArcana.MODID)
public final class ManaEvents {
    private ManaEvents() {}

    @Mod.EventBusSubscriber(modid = TieredArcana.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModBusEvents {
        @SubscribeEvent
        public static void registerCaps(RegisterCapabilitiesEvent event) {
            event.register(ArcanePlayerData.class);
        }
    }

    @SubscribeEvent
    public static void attach(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player && !event.getObject().getCapability(ArcanePlayerData.CAP).isPresent()) {
            event.addCapability(TieredArcana.id("arcane_data"), new ArcanePlayerDataProvider());
        }
    }

    @SubscribeEvent
    public static void clone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        original.reviveCaps();
        original.getCapability(ArcanePlayerData.CAP).ifPresent(old ->
                event.getEntity().getCapability(ArcanePlayerData.CAP).ifPresent(now -> now.copyFrom(old, event.isWasDeath())));
        original.invalidateCaps();
    }

    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) sync(sp, true);
    }

    @SubscribeEvent
    public static void respawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) sync(sp, true);
    }

    @SubscribeEvent
    public static void changeDim(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) sync(sp, true);
    }

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer sp)) return;
        sp.getCapability(ArcanePlayerData.CAP).ifPresent(data -> {
            if (sp.tickCount % 20 == 0) {
                data.recalcMax();
                if (data.getMana() < data.getMaxMana()) {
                    data.addMana((float) regenPerSecond(sp, data));
                }
            }
            if (data.isDirty()) {
                ModNetwork.sendTo(sp, SyncArcaneDataPacket.of(sp, data));
                data.clearDirty();
            }
        });
    }

    /** Works out this second's regen: passive, shrine circle, and moonlight bonuses. */
    public static double regenPerSecond(ServerPlayer sp, ArcanePlayerData data) {
        ServerLevel level = sp.serverLevel();
        double regen = ArcanaConfig.BASE_REGEN.get();
        if (data.hasShrineBoost(level.getGameTime())) {
            regen *= ArcanaConfig.SHRINE_REGEN_MULTIPLIER.get() + Math.max(0, data.getShrineBoostTier() - 1);
        }
        if (level.dimensionType().hasSkyLight() && level.isNight() && level.canSeeSky(sp.blockPosition().above())) {
            boolean fullMoon = level.getMoonPhase() == 0;
            regen *= 1.0 + (fullMoon ? ArcanaConfig.FULL_MOON_REGEN_BONUS.get() : ArcanaConfig.NIGHT_REGEN_BONUS.get());
        }
        return regen;
    }

    public static void sync(ServerPlayer sp, boolean recalc) {
        sp.getCapability(ArcanePlayerData.CAP).ifPresent(data -> {
            if (recalc) data.recalcMax();
            ModNetwork.sendTo(sp, SyncArcaneDataPacket.of(sp, data));
            data.clearDirty();
        });
    }
}
