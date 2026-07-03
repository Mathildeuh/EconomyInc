package fr.fifoube.gui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class MaskedPinEditBox extends EditBox {

    public MaskedPinEditBox(net.minecraft.client.gui.Font font, int x, int y, int width, int height, Component hint) {
        super(font, x, y, width, height, hint);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        String realValue = this.getValue();
        if (!realValue.isEmpty()) {
            super.setValue("*".repeat(realValue.length()));
        }
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
        if (!realValue.isEmpty()) {
            super.setValue(realValue);
        }
    }
}
