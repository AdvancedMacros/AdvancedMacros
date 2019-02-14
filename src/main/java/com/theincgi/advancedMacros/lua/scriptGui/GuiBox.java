package com.theincgi.advancedMacros.lua.scriptGui;

import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import com.theincgi.advancedMacros.gui.Gui;

public class GuiBox extends ScriptGuiElement{
	public float thickness=1;
	public GuiBox(Gui gui, Group parent) {
		super(gui, parent);
		enableColorControl();
		enableSizeControl();
		set("setThickness", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				thickness = (float) arg.checkdouble();
				return NONE;
			}
		});
		set("getThickness", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return LuaValue.valueOf(thickness);
			}
		});
		set("__class", "advancedMacros.GuiBox");
	}
	
	@Override
	public void onDraw(Gui g, int mouseX, int mouseY, float partialTicks) {
		super.onDraw(g, mouseX, mouseY, partialTicks);
		if(!visible) return;
		
		GuiRectangle.drawRectangle(x,		y, 			wid, 		thickness, 	color, z);
		GuiRectangle.drawRectangle(x, 		y, 			thickness, 	hei, 		color, z);
		GuiRectangle.drawRectangle(x+wid-1, 	y, 			thickness, 	hei+thickness-1, 		color, z);
		GuiRectangle.drawRectangle(x, 		y+hei-1, 	wid+thickness-1, 		thickness, 	color, z);
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
	
}
