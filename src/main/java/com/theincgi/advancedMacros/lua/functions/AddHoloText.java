package com.theincgi.advancedMacros.lua.functions;

import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import com.theincgi.advancedMacros.hud.hud3D.HudText;

public class AddHoloText extends ZeroArgFunction {
	
	@Override
	public LuaValue call() {
		HudText hudText = new HudText(true);
		return hudText.getControls(); 
	}
	
	
		
		
		
			
	
}
