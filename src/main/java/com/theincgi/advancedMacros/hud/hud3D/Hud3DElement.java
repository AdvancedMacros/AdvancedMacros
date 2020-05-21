package com.theincgi.advancedMacros.hud.hud3D;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
//import com.mojang.blaze3d.platform.GlStateManager.CullFace;
import com.theincgi.advancedMacros.lua.LuaValTexture;
import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.client.renderer.BufferBuilder;
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
		//data.set("depthTest", true);
		data.set("cullFace", false);
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
				if(args.arg1().isnil())
					texture = null;
				else
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
			//GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
			GlStateManager.translated(-playerX, -playerY, -playerZ);
			GlStateManager.translatef(x, y, z);

			GlStateManager.rotatef(roll, 0, 0, 1);
			GlStateManager.rotatef(pitch, 1, 0, 0);
			GlStateManager.rotatef(yaw, 0, 1, 0);


			GlStateManager.translatef(-x, -y, -z);
			GlStateManager.translated(playerX, playerY, playerZ);

			LuaTable data = getControls().get("data").checktable();

			GlStateManager.enableBlend();
			GlStateManager.enableAlphaTest();
			GlStateManager.enableDepthTest();

			LuaValue cullMode = data.get("cullFace");
			if(cullMode.isnil() || cullMode.isboolean() && !cullMode.checkboolean()) {
				GlStateManager.disableCull();
			}else {
				GlStateManager.enableCull();
				//GlStateManager.cullFace( CullFace.valueOf(cullMode.checkjstring().replace(' ', '_').toUpperCase()));
			}
			
			if(data.get("lighting").checkboolean())
			GlStateManager.enableLighting();
			//			GlStateManager.enableCull();
			//			GlStateManager.cullFace(CullFace.BACK);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);



			render(data, playerX, playerY, playerZ);
			GlStateManager.enableCull();
			GlStateManager.disableLighting();
			//GL11.glPopAttrib();
			GlStateManager.popMatrix();
		}catch (Throwable e) {
			Utils.logError(e);
			disableDraw();
		}
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
			//drawSideFace(playerX, playerY, playerZ, 1, 1, 0);
			LuaValue verts = t.get("verts").checktable();
			LuaValue uvs = t.get("uv");
			boolean useTexture = !(texture==null) && !texture.isnil() && uvs.istable() &&
					uvs.length() >= 3;
					VertexFormat type = DefaultVertexFormats.POSITION;
					if(useTexture) {
						texture.bindTexture();
						GlStateManager.enableTexture();
						type = DefaultVertexFormats.POSITION_TEX;
					}else {
						GlStateManager.disableTexture();
					}

					int gLMode = 	mode.equals("strip"		)? GL11.GL_TRIANGLE_STRIP :
									mode.equals("fan"		)? GL11.GL_TRIANGLE_FAN :
									mode.equals("quads"		)? GL11.GL_QUADS:
									mode.equals("triangles"	)? GL11.GL_TRIANGLES:
									mode.equals("quadStrip"	)? GL11.GL_QUAD_STRIP : -1;
					if(gLMode == -1) throw new LuaError("Invalid GL Mode in hud3D object");


					if(verts.length() < 3) throw new LuaError("Hud3D object missing vertex data");
					if(gLMode == GL11.GL_TRIANGLES && verts.length() % 3 != 0) throw new LuaError("Triangles mode requires vertex count % 3 == 0");
					if(gLMode == GL11.GL_QUADS && verts.length() % 4 != 0) throw new LuaError("Quads mode requires vertex count % 4 == 0");
					if(gLMode == GL11.GL_QUAD_STRIP && verts.length() % 2 != 0) throw new LuaError("Quads strip mode requires vertex count % 2 == 0");




					color.apply();
					BufferBuilder buffer = Tessellator.getInstance().getBuffer();
					buffer.begin(gLMode, type);
					double x = this.x - playerX;
					double y = this.y - playerY;
					double z = this.z - playerZ;

					int vertsLength = verts.length();
					for(int i = 1; i<=vertsLength; i++) {
						double vx, vy, vz;
						LuaValue v = verts.get(i);
						vx = v.get(1).checkdouble() + x;
						vy = v.get(2).checkdouble() + y;
						vz = v.get(3).checkdouble() + z;
						buffer.pos(vx, vy, vz);
						if(useTexture) {
							LuaTable uv = uvs.get(i).checktable();
							double uCoord, vCoord;
							uCoord = uv.get(1).optdouble(0);
							vCoord = uv.get(2).optdouble(0);

							buffer.tex((float)uCoord, (float)vCoord);
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