package com.theincgi.advancedMacros.lua.scriptGui;

import com.theincgi.advancedMacros.gui.Color;
import com.theincgi.advancedMacros.gui.Gui;
import com.theincgi.advancedMacros.gui.elements.GuiRect;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class GuiRectangle extends ScriptGuiElement{
	public GuiRectangle(Gui gui, Group parent) {
		super(gui, parent);
		enableColorControl();
		enableSizeControl();
	}
	
	@Override
	public void onDraw(Gui g, int mouseX, int mouseY, float partialTicks) {
		super.onDraw(g, mouseX, mouseY, partialTicks);
		if(!visible) return;
		
		
		
		drawRectangle(x, y, wid, hei, color, z);
		if(getHoverTint()!=null && GuiRect.isInBounds(mouseX, mouseY, (int)x, (int)y, (int)wid, (int)hei)) {
			drawRectangle(x, y, wid, hei, getHoverTint(), z);
		}
	}

	@Override
	public int getItemHeight() {
		return (int)hei;
	}

	@Override
	public int getItemWidth() {
		return (int)wid;
	}

	@Override
	public void setWidth(int i) {
		this.wid = i;
	}

	@Override
	public void setHeight(int i) {
		this.hei = i;
	}
	
	public static void drawRectangle(float dx, float dy, float dw, float dh, Color color, float z) {
		
		
		
		
		//GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		
		//GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
		//GlStateManager.enableAlpha();
//		GlStateManager.enableBlend();
		GlStateManager.color(color.getR()/255f, color.getG()/255f, color.getB()/255f, color.getA()/255f);
		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		GlStateManager.disableBlend();
		GlStateManager.enableBlend();
	    //GlStateManager.disableTexture2D();
	    GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
	    GlStateManager.disableAlpha();
		GlStateManager.enableAlpha();
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
	
}
