package com.theincgi.advancedMacros.hud.hud3D;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.ThreeArgFunction;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.gui.Color;
import com.theincgi.advancedMacros.hud.hud3D.HoloBlock.DrawType;
import com.theincgi.advancedMacros.misc.CallableTable;
import com.theincgi.advancedMacros.misc.Utils;

public abstract class WorldHudItem {
	protected DrawType drawType = DrawType.NO_XRAY;
	private LuaValue controls;
	Color color = Color.WHITE;
	
	public DrawType getDrawType() {
		return drawType;
	}
	public void setDrawType(DrawType drawType) {
		this.drawType = drawType;
	}
	protected float x,y,z,yaw,pitch,roll;
	protected boolean isDrawing;
	/**should call to {@link #disableDraw()} and to make all functions un-usable as it is no longer in use*/
	public void destroy() {
			disableDraw();
			LuaTable t = controls.checktable();
			LuaValue k = LuaValue.NIL;
			do{
				k = t.next(k).arg1();
				if(k.isnil()){break;}
				//System.out.println(k);
				t.set(k, LuaValue.NIL);
			}while(!k.isnil());
			controls = LuaValue.FALSE;
	}
	/**Add this to the forgeEventHandler draw list if it isnt already<br>
	 * @see<br>
	 * {@link #disableDraw()}<br>
	 * {@link #isDrawing()}*/
	public void enableDraw(){
		if(isDrawing) return;
		isDrawing=true;
		AdvancedMacros.forgeEventHandler.addWorldHudItem(this);
	}
	/**basicly removes it from the list of stuff to draw<br>
	 * @see
	 * {@link #enableDraw()}<br>
	 * {@link #isDrawing()}*/
	public void disableDraw(){
		isDrawing = false;
		AdvancedMacros.forgeEventHandler.removeWorldHudItem(this);
	}
	public boolean isDrawing(){
		return isDrawing;
	}
	public void setX(float x) {
		this.x = x;
	}
	public void setY(float y) {
		this.y = y;
	}
	public void setZ(float z) {
		this.z = z;
	}
	public float getX() {
		return x;
	}
	public float getY() {
		return y;
	}
	public float getZ() {
		return z;
	}
	public void setYaw(float yaw) {
		this.yaw = yaw;
	}
	public void setPitch(float pitch) {
		this.pitch = pitch;
	}
	public void setRoll(float roll) {
		this.roll = roll;
	}
	public float getYaw() {
		return yaw;
	}
	public float getPitch() {
		return pitch;
	}
	public float getRoll() {
		return roll;
	}
	public void setPos(float x, float y, float z){
		setX(x);
		setY(y);
		setZ(z);
	}
	public void setRotation(float yaw, float pitch, float roll) {
		setYaw(yaw);
		setPitch(pitch);
		setRoll(roll);
	}
	public abstract void render(double playerX, double playerY, double playerZ);
	public float getOpacity() {
		return color.getAFloat();
	}
	public void setOpacity(float opacity) {
		this.color.setA(opacity);
	}
	
	
	
	/**Override load controls to add stuff*/
	public final LuaValue getControls(){
		if(controls!=null) {
			return controls;
		}
		loadControls(controls = new LuaTable());
		return controls;
		
	}
	public void loadControls(LuaValue t) {
		for (Hud3DElementOp op : Hud3DElementOp.defValues) {
			t.set(op.toString(), new CallableTable(op.getDocLocation(), new DoOp(op)));
		}
	}
	
	public void enableColorControls(LuaValue t) {
		for (Hud3DElementOp op : Hud3DElementOp.colorControls) {
			t.set(op.toString(), new CallableTable(op.getDocLocation(), new DoOp(op)));
		}
	}
	
	private static enum Hud3DElementOp{
		destroy,
		setPos,
		setX,
		setY,
		setZ,
		setRot,
		setOpacity,
		getOpacity,
		getPos,
		getRot,
		enableDraw,
		disableDraw,
		isDrawing,
		xray,
		
		setColor,
		getColor;
		
		public static final Hud3DElementOp[] defValues = {
				destroy, setPos, setX, setY, setZ, setRot, setOpacity, getOpacity,
				getPos, getRot, enableDraw, disableDraw, isDrawing, xray
		};
		
		public static final Hud3DElementOp[] colorControls = {
			setColor, getColor
		};
		
		String[] getDocLocation() {
			String[] loc = new String[3];
			loc[0] = "hud3D";
			loc[1] = "hudItem"; //end user won't see this value
			switch (this) {
			case destroy:
			case disableDraw:
			case enableDraw:
			case getOpacity:
			case getPos:
			case getRot:
			case isDrawing:
			case setOpacity:
			case setPos:
			case setRot:
			case setX:
			case setY:
			case setZ:
			case xray:
			case setColor:
			case getColor:
				loc[2] = this.name();
				return loc;
			default:
				return null;
			}
		}
	}
	
	private class DoOp extends VarArgFunction {
		Hud3DElementOp op;
		public DoOp(Hud3DElementOp op) {
			super();
			this.op = op;
		}
		@Override
		public Varargs invoke(Varargs args) {
			switch (op) {
			case destroy:
				destroy();
				return LuaValue.NONE;
			case disableDraw:
				disableDraw();
				return LuaValue.NONE;
			case enableDraw:
				if(args.arg1().optboolean(true))
					WorldHudItem.this.enableDraw();
				else
					WorldHudItem.this.disableDraw();
				return LuaValue.NONE;
			case getOpacity:
				return LuaValue.valueOf(WorldHudItem.this.getOpacity());
			case getPos:{
				LuaTable table = new LuaTable();
				table.set(1, LuaValue.valueOf(WorldHudItem.this.getX()));
				table.set(2, LuaValue.valueOf(WorldHudItem.this.getY()));
				table.set(3, LuaValue.valueOf(WorldHudItem.this.getZ()));
				return table.unpack();
			}
			case getRot:{
				LuaTable table = new LuaTable();
				table.set(1, LuaValue.valueOf(WorldHudItem.this.getYaw()));
				table.set(2, LuaValue.valueOf(WorldHudItem.this.getPitch()));
				table.set(3, LuaValue.valueOf(WorldHudItem.this.getRoll()));
				return table.unpack();
			}
			case isDrawing:
				return LuaValue.valueOf(isDrawing);
			case setOpacity:
				WorldHudItem.this.setOpacity((float)args.arg1().checkdouble());
				return LuaValue.NONE;
			case setPos:
				WorldHudItem.this.setPos((float)args.arg(1).checkdouble(),
						                 (float)args.arg(2).checkdouble(),
						                 (float)args.arg(3).checkdouble());
				return LuaValue.NONE;
			case setRot:
				WorldHudItem.this.setRotation((float)args.arg1().checkdouble(), (float)args.arg(2).checkdouble(), (float)args.arg(3).checkdouble());
				return LuaValue.NONE;
			case setX:
				setX((float)args.arg1().checkdouble());
				return LuaValue.NONE;
			case setY:
				setY((float) args.arg1().checkdouble());
				return LuaValue.NONE;
			case setZ:
				setZ((float) args.arg1().checkdouble());
				return LuaValue.NONE;
			case xray:
				WorldHudItem.this.setDrawType(args.arg1().optboolean(true)?DrawType.XRAY:DrawType.NO_XRAY);
				return LuaValue.NONE;
			case getColor:
				return color.toLuaValue(AdvancedMacros.COLOR_SPACE_IS_255);
			case setColor:
				color = Utils.parseColor(args, AdvancedMacros.COLOR_SPACE_IS_255);
				return NONE;
			default:
				throw new LuaError("unimplemented function "+op);
			}
		}
	}
	
	
}