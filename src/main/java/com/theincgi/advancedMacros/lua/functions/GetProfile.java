package com.theincgi.advancedMacros.lua.functions;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ZeroArgFunction;

import com.theincgi.advancedMacros.AdvancedMacros;

public class GetProfile extends ZeroArgFunction{
	@Override
	public LuaValue call() {
		return LuaValue.valueOf(AdvancedMacros.macroMenuGui.getProfileName());
	}
}
