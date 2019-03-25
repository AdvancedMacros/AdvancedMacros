package com.theincgi.advancedMacros.hud.hud3D;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.ThreeArgFunction;
import org.luaj.vm2_v3_0_1.lib.TwoArgFunction;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.gui.Color;
import com.theincgi.advancedMacros.lua.LuaValTexture;
import com.theincgi.advancedMacros.misc.CallableTable;
import com.theincgi.advancedMacros.misc.Settings;
import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class HoloBlock extends WorldHudItem{
	LuaValTexture texture;
	LuaValTexture textureUp;
	LuaValTexture textureDown;
	LuaValTexture textureNorth;
	LuaValTexture textureEast;
	LuaValTexture textureSouth;
	LuaValTexture textureWest;

	Color colorUp,   colorDown, colorNorth,
	colorWest, colorEast, colorSouth;

	float uMin, uMax, vMin,vMax, width;


	private double scale;
	public HoloBlock(){
		this("resource:holoblock.png");
	}
	public HoloBlock(String resourceName) {
		this(resourceName, 0,0,1,1);
	}
	public HoloBlock(String resourceName, float uMin, float vMin, float uMax, float vMax) {
		this(Utils.checkTexture(Settings.getTextureID(resourceName)), uMin, vMin, uMax,vMax);
	}
	public HoloBlock(LuaValTexture texture){
		this(texture, 0, 0, 1, 1);
	}
	public HoloBlock(LuaValTexture texture, float uMin, float vMin, float uMax, float vMax){
		this.texture = texture;
		setUV(uMin, vMin, uMax, vMax);
		width = 1;

	}


	/**flip'd with 1-# for the way I like it, 0,0 in top left*/
	public void setUV(float uMin,float vMin, float uMax, float vMax){
		this.uMin = uMin;
		this.uMax = uMax;
		this.vMin = vMin;
		this.vMax = vMax;
	}

	@Override
	public void render(double playerX, double playerY, double playerZ) {
		//TODO dont render if player is facing other way
		if(texture!=null){
			texture.bindTexture();
			//GlStateManager.pushAttrib();
			GlStateManager.pushMatrix(); //TODO include obj rotation in another push pop matrix about here
			
			GlStateManager.translate(-playerX, -playerY, -playerZ);
			GlStateManager.translate(x, y, z);
			
			GlStateManager.rotate(roll, 0, 0, 1);
			GlStateManager.rotate(pitch, 1, 0, 0);
			GlStateManager.rotate(yaw, 0, 1, 0);
			
			
			GlStateManager.translate(-x, -y, -z);
			GlStateManager.translate(playerX, playerY, playerZ);
			
			color.apply();

			if(texture!=null || textureNorth!=null) {
				bindOrBind(textureNorth, texture);
				drawSideFace(playerX,   playerY, playerZ  ,  width, width,  0); //draw front
			}
			if(texture!=null || textureEast!=null) {
				bindOrBind(textureEast, texture);
				drawSideFace(playerX-width, playerY, playerZ  ,  0, width,  width); //draw front
			}
			if(texture!=null || textureSouth!=null) {
				bindOrBind(textureSouth, texture);
				drawSideFace(playerX-width, playerY, playerZ-width, -width, width,  0);
			}
			if(texture!=null || textureWest!=null) {
				bindOrBind(textureWest, texture);
				drawSideFace(playerX  , playerY, playerZ-width,  0, width, -width);
			}
			if(texture!=null || textureUp!=null) {
				bindOrBind(textureUp, texture);
				drawTopFace(playerX, playerY-width, playerZ, width, width);
			}
			if(texture!=null || textureDown!=null) {
				bindOrBind(textureDown, texture);
				drawBottomFace(playerX, playerY, playerZ, width, width);
			}


			GlStateManager.popMatrix();//protection for not messing up matrix
			//GlStateManager.popAttrib();
		}
	}

	private void bindOrBind(LuaValTexture pref, LuaValTexture def) {
		if(pref!=null) {
			pref.bindTexture();
			return;
		}
		if(def!=null)
			def.bindTexture();
	}

	//north face
	private void drawSideFace(double px, double py, double pz, double xWid, double yHei, double zWid){
		double x = this.x - px;
		double y = this.y - py;
		double z = this.z - pz;
		//		double x = px - this.x;
		//		double y = py - this.y ;
		//		double z = pz - this.z;
		//		double x,y,z;
		//		x=y=z=0;
		//		if(drawType.isScaled()){
		//			width*=scale;
		//			
		//		}else if(drawType.equals(DrawType.OVERLAY)){
		//			width*=1.0001;
		//			z+=.001;
		//		}else if(drawType.equals(DrawType.INSIDE)){
		//			width/=1.0001;
		//			z-=.001;
		//		}
		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
		buffer.pos(x     , y     , z     ).tex(uMax, vMax).endVertex();
		buffer.pos(x     , y+yHei, z     ).tex(uMax, vMin).endVertex();
		buffer.pos(x+xWid, y+yHei, z+zWid) .tex(uMin, vMin).endVertex();
		buffer.pos(x+xWid, y     , z+zWid).tex(uMin, vMax).endVertex();
		Tessellator.getInstance().draw();
	}
	private void drawTopFace(double px, double py, double pz, double wid, double len){
		double x = this.x - px;
		double y = this.y - py;
		double z = this.z - pz;

		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
		buffer.pos(x    , y, z    ).tex(uMin, vMin).endVertex();
		buffer.pos(x    , y, z+len).tex(uMin, vMax).endVertex();
		buffer.pos(x+wid, y, z+len).tex(uMax, vMax).endVertex();
		buffer.pos(x+wid, y, z    ).tex(uMax, vMin).endVertex();
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

	public void setWidth(float width){
		this.width = width;
	}
	public void setTexture(LuaValTexture v){
		texture = v;
	}


	/**Doesnt do much unless you are set to custom scale mode*/
	public void setScale(double scale) {
		this.scale = scale;
	}


	/**do not give null type*/
	@Override
	public void setDrawType(DrawType drawType) {
		if(drawType==null){throw new NullPointerException("Draw type may not be null");}
		this.drawType = drawType;
	}
	public enum DrawType{
		XRAY,
		NO_XRAY;
		public boolean isXRAY(){
			switch (this) {
			case XRAY:
				return true;
			default:
				return false;
			}
		}
	}
	@Override
	public void loadControls(LuaValue t) {
		super.loadControls(t);
		t.set("setWidth",      new CallableTable(new String[]{"hud3D","newBlock()","setWidth"}     , new SetWidth()     ));
		t.set("changeTexture", new CallableTable(new String[]{"hud3D","newBlock()","changeTexture"}, new ChangeTexture()));
		t.set("setUV",         new CallableTable(new String[]{"hud3D","newBlock()","setUV"}        , new SetUV()        ));
		t.set("overlay",       new CallableTable(new String[]{"hud3D","newBlock()","overlay"}      , new Overlay()      ));
		t.set("setColor",      new CallableTable(new String[]{"hud3D","newBlock()","setColor"}     , new SetColorSide() ));
		t.set("getColor",      new CallableTable(new String[]{"hud3D","newBlock()","getColor"}     , new GetColorSide() ));
	}



	private class SetColorSide extends TwoArgFunction {
		@Override
		public LuaValue call(LuaValue color, LuaValue optSide) {
			Color c = Utils.parseColor(color, AdvancedMacros.COLOR_SPACE_IS_255);
			if(optSide.isstring())
				switch (optSide.checkjstring().toLowerCase()) {
				case "up":
				case "top":
					colorUp = c;
					return NONE;
				case "north":
					colorNorth = c;
					return NONE;
				case "west":
					colorWest = c;
					return NONE;
				case "east":
					colorEast = c;
					return NONE;
				case "south":
					colorSouth = c;
					return NONE;
				case "down":
				case "bottom":
					colorDown = c;
					return NONE;
				default:
					throw new LuaError("Undefined side '"+optSide.tojstring()+"'");
				}
			HoloBlock.this.color = c;
			return NONE;
		}
	}
	private class GetColorSide extends OneArgFunction {
		@Override
		public LuaValue call(LuaValue optside) {
			boolean use = AdvancedMacros.COLOR_SPACE_IS_255;
			if(optside.isstring())
				switch (optside.checkjstring().toLowerCase()) {
				case "up":
				case "top":
					return colorUp.toLuaValue(use);
				case "north":
					return colorNorth.toLuaValue(use);
				case "west":
					return colorWest.toLuaValue(use);
				case "east":
					return colorEast.toLuaValue(use);
				case "south":
					return colorSouth.toLuaValue(use);
				case "down":
				case "bottom":
					return colorDown.toLuaValue(use);
				default:
					throw new LuaError("Undefined side '"+optside.tojstring()+"'");
				}
			return color.toLuaValue(use);
		}
	}
	private class SetWidth extends OneArgFunction{
		@Override
		public LuaValue call(LuaValue arg) {
			setWidth((float)arg.checkdouble());
			return LuaValue.NONE;
		}
	}
	private class ChangeTexture extends TwoArgFunction{
		@Override
		public LuaValue call(LuaValue arg, LuaValue optSide) {
			if(optSide.isnil())
				texture = Utils.parseTexture(arg);
			else {
				LuaValTexture tmp = Utils.parseTexture(arg);
				switch (optSide.tojstring().toLowerCase()) {
				case "up":
				case "top":
					textureUp = tmp;
					break;
				case "north":
					textureNorth = tmp;
					break;
				case "west":
					textureWest = tmp;
					break;
				case "east":
					textureEast = tmp;
					break;
				case "south":
					textureSouth = tmp;
					break;
				case "down":
				case "bottom":
					textureDown = tmp;
					break;

				default:
					throw new LuaError("Unknown side '"+optSide.tojstring()+"'");
				}
			}
			//			setTexture(tex);
			//			if(arg instanceof LuaValTexture)
			//				setTexture(tex = Utils.checkTexture(arg));
			//			else
			//				setTexture(tex = Utils.checkTexture(Settings.getTextureID(arg.checkjstring())));
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

	private class Overlay extends ThreeArgFunction{
		@Override
		public LuaValue call(LuaValue arg1, LuaValue arg2, LuaValue arg3) {
			setPos((float)(arg1.optdouble(x)-.0005), (float)(arg2.optdouble(y)-.0005), (float)(arg3.optdouble(z)-.0005));
			width = 1.001f;
			return LuaValue.NONE;
		}
	}

	@Override
	public String toString() {
		return "HoloBlock [width=" + width + ", color=" + color + ", drawType=" + drawType + ", x=" + x + ", y=" + y
				+ ", z=" + z + ", isDrawing()=" + isDrawing() + "]";
	}


}