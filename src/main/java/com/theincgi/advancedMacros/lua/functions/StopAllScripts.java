package com.theincgi.advancedMacros.lua.functions;

import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import com.theincgi.advancedMacros.AdvancedMacros;

public class StopAllScripts extends ZeroArgFunction{
	@Override
	public LuaValue call() {
		AdvancedMacros.stopAll();
		return LuaValue.NONE;
	}
}
