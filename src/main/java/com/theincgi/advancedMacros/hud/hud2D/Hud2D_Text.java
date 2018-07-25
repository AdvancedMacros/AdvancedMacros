package com.theincgi.advancedMacros.hud.hud2D;

import java.util.Scanner;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;
import org.lwjgl.opengl.GL11;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.misc.CustomFontRenderer;
import com.theincgi.advancedMacros.misc.FontRendererOverride;
import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.client.renderer.GlStateManager;

public class Hud2D_Text extends Hud2DItem {
	String text = "";
	float size = 12; 

	boolean monospaced = false;

	public Hud2D_Text() {
		super();
		getControls().set("setText", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				String t = arg.tojstring();
				if(!monospaced)
					text = Utils.toMinecraftColorCodes(t);
				return LuaValue.NONE;
			}
		});
		getControls().set("getText", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return LuaValue.valueOf(text);
			}
		});
		getControls().set("setTextSize", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				size = (float) arg.checkdouble();
				return LuaValue.NONE;
			}
		});
		getControls().set("getTextSize", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return LuaValue.valueOf(size);
			}
		});
		getControls().set("getWidth", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return LuaValue.valueOf(CustomFontRenderer.measureWidth(text, size));
			}
		});
		getControls().set("getHeight", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return LuaValue.valueOf(CustomFontRenderer.measureHeight(text, size));
			}
		});
		getControls().set("getSize", new VarArgFunction() {
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
		if(monospaced) {
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			AdvancedMacros.customFontRenderer.renderText(dx, dy, z, text, getOpacity(), size);
		}else {
			Scanner s = new Scanner(text);
			GlStateManager.enableBlend();
			GlStateManager.enableAlpha();
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			FontRendererOverride fr = AdvancedMacros.otherCustomFontRenderer;
			//GlStateManager.bindTexture();
			float old = fr.FONT_HEIGHT;
			fr.FONT_HEIGHT = (int)size;
			for(int i = 0; s.hasNextLine(); i+=size)
				fr.renderText(dx, dy+i, z, s.nextLine(), getOpacity(), size);//(text, (int)x, (int)y, color.toInt());
			fr.FONT_HEIGHT = old;
			s.close();
		}
		GlStateManager.bindTexture(0);
		//		AdvancedMacros.customFontRenderer.renderText(dx, dy, z, text, getOpacity(), size);
		//	GlStateManager.disableBlend();
	}

}
