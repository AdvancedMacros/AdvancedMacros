package com.theincgi.advancedmacros.event.handlers;

import com.theincgi.advancedmacros.event.EventHandler;
import com.theincgi.advancedmacros.event.CombinedEventHandler.EventName;
import com.theincgi.advancedmacros.event.events.DeathEvent;

public class OnDeath extends EventHandler<DeathEvent> {
	
	public static OnDeath onDeath = new OnDeath();
	
	@Override
	public String getEventValue(DeathEvent event) {
		return EventName.Death.name();
	}
}
