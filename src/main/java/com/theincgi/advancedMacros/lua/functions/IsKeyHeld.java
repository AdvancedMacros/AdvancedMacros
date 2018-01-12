package com.theincgi.advancedMacros.lua.functions;

import java.util.ArrayList;
import java.util.HashMap;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.lwjgl.input.Keyboard;

import com.theincgi.advancedMacros.AdvancedMacros;

public class IsKeyHeld  extends OneArgFunction{
	@Override
	public LuaValue call(LuaValue arg0) {
		String s = arg0.checkjstring();
		HashMap<Integer, Boolean> heldKeys = AdvancedMacros.forgeEventHandler.getHeldKeys();
		ArrayList<Boolean> heldMouseButtons = AdvancedMacros.forgeEventHandler.getHeldMouseButtons();
		switch (s) {
		case "LMB":
			return LuaValue.valueOf(heldMouseButtons.get(0));
		case "RMB":
			return LuaValue.valueOf(heldMouseButtons.get(1));
		case "MMB":
			return LuaValue.valueOf(heldMouseButtons.get(2));
		default:
			if(s.startsWith("MOUSE:")) {
				try {
				int index = Integer.parseInt(s.substring(s.lastIndexOf(":")+1));
				return LuaValue.valueOf(heldMouseButtons.get(index));
				}catch (Exception e) {
					throw new LuaError("Could not get MOUSE:"+INDEX);
				}
			}else{
				int in = Keyboard.getKeyIndex(s);
				if(in==Keyboard.KEY_NONE)
					throw new LuaError("Could not get key \""+s+"\"");
				return LuaValue.valueOf(heldKeys.getOrDefault(in, false));
			}
		}
	}
}