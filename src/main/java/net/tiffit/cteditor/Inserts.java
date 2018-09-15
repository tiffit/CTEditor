package net.tiffit.cteditor;

import java.util.LinkedHashMap;

public class Inserts {

	public static LinkedHashMap<String, String> MAP = new LinkedHashMap<String, String>();
	
	static{
		MAP.put("Add Shaped Recipe", "recipes.addShaped(\"\", <output>, [[null, null, null],[null, null, null], [null, null, null]]);");
		MAP.put("Remove Crafting Recipe", "recipes.remove(<output>);");
		MAP.put("Remove Crafting Recipe (NBT Match)", "recipes.remove(<output>, true);");
		MAP.put("Add Furnace Recipe", "furnace.addRecipe(<output>, <input>, xp);");
		MAP.put("Remove Furnace Recipe", "furnace.remove(<output>);");
		MAP.put("Set Furnace Fuel", "furnace.setFuel(<fuel>, time);");
		MAP.put("Add To Ore Dictionary", "<ore:>.add(<>);");
		MAP.put("Remove From Ore Dictionary", "<ore:>.remove(<>);");
	}
	
}
