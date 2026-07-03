/*******************************************************************************
 *******************************************************************************/
package fr.fifoube.packets;

import fr.fifoube.main.atm.AtmService;
import fr.fifoube.main.capabilities.CapabilityMoney;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketCardChange {

	private long amount;
	private boolean deposit;

	public PacketCardChange() {
	}

	public PacketCardChange(long amount, boolean deposit) {
		this.amount = amount;
		this.deposit = deposit;
	}

	public static PacketCardChange decode(FriendlyByteBuf buf) {
		long amount = buf.readLong();
		boolean deposit = buf.readBoolean();
		return new PacketCardChange(amount, deposit);
	}

	public static void encode(PacketCardChange packet, FriendlyByteBuf buf) {
		buf.writeLong(packet.amount);
		buf.writeBoolean(packet.deposit);
	}

	public static void handle(PacketCardChange packet, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Player player = ctx.get().getSender();
			if (!(player instanceof ServerPlayer)) {
				return;
			}
			if (!AtmService.isValidAmount(packet.amount)) {
				player.sendSystemMessage(Component.translatable("title.invalidAmount"));
				return;
			}
			if (!AtmService.canUseAtm(player)) {
				player.sendSystemMessage(Component.translatable("title.atmAccessDenied"));
				return;
			}
			player.getCapability(CapabilityMoney.MONEY_CAPABILITY, null).ifPresent(data -> {
				if (packet.deposit) {
					AtmService.deposit(player, data, packet.amount);
				} else {
					AtmService.withdraw(player, data, packet.amount);
				}
			});
		});
		ctx.get().setPacketHandled(true);
	}
}
