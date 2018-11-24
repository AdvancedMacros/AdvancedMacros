package com.theincgi.advancedMacros.lua.functions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.TwoArgFunction;

import com.theincgi.advancedMacros.misc.CallableTable;
import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;


public class GetRecipe extends CallableTable {
	public static final String GUI_NAME = "guiName";
	public GetRecipe() {
		super(new String[]{"getRecipe"}, new TwoArgFunction() {

			@Override
			public LuaValue call(LuaValue arg, LuaValue dmg) {

				LuaTable types = new LuaTable();
				ItemStack is = new ItemStack(Item.getByNameOrId(arg.checkjstring()));
				is.setItemDamage(dmg.optint(0));

				//crafting
				{
					LuaTable recipes = new LuaTable();
					for(IRecipe r : getValid(is)) {
						recipes.set(recipes.length()+1, iRecipeToTable(r));
					}
					types.set("crafting", recipes);
				}
				//furnace
				{
					LuaTable recipes = new LuaTable();
					Map<ItemStack, ItemStack> list = FurnaceRecipes.instance().getSmeltingList();
					for(java.util.Map.Entry<ItemStack, ItemStack> e : list.entrySet()) {
						if(e.getValue().isItemEqual(is)) {
							LuaTable a = new LuaTable();
							a.set("type", "furnace");

							a.set(1, Utils.itemStackToLuatable(e.getKey()));
							recipes.set(recipes.length()+1, a);
						}
					}
					types.set("furnace", recipes);
				}

				return types;
			}
		});
	}
	//look familiar?
	public static List<IRecipe> getValid(ItemStack sItem){
		ArrayList<IRecipe> out = new ArrayList<IRecipe>();

		RegistryNamespaced<ResourceLocation, IRecipe> recipes = CraftingManager.REGISTRY;
		for (IRecipe iRecipe : recipes) {
			ItemStack is = iRecipe.getRecipeOutput();
			if(sItem.isItemEqual(is)){
				out.add(iRecipe);
			}
		}
		return out;
	}
	//very familiar?
	/**convert from grid size wid*hei to 3x3 for recipe*/
	protected static int realIndx(int i, int wid, int hei){ //fixme for iron door
		int x = i%wid;
		int y = (i-x)/wid;
		return x+y*3;
	}

	public static LuaValue iRecipeToTable(IRecipe r) {
		LuaTable t;

		if(r instanceof ShapedRecipes) {
			ShapedRecipes sr = (ShapedRecipes) r;
			t = dimTable(sr.recipeWidth, sr.recipeHeight);
			for (int i = 0; i < sr.recipeItems.size(); i++) {
				ItemStack[] match = sr.recipeItems.get(i).getMatchingStacks();
				int u = realIndx(i, sr.recipeWidth, sr.recipeHeight);
				int x = u%3, y  = u/3;

				for(int s = 0; s<match.length; s++) {
					t.get(x+1).get(y+1).set(s+1, Utils.itemStackToLuatable(match[s]));
				}
			}
			t.set("type", "shaped");
			return t;
		}else if(r instanceof ShapedOreRecipe) {
			ShapedOreRecipe sr = (ShapedOreRecipe) r;
			t = dimTable(sr.getRecipeWidth(), sr.getRecipeHeight());
			for (int i = 0; i < sr.getIngredients().size(); i++) {
				ItemStack[] match = sr.getIngredients().get(i).getMatchingStacks();
				int u = realIndx(i, sr.getRecipeWidth(), sr.getRecipeHeight());
				int x = u%3, y  = u/3;

				for(int s = 0; s<match.length; s++) {
					t.get(x+1).get(y+1).set(s+1, Utils.itemStackToLuatable(match[s]));
				}
			}
			t.set("type", "shaped ore");
			return t;

		}else if(r instanceof ShapelessRecipes) {
			ShapelessRecipes sr = (ShapelessRecipes) r;
			t = new LuaTable();
			for(int i = 0; i< sr.getIngredients().size(); i++) {
				ItemStack[] matching = sr.getIngredients().get(i).getMatchingStacks();
				for(int m = 0; m<matching.length; m++) {
					t.set(i+1, Utils.itemStackToLuatable(matching[m]));
				}
			}
			t.set("type", "shapeless");
			return t;
		}else if(r instanceof ShapelessOreRecipe) {
			ShapelessOreRecipe sr = (ShapelessOreRecipe) r;
			t = new LuaTable();
			for(int i = 0; i< sr.getIngredients().size(); i++) {
				ItemStack[] matching = sr.getIngredients().get(i).getMatchingStacks();
				for(int m = 0; m<matching.length; m++) {
					t.set(i+1, Utils.itemStackToLuatable(matching[m]));
				}
			}
			t.set("type", "shapeless ore");
			return t;
		}else {
			t = new LuaTable();
			for(int i = 0; i< r.getIngredients().size(); i++) {
				ItemStack[] matching = r.getIngredients().get(i).getMatchingStacks();
				for(int m = 0; m<matching.length; m++) {
					t.set(i+1, Utils.itemStackToLuatable(matching[m]));
				}
			}
			t.set("type", r.getClass().getName());
			return t;
		}
	}

	//indexed x,y, itemOption
	private static LuaTable dimTable(int width, int height) {
		LuaTable out = new LuaTable();
		for (int x = 1; x <= width; x++) {
			LuaTable tmp = new LuaTable();
			for(int y = 1; y<= height; y++) {
				tmp.set(y, new LuaTable());
			}
			out.set(x, tmp);
		}
		return out;
	}

}
