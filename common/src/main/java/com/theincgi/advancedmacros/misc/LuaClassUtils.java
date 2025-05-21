package com.theincgi.advancedmacros.misc;

import org.luaj.vm2_v3_0_1.LuaValue;

import com.theincgi.advancedmacros.AdvancedMacros;

public class LuaClassUtils {
	
	public static LuaValue require(String name) {
		return AdvancedMacros.globals.get("require").call(name);
	}
	
	public static LuaValue newInstance(String className) {
		var iClass = LuaClassUtils.require(className);
		return iClass.get("new").call(iClass);
	}
	
	public static LuaValue newInstance(String className, LuaValue arg) {
		var iClass = LuaClassUtils.require(className);
		return iClass.get("new").call(iClass, arg);
	}
	
	public static boolean instanceOf(LuaValue v, String className) {
		var cls = require(className);
		return AdvancedMacros.globals.get("instanceOf").call(v, cls).checkboolean();
	}
	
}
