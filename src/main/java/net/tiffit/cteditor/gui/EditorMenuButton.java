package net.tiffit.cteditor.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public class EditorMenuButton extends GuiButton {

	public EditorMenuButton(int buttonId,  String buttonText) {
		super(buttonId, 72 + buttonId*101, 1, 100, 14, buttonText);
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		drawRect(x, y, x + width, y + height, 0xffffffff);
		drawRect(x + 1, y + 1, x + width - 1, y + height - 1, 0xff444444);
		drawCenteredString(mc.fontRenderer, displayString, x + width/2, y + 3, 0xffffffff);
	}

}
