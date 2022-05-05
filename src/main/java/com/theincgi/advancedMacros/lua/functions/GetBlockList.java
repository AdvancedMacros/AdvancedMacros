package com.theincgi.advancedMacros.lua.functions;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.statemap.BlockStateMapper;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

public class GetBlockList extends ZeroArgFunction{
	@Override
	public LuaValue call() {
		LuaTable t = new LuaTable();
		for(ResourceLocation r : Block.REGISTRY.getKeys()) {
			Block b = Block.REGISTRY.getObject(r);
			Item item = Item.getItemFromBlock(Block.REGISTRY.getObject(r));
			
			if(item.getHasSubtypes()) {
				NonNullList<ItemStack> subtypes = NonNullList.create();
				item.getSubItems(CreativeTabs.SEARCH, subtypes);
				LuaTable types = new LuaTable();
				t.set(r.getNamespace()+":"+r.getPath(), types);
				for(ItemStack s : subtypes) {
					types.set(s.getItemDamage(), Utils.itemStackToLuatable(s));
				}
			}else {
				t.set(item.getRegistryName().toString(), Utils.itemStackToLuatable(new ItemStack(item)));
			}
			
		}
		return t;
	}

}
