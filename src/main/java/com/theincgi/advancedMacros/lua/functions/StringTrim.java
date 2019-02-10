package com.theincgi.advancedMacros.lua.functions;

import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;

public class StringTrim extends OneArgFunction{
	@Override
	public LuaValue call(LuaValue str) {
		return valueOf(str.tojstring().trim());
	}
}
