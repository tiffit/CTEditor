package net.tiffit.cteditor.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.tiffit.cteditor.document.ZenDocument;
import net.tiffit.cteditor.document.ZenLine;

public class BracketCompleter {

	private final int shownPerPage = 10;

	private final ZenDocument doc;
	public boolean active = false;
	private String prefix = "";
	private int scrollAmount = 0;
	private List<String> possibilities = new ArrayList<String>();

	public BracketCompleter(ZenDocument doc) {
		this.doc = doc;
	}

	public void render() {
		if (active) {
			FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
			List<String> currentView = new ArrayList<String>();
			int maxWidth = 0;
			if (possibilities.size() == 0) {
				currentView.add("No Item Found!");
				maxWidth = fr.getStringWidth("No Item Found!");
			} else {
				for (int i = scrollAmount; i < Math.min(possibilities.size(), scrollAmount + shownPerPage); i++) {
					String val = possibilities.get(i);
					currentView.add(val);
					int newWidth = fr.getStringWidth(val + " " + getItemName(val));
					if (newWidth > maxWidth)
						maxWidth = newWidth;
				}
			}
			maxWidth += 5;
			int leftPos = fr.getStringWidth(doc.text.get(doc.pointer_line).line.substring(0, doc.pointer_line_index)) + ZenLine.startX + 3 + doc.scroll.hScroll;
			int topPos = ZenLine.startY + doc.pointer_line * 10 + 11;
			Gui.drawRect(leftPos, topPos, leftPos + maxWidth, topPos + currentView.size() * 10 + 1, 0xffffffff);
			Gui.drawRect(leftPos + 1, topPos + 1, leftPos + maxWidth - 1, topPos + currentView.size() * 10, 0xff181818);
			for (int i = 0; i < currentView.size(); i++) {
				fr.drawString(currentView.get(i), leftPos + 3, topPos + 2 + i * 10, i == 0 ? 0xf4bc42 : 0xffffffff);
				fr.drawString(getItemName(currentView.get(i)), leftPos + 3 + fr.getStringWidth(currentView.get(i) + " "), topPos + 2 + i * 10, 0xff909090);
			}
		}
	}

	private String getItemName(String rsString) {
		String[] values = rsString.split(":");
		if(values.length < 2)return "";
		Item item = Item.REGISTRY.getObject(new ResourceLocation(values[0], values[1]));
		return new ItemStack(item, 1, values.length == 3 ? Integer.valueOf(values[2]) : 0).getDisplayName();
	}

	public void scrollUp() {
		if (scrollAmount > 0)
			scrollAmount--;
	}

	public void scrollDown() {
		if (scrollAmount < possibilities.size() - 1)
			scrollAmount++;
	}

	public void input() {
		String addition = possibilities.get(scrollAmount).substring(prefix.length()) + ">";
		doc.typeString(addition);
		active = false;
	}

	public void addPrefix(char chr) {
		if (chr == '<') {
			active = true;
			prefix = "";
			getPossibilities();
		} else if (chr == '>') {
			active = false;
			prefix = "";
		} else if (active) {
			prefix += chr;
			getPossibilities();
		}
		scrollAmount = 0;
	}

	public void deletePrefixChr() {
		if (active) {
			if (prefix.isEmpty()) {
				active = false;
				prefix = "";
			} else {
				prefix = prefix.substring(0, prefix.length() - 1);
				getPossibilities();
			}
		}
		scrollAmount = 0;
	}

	public void getPossibilities() {
		possibilities.clear();
		for (ResourceLocation loc : Item.REGISTRY.getKeys()) {
			String locString = loc.toString().toLowerCase();
			Item item = Item.REGISTRY.getObject(loc);
			if (item.getHasSubtypes()) {
				NonNullList<ItemStack> stackList = NonNullList.create();
				item.getSubItems(CreativeTabs.SEARCH, stackList);
				List<Integer> metaValues = new ArrayList<Integer>();
				List<ItemStack> toUseStacks = new ArrayList<ItemStack>();
				for (ItemStack is : stackList) {
					if (!metaValues.contains(is.getMetadata())) {
						metaValues.add(is.getMetadata());
						toUseStacks.add(is);
					}
				}
				for (ItemStack stack : toUseStacks) {
					addIfMatches(locString + ":" + stack.getMetadata());
				}
			} else {
				addIfMatches(locString);
			}
		}
	}

	private void addIfMatches(String match) {
		if (match.startsWith(prefix.toLowerCase()))
			possibilities.add(match);
	}

}
