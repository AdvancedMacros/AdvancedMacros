package com.theincgi.advancedmacros.event.handlers;

import com.theincgi.advancedmacros.event.CombinedEventHandler.EventName;
import com.theincgi.advancedmacros.event.EventHandler;
import com.theincgi.advancedmacros.event.events.BlockInteractEvent;

public class OnBlockInteract extends EventHandler<BlockInteractEvent> {

	public static OnBlockInteract onBlockInteract = new OnBlockInteract();
	
	@Override
	public String getEventValue(BlockInteractEvent event) {
		return EventName.BlockInteract.name();
	}
	
}
