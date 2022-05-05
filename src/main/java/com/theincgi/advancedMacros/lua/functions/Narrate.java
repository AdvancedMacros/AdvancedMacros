package com.theincgi.advancedMacros.lua.functions;

import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.TwoArgFunction;

import com.mojang.text2speech.Narrator;

public class Narrate extends TwoArgFunction{
	Narrator narrator = com.mojang.text2speech.Narrator.getNarrator();
	@Override
	public LuaValue call(LuaValue arg1, LuaValue arg2) {
		if(arg2.optboolean(false))
			narrator.clear();
		narrator.say(arg1.checkjstring());
		return NONE;
	}
}
