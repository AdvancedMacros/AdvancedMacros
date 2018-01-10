package com.theincgi.advancedMacros.gui.elements;

import com.theincgi.advancedMacros.gui.Gui;

public interface Drawable {
	public void onDraw(Gui g, int mouseX, int mouseY, float partialTicks);
}