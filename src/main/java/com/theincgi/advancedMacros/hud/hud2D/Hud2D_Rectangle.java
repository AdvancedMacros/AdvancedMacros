package com.theincgi.advancedMacros.hud.hud2D;

import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;
import org.lwjgl.opengl.GL11;

import com.theincgi.advancedMacros.gui.Color;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class Hud2D_Rectangle extends Hud2DItem {
	//Color color = Color.BLACK; 
	int   colorInt;
	float wid, hei;
	float lastWid, lastHei;
	public Hud2D_Rectangle() {
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
		super.enableColorControl();
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
		drawRectangle(dx, dy, dw, dh, color, z);
	}

	/**@param dx - draw X
	 * @param dy - draw Y
	 * @param dw - draw Width
	 * @param dh - draw Height*/
	public static void drawRectangle(float dx, float dy, float dw, float dh, Color color, float z) {
		
		
		
		
//		//GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
//		
//		//GlStateManager.enableBlend();
//        GlStateManager.disableTexture2D();
//        GlStateManager.bindTexture(0);
//		//GlStateManager.enableAlpha();
//		//GL11.glEnable(GL11.GL_ALPHA);
//        
		float a = color.getA()/255f;
		
		//a = ((float) ((Math.sin(System.currentTimeMillis()%100000/500f)+1 )/2)) * 1f + .0f;
		a*=a;
		//System.out.println(a);
		GlStateManager.color(color.getR()/255f, color.getG()/255f, color.getB()/255f, a);//color.getA()/255f);
		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
//		
//		GlStateManager.disableBlend();
//		GlStateManager.enableBlend();
//	    
//		//GlStateManager.disableTexture2D();
//		//GlStateManager.glBlendEquation(GL11.GL_ADD);
//		GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_DST_ALPHA);
	    //GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
//		GlStateManager.enableAlpha();
//		GlStateManager.disableAlpha();
		buffer.begin(7, DefaultVertexFormats.POSITION); //7 is GL_QUADS btw
		buffer.pos(dx	  	, dy     	, z).endVertex(); //bottom left -> bottom right -> top right -> top left
		buffer.pos(dx       , dy+dh 	, z).endVertex(); //top left 
		buffer.pos(dx+dw   	, dy+dh   	, z).endVertex(); //top right
		buffer.pos(dx+dw 	, dy       	, z).endVertex(); //bottom right
		
		Tessellator.getInstance().draw();
		GlStateManager.enableTexture2D();
		//GlStateManager.disableBlend();
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
