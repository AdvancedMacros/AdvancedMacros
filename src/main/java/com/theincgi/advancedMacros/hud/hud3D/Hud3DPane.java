package com.theincgi.advancedMacros.hud.hud3D;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.TwoArgFunction;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.gui.Color;
import com.theincgi.advancedMacros.lua.LuaValTexture;
import com.theincgi.advancedMacros.misc.CallableTable;
import com.theincgi.advancedMacros.misc.Settings;
import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class Hud3DPane extends WorldHudItem{
	LuaValTexture texture;
	float uMin=0, uMax=1, vMin=0, vMax=1, width = 1, length = 1;
	AxisFace axisFace;
	public Hud3DPane(String face) {
		this(face, "resource:holoblock.png");
	}
	public Hud3DPane(String face, String texture) {
		setFace(face);
		this.texture = Utils.checkTexture(Settings.getTextureID(texture));
	}
	
	private void setFace(String face) {
		face = face.toUpperCase();
		switch (face) {
		case "XZ+": case "Y+":
			axisFace = AxisFace.XZP; break;
		case "XZ-": case "Y-":
			axisFace = AxisFace.XZM; break;
		case "XY+": case "Z+":
			axisFace = AxisFace.XYP; break;
		case "XY-": case "Z-":
			axisFace = AxisFace.XYM; break;
		case "YZ+": case "X+":
			axisFace = AxisFace.YZP; break;
		case "YZ-": case "X-":
			axisFace = AxisFace.YZM; break;
		case "XZ": case "Y": axisFace = AxisFace.XZ; break;
		case "XY": case "Z": axisFace = AxisFace.XY; break;
		case "YZ": case "X": axisFace = AxisFace.YZ; break;
		default: throw new LuaError("invalid face");
		}
	}
	@Override
	public void render(MatrixStack ms, Matrix4f projection, float playerX, float playerY, float playerZ, float playerYaw, float playerPitch) {
		if(texture!=null)
			texture.bindTexture();
//		GlStateManager.pushMatrix();
//		GlStateManager.translated(-playerX, -playerY, -playerZ);
//		GlStateManager.translatef(x, y, z);
//		
//		GlStateManager.rotatef(roll, 0, 0, 1);
//		GlStateManager.rotatef(pitch, 1, 0, 0);
//		GlStateManager.rotatef(yaw, 0, 1, 0);
//		
//		
//		GlStateManager.translatef(-x, -y, -z);
//		GlStateManager.translated(playerX, playerY, playerZ);
		
		
		color.apply();
		switch (axisFace) {
		case XYM:
			drawSideFace(playerX, playerY, playerZ, width, length, 0);
			break;
		case XYP:
			drawSideFace(playerX-width, playerY, playerZ, -width, length, 0);
			break;
		case XZM:
			drawBottomFace(playerX, playerY, playerZ, width, length);
			break;
		case XZP:
			drawBottomFace(playerX-width, playerY, playerZ, -width, length);
			break;
		case YZP:
			drawSideFace(playerX, playerY, playerZ, 0, length, width);
			break;
		case YZM:
			drawSideFace(playerX, playerY, playerZ-width, 0, length, -width);
			break;
		
		case XY:
			GlStateManager.disableCull();
			drawSideFace(playerX, playerY, playerZ, width, length, 0);
			GlStateManager.enableCull();
			break;
		case XZ:
			GlStateManager.disableCull();
			drawBottomFace(playerX, playerY, playerZ, width, length);
			GlStateManager.enableCull();
			break;
		case YZ:
			GlStateManager.disableCull();
			drawSideFace(playerX, playerY, playerZ, 0, length, width);
			GlStateManager.enableCull();
			break;
		default:
			break;
		}
		//GlStateManager.popMatrix();
	}
	
	private static enum AxisFace {
		XZP, XZM, XZ,
		XYP, XYM, XY,
		YZP, YZM, YZ; 
	}
	private void drawSideFace(double px, double py, double pz, double xWid, double yHei, double zWid){
		double x = this.x - px;
		double y = this.y - py;
		double z = this.z - pz;

		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
		buffer.pos(x     , y     , z     ).tex(uMax, vMax).endVertex();
		buffer.pos(x     , y+yHei, z     ).tex(uMax, vMin).endVertex();
		buffer.pos(x+xWid, y+yHei, z+zWid) .tex(uMin, vMin).endVertex();
		buffer.pos(x+xWid, y     , z+zWid).tex(uMin, vMax).endVertex();
		Tessellator.getInstance().draw();
	}
	private void drawBottomFace(double px, double py, double pz, double wid, double len){
		double x = this.x - px;
		double y = this.y - py;
		double z = this.z - pz;

		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
		buffer.pos(x+wid, y, z    ).tex(uMin, vMin).endVertex();
		buffer.pos(x+wid, y, z+len).tex(uMin, vMax).endVertex();
		buffer.pos(x    , y, z+len).tex(uMax, vMax).endVertex();
		buffer.pos(x    , y, z    ).tex(uMax, vMin).endVertex();
		Tessellator.getInstance().draw();
	}
	
	@Override
	public void loadControls(LuaValue t) {
		super.loadControls(t);
		t.set("setWidth",      new CallableTable(new String[]{"hud3D","newPane()","setWidth"}     , new SetWidth()     ));
		t.set("setLength",      new CallableTable(new String[]{"hud3D","newPane()","setWidth"}     , new SetWidth()     ));
		t.set("changeTexture", new CallableTable(new String[]{"hud3D","newPane()","changeTexture"}, new ChangeTexture()));
		t.set("setUV",         new CallableTable(new String[]{"hud3D","newPane()","setUV"}        , new SetUV()        ));
		t.set("setColor",      new CallableTable(new String[]{"hud3D","newPane()","setColor"}     , new SetColor() ));
		t.set("getColor",      new CallableTable(new String[]{"hud3D","newPane()","getColor"}     , new GetColor() ));
		t.set("setSize",       new CallableTable(new String[]{"hud3D","newPane()", "setSize"},    new setSize()  ));
	}
	
	public void setWidth(float width){
		this.width = width;
	}
	public void setLength(float length){
		this.length = length;
	}
	/**flip'd with 1-# for the way I like it, 0,0 in top left*/
	public void setUV(float uMin,float vMin, float uMax, float vMax){
		this.uMin = uMin;
		this.uMax = uMax;
		this.vMin = vMin;
		this.vMax = vMax;
	}
	
	private class SetWidth extends OneArgFunction{
		@Override
		public LuaValue call(LuaValue arg) {
			setWidth((float)arg.checkdouble());
			return LuaValue.NONE;
		}
	}
	private class setLength extends OneArgFunction{
		@Override
		public LuaValue call(LuaValue arg) {
			setLength((float)arg.checkdouble());
			return LuaValue.NONE;
		}
	}
	private class ChangeTexture extends TwoArgFunction{
		@Override
		public LuaValue call(LuaValue arg, LuaValue optSide) {
			texture = Utils.parseTexture(arg);
			if(texture!=null)
				setUV(texture.uMin(), texture.vMin(), texture.uMax(), texture.vMax());
			return LuaValue.NONE;
		}
	}
	private class SetUV extends VarArgFunction{
		@Override
		public Varargs invoke(Varargs args) {
			if(args.narg()<4){throw new LuaError("Not enough args (uMin, vMin, uMax, vMax)");}
			setUV((float)args.arg(1).checkdouble(), (float)args.arg(2).checkdouble(), (float)args.arg(3).checkdouble(), (float)args.arg(4).checkdouble());
			return LuaValue.NONE;
		}
	}
	private class SetColor extends TwoArgFunction {
		@Override
		public LuaValue call(LuaValue color, LuaValue optSide) {
			Color c = Utils.parseColor(color, AdvancedMacros.COLOR_SPACE_IS_255);
			Hud3DPane.this.color = c;
			return NONE;
		}
	}
	private class GetColor  extends OneArgFunction {
		@Override
		public LuaValue call(LuaValue optside) {
			boolean use = AdvancedMacros.COLOR_SPACE_IS_255;
			return color.toLuaValue(use);
		}
	}
	private class setSize extends TwoArgFunction {
		public LuaValue call(LuaValue w, LuaValue l) {
			Hud3DPane.this.width = (float) w.checkdouble();
			Hud3DPane.this.length = (float) l.checkdouble();
			return NONE;
		};
	}
	public void changeTexture(LuaValue arg) {
		texture = Utils.parseTexture(arg);
	}
}
