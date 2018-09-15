package net.tiffit.cteditor.document;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.tiffit.cteditor.gui.EditorColors;

public class ZenLine {

	public String line;
	private FontRenderer fr;
	public static final int startX = 95, startY = 20;

	public ZenLine(String line) {
		this.line = line.replaceAll("\u0009", "     ");
		fr = Minecraft.getMinecraft().fontRenderer;
	}

	public static boolean sel_blockcomment = false;
	private boolean sel_bracket = false;
	private boolean sel_string = false;
	private boolean sel_comment = false;

	public void render(int index, int line_number, int select_index) {
		sel_bracket = false;
		sel_string = false;
		sel_comment = false;
		drawString(line, startX, startY + index * 10);
		if (select_index != -1) {
			if (System.currentTimeMillis() % 1000 > 500 && this.line.length() >= select_index) {
				int pointerX = startX + fr.getStringWidth(this.line.substring(0, select_index)) - 1;
				Gui.drawRect(pointerX, startY + index * 10 - 2, pointerX + 1, startY + index * 10 + 8, 0xff777777);
				
			}
		}
	}

	public int getIndexPos(int pos) {
		if (line.length() == 0)
			return 0;
		for (int i = 1; i < line.length(); i++) {
			int width = fr.getStringWidth(line.substring(0, i));
			if (pos < width)
				return i - 1;
		}
		return line.length();
	}
	
	private void drawString(String str, int x, int y){
		int width = 0;
		char[] arr = str.toCharArray();
		for(int i = 0 ; i < arr.length; i++){
			char chr = arr[i];
			if(chr == '/' && getCharAt(str, i+1) == '*')sel_blockcomment = true;
			if(chr == '/' && getCharAt(str, i+1) == '/')sel_comment = true;
			if(chr == '<')sel_bracket = true;
			if(chr == '"' && getCharAt(str, i-1) != '\\')sel_string = !sel_string;
			if(chr == '/' && getCharAt(str, i+1) == '/')sel_comment = true;
			int color = EditorColors.TEXT_REGULAR;
			if(sel_blockcomment){
				color = EditorColors.TEXT_COMMENT;
				if(chr == '/' && getCharAt(str, i-1) == '*')sel_blockcomment = false;
			}
			else if(sel_comment)color = EditorColors.TEXT_COMMENT;
			else if(str.startsWith("import"))color = EditorColors.TEXT_IMPORT;
			else if(str.startsWith("#"))color = EditorColors.TEXT_LOADER;
			else if(sel_string || chr == '"')color = EditorColors.TEXT_STRING;
			else if(sel_bracket){
				color = EditorColors.TEXT_BRACKET;
				if(chr == '>')sel_bracket = false;
			}
			fr.drawString(chr + "", x + width, y, color);
			width += fr.getCharWidth(chr);
		}
	}

	private char getCharAt(String str, int index){
		try{
		return str.charAt(index);
		}catch(IndexOutOfBoundsException e){
			return '\u0000';
		}
	}
	
}
