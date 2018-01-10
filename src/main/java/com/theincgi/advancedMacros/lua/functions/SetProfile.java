package com.theincgi.advancedMacros.lua.functions;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;

import com.theincgi.advancedMacros.AdvancedMacros;

public class SetProfile extends OneArgFunction{
	@Override
	public LuaValue call(LuaValue arg0) {
		return LuaValue.valueOf(AdvancedMacros.macroMenuGui.loadProfile(arg0.checkjstring()));
	}
}
