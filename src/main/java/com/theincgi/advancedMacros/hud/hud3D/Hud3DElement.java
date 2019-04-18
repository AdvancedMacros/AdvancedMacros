package com.theincgi.advancedMacros.hud.hud3D;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;
import org.lwjgl.opengl.GL11;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.gui.Color;
import com.theincgi.advancedMacros.lua.LuaValTexture;
import com.theincgi.advancedMacros.misc.Settings;
import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.CullFace;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;

public class Hud3DElement extends WorldHudItem{
	//public LuaTable data;
	LuaValTexture texture;// = Utils.checkTexture("resource:holoblock.png");
	
	public Hud3DElement() {
		
	}
	
	@Override
	public void loadControls(LuaValue t) {
		super.loadControls(t);
		enableColorControls(t);
		
		LuaTable data = new LuaTable();
		data.set("lighting", false);
		//data.set("alphaTest", true);
		data.set("depthTest", true);
		data.set("cullFace", "front and back");
		data.set("mode", "triangles");
		data.set("verts", new LuaTable());
		data.set("uv", new LuaTable());
		t.set("data", data);
		
		for(ElementOps op: ElementOps.values())
			t.set(op.name(), new DoElementOp(op));
	}
	
	public class DoElementOp extends VarArgFunction {
		ElementOps op;
		
		public DoElementOp(ElementOps op) {
			this.op = op;
		}

		@Override
		public Varargs invoke(Varargs args) {
			switch (op) {
			case setTexture:
				texture = Utils.parseTexture(args.arg1());
				return NONE;
			default:
				throw new LuaError("Unimplemented function");
			}
		}
	}
	public enum ElementOps {
		setTexture
	}
	
	@Override
	public void render(double playerX, double playerY, double playerZ) {
		try {
			GlStateManager.pushMatrix();
			GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
			GlStateManager.translate(-playerX, -playerY, -playerZ);
			GlStateManager.translate(x, y, z);
			
			GlStateManager.rotate(roll, 0, 0, 1);
			GlStateManager.rotate(pitch, 1, 0, 0);
			GlStateManager.rotate(yaw, 0, 1, 0);
			
			
			GlStateManager.translate(-x, -y, -z);
			GlStateManager.translate(playerX, playerY, playerZ);
			
			
			GlStateManager.disableCull();
			color.apply();
			
			render(getControls().get("data").checktable(), playerX, playerY, playerZ);
			
			GL11.glPopAttrib();
			GlStateManager.popMatrix();
		}catch (Throwable e) {
			Utils.logError(e);
			disableDraw();
		}
	}
	
	private void drawSideFace(double px, double py, double pz, double xWid, double yHei, double zWid){
		double x = this.x - px;
		double y = this.y - py;
		double z = this.z - pz;

		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		buffer.begin(7, DefaultVertexFormats.POSITION);
		buffer.pos(x     , y     , z     ).endVertex();
		buffer.pos(x     , y+yHei, z     ).endVertex();
		buffer.pos(x+xWid, y+yHei, z+zWid) .endVertex();
		buffer.pos(x+xWid, y     , z+zWid).endVertex();
		Tessellator.getInstance().draw();
	}
	
	/**xyz is players acurate location for partial tick*/
	private void render(LuaTable t, double playerX, double playerY, double playerZ){
//		if(t.get("lighting").checkboolean()){
//			GlStateManager.enableLighting();
//		}else{
//			GlStateManager.disableLighting();
//		}
//		if(t.get("alphaTest").checkboolean()){
//			GlStateManager.enableAlpha();
//		}else{
//			GlStateManager.disableAlpha();
//		}
//		if(t.get("depthTest").checkboolean()){
//			GlStateManager.enableDepth();
//		}else{
//			GlStateManager.disableDepth();
//		}
//		String cullSide = t.get("cullFace").checkjstring().toLowerCase();
//		if(cullSide.equals("back")){
//			GlStateManager.cullFace(CullFace.BACK);
//		}else if(cullSide.equals("front")){
//			GlStateManager.cullFace(CullFace.FRONT);
//		}else if(cullSide.equals("front and back")){
//			GlStateManager.cullFace(CullFace.FRONT_AND_BACK);
//		}else {
//			GlStateManager.cullFace(CullFace.FRONT);
//		}
		String mode = t.get("mode").checkjstring();
		//drawSideFace(playerX, playerY, playerZ, 1, 1, 0);
		doGeomotryRender(t, playerX, playerY, playerZ, mode);
		
	}



	private void doGeomotryRender(LuaTable t, double playerX, double playerY, double playerZ, String mode){
		try {
			drawSideFace(playerX, playerY, playerZ, 1, 1, 0);
			boolean useTexture = !(texture==null) && !texture.isnil() && t.get("uv").istable();
			VertexFormat type = DefaultVertexFormats.POSITION;
			if(useTexture) {
				texture.bindTexture();
				GlStateManager.enableTexture2D();
				type = DefaultVertexFormats.POSITION_TEX;
			}else {
				GlStateManager.disableTexture2D();
			}
			
			int gLMode = mode.equals("strip")? GL11.GL_TRIANGLE_STRIP :
				         mode.equals("fan")? GL11.GL_TRIANGLE_FAN :
				         mode.equals("quads")? GL11.GL_QUADS:
			        	 mode.equals("triangles")? GL11.GL_TRIANGLES:
		        		 mode.equals("quadStrip")? GL11.GL_QUAD_STRIP : -1;
			if(gLMode == -1) throw new LuaError("Invalid GL Mode in hud3D object");
			
			LuaValue verts = t.get("verts");
			if(verts.length() < 3) throw new LuaError("Hud3D object missing vertex data");
			if(gLMode == GL11.GL_TRIANGLES && verts.length() % 3 != 0) throw new LuaError("Triangles mode requires vertex count % 3 == 0");
			if(gLMode == GL11.GL_QUADS && verts.length() % 4 != 0) throw new LuaError("Quads mode requires vertex count % 4 == 0");
			if(gLMode == GL11.GL_QUAD_STRIP && verts.length() % 2 != 0) throw new LuaError("Quads strip mode requires vertex count % 2 == 0");
			
			
			
			
			BufferBuilder buffer = Tessellator.getInstance().getBuffer();
			buffer.begin(gLMode, type);
			double x = this.x - playerX;
			double y = this.y - playerY;
			double z = this.z - playerZ;
			for(int i = 1; i<=verts.length(); i++) {
				double vx, vy, vz;
				LuaValue v = verts.get(i);
				vx = v.get(1).checkdouble() + x;
				vy = v.get(2).checkdouble() + y;
				vz = v.get(3).checkdouble() + z;
				buffer.pos(vx, vy, vz);
				if(useTexture) {
					LuaValue uv = t.get("uv");
					double uCoord, vCoord;
					uCoord = uv.get(1).checkdouble();
					vCoord = uv.get(2).checkdouble();
					buffer.tex(uCoord, vCoord);
				}
				buffer.endVertex();
			}
			
		}catch(Throwable e){
			Utils.logError(e);
		}finally {
			Tessellator.getInstance().draw();
		}
	}



	





}