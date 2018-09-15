package net.tiffit.cteditor.gui;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import crafttweaker.CraftTweakerAPI;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextFormatting;
import net.tiffit.cteditor.document.ZenDocument;
import net.tiffit.cteditor.document.ZenLine;

public class EditorGui extends GuiScreen {

	private List<ZenDocument> documents = new ArrayList<ZenDocument>();
	public ZenDocument openDoc = null;

	private List<String> log;
	private DocScroller logScroll = new DocScroller(30);

	public EditorGui() {
		Keyboard.enableRepeatEvents(true);
		File scriptFolder = new File("./scripts/");
		if (scriptFolder.exists() && scriptFolder.isDirectory()) {
			File[] scripts = scriptFolder.listFiles((File dir, String name) -> {
				return name.endsWith(".zs");
			});
			for (File script : scripts)
				documents.add(new ZenDocument(script));
		}
	}

	private void loadLog() {
		log = new ArrayList<String>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader("./crafttweaker.log"));
			String line = null;
			while ((line = reader.readLine()) != null) {
				String str = line.trim();
				if(str.contains("[ERROR]"))str = TextFormatting.RED + str;
				log.add(str);
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		buttonList.add(new EditorMenuButton(0, "Save"));
		buttonList.add(new EditorMenuButton(1, "Insert"));
		buttonList.add(new EditorMenuButton(2, "Syntax"));
		buttonList.add(new EditorMenuButton(3, "Clear Logs"));
		buttonList.add(new EditorMenuButton(4, "Scripts Folder"));
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		ScaledResolution sr = new ScaledResolution(mc);
		drawGradientRect(0, 0, sr.getScaledWidth(), sr.getScaledHeight(), EditorColors.BACKGROUND, EditorColors.BACKGROUND);
		drawDocumentList();
		if (openDoc != null)
			openDoc.drawDocument();
		else {
			if (log == null)
				loadLog();
			drawString(fontRenderer, "Logs (" + log.size() + " lines)", 73, 20, 0xffcc8800);
			for (int i = logScroll.vScroll; i < log.size(); i++) {
				drawString(fontRenderer, "" + (i + 1), 73, 30 + (i - logScroll.vScroll) * 10, 0xff555555);
			}
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
			GL11.glScissor(95 * sr.getScaleFactor(), 0, mc.displayWidth, mc.displayHeight);
			GlStateManager.translate(-logScroll.hScroll, 0, 0);
			for (int i = logScroll.vScroll; i < log.size(); i++) {
				drawString(fontRenderer, log.get(i), 100, 30 + (i - logScroll.vScroll) * 10, 0xffffffff);
			}
			GlStateManager.translate(logScroll.hScroll, 0, 0);
			GL11.glDisable(GL11.GL_SCISSOR_TEST);
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	private void drawDocumentList() {
		drawString(fontRenderer, "Scripts", 2, 5, 0xffe5a620);
		for (int i = 0; i < documents.size(); i++) {
			ZenDocument document = documents.get(i);
			drawString(fontRenderer, (document.changes ? "*" : "") + document.file.getName(), 2, i * 10 + 20, document == openDoc ? 0xff888888 : 0xffffffff);
		}
		ScaledResolution sr = new ScaledResolution(mc);
		drawGradientRect(70, 0, sr.getScaledWidth(), sr.getScaledHeight(), EditorColors.BACKGROUND, EditorColors.BACKGROUND);
		drawVerticalLine(70, 0, sr.getScaledHeight(), 0xffffffff);
		drawHorizontalLine(0, sr.getScaledWidth(), 16, 0xffffffff);
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		int id = button.id;
		if (id == 0) {
			if (openDoc != null)
				openDoc.save();
		} else if (id == 1) {
			if (openDoc != null) {
				mc.displayGuiScreen(new EditorOverlayInsertGui(this));
			}
		} else if (id == 2) {
			CraftTweakerAPI.tweaker.loadScript(true, "#crafttweaker");
			loadLog();
		} else if (id == 3) {
			logScroll.vScroll = 0;
			new PrintWriter("./crafttweaker.log").close();
			loadLog();
		} else if (id == 4) {
			Desktop.getDesktop().open(new File("./scripts/"));
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		if (mouseX < 70 && mouseY > 20 && mouseX >= 0 && mouseY < documents.size() * 10 + 20) {
			ZenDocument newDoc = documents.get((mouseY - 20) / 10);
			if (newDoc == openDoc) {
				openDoc = null;
				loadLog();
			} else
				openDoc = newDoc;
		} else if (mouseX > 70 && mouseY > 20) {
			if (openDoc != null) {
				int index = (mouseY - 20) / 10 + openDoc.scroll.vScroll;
				if (index >= 0 && index < openDoc.text.size()) {
					ZenLine line = openDoc.text.get(index);
					openDoc.pointer_line = index;
					openDoc.pointer_line_index = line.getIndexPos(mouseX - 93 - openDoc.scroll.hScroll);
					openDoc.selection_line = -1;
				}
			}
			if (openDoc != null)
				openDoc.completer.active = false;
		}
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		if (mouseX > 70 && mouseY > 20) {
			if (openDoc != null) {
				int index = (mouseY - 20) / 10 + openDoc.scroll.vScroll;
				if (index >= 0 && index < openDoc.text.size()) {
					ZenLine line = openDoc.text.get(index);
					if (openDoc.selection_line == -1) {
						openDoc.selection_line = openDoc.pointer_line;
						openDoc.selection_line_index = openDoc.pointer_line_index;
					}
					openDoc.pointer_line = index;
					openDoc.pointer_line_index = line.getIndexPos(mouseX - 93 - openDoc.scroll.hScroll);
				}
			}
		}
	}

	@Override
	public void handleMouseInput() {
		try {
			super.handleMouseInput();
		} catch (IOException e) {
			e.printStackTrace();
		}
		int i = Integer.signum(Mouse.getEventDWheel());
		if (i != 0)
			mouseScrolled(i);
	}

	private void mouseScrolled(int scroll) {
		if (openDoc != null && openDoc.completer.active) {
			if (scroll > 0)
				openDoc.completer.scrollUp();
			else if (scroll < 0)
				openDoc.completer.scrollDown();
			return;
		}
		DocScroller scroller = getBestScroller();
		List<String> lines = new ArrayList<String>();
		if (openDoc == null)
			lines.addAll(log);
		else {
			for (ZenLine line : openDoc.text)
				lines.add(line.line);
		}
		scroller.scroll(scroll, lines);
	}

	public DocScroller getBestScroller() {
		if (openDoc == null)
			return logScroll;
		else
			return openDoc.scroll;
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) {
		if (keyCode == 1) {
			boolean unsaved = false;
			for (ZenDocument doc : documents)
				if (doc.changes)
					unsaved = true;
			if (unsaved)
				mc.displayGuiScreen(new EditorOverlayExitGui(this));
			else
				mc.displayGuiScreen(null);
			return;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
			if (openDoc != null) {
				if (keyCode == Keyboard.KEY_A) {
					openDoc.selection_line = 0;
					openDoc.selection_line_index = 0;
					openDoc.pointer_line = openDoc.text.size() - 1;
					openDoc.pointer_line_index = openDoc.text.get(openDoc.text.size() - 1).line.length();
				} else if (keyCode == Keyboard.KEY_S)
					openDoc.save();
				else if (keyCode == Keyboard.KEY_V)
					openDoc.paste();
				else if (keyCode == Keyboard.KEY_C)
					openDoc.copy();
				else if (keyCode == Keyboard.KEY_X) {
					openDoc.copy();
					openDoc.delete();
				}
			}
			return;
		}
		if (isValidCharacter(Character.getType(typedChar)) || ' ' == typedChar) {
			if (openDoc != null) {
				openDoc.type(typedChar);
			}
		}
		if (openDoc != null) {
			if (openDoc.movePointer(keyCode))
				openDoc.completer.active = false;
			if (keyCode == Keyboard.KEY_DELETE || keyCode == Keyboard.KEY_BACK)
				openDoc.delete();
			if (keyCode == Keyboard.KEY_RETURN)
				openDoc.enter();
		}
		try {
			super.keyTyped(typedChar, keyCode);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean isValidCharacter(int type) {
		return type == 1 || type == 2 || type == 9 || type == 21 || type == 22 || type == 23 || type == 24 || type == 25 || type == 26 || type == 27;
	}

}
