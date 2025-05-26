package com.theincgi.advancedmacros.event;

import static com.theincgi.advancedmacros.guiScripts.BindingsMenu.getBindingsMenu;

import com.theincgi.advancedmacros.misc.Utils;
import com.theincgi.advancedmacros.misc.Workspace;

abstract public class EventHandler<T extends Event> {
	
	public static final String KEY_TYPE = "key";
	public static final String EVENT_TYPE = "event";
	
	public void onEvent(T event) {
		try(var reset = Utils.tempSetCurrentWorkspace(Workspace.INTERNAL)) {
			getBindingsMenu().triggerEvent(EVENT_TYPE, getEventValue(event), event.createArgsTable());
		}
	}
	
	abstract public String getEventValue(T event); 
}
