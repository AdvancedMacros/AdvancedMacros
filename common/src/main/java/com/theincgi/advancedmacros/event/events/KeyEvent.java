package com.theincgi.advancedmacros.event.events;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.theincgi.advancedmacros.event.Event;
import com.theincgi.advancedmacros.misc.HIDUtils;

public class KeyEvent extends Event {
	long window;
	int key, scancode, action, mods;
	
	public KeyEvent(long window, int key, int scancode, int action, int mods, CallbackInfo ci) {
		super(ci);
		this.window = window;
		this.key = key;
		this.scancode = scancode;
		this.action = action;
		this.mods = mods;
	}
	
	public String getValue() {
		String name = HIDUtils.Keyboard.nameOf(key);
		return name.equals(HIDUtils.Keyboard.UNKNOWN_KEY_NAME) ? HIDUtils.Mouse.nameOf(key) : name;
	}
	
	@Override
	public LuaTable createArgsTable() {
		LuaTable args = new LuaTable();
        args.set("action", LuaValue.valueOf(action == GLFW.GLFW_PRESS ? "down" : "up"));
        args.set("keyCode", LuaValue.valueOf(key));
        args.set("scanCode", LuaValue.valueOf(scancode));
        args.set("mods", HIDUtils.Keyboard.modifiersToLuaTable(mods));
        return args;
	}
}