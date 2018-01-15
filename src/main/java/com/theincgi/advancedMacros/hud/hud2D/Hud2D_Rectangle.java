package com.theincgi.advancedMacros.hud.hud2D;

import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;
import org.lwjgl.opengl.GL11;

import com.theincgi.advancedMacros.gui.Color;
import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class Hud2D_Rectangle extends Hud2DItem {
	Color color = Color.BLACK; 
	int   colorInt;
	float wid, hei;
	float lastWid, lastHei;
	public Hud2D_Rectangle() {
		getControls().set("setColor", new VarArgFunction() {
			@Override
			public Varargs invoke(Varargs args) {
				color = Utils.parseColor(args);
				colorInt = color.toInt();
				return LuaValue.NONE;
			}
		});
		getControls().set("getColor", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return color.toLuaValue();
			}
		});
		getControls().set("setWidth", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				wid = (float) arg.checkdouble();
				return LuaValue.NONE;
			}
		});
		getControls().set("setHeight", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				hei = (float) arg.checkdouble();
				return LuaValue.NONE;
			}
		});
		getControls().set("getWidth", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return LuaValue.valueOf(wid);
			}
		});
		getControls().set("getHeight", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return LuaValue.valueOf(hei);
			}
		});
	}

	@Override
	public void render(float partialTicks) {
		
		float dx = x, dy = y, dw = wid, dh = hei;
		if(allowFrameInterpolation) {
			dx = interpolate(dx, lastX, partialTicks);
			dy = interpolate(dy, lastY, partialTicks);
			dw = interpolate(dw, lastWid, partialTicks);
			dh = interpolate(dh, lastHei, partialTicks);
		}
		drawRectangle(dx, dy, dw, dh, color);
	}

	/**@param dx - draw X
	 * @param dy - draw Y
	 * @param dw - draw Width
	 * @param dh - draw Height*/
	public static void drawRectangle(float dx, float dy, float dw, float dh, Color color) {
		
		
		
		
		//GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		
		//GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
		
		GlStateManager.color(color.getR()/255f, color.getG()/255f, color.getB()/255f, color.getA()/255f);
		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		buffer.begin(7, DefaultVertexFormats.POSITION); //7 is GL_QUADS btw
		buffer.pos(dx	  	, dy     	, 0).endVertex(); //bottom left -> bottom right -> top right -> top left
		buffer.pos(dx       , dy+dh 	, 0).endVertex(); //top left 
		buffer.pos(dx+dw   	, dy+dh   	, 0).endVertex(); //top right
		buffer.pos(dx+dw 	, dy       	, 0).endVertex(); //bottom right
		
		Tessellator.getInstance().draw();
		GlStateManager.enableTexture2D();
       // GlStateManager.disableBlend();
       // Gui.drawRect((int)dx, (int)dy, (int)dx+dw, (int)dy+dh, color.toInt());
		//GL11.glPopAttrib();
	}
	
	@Override
	public void updateLastPos() {
		super.updateLastPos();
		lastWid = wid;
		lastHei = hei;
	}

}
