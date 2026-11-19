package com.teammetallurgy.atum.network;

import com.teammetallurgy.atum.Atum;
import com.teammetallurgy.atum.network.packet.OpenWolfGuiPacket;
import com.teammetallurgy.atum.network.packet.SyncArrowFlightPathPacket;
import com.teammetallurgy.atum.network.packet.SyncHandStackSizePacket;
import com.teammetallurgy.atum.network.packet.weather.StormStrengthPacket;
import com.teammetallurgy.atum.network.packet.weather.WeatherPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "ATUM2";
    private static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(Atum.MOD_ID, "atum_channel"))
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .simpleChannel();

    public static void initialize() {
        CHANNEL.messageBuilder(OpenWolfGuiPacket.class, 0, NetworkDirection.PLAY_TO_SERVER)
                .encoder(OpenWolfGuiPacket::encode).decoder(OpenWolfGuiPacket::decode)
                .consumerMainThread(OpenWolfGuiPacket.Handler::handle).add();
        CHANNEL.messageBuilder(WeatherPacket.class, 1, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(WeatherPacket::encode).decoder(WeatherPacket::decode)
                .consumerMainThread(WeatherPacket.Handler::handle).add();
        CHANNEL.messageBuilder(StormStrengthPacket.class, 2, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(StormStrengthPacket::encode).decoder(StormStrengthPacket::decode)
                .consumerMainThread(StormStrengthPacket.Handler::handle).add();
        CHANNEL.messageBuilder(SyncHandStackSizePacket.class, 3, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncHandStackSizePacket::encode).decoder(SyncHandStackSizePacket::decode)
                .consumerMainThread(SyncHandStackSizePacket.Handler::handle).add();
        CHANNEL.messageBuilder(SyncArrowFlightPathPacket.class, 4, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncArrowFlightPathPacket::encode).decoder(SyncArrowFlightPathPacket::decode)
                .consumerMainThread(SyncArrowFlightPathPacket.Handler::handle).add();
    }

    public static void sendTo(ServerPlayer playerMP, Object toSend) {
        CHANNEL.sendTo(toSend, playerMP.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendToServer(Object msg) {
        CHANNEL.sendToServer(msg);
    }

    public static void sendToDimension(Object packet, ServerLevel serverLevel, ResourceKey<Level> dimension) {
        PlayerList playerList = serverLevel.getServer().getPlayerList();
        for (int i = 0; i < playerList.getPlayerCount(); ++i) {
            ServerPlayer serverPlayer = playerList.getPlayers().get(i);
            if (serverPlayer.level().dimension() == dimension) {
                sendTo(serverPlayer, packet);
            }
        }
    }

    /*
     * Used to update TESRs
     */
    public static void sendToTracking(ServerLevel level, BlockPos blockPos, Packet<?> packet, boolean boundaryOnly) {
        level.getChunkSource().chunkMap.getPlayers(new ChunkPos(blockPos), boundaryOnly).forEach(p -> p.connection.send(packet));
    }
}
