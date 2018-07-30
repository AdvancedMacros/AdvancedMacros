package com.theincgi.advancedMacros.hud.hud3D;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.ThreeArgFunction;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.hud.hud3D.HoloBlock.DrawType;

public abstract class WorldHudItem {
	protected DrawType drawType = DrawType.NO_XRAY;
	private LuaValue controls;
	public DrawType getDrawType() {
		return drawType;
	}
	public void setDrawType(DrawType drawType) {
		this.drawType = drawType;
	}
	protected float x,y,z,yaw,pitch,roll,opacity=1;
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
		return opacity;
	}
	public void setOpacity(float opacity) {
		this.opacity = opacity;
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
		t.set("destroy", new Destroy());
		t.set("setPos", new SetPos());
		t.set("setX", new SetX());
		t.set("setY", new SetY());
		t.set("setZ", new SetZ());
		t.set("setRot", new SetRotation());
		t.set("setOpacity", new SetOpacity());
		t.set("getOpacity", new GetOpacity());
		t.set("getPos", new GetPos());
		t.set("getRot", new GetRot());
		t.set("enableDraw", new EnableDraw());
		t.set("disableDraw", new DisableDraw());
		t.set("isDrawing", new IsDrawing());
		t.set("xray", new XRay());
	}
	private class SetPos extends ThreeArgFunction{
		@Override
		public LuaValue call(LuaValue x, LuaValue y, LuaValue z) {
			WorldHudItem.this.setPos((float)x.checkdouble(), (float)y.checkdouble(), (float)z.checkdouble());
			return LuaValue.NONE;
		}
	}
	private class SetX extends OneArgFunction{
		@Override
		public LuaValue call(LuaValue arg) {
			setX((float)arg.checkdouble());
			return LuaValue.NONE;
		}
	}
	private class SetY extends OneArgFunction{
		@Override
		public LuaValue call(LuaValue arg) {
			setY((float) arg.checkdouble());
			return LuaValue.NONE;
		}
	}
	private class SetZ extends OneArgFunction{
		@Override
		public LuaValue call(LuaValue arg) {
			setZ((float) arg.todouble());
			return LuaValue.NONE;
		}
	}
	private class SetRotation extends ThreeArgFunction{
		@Override
		public LuaValue call(LuaValue yaw, LuaValue pitch, LuaValue roll) {
			WorldHudItem.this.setRotation((float)yaw.checkdouble(), (float)pitch.checkdouble(), (float)roll.checkdouble());
			return LuaValue.NONE;
		}
	}
	private class EnableDraw extends OneArgFunction{
		@Override
		public LuaValue call(LuaValue arg0) {
			if(arg0.optboolean(true))
				WorldHudItem.this.enableDraw();
			else
				WorldHudItem.this.disableDraw();
			return LuaValue.NONE;
		}
	}
	private class DisableDraw extends ZeroArgFunction{
		@Override
		public LuaValue call() {
			disableDraw();
			return LuaValue.NONE;
		}
	}
	private class Destroy extends ZeroArgFunction{
		@Override
		public LuaValue call() {
			destroy();
			return LuaValue.NONE;
		}
	}
	private class IsDrawing extends ZeroArgFunction{
		@Override
		public LuaValue call() {
			return LuaValue.valueOf(isDrawing);
		}
	}
	private class SetOpacity extends OneArgFunction{
		@Override
		public LuaValue call(LuaValue arg0) {
			WorldHudItem.this.setOpacity((float)arg0.checkdouble());
			return LuaValue.NONE;
		}
	}
	private class GetOpacity extends ZeroArgFunction{
		@Override
		public LuaValue call() {
			return LuaValue.valueOf(WorldHudItem.this.getOpacity());
		}
	}
	private class GetPos extends VarArgFunction{
		@Override
		public Varargs invoke(Varargs args) {
			LuaTable table = new LuaTable();
			table.set(1, LuaValue.valueOf(WorldHudItem.this.getX()));
			table.set(2, LuaValue.valueOf(WorldHudItem.this.getY()));
			table.set(3, LuaValue.valueOf(WorldHudItem.this.getZ()));
			return table.unpack();
		}
	}
	private class GetRot extends VarArgFunction{
		@Override
		public Varargs invoke(Varargs args) {
			LuaTable table = new LuaTable();
			table.set(1, LuaValue.valueOf(WorldHudItem.this.getYaw()));
			table.set(2, LuaValue.valueOf(WorldHudItem.this.getPitch()));
			table.set(3, LuaValue.valueOf(WorldHudItem.this.getRoll()));
			return table.unpack();
		}
	}
	private class XRay extends OneArgFunction{
		@Override
		public LuaValue call(LuaValue bool) {
			WorldHudItem.this.setDrawType(bool.optboolean(true)?DrawType.XRAY:DrawType.NO_XRAY);
			return LuaValue.NONE;
		}
	}
}