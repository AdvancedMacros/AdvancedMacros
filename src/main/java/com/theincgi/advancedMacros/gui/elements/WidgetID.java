package com.theincgi.advancedMacros.gui.elements;

import com.theincgi.advancedMacros.AdvancedMacros;

/**
 *    0 -   99 = MacroMenu
 *  100 -  199 = 
 *  200 -  299 = 
 *  300 -  399 = CTA(300) Editor/filePreview
 *  400 -  499 = PopupPrompt(404) ScriptBrowser
 *  500 -  599 = PopupPrompt2(505)
 *  600 -  699 = ScriptBrowser2
 *  700 -  799 = RunningScripts
 *  800 -  899 = InputGui
 *  900 -  999 = 
 * 1000 - 1099 =  
 * */
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