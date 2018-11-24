package com.theincgi.advancedMacros.lua.scriptGui;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;
import org.lwjgl.opengl.GL11;

import com.theincgi.advancedMacros.gui.Color;
import com.theincgi.advancedMacros.gui.Gui;
import com.theincgi.advancedMacros.gui.elements.GuiRect;
import com.theincgi.advancedMacros.lua.LuaValTexture;
import com.theincgi.advancedMacros.misc.Settings;
import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class GuiImage extends ScriptGuiElement{
	LuaValTexture lvt = Utils.checkTexture(Settings.getTextureID("resource:holoblock.png"));
	float uMin=0, uMax=1, vMin=0, vMax=1;
	public GuiImage(Gui gui, Group parent) {
		super(gui, parent);
		enableColorControl();
		enableSizeControl();
		color = Color.WHITE;
		set("setImage", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue v) {
				setTexture(v);
				return LuaValue.NONE;
			}
		});
		set("getImage", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return lvt;
			}
		});
		set("setUV", new VarArgFunction() {
			@Override
			public Varargs invoke(Varargs args) {
				if(args.narg()<4){throw new LuaError("Not enough args (uMin, vMin, uMax, vMax)");}
				setUV((float)args.arg(1).checkdouble(), (float)args.arg(2).checkdouble(), (float)args.arg(3).checkdouble(), (float)args.arg(4).checkdouble());
				return LuaValue.NONE;
			}
		});
		
	}
	
	@Override
	public void onDraw(Gui g, int mouseX, int mouseY, float partialTicks) {
		super.onDraw(g, mouseX, mouseY, partialTicks);
		if(!visible) return;
		
		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		GlStateManager.pushAttrib();
		float dx = x, dy = y, dw = wid, dh = hei;
		
		
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		//GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_COLOR, DestFactor.SRC_COLOR, SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		
//		GlStateManager.disableAlpha();
//		GlStateManager.enableAlpha();
////////		
		GlStateManager.disableBlend();
		GlStateManager.enableBlend();
//////		
////		GlStateManager.disableTexture2D();
//		GlStateManager.enableTexture2D();
		GlStateManager.color(color.getR()/255f, color.getG()/255f, color.getB()/255f, color.getA()/255f);
		//GlStateManager.color(1,0,0);
		
		
		//GlStateManager.enable
		
		//GlStateManager.enableColorMaterial();
		if(lvt!=null)
			lvt.bindTexture();
		
		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
		buffer.pos(dx     	, dy     , z).tex(uMin, vMin).endVertex();
		buffer.pos(dx     	, dy+dh	, z).tex(uMin, vMax).endVertex();
		buffer.pos(dx+dw	, dy+dh	, z).tex(uMax, vMax).endVertex();
		buffer.pos(dx+dw	, dy     , z).tex(uMax, vMin).endVertex();
		Tessellator.getInstance().draw();
		//GlStateManager.disableBlend();
		GlStateManager.popAttrib();
		GL11.glPopAttrib();
		
		if(getHoverTint()!=null && GuiRect.isInBounds(mouseX, mouseY, (int)x, (int)y, (int)wid, (int)hei)) {
			GuiRectangle.drawRectangle(x, y, wid, hei, getHoverTint(), z);
		}
	}
	
	public void setTexture(LuaValue v) {
		lvt = Utils.parseTexture(v);
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
	public void setUV(float uMin, float vMin, float uMax, float vMax) {
		this.uMin = uMin;
		this.vMin = vMin;
		this.uMax = uMax;
		this.vMax = vMax;
	}
}
