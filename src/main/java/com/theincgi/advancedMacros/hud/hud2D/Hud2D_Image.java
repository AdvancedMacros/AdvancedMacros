package com.theincgi.advancedMacros.hud.hud2D;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;
import org.lwjgl.opengl.GL11;

import com.theincgi.advancedMacros.lua.LuaValTexture;
import com.theincgi.advancedMacros.misc.Settings;
import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class Hud2D_Image extends Hud2D_Rectangle {
	LuaValTexture lvt = Utils.checkTexture(Settings.getTextureID("resource:holoblock.png"));
	float uMin, vMin, uMax=1, vMax=1;
	public Hud2D_Image() {
		super();
		
		lvt = Utils.checkTexture(Settings.getTextureID("resource:holoblock.png"));
		
		getControls().set("setImage", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue v) {
				setTexture(v);
				return LuaValue.NONE;
			}
		});
		getControls().set("getImage", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return lvt;
			}
		});
		getControls().set("setUV", new VarArgFunction() {
			@Override
			public Varargs invoke(Varargs args) {
				if(args.narg()<4){throw new LuaError("Not enough args (uMin, vMin, uMax, vMax)");}
				setUV((float)args.arg(1).checkdouble(), (float)args.arg(2).checkdouble(), (float)args.arg(3).checkdouble(), (float)args.arg(4).checkdouble());
				return LuaValue.NONE;
			}
		});
		super.enableColorControl();
	}
	
	public void setTexture(LuaValue v) {
		if(v instanceof LuaValTexture){
			lvt = (LuaValTexture) v;
		}else if(v.isstring()){
			lvt = Utils.checkTexture(Settings.getTextureID(v.checkjstring()));
		}else{
			lvt = Utils.checkTexture(Settings.getTextureID("resource:holoblock.png"));
		}
	}

	public void setUV(float uMin, float vMin, float uMax, float vMax) {
		this.uMin = uMin;
		this.vMin = vMin;
		this.uMax = uMax;
		this.vMax = vMax;
	}

	@Override
	public void render(float partialTicks) {
		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		float dx = x, dy = y, dw = wid, dh = hei;
		if(allowFrameInterpolation) {
			dx = interpolate(dx, lastX, partialTicks);
			dy = interpolate(dy, lastY, partialTicks);
			dw = interpolate(dw, lastWid, partialTicks);
			dh = interpolate(dh, lastHei, partialTicks);
		}
		
		
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		
		//GlStateManager.enableColorMaterial();
		Minecraft.getMinecraft().getTextureManager().bindTexture(lvt.getResourceLocation());
		
		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
		buffer.pos(dx     	, y     , z).tex(uMin, vMin).endVertex();
		buffer.pos(dx     	, y+dh	, z).tex(uMin, vMax).endVertex();
		buffer.pos(dx+dw	, y+dh	, z).tex(uMax, vMax).endVertex();
		buffer.pos(dx+dw	, y     , z).tex(uMax, vMin).endVertex();
		Tessellator.getInstance().draw();
		//GlStateManager.disableBlend();
		GL11.glPopAttrib();
	}
	
	
}
