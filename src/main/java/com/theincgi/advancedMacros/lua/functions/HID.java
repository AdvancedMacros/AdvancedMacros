package com.theincgi.advancedMacros.lua.functions;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import com.theincgi.advancedMacros.misc.HIDUtils;

public class HID {
	public static final String TYPE_KEYBOARD = "keyboard", TYPE_MOUSE = "mouse", TYPE_GAMEPAD = "gamepad", TYPE_JOYSTICK = "joystick";
	public static class GetHIDTypes extends ZeroArgFunction {
		@Override
		public LuaValue call() {
			LuaTable t = new LuaTable();
			t.set(1, TYPE_KEYBOARD);
			t.set(2, TYPE_MOUSE);
			t.set(3, TYPE_GAMEPAD);
			t.set(4, TYPE_JOYSTICK);
			return t;
		}
	}
	//TODO isCtrl isShift isAlt
	public static class GetHIDState extends VarArgFunction {
		@Override
		public Varargs invoke(Varargs args) {
			LuaTable out = new LuaTable();
			if(args.narg()==0) {
				out.set(TYPE_KEYBOARD, HIDUtils.Keyboard.getStateTable());
				out.set(TYPE_MOUSE, HIDUtils.Mouse.getStateTable());
				out.set(TYPE_GAMEPAD, HIDUtils.GamePad.getStateTable());
				out.set(TYPE_JOYSTICK, HIDUtils.Joystick.getStateTable());
			}else {
				LuaValue tmp;
				for(int i = 1; i<=args.narg(); i++) {
					switch(args.arg(i).checkjstring()) {
					case TYPE_KEYBOARD:
						tmp = HIDUtils.Keyboard.getStateTable();
						break;
					case TYPE_MOUSE:
						tmp = HIDUtils.Keyboard.getStateTable();
						break;
					case TYPE_GAMEPAD:
						tmp = HIDUtils.GamePad.getStateTable();
						break;
					case TYPE_JOYSTICK:
						tmp = HIDUtils.Joystick.getStateTable();
						break;
					default: tmp = NIL;
					}
					if(args.narg()==1) return tmp;
					out.set(args.arg(i).checkjstring(), tmp);
				}
			}
			return out;
		}
	}
}
