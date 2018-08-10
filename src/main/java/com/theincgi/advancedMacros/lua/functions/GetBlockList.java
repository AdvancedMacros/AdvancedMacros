package com.theincgi.advancedMacros.lua.functions;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraftforge.common.MinecraftForge;

public class GetBlockList extends ZeroArgFunction{
	@Override
	public LuaValue call() {
		LuaTable t = new LuaTable();
		for(ResourceLocation r : Block.REGISTRY.getKeys()) {
			t.set(t.length()+1, r.getResourcePath());
		}
		return t;
	}

}
