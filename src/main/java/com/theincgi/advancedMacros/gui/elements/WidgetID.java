package com.theincgi.advancedMacros.gui.elements;

import com.theincgi.advancedMacros.AdvancedMacros;

public class WidgetID {
	private static long nextId = 1;
	private long ID = nextId++;
	boolean force = false;
	public long getID() {
		return ID;
	}
//	private WidgetID(){
//	}
	public WidgetID(long forceID){
		ID = forceID;
		force=true;
	}
}