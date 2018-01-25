package com.theincgi.advancedMacros.lua.scriptGui;

import com.theincgi.advancedMacros.gui.Gui;
import com.theincgi.advancedMacros.gui.elements.GuiRect;
import com.theincgi.advancedMacros.hud.hud2D.Hud2D_Rectangle;

public class GuiRectangle extends ScriptGuiElement{
	public GuiRectangle(Gui gui, Group parent) {
		super(gui, parent);
		enableColorControl();
		enableSizeControl();
	}
	
	@Override
	public void onDraw(Gui g, int mouseX, int mouseY, float partialTicks) {
		super.onDraw(g, mouseX, mouseY, partialTicks);
		if(!visible) return;
		Hud2D_Rectangle.drawRectangle(x, y, wid, hei, color, z);
		if(getHoverTint()!=null && GuiRect.isInBounds(mouseX, mouseY, (int)x, (int)y, (int)wid, (int)hei)) {
			Hud2D_Rectangle.drawRectangle(x, y, wid, hei, getHoverTint(), z);
		}
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
