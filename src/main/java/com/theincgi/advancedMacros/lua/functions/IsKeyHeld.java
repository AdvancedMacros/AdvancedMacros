package com.theincgi.advancedMacros.lua.functions;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.lwjgl.glfw.GLFW;

import com.theincgi.advancedMacros.misc.HIDUtils.Keyboard;
import com.theincgi.advancedMacros.misc.HIDUtils.Mouse;

public class IsKeyHeld  extends OneArgFunction{
	@Override
	public LuaValue call(LuaValue arg0) {
		String s = arg0.checkjstring();
		switch (s) {
		case "LMB":
			return LuaValue.valueOf(Mouse.isDown(GLFW.GLFW_MOUSE_BUTTON_LEFT));
		case "RMB":
			return LuaValue.valueOf(Mouse.isDown(GLFW.GLFW_MOUSE_BUTTON_RIGHT));
		case "MMB":
			return LuaValue.valueOf(Mouse.isDown(GLFW.GLFW_MOUSE_BUTTON_MIDDLE));
		default:
			if(s.startsWith("MOUSE:")) {
				try {
				int index = Integer.parseInt(s.substring(s.lastIndexOf(":")+1));
				return LuaValue.valueOf(Mouse.isDown(index));
				}catch (Exception e) {
					throw new LuaError("Could not get MOUSE:"+INDEX);
				}
			}else{
				int in = Keyboard.codeOf(s);
				if(in==Keyboard.UNKNOWN_KEY_CODE)
					throw new LuaError("Could not get key \""+s+"\"");
				return LuaValue.valueOf(Keyboard.isDown(in));
			}
		}
	}
}