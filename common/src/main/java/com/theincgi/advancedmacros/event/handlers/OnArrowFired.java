package com.theincgi.advancedmacros.event.handlers;

import com.theincgi.advancedmacros.event.CombinedEventHandler.EventName;
import com.theincgi.advancedmacros.event.EventHandler;
import com.theincgi.advancedmacros.event.events.ArrowFiredEvent;
import com.theincgi.advancedmacros.mixin.events.MixinBowItem;

public class OnArrowFired extends EventHandler<ArrowFiredEvent> {
	
	/**{@link MixinBowItem}*/
	public static OnArrowFired onArrowFired = new OnArrowFired();
	
	@Override
	public String getEventValue(ArrowFiredEvent event) {
		return EventName.ArrowFired.name();
	}
	
}
