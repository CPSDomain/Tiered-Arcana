package com.bobroflza.tieredarcana.network;

import com.bobroflza.tieredarcana.TieredArcana;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            TieredArcana.id("main"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    private ModNetwork() {}

    public static void register() {
        int id = 0;
        CHANNEL.messageBuilder(SyncArcaneDataPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncArcaneDataPacket::encode)
                .decoder(SyncArcaneDataPacket::new)
                .consumerMainThread(SyncArcaneDataPacket::handle)
                .add();
        CHANNEL.messageBuilder(SelectSpellPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(SelectSpellPacket::encode)
                .decoder(SelectSpellPacket::new)
                .consumerMainThread(SelectSpellPacket::handle)
                .add();
        CHANNEL.messageBuilder(CycleSpellPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(CycleSpellPacket::encode)
                .decoder(CycleSpellPacket::new)
                .consumerMainThread(CycleSpellPacket::handle)
                .add();
    }

    public static void sendTo(ServerPlayer player, Object msg) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), msg);
    }

    public static void sendToServer(Object msg) {
        CHANNEL.sendToServer(msg);
    }
}
