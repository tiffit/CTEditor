package net.tiffit.cteditor.gui;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

public class EditorOverlayExitGui extends GuiScreen {

	private EditorGui bg;

	public EditorOverlayExitGui(EditorGui bg) {
		this.bg = bg;
	}

	@Override
	public void initGui() {
		ScaledResolution sr = new ScaledResolution(mc);
		buttonList.add(new ExitButton(0, sr.getScaledWidth()/2 - 70, sr.getScaledHeight()/2, 69, 15, "Exit"));
		buttonList.add(new ExitButton(1, sr.getScaledWidth()/2, sr.getScaledHeight()/2, 69, 15, "Cancel"));
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		bg.drawScreen(-1, -1, partialTicks);
		ScaledResolution sr = new ScaledResolution(mc);
		Gui.drawRect(0, 0, sr.getScaledWidth(), sr.getScaledHeight(), 0xcc000000);
		
		drawCenteredString(fontRenderer, "You have unsaved changes!", sr.getScaledWidth()/2, sr.getScaledHeight()/2- 20, 0xffffffff);
		
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if(button.id == 0){
			mc.displayGuiScreen(null);
		}else{
			mc.displayGuiScreen(bg);
		}
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode){
		if (keyCode == 1) {
			mc.displayGuiScreen(bg);
		}
	}

	private static class ExitButton extends GuiButton{

		
		public ExitButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
			super(buttonId, x, y, widthIn, heightIn, buttonText);
		}

		@Override
		public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
			drawRect(x, y, x + width, y + height, 0xffffffff);
			drawRect(x + 1, y + 1, x + width - 1, y + height - 1, 0xff444444);
			drawCenteredString(mc.fontRenderer, displayString, x + width/2, y + 3, 0xffffffff);
		}
		
	}
	
}
