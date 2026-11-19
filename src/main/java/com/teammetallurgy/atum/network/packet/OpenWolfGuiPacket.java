package com.teammetallurgy.atum.network.packet;

import com.teammetallurgy.atum.entity.animal.DesertWolfEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import java.util.function.Supplier;

public class OpenWolfGuiPacket {
	private final int wolfID;

	public OpenWolfGuiPacket(int wolfID) {
		this.wolfID = wolfID;
	}

	public static void encode(OpenWolfGuiPacket packet, FriendlyByteBuf buf) {
		buf.writeInt(packet.wolfID);
	}

	public static OpenWolfGuiPacket decode(FriendlyByteBuf buf) {
		return new OpenWolfGuiPacket(buf.readInt());
	}

	public static class Handler {
		public static void handle(OpenWolfGuiPacket message, Supplier<NetworkEvent.Context> ctx) {
			ServerPlayer player = ctx.get().getSender();
			if (player != null && !(player instanceof FakePlayer)
					&& player.getVehicle() instanceof DesertWolfEntity wolf
					&& wolf.getId() == message.wolfID && wolf.isAlpha() && wolf.isTame()) {
				NetworkHooks.openScreen(player, wolf, buffer -> buffer.writeInt(wolf.getId()));
			}
		}
	}
}
