package com.theincgi.advancedMacros.hud.hud2D;

import java.util.Scanner;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.misc.CustomFontRenderer;
import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.client.gui.FontRenderer;

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
				return LuaValue.valueOf(AdvancedMacros.getMinecraft().fontRenderer.getStringWidth(widestLine(text)) * (size/7.99f) );
			}
		});
		getControls().set("getHeight", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return LuaValue.valueOf(size*countLines(text));
			}
		});
		getControls().set("getSize", new VarArgFunction() {
			@Override
			public Varargs invoke(Varargs args) {
				LuaTable temp = new LuaTable();
				temp.set(1, LuaValue.valueOf(AdvancedMacros.getMinecraft().fontRenderer.getStringWidth(widestLine(text))));
				temp.set(2, LuaValue.valueOf(size *countLines(text)));
				return temp.unpack();
			}
		});
	}
	
	public String widestLine(String text) {
		String maxLine = "";
		Scanner temp = new Scanner(text);
		while(temp.hasNextLine()) {
			String m = temp.nextLine().replaceAll(Utils.mcSelectCode+"[0-9a-flkmnor]", "");
			if( m.length() > maxLine.length() )
				maxLine = m;
		}
		temp.close();

		return maxLine+" ";
	}

	public int countLines(String text) {
		int out = 1;
		for(int i = 0; i<text.length(); i++) {
			if(text.charAt(i)=='\n')
				out++;
		}
		return out;
	}

	@Override
	public void render(float partialTicks) {
		GlStateManager.pushMatrix();
		applyTransformation();
		float dx = 0, dy = 0;
		if(allowFrameInterpolation) {
			dx = interpolate(dx, lastX, partialTicks);
			dy = interpolate(dy, lastY, partialTicks);
		}
		if(monospaced) {
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			AdvancedMacros.customFontRenderer.renderText(dx, dy, z, text, getOpacity(), size);
		}else {
			Scanner s = new Scanner(text);
			//GlStateManager.enableBlend();
			//GlStateManager.enableAlpha();
			GlStateManager.disableAlphaTest();
			//GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			FontRenderer fr = AdvancedMacros.getMinecraft().fontRenderer;//AdvancedMacros.customFontRenderer;
			//float old = fr.FONT_HEIGHT;
			//fr.FONT_HEIGHT = (int)size;
			GlStateManager.pushMatrix();
			GlStateManager.translatef(0, 0, z);	//TESTME hud2d Z translate
			float sc = size / 7.99f;
			GlStateManager.scalef(sc, sc, 1);
			for(int i = 0; s.hasNextLine(); i+=size)
				fr.drawString(s.nextLine(), dx, dy+i/sc, color.toInt());//(text, (int)x, (int)y, color.toInt());
			GlStateManager.popMatrix();
			s.close();
			//GlStateManager.disableAlpha();
		}
		GlStateManager.bindTexture(0);
		//		AdvancedMacros.customFontRenderer.renderText(dx, dy, z, text, getOpacity(), size);
		//	GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}

}