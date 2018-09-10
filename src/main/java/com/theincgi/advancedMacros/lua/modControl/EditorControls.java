package com.theincgi.advancedMacros.lua.modControl;

import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.TwoArgFunction;

import com.theincgi.advancedMacros.AdvancedMacros;

public class EditorControls {
	
	public static class JumpToLine extends TwoArgFunction {

		@Override
		public LuaValue call(LuaValue arg1, LuaValue arg2) {
			int x,y;
			if(arg2.isnil()) {
				x = 0;
				y = arg1.checkint();
			}else {
				x = arg1.checkint();
				y = arg2.checkint();
			}
			AdvancedMacros.editorGUI.getCta().jumpToLine(x, y);
			return null;
		}
		
	} 
}
