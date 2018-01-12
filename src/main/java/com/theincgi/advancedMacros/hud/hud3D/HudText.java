package com.theincgi.advancedMacros.hud.hud3D;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import com.theincgi.advancedMacros.AdvancedMacros;

import net.minecraft.client.Minecraft;

public class HudText extends WorldHudItem{
	Minecraft mc = Minecraft.getMinecraft();
	String text="";
	private boolean is3D;
	//Color color;
	public HudText(boolean is3D) {
		this.is3D = is3D;
	}
	@Override
	public float getOpacity() {
		return opacity;
	}
	@Override
	public void render(double playerX, double playerY, double playerZ) {
		if(is3D)
			AdvancedMacros.customFontRenderer.renderText(playerX, playerY, playerZ, x, y, z, yaw, pitch, roll, text, opacity);
		else
			AdvancedMacros.customFontRenderer.renderText(x, y, text);
	}
	@Override
	public void destroy() {
		disableDraw();
	}
	public void setText(String checkjstring) {
		text = checkjstring;
	}
	public void setRotation(float yaw, float pitch, float roll) {
		setYaw(yaw);
		setPitch(pitch);
		setRoll(roll);
	}
	public String getText() {
		return text;
	}
	
	@Override
	public void loadControls(LuaValue t) {
		super.loadControls(t);
		t.set("setText", new SetText());
		t.set("getText", new GetText());
	}
	
	//Definitly stolen from the FontRenderer class
	private class SetText extends OneArgFunction{
		@Override
		public LuaValue call(LuaValue arg0) {
			setText(arg0.checkjstring());
			return LuaValue.NONE;
		}
	}
	private class GetText extends ZeroArgFunction{
		@Override
		public LuaValue call() {
			return LuaValue.valueOf(getText());
		}
	}
	

}
