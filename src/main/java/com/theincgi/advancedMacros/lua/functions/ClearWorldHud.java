package com.theincgi.advancedMacros.lua.functions;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ZeroArgFunction;

import com.theincgi.advancedMacros.AdvancedMacros;

public class ClearWorldHud extends ZeroArgFunction{
	@Override
	public LuaValue call() {
		AdvancedMacros.forgeEventHandler.clearWorldHud();
		return LuaValue.NONE;
	}
}