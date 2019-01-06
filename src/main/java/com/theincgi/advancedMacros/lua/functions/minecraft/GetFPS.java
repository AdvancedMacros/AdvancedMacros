package com.theincgi.advancedMacros.lua.functions.minecraft;

import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import com.theincgi.advancedMacros.lua.functions.MinecraftSettings;
import com.theincgi.advancedMacros.misc.CallableTable;

import net.minecraft.client.Minecraft;

public class GetFPS extends CallableTable {
	
	public GetFPS() {
		super(new String[] {"getFps"}, new Op());
	}
	
	private static class Op extends ZeroArgFunction {
		@Override
		public LuaValue call() {
			return valueOf(Minecraft.getDebugFPS());
		}
	}
}
