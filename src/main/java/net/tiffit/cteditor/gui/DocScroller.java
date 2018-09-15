package net.tiffit.cteditor.gui;

import java.util.List;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;

public class DocScroller {

	public int vScroll = 0;
	public int hScroll = 0;

	private int yOffset;
	private FontRenderer fr = Minecraft.getMinecraft().fontRenderer;

	public DocScroller(int yOffset) {
		this.yOffset = yOffset;
	}

	public void scroll(int scroll, List<String> lines) {
		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
			if (scroll > 0){
				hScroll -= 3;
				if(hScroll < 0)hScroll = 0;
			}else{
				hScroll += 3;
				int maxScroll = 0;
				for(String line : lines){
					int lineW = fr.getStringWidth(line);
					if(lineW > maxScroll)maxScroll = lineW;
				}
				if(hScroll > maxScroll && maxScroll > 0)hScroll = maxScroll;
			}
		} else {
			if (scroll > 0) {
				if (vScroll > 0)
					vScroll--;
			} else {
				if (lines.size() - getLinesPerPage() - vScroll > 0)
					vScroll++;
			}
		}
	}

	private int getLinesPerPage() {
		return (new ScaledResolution(Minecraft.getMinecraft()).getScaledHeight() - yOffset) / 10;
	}

}
