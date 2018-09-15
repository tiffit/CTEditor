package net.tiffit.cteditor.gui;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.tiffit.cteditor.Inserts;
import net.tiffit.cteditor.document.ZenDocument;
import net.tiffit.cteditor.document.ZenLine;

public class EditorOverlayInsertGui extends GuiScreen {

	private EditorGui bg;

	public EditorOverlayInsertGui(EditorGui bg) {
		this.bg = bg;
	}

	@Override
	public void initGui() {
		int index = 0;
		ScaledResolution sr = new ScaledResolution(mc);
		for(String key : Inserts.MAP.keySet()){
			buttonList.add(index, new InsertButton(index, sr, key));
			index++;
		}
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		bg.drawScreen(-1, -1, partialTicks);
		ScaledResolution sr = new ScaledResolution(mc);
		Gui.drawRect(0, 0, sr.getScaledWidth(), sr.getScaledHeight(), 0xcc000000);
		
		drawCenteredString(fontRenderer, "Insert", sr.getScaledWidth()/2, 20, 0xffffffff);
		Gui.drawRect(sr.getScaledWidth()/2 - 100, 30, sr.getScaledWidth()/2 + 100, sr.getScaledHeight() - 10, 0xffffffff);
		Gui.drawRect(sr.getScaledWidth()/2 - 100 + 1, 30 + 1, sr.getScaledWidth()/2 + 100 - 1, sr.getScaledHeight() - 10 -1, 0xff444444);
		
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		String text = Inserts.MAP.get(button.displayString);
		ZenDocument doc = bg.openDoc;
		if(doc.text.get(doc.pointer_line).line.trim().isEmpty()){
			doc.text.get(doc.pointer_line).line = text;
		}else{
			ZenLine line = new ZenLine(text);
			if(doc.pointer_line == doc.text.size() - 1)doc.text.add(line);
			else doc.text.add(doc.pointer_line + 1, line);
		}
		doc.pointer_line++;
		doc.pointer_line_index = 0;
		mc.displayGuiScreen(bg);
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode){
		if (keyCode == 1) {
			mc.displayGuiScreen(bg);
		}
	}

	private static class InsertButton extends GuiButton{

		public InsertButton(int id, ScaledResolution sr, String text) {
			super(id, sr.getScaledWidth()/2 - 100 + 2, 32 + id*14, 196, 13, text);
		}
		
		@Override
		public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
			drawRect(x, y, x + width, y + height, 0xffffffff);
			drawRect(x + 1, y + 1, x + width - 1, y + height - 1, 0xff444444);
			drawCenteredString(mc.fontRenderer, displayString, x + width/2, y + 3, 0xffffffff);
		}
		
	}
	
}
