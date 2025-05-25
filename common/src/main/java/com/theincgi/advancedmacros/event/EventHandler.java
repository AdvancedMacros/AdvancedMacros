package com.theincgi.advancedmacros.event;

import static com.theincgi.advancedmacros.guiScripts.BindingsMenu.getBindingsMenu;

abstract public class EventHandler<T extends Event> {
	
	public static final String KEY_TYPE = "key";
	public static final String EVENT_TYPE = "event";
	
	public void onEvent(T event) {
		getBindingsMenu().triggerEvent(EVENT_TYPE, getEventValue(event), event.createArgsTable());
	}
	
	abstract public String getEventValue(T event); 
}
