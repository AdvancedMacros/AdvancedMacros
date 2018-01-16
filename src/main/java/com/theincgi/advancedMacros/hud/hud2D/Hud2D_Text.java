package com.theincgi.advancedMacros.hud.hud2D;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;
import org.lwjgl.opengl.GL11;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.misc.CustomFontRenderer;

public class Hud2D_Text extends Hud2DItem {
	String text = "";
	float size = 12; 
	
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
		getControls().set("measureWidth", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return LuaValue.valueOf(CustomFontRenderer.measureWidth(text, size));
			}
		});
		getControls().set("measureHeight", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return LuaValue.valueOf(CustomFontRenderer.measureHeight(text, size));
			}
		});
		getControls().set("measure", new VarArgFunction() {
			@Override
			public Varargs invoke(Varargs args) {
				LuaTable temp = new LuaTable();
				temp.set(1, LuaValue.valueOf(CustomFontRenderer.measureWidth(text, size)));
				temp.set(2, LuaValue.valueOf(CustomFontRenderer.measureHeight(text, size)));
				return temp.unpack();
			}
		});
	}
	
	
	
	@Override
	public void render(float partialTicks) {
		float dx = x, dy = y;
		if(allowFrameInterpolation) {
			dx = interpolate(dx, lastX, partialTicks);
			dy = interpolate(dy, lastY, partialTicks);
		}
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		AdvancedMacros.customFontRenderer.renderText(dx, dy, z, text, getOpacity(), size);
	}

}
