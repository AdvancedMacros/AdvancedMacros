package com.theincgi.advancedMacros.gui.elements;

@FunctionalInterface
public interface OnClickHandler {
	public static final int LMB = 0, RMB = 1, MMB = 2;
	
	abstract public void onClick(int button, GuiButton sButton);
	
}