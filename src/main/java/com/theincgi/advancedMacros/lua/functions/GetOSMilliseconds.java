package com.theincgi.advancedMacros.lua.functions;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ZeroArgFunction;

public class GetOSMilliseconds extends ZeroArgFunction{
	@Override
	public LuaValue call() {
		return LuaValue.valueOf(System.currentTimeMillis());
	}
}