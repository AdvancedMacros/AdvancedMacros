package com.theincgi.advancedmacros.event.handlers;

import com.theincgi.advancedmacros.event.EventHandler;
import com.theincgi.advancedmacros.event.events.ArrowFiredEvent;

public class OnArrowFired extends EventHandler<ArrowFiredEvent> {

	@Override
	public String getEventValue(ArrowFiredEvent event) {
		return "ArrowFired";
	}
	
}
