package com.theincgi.advancedMacros.hud.hud3D;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.lwjgl.opengl.GL11;

import com.theincgi.advancedMacros.gui.Color;
import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.CullFace;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class Hud3DElement{

	/**xyz is players acurate location for partial tick*/
	public static void render(LuaTable t, double x, double y, double z){
		if(Utils.tableFromProp(t, "opts.lighting", LuaValue.FALSE).checkboolean()){
			GlStateManager.enableLighting();
		}else{
			GlStateManager.disableLighting();
		}
		if(Utils.tableFromProp(t, "opts.alpha", LuaValue.TRUE).checkboolean()){
			GlStateManager.enableAlpha();
		}else{
			GlStateManager.disableAlpha();
		}
		if(Utils.tableFromProp(t, "opts.depth", LuaValue.TRUE).checkboolean()){
			GlStateManager.enableDepth();
		}else{
			GlStateManager.disableDepth();
		}
		if(Utils.tableFromProp(t, "opts.color", LuaValue.NIL).isnil()){
			GlStateManager.resetColor();
		}else{
			LuaValue v = Utils.tableFromProp(t, "opts.color", LuaValue.NIL);
			if(!v.isnil()){
				Color c = Utils.parseColor(v);
				GlStateManager.color(c.getR()/255f, c.getG()/255f, c.getB()/255f, c.getA()/255f);
			}
		}
		if(Utils.tableFromProp(t, "opts.cullFace", LuaValue.FALSE).checkboolean()){
			GlStateManager.cullFace(CullFace.FRONT_AND_BACK);
		}else{
			GlStateManager.cullFace(CullFace.FRONT);
		}
		
		LuaValue mode = Utils.tableFromProp(t, "opts.mode", LuaValue.valueOf("quad"));
		
		doGeomotryRender(t, x, y, z, mode.checkstring().tojstring());
	}



	public static void doGeomotryRender(LuaTable t, double pX, double pY, double pZ, String mode){
		LuaTable verts = Utils.tableFromProp(t, "", LuaValue.NIL).checktable();
		LuaValue texture = Utils.tableFromProp(t, "tex", LuaValue.NIL);
		LuaTable uv = null;
		boolean doUV = false;
		{
			LuaValue temp  = Utils.tableFromProp(t, "uv", LuaValue.NIL);
			if(!temp.isnil() && !texture.isnil()){
				uv = temp.checktable();
				doUV = true;
			}
		}
		mode = mode.toLowerCase();

		if(uv!=null){
			if(verts.length()!=uv.length()){
				throw new LuaError("UV and Vertex arrays of differnt size");
			}
		}
		
		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
		
		if(mode.equals("strip")){
			buffer.begin(GL11.GL_TRIANGLE_STRIP, texture.isnil()? DefaultVertexFormats.POSITION : DefaultVertexFormats.POSITION_TEX);
		}else if(mode.equals("fan")){
			buffer.begin(GL11.GL_TRIANGLE_FAN, texture.isnil()? DefaultVertexFormats.POSITION : DefaultVertexFormats.POSITION_TEX);
		}else{
			throw new LuaError("Undefined mode");
		}
		
		
		for(int i = 0; i<verts.length(); i++){
			LuaTable vert = verts.checktable();
			buffer.pos(vert.get(1).checkdouble()-pX, vert.get(2).checkdouble()-pY, vert.get(3).checkdouble()-pZ);
			if(doUV){
				LuaTable uvc = uv.get(i).checktable();
				buffer.tex(uvc.get(1).checkdouble(), uvc.get(2).checkdouble());
			}
			buffer.endVertex();
		}
		
//		buffer.pos(x, y, z).tex(uMin, vMin).endVertex();
//		buffer.pos(x, y+hei, z).tex(uMin, vMax).endVertex();
//		buffer.pos(x+hei, y+hei, z).tex(uMax, vMax).endVertex();
//		buffer.pos(x+hei, y, z).tex(uMax, vMin).endVertex();
		Tessellator.getInstance().draw();
	}





}