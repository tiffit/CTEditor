package net.tiffit.cteditor.document;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.tiffit.cteditor.gui.BracketCompleter;
import net.tiffit.cteditor.gui.DocScroller;
import net.tiffit.cteditor.gui.EditorColors;

public class ZenDocument {

	public File file;
	public int pointer_line = 0;
	public int pointer_line_index = 0;
	public int selection_line = -1;
	public int selection_line_index = 0;
	public List<ZenLine> text = new ArrayList<ZenLine>();
	public boolean changes = false;
	public DocScroller scroll = new DocScroller(20);
	public BracketCompleter completer = new BracketCompleter(this);

	public ZenDocument(String file) {
		this(new File(file));
	}

	public ZenDocument(File file) {
		this.file = file;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = reader.readLine()) != null) {
				text.add(new ZenLine(line));
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void drawDocument() {
		Minecraft mc = Minecraft.getMinecraft();
		ScaledResolution sr = new ScaledResolution(mc);
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor(85*sr.getScaleFactor(), 0, mc.displayWidth, mc.displayHeight);
		GlStateManager.translate(-scroll.hScroll, 0, 0);
		FontRenderer fr = mc.fontRenderer;
		if (selection_line >= 0) { // Selection stuff
			if (pointer_line == selection_line) {
				int minPos = Math.min(pointer_line_index, selection_line_index);
				int maxPos = Math.max(pointer_line_index, selection_line_index);
				ZenLine line = text.get(pointer_line);
				int leftPos = fr.getStringWidth(line.line.substring(0, minPos));
				int rightPos = fr.getStringWidth(line.line.substring(0, maxPos));
				int top = ZenLine.startY + pointer_line * 10;
				Gui.drawRect(ZenLine.startX + leftPos, top, ZenLine.startX + rightPos, top + 10, EditorColors.TEXT_SELECTION);
			} else {
				int minLine = Math.min(pointer_line, selection_line);
				String minLineText = text.get(minLine).line;
				int maxLine = Math.max(pointer_line, selection_line);
				String maxLineText = text.get(maxLine).line;
				for (int i = minLine + 1; i < maxLine; i++) {
					Gui.drawRect(ZenLine.startX, ZenLine.startY + i * 10, ZenLine.startX + fr.getStringWidth(text.get(i).line), ZenLine.startY + i * 10 + 10, EditorColors.TEXT_SELECTION);
				}
				int startPos = pointer_line == minLine ? pointer_line_index : selection_line_index;
				int endPos = pointer_line == minLine ? selection_line_index : pointer_line_index;
				Gui.drawRect(ZenLine.startX + fr.getStringWidth(minLineText.substring(0, startPos)), ZenLine.startY + minLine * 10, ZenLine.startX + fr.getStringWidth(minLineText), ZenLine.startY + minLine * 10 + 10, EditorColors.TEXT_SELECTION);
				Gui.drawRect(ZenLine.startX, ZenLine.startY + maxLine * 10, ZenLine.startX + fr.getStringWidth(maxLineText.substring(0, endPos)), ZenLine.startY + maxLine * 10 + 10, EditorColors.TEXT_SELECTION);
			}
		}
		for (int i = scroll.vScroll; i < text.size(); i++)text.get(i).render(i - scroll.vScroll, i, pointer_line == i ? pointer_line_index : -1);
		GlStateManager.translate(scroll.hScroll, 0, 0);
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
		for (int i = scroll.vScroll; i < text.size(); i++)fr.drawStringWithShadow("" + (i + 1), ZenLine.startX - 22, ZenLine.startY + (i-scroll.vScroll) * 10, 0xff555555);
		ZenLine.sel_blockcomment = false;
		
		
		
		completer.render();
	}

	public void save() {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			for (ZenLine line : text)
				writer.write(line.line + "\n");
			writer.close();
			changes = false;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean movePointer(int keycode) {
		if (keycode == Keyboard.KEY_UP) {
			if (completer.active)
				completer.scrollUp();
			else {
				if (pointer_line == 0)
					return true;
				pointer_line--;
				int length = text.get(pointer_line).line.length();
				if (pointer_line_index > length)
					pointer_line_index = length;
			}
		} else if (keycode == Keyboard.KEY_DOWN) {
			if (completer.active)
				completer.scrollDown();
			else {
				if (pointer_line == text.size() - 1)
					return true;
				pointer_line++;
				int length = text.get(pointer_line).line.length();
				if (pointer_line_index > length)
					pointer_line_index = length;
			}
		} else if (keycode == Keyboard.KEY_LEFT) {
			if (pointer_line_index == 0) {
				if (pointer_line == 0)
					return true;
				pointer_line--;
				pointer_line_index = text.get(pointer_line).line.length();
			} else
				pointer_line_index--;
		} else if (keycode == Keyboard.KEY_RIGHT) {
			if (pointer_line_index == text.get(pointer_line).line.length()) {
				if (pointer_line == text.size() - 1)
					return true;
				pointer_line++;
				pointer_line_index = 0;
			} else
				pointer_line_index++;
		}
		return keycode == Keyboard.KEY_LEFT || keycode == Keyboard.KEY_RIGHT;
	}

	public void type(char chr) {
		typeString(chr + "");
		completer.addPrefix(chr);
	}

	public void typeString(String str) {
		changes = true;
		ZenLine line = text.get(pointer_line);
		if (selection_line != -1)
			delete();
		if (pointer_line_index == 0)
			line.line = str + line.line;
		else if (pointer_line_index == line.line.length())
			line.line += str;
		else {
			String before = line.line.substring(0, pointer_line_index);
			String after = line.line.substring(pointer_line_index);
			line.line = before + str + after;
		}
		String[] lines = line.line.split("\n");
		if (lines.length > 1) {
			line.line = lines[0];
			for (int i = 1; i < lines.length; i++) {
				addLine(pointer_line + i, new ZenLine(lines[i]));
			}
			pointer_line += lines.length - 1;
			pointer_line_index = text.get(pointer_line).line.length();
		} else
			pointer_line_index += str.length();
	}

	private void addLine(int pos, ZenLine line) {
		if (pos >= text.size())
			text.add(line);
		else
			text.add(pos, line);
	}

	public void paste() {
		changes = true;
		completer.active = false;
		try {
			String clipboard = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
			typeString(clipboard);
		} catch (HeadlessException | UnsupportedFlavorException | IOException e) {
			e.printStackTrace();
		}
	}

	public void copy() {
		if (selection_line == -1)
			return;
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		String data = "";
		if (pointer_line == selection_line) {
			int minPos = Math.min(pointer_line_index, selection_line_index);
			int maxPos = Math.max(pointer_line_index, selection_line_index);
			data = text.get(pointer_line).line.substring(minPos, maxPos);
		} else {
			int minLine = Math.min(pointer_line, selection_line);
			int maxLine = Math.max(pointer_line, selection_line);
			int minPos = minLine == pointer_line ? pointer_line_index : selection_line_index;
			int maxPos = minLine == selection_line ? pointer_line_index : selection_line_index;
			data += text.get(minLine).line.substring(minPos) + "\n";
			String maxLineText = text.get(maxLine).line.substring(0, maxPos);
			for (int i = minLine + 1; i < maxLine; i++) {
				data += text.get(i).line + "\n";
			}
			data += maxLineText;
		}
		StringSelection ss = new StringSelection(data);
		clipboard.setContents(ss, ss);
	}

	public void delete() {
		changes = true;
		completer.deletePrefixChr();
		if (selection_line == -1) {
			ZenLine line = text.get(pointer_line);
			if (pointer_line_index == 0) {
				if (pointer_line == 0)
					return;
				pointer_line--;
				pointer_line_index = text.get(pointer_line).line.length();
				text.get(pointer_line).line += line.line;
				text.remove(line);
				int linesPerPage = (new ScaledResolution(Minecraft.getMinecraft()).getScaledHeight() - 20) / 10;
				if (text.size() >= linesPerPage && text.size() - linesPerPage - scroll.vScroll < 0)
					scroll.vScroll--;
			} else {
				String before = line.line.substring(0, pointer_line_index - 1);
				String after = line.line.substring(pointer_line_index);
				line.line = before + after;
				pointer_line_index--;
			}
		} else {
			if (selection_line == pointer_line) {
				String txt = text.get(selection_line).line;
				int minPos = Math.min(pointer_line_index, selection_line_index);
				int maxPos = Math.max(pointer_line_index, selection_line_index);
				text.get(selection_line).line = txt.substring(0, minPos) + txt.substring(maxPos);
			} else {
				int minLine = Math.min(pointer_line, selection_line);
				int maxLine = Math.max(pointer_line, selection_line);
				int minPos = minLine == pointer_line ? pointer_line_index : selection_line_index;
				int maxPos = minLine == selection_line ? pointer_line_index : selection_line_index;
				selection_line = -1;
				pointer_line = minLine;
				pointer_line_index = minPos;
				ZenLine lastLine = text.get(maxLine);
				text.get(minLine).line = text.get(minLine).line.substring(0, minPos) + lastLine.line.substring(maxPos);
				text.remove(lastLine);
				for (int i = minLine + 1; i < maxLine; i++) {
					text.remove(minLine + 1);
				}
			}
			selection_line = -1;
		}
	}

	public void enter() {
		changes = true;
		if (completer.active) {
			completer.input();
		} else {
			ZenLine line = text.get(pointer_line);
			String before = line.line.substring(0, pointer_line_index);
			String after = line.line.substring(pointer_line_index);
			line.line = before;
			ZenLine newLine = new ZenLine(after);
			addLine(pointer_line + 1, newLine);
			int linesPerPage = (new ScaledResolution(Minecraft.getMinecraft()).getScaledHeight() - 20) / 10;
			if (text.size() - linesPerPage - scroll.vScroll > 0)
				scroll.vScroll++;
			pointer_line++;
			pointer_line_index = 0;
		}
	}

}
