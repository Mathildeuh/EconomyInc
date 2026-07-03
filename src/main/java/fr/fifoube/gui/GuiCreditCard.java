/*******************************************************************************
 *******************************************************************************/
package fr.fifoube.gui;

import fr.fifoube.main.ModEconomyInc;
import fr.fifoube.main.atm.AtmService;
import fr.fifoube.main.capabilities.CapabilityMoney;
import fr.fifoube.packets.PacketCardChange;
import fr.fifoube.packets.PacketsRegistery;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.*;
import java.util.OptionalLong;

@OnlyIn(Dist.CLIENT)
public class GuiCreditCard extends Screen {

	private static final ResourceLocation background = new ResourceLocation(ModEconomyInc.MOD_ID ,"textures/gui/screen/gui_item.png");

	private EditBox amountField;
	private Button depositButton;
	private Button withdrawButton;

	private double funds;
	private String name = Minecraft.getInstance().player.getDisplayName().getString();

	protected int xSize = 256;
	protected int ySize = 124;
	protected int guiLeft;
	protected int guiTop;

	public GuiCreditCard() {
		super(Component.translatable("gui.creditcard"));
	}

	@Override
	protected void init() {
		super.init();
		this.guiLeft = (this.width - this.xSize) / 2;
		this.guiTop = (this.height - this.ySize) / 2;

		this.amountField = new EditBox(this.font, this.width / 2 - 60, this.height / 2 + 2, 120, 18, Component.translatable("title.amountInput"));
		this.amountField.setMaxLength(12);
		this.amountField.setFilter(text -> text.isEmpty() || text.matches("\\d+"));
		this.amountField.setHint(Component.translatable("title.amountInput"));
		this.addRenderableWidget(this.amountField);

		this.depositButton = this.addRenderableWidget(Button.builder(Component.translatable("title.deposit"), button -> submit(true))
				.pos(this.width / 2 - 125, this.height / 2 + 28).size(110, 20).build());
		this.withdrawButton = this.addRenderableWidget(Button.builder(Component.translatable("title.withdraw"), button -> submit(false))
				.pos(this.width / 2 + 15, this.height / 2 + 28).size(110, 20).build());

		this.setInitialFocus(this.amountField);
	}

	private void submit(boolean deposit) {
		OptionalLong amount = parseAmount(this.amountField.getValue());
		if (amount.isEmpty() || !AtmService.isValidAmount(amount.getAsLong())) {
			if (this.minecraft.player != null) {
				this.minecraft.player.displayClientMessage(Component.translatable("title.invalidAmount"), true);
			}
			return;
		}
		PacketsRegistery.CHANNEL.sendToServer(new PacketCardChange(amount.getAsLong(), deposit));
	}

	private static OptionalLong parseAmount(String text) {
		if (text == null || text.isBlank()) {
			return OptionalLong.empty();
		}
		try {
			return OptionalLong.of(Long.parseLong(text.trim()));
		} catch (NumberFormatException e) {
			return OptionalLong.empty();
		}
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == 256) {
			this.onClose();
			return true;
		}
		if (keyCode == 257 || keyCode == 335) {
			submit(true);
			return true;
		}
		return this.amountField.keyPressed(keyCode, scanCode, modifiers)
				|| super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void tick() {
		super.tick();
		this.amountField.tick();
		Player playerIn = Minecraft.getInstance().player;
		if (playerIn != null) {
			playerIn.getCapability(CapabilityMoney.MONEY_CAPABILITY, null).ifPresent(data -> this.funds = data.getMoney());
		}
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(guiGraphics);
		int i = this.guiLeft;
		int j = this.guiTop;
		guiGraphics.blit(background, this.guiLeft, this.guiTop, 0, 0, xSize, ySize);
		InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, i + 28, j + 58, 25,
				(float) (i + 51) - mouseX, (float) (j + 75 - 50) - mouseY, this.minecraft.player);
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		guiGraphics.drawString(this.font, Component.translatable("title.ownerCard", name), (this.width / 2) - 75, (this.height / 2) - 55, Color.DARK_GRAY.getRGB());
		guiGraphics.drawString(this.font, Component.translatable("title.fundsCard", funds), (this.width / 2) - 75, (this.height / 2) - 45, Color.DARK_GRAY.getRGB());
	}
}
