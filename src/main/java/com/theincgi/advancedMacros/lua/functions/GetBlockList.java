package com.theincgi.advancedMacros.lua.functions;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.Registry;

public class GetBlockList extends ZeroArgFunction{
	@Override
	public LuaValue call() {
		LuaTable t = new LuaTable();
		Registry.BLOCK.forEach((b)->{
			Item item = Item.getItemFromBlock(b);//Item.getItemFromBlock(b);
			
			t.set(item.getRegistryName().toString(), Utils.itemStackToLuatable(new ItemStack(item)));
			
		});
		return t;
	}

}
