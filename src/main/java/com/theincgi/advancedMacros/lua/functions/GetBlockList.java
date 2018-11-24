package com.theincgi.advancedMacros.lua.functions;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;

public class GetBlockList extends ZeroArgFunction{
	@Override
	public LuaValue call() {
		LuaTable t = new LuaTable();
		for(ResourceLocation r : Block.REGISTRY.getKeys()) {
			t.set(t.length()+1, r.getNamespace()+":"+r.getPath());
		}
		return t;
	}

}
