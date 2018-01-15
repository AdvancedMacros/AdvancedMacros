package com.theincgi.advancedMacros.hud.hud2D;

import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import com.theincgi.advancedMacros.AdvancedMacros;

public class Hud2D_Text extends Hud2DItem {
	String text;
	float size; 
	
	public Hud2D_Text() {
		super();
		getControls().set("setText", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				text = arg.tojstring();
				return LuaValue.NONE;
			}
		});
		getControls().set("getText", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return LuaValue.valueOf(text);
			}
		});
		getControls().set("setSize", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				size = (float) arg.checkdouble();
				return LuaValue.NONE;
			}
		});
		getControls().set("getSize", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return LuaValue.valueOf(size);
			}
		});
		this.opacity = 0;
	}
	
	
	
	@Override
	public void render(float partialTicks) {
		float dx = x, dy = y;
		if(allowFrameInterpolation) {
			dx = interpolate(dx, lastX, partialTicks);
			dy = interpolate(dy, lastY, partialTicks);
		}
		AdvancedMacros.customFontRenderer.renderText(dx, dy, text, getOpacity());
	}

}
